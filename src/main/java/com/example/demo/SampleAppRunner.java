package com.example.demo;

import com.example.demo.jsonData.Customer;
import com.example.demo.jsonData.PartStock;
import com.example.demo.jsonData.PartStockLine;
import com.exanple.demo.utils.NotificationException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
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
        // region open FTP sessionS
        try {
            ftp.connect("localhost", 21);
            ftp.enterLocalPassiveMode();
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                throw new NotificationException("Не удалось подключиться к FTP");
            }
            if (!ftp.login("anonymous", null)) {
                throw new NotificationException("Не удалось авторизоваться на FTP");
            }
            // endregion

            // ------------------------------Обработка входящих сообщений //FIXME add INOUT direction on prefix
/*
            if (!ftp.changeWorkingDirectory(resp.getPath()))
                throw new NotificationException("Не удалось сменить директорию");
*/
            ftp.changeWorkingDirectory("/in"); //FIXME из таблицы
            FTPFileFilter filter = ftpFile -> (ftpFile.isFile() && ftpFile.getName().endsWith(".xml"));
            FTPFile[] listFile = ftp.listFiles("/in", filter);//FIXME из таблицы
            for (FTPFile file : listFile) {
                String dir = file.getName().substring(0, 1).toUpperCase();
                switch (dir) {
                    case ("P"): {
                        InputStream remoteInput = ftp.retrieveFileStream(file.getName()); //получаем файл в виде потока
                        PartStock stockRq = xmlMapper.readValue(remoteInput, PartStock.class); //  десериализуем (из потока создаём объект)

                        //Получить клиента по ВН https://mkyong.com/spring/queryforobject-throws-emptyresultdataaccessexception-when-record-not-found/
                        Customer customer;
                        try {
                            customer = jdbcTemplate.queryForObject("SELECT ID,ID_SVH,ID_WMS,ID_USR,N_ZAK,ID_KLIENT FROM kb_zak WHERE " +
                                    "id_usr IN ('KB_USR92734', 'KB_USR99992') AND id_klient = ?", new CustomerRowMapper(), stockRq.getCustomerID());
                        } catch (EmptyResultDataAccessException e) {
                            throw new NotificationException("ВН " + stockRq.getCustomerID() + " не найден");
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

                        // TODO имя файла PS_VN_TIMESTAMP
                        String fileName = "PS_" + customer.getClientId() + "_" + new Date().getTime();
                        // сериализация
                        String xml = xmlMapper.writer().writeValueAsString(stockRq);
//                        System.out.println(xml);

                        // выгрузка на FTP
//                        ftp.changeWorkingDirectory("/respond"); //FIXME из таблицы
                        is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
                        boolean ok = ftp.storeFile(fileName, is);
                        is.close();
                        if (ok) { //TODO обсудить логику/последовательность
                            //Поиск/создание суточного заказа //TODO поле detail ? обсудить HELLMAN_STOCK
                            String dailyOrderSql = "SELECT sp.id FROM kb_spros sp WHERE sp.n_gruz = 'STOCK' AND trunc(sp.dt_zakaz) = trunc(SYSDATE) AND sp.id_zak = ?";
                            String dailyOrderId = null;
                            try {
                                dailyOrderId = jdbcTemplate.queryForObject(dailyOrderSql, String.class, customer.getId());
                            } catch (EmptyResultDataAccessException e) {
                                //TODO доделать
                                SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("tab").usingGeneratedKeyColumns("id");
                                MapSqlParameterSource params = new MapSqlParameterSource();
                                params.addValue("dt_zakaz", new Date())
                                        .addValue("id_zak", customer.getId())
                                        .addValue("id_pok", customer.getId())
                                        .addValue("n_gruz", "STOCK")
                                        .addValue("usl", "Суточный заказ по пакетам PS");
/*
  INSERT INTO kb_spros
  (id, dt_zakaz, id_zak, id_pok, n_gruz, usl)
VALUES
  (v_id_obsl,
   trunc(SYSDATE),
   l_id_zak,
   l_id_zak,
   'HELLMAN_STOCK',
   'Суточный заказ Хеллманн по пакетам PS');
*/
//                            Number newId = simpleJdbcInsert.executeAndReturnKey(params);
                                KeyHolder keyHolder = simpleJdbcInsert.executeAndReturnKeyHolder(params);
//                            dailyOrderId = keyHolder.getKeyAs(String.class);
                            }

                            // добавляем событие 4301 в заказ Получено входящее сообщение
                            jdbcTemplate.update("INSERT INTO kb_sost (id_obsl, dt_sost, dt_sost_end, id_sost,  sost_prm, id_isp)VALUES (?, ?, ?, ?,?,?)",
                                    dailyOrderId, new Date(), new Date(), "KB_USL99770", new Date(), new Date(), "Получен запрос PART_STOCK", "010277043");
                            // добавляем 4302 подтверждение что по данному заказу мы отправили уведомление
                            jdbcTemplate.update(
                                    "INSERT INTO kb_sost (id_obsl, id_sost, dt_sost, dt_sost_end, sost_prm) VALUES (?, ?, ?, ?,?)",
                                    dailyOrderId, "KB_USL99771", new Date(), new Date(), fileName);

                            //TODO ftp.deleteFile(file.getName());//TODO удаляем принятый файл
                             log.info("Uploaded " + fileName);
                        } else {
                            throw new NotificationException("Не удалось выгрузить " + fileName);
                        }
                        System.out.println("stop");
                   }
                    break;
                    case ("S"): {

                    }
                    break;
                    case ("I"): {

                    }
                    case ("O"): {

                    }
                    break;
                    default:
                        break;
                }
            }


            ftp.logout();
        } catch (IOException | //TODO
                NotificationException e) {
            e.printStackTrace();
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
