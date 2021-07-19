package com.example.demo;

import com.example.demo.jsonData.Customer;
import com.example.demo.jsonData.PartStock;
import com.example.demo.jsonData.PartStockLine;
import com.example.demo.jsonData.SKU;
import com.exanple.demo.utils.FTPException;
import com.exanple.demo.utils.MonitorException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
//@Repository
public class SampleAppRunner implements ApplicationRunner {
    @Autowired
    private DataSourceTransactionManager txManager;
//    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    FTPClient ftp;
    @Autowired
    XmlMapper xmlMapper;//TODO   @Autowired

    private InputStream is;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Executing SampleAppRunner");
        try {
            // region open FTP sessionS
            ftp.connect("localhost", 21);
            ftp.enterLocalPassiveMode();
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                throw new FTPException("Не удалось подключиться к FTP");
            }
            if (!ftp.login("anonymous", null)) {
                throw new FTPException("Не удалось авторизоваться на FTP");
            }
            // endregion

            // ------------------------------Обработка входящих сообщений //FIXME add INOUT direction on prefix
/*
            if (!ftp.changeWorkingDirectory(resp.getPath()))
                throw new NotificationException("Не удалось сменить директорию");
*/
            //TODO main cycle for (ResponseFtp resp : responses) {}
            int inout=1; //FIXME from MsgType
            try {
                switch (inout){
                    case(1):{ // с входящими документами
                        ftp.changeWorkingDirectory("/in"); //FIXME из таблицы resp.getPathIn()
                        FTPFileFilter filter = ftpFile -> (ftpFile.isFile() && ftpFile.getName().endsWith(".xml"));
                        FTPFile[] listFile = ftp.listFiles("/in", filter);//FIXME из таблицы resp.getPathIn()
                        for (FTPFile file : listFile) {
                            String filePrefix = file.getName().substring(0, 1).toUpperCase();
                            switch (filePrefix) {
                                case ("P"): { //PART_STOCK
                                    InputStream remoteInput = ftp.retrieveFileStream(file.getName()); //загрузка файла в виде потока
                                    if (!ftp.completePendingCommand()) {
                                        throw new FTPException("Completing Pending Commands Not Successfull");
                                    }
                                    PartStock stockRq = xmlMapper.readValue(remoteInput, PartStock.class); //  десериализуем (из потока создаём объект)
                                    Customer customer;//Получить клиента по ВН https://mkyong.com/spring/queryforobject-throws-emptyresultdataaccessexception-when-record-not-found/
                                    try {
                                        customer = jdbcTemplate.queryForObject("SELECT ID,ID_SVH,ID_WMS,ID_USR,N_ZAK,ID_KLIENT FROM kb_zak WHERE " +
                                                "id_usr IN ('KB_USR92734', 'KB_USR99992') AND id_klient = ?", new CustomerRowMapper(), stockRq.getClientId());
                                    } catch (EmptyResultDataAccessException e) {
                                        throw new MonitorException("ВН " + stockRq.getClientId() + " не найден");
                                    }
                                    //Получить остатки
                                    SqlParameterSource ftpParam = new MapSqlParameterSource().addValue("id", customer.getHolderID());
                                    List<PartStockLine> partStockLines = namedParameterJdbcTemplate.query(//TODO пустой набор не ошибка?
                                            "SELECT * FROM loads WHERE holder_id = :id", //FIXME поле master
                                            ftpParam, (rs, i) -> new PartStockLine(
                                                    rs.getInt("LINENO"),
                                                    rs.getString("ARTICLE"),
                                                    rs.getString("UPC"),
                                                    rs.getString("NAME"),
                                                    rs.getInt("QTY")));
                                    stockRq.setTimeStamp(new Date()); //текущая дата
                                    stockRq.setStockLines(partStockLines);

                                    // TODO имя файла PS_VN_TIMESTAMP/ PS from field prefix
                                    String fileName = "PS_" + customer.getClientId() + "_" + new Date().getTime() + ".xml";
                                    String xml = xmlMapper.writer().writeValueAsString(stockRq); // сериализация

                                    // выгрузка на FTP
                                    ftp.changeWorkingDirectory("/response"); //FIXME из таблицы
                                    is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
                                    boolean ok = ftp.storeFile(fileName, is);
                                    is.close();
                                    ftp.changeWorkingDirectory("/in"); //FIXME из таблицы вернуть
                                    if (ok) {
                                        //TODO обсудить логику/последовательность транзакции обмен
                                        //region Поиск/создание суточного заказа //TODO поле detail ? обсудить HELLMAN_STOCK
                                        String dailyOrderSql = "SELECT sp.id FROM kb_spros sp WHERE sp.n_gruz = 'STOCK' AND trunc(sp.dt_zakaz) = trunc(SYSDATE) AND sp.id_zak = ?";
                                        String dailyOrderId;//String.valueOf(++j);//fixme remove me
                                        try {
                                            dailyOrderId = jdbcTemplate.queryForObject(dailyOrderSql, String.class, customer.getId());
                                        } catch (EmptyResultDataAccessException e) {
                                            //FIXME доделать .withTableName("tab")
                                            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("kb_spros").usingGeneratedKeyColumns("id");
                                            MapSqlParameterSource params = new MapSqlParameterSource();
                                            params.addValue("dt_zakaz", new Date())
                                                    .addValue("id_zak", customer.getId())
                                                    .addValue("id_pok", customer.getId())
                                                    .addValue("n_gruz", "STOCK")
                                                    .addValue("usl", "Суточный заказ по пакетам PS");
                                            KeyHolder keyHolder = simpleJdbcInsert.executeAndReturnKeyHolder(params);
                                            dailyOrderId = keyHolder.getKeyAs(String.class);
                                        }
                                        //endregion

                                        // добавляем событие 4301 в заказ Получено входящее сообщение
                                        jdbcTemplate.update("INSERT INTO kb_sost (id_obsl, dt_sost, dt_sost_end, id_sost,  sost_prm, id_isp)VALUES (?, ?, ?, ?,?,?)",
                                                dailyOrderId, new Date(), new Date(), "KB_USL99770", "Получен запрос PART_STOCK", "010277043");
                                        // добавляем 4302 подтверждение что по данному заказу мы отправили уведомление
                                        jdbcTemplate.update(
                                                "INSERT INTO kb_sost (id_obsl, id_sost, dt_sost, dt_sost_end, sost_prm) VALUES (?, ?, ?, ?,?)",
                                                dailyOrderId, "KB_USL99771", new Date(), new Date(), fileName);

                                        //fixme ftp.deleteFile(file.getName());//TODO удаляем принятый файл
                                        log.info("Uploaded " + fileName);
                                    } else {
                                        throw new FTPException("Не удалось выгрузить " + fileName);
                                    }

                                    System.out.println(xml);
                                    System.out.println("stop");
                                }
                                break;
                                case ("S"): {
                                    InputStream remoteInput = ftp.retrieveFileStream(file.getName()); //загрузка файла в виде потока
                                    if (!ftp.completePendingCommand()) {
                                        throw new FTPException("Completing Pending Commands Not Successfull");
                                    }
                                    SKU sku = xmlMapper.readValue(remoteInput, SKU.class);
                                    Customer customer;//Получить клиента по ВН https://mkyong.com/spring/queryforobject-throws-emptyresultdataaccessexception-when-record-not-found/
                                    try {
                                        customer = jdbcTemplate.queryForObject("SELECT ID,ID_SVH,ID_WMS,ID_USR,N_ZAK,ID_KLIENT FROM kb_zak WHERE " +
                                                "id_usr IN ('KB_USR92734', 'KB_USR99992') AND id_klient = ?", new CustomerRowMapper(), sku.getClientId());
                                    } catch (EmptyResultDataAccessException e) {
                                        throw new FTPException("ВН " + sku.getClientId() + " не найден");
                                    }

                                    // передача номенклатуры
                                    // е.и. из справочника
                                    String uofm ="";
                                    try {
                                        uofm = jdbcTemplate.queryForObject("SELECT val_id  FROM sv_hvoc  " +
                                                "WHERE voc_id = 'KB_MEA' AND UPPER(val_short) = UPPER(?)", String.class, sku.getMeasure());
                                    } catch (EmptyResultDataAccessException e) {
                                        uofm = null;
                                    }
                                    //region поиск/создание суточного заказа
                                    String dailyOrderSql = "SELECT sp.id FROM kb_spros sp WHERE sp.n_gruz = 'SKU' AND trunc(sp.dt_zakaz) = trunc(SYSDATE) AND sp.id_zak = ?";
                                    String dailyOrderId;
                                    try {
                                        dailyOrderId = jdbcTemplate.queryForObject(dailyOrderSql, String.class, customer.getId());
                                    } catch (EmptyResultDataAccessException e) {
                                        //FIXME доделать .withTableName("tab")
                                        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("kb_spros").usingGeneratedKeyColumns("id");
                                        MapSqlParameterSource params = new MapSqlParameterSource();
                                        params.addValue("dt_zakaz", new Date())
                                                .addValue("id_zak", customer.getId())
                                                .addValue("id_pok", customer.getId())
                                                .addValue("n_gruz", "SKU")
                                                .addValue("usl", "Суточный заказ по пакетам SKU");
                                        KeyHolder keyHolder = simpleJdbcInsert.executeAndReturnKeyHolder(params);
                                        dailyOrderId = keyHolder.getKeyAs(String.class);
                                    }
                                    //endregion
/*
    INSERT INTO KB_T_ARTICLE
      (id_sost, comments, measure, marker, str_sr_godn, storage_pos, tip_tov) -- str_mu_code,categ, MARKER)
    VALUES
      (article, art_name, v_uof, upc, control_date, storage_pos, billing_class);
      '/AddingGoods/PRODUCT_LIFE') control_date, -
'/AddingGoods/STORAGE_POS') storage_pos, --
'/AddingGoods/BILLING_CLASS') billing_class

 */

                                    //TODO txManager.commit(TransactionStatus new );
                                    jdbcTemplate.update("DELETE FROM KB_T_ARTICLE");
                                    jdbcTemplate.update("INSERT INTO KB_T_ARTICLE (id_sost, comments, measure, marker, str_sr_godn, storage_pos, tip_tov) VALUES (?, ?, ?, ?,?,?,?)",
                                            sku.getArticle(), sku.getName(),uofm,sku.getUpc(),sku.getProductLife(),sku.getStoragePos(),sku.getBillingClass());
//TODO UPDATE KB_T_ARTICLE SET COMMENTS = REPLACE(REPLACE(RTRIM(LT --- ???

                                    // TODO kb_pack.wms3_updt_sku(l_id_zak, v_prf_wms, p_err);

                                    // добавляем событие 4301 в заказ Получено входящее сообщение
                                    jdbcTemplate.update("INSERT INTO kb_sost (id_obsl, dt_sost, dt_sost_end, id_sost,  sost_prm, id_isp)VALUES (?, ?, ?, ?,?,?)",
                                            dailyOrderId, new Date(), new Date(), "KB_USL99770", "Артикул"+ sku.getArticle() + " отправлен в СОХ", "010277043");

                                    System.out.println(customer.getClientId());
                                }
                                break;
                                case ("I"): {
                                    System.out.println("I");
                                    break;
                                }
                                case ("O"): {
                                    System.out.println("O");
                                }
                                break;
                                default:
                                    break;
                            }
                        }

                        break;
                    }
                }

            } catch (MonitorException e) { //обработчик работы с данными
                e.printStackTrace(); //TODO как обрабатывать? документ
            }catch (DataAccessException e){
                e.printStackTrace(); //TODO как обрабатывать? ошибка доступа
            }

            ftp.logout();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FTPException e) {
            log.error(e.getMessage() + ". Код " + ftp.getReplyCode());
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                    // do nothing
                }
            }
        }


    }

}
