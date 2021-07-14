package com.example.demo;

import com.example.demo.jsonData.Customer;
import com.example.demo.jsonData.PartStock;
import com.example.demo.jsonData.PartStockLine;
import com.exanple.demo.utils.MonitorCommonException;
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
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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


    @Override
    public void run(ApplicationArguments args) {
        log.info("Executing SampleAppRunner");
        // region open FTP sessionS
        try {
            ftp.connect("localhost", 21);
            ftp.enterLocalPassiveMode();
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                throw new MonitorCommonException("Не удалось подключиться к FTP");
            }
            if (!ftp.login("anonymous", null)) {
                throw new MonitorCommonException("Не удалось авторизоваться на FTP");
            }
            // endregion

            ftp.changeWorkingDirectory("/in");

            FTPFileFilter filter = ftpFile -> (ftpFile.isFile() && ftpFile.getName().endsWith(".xml"));
            FTPFile[] listFile = ftp.listFiles("/in", filter);
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
                            throw new MonitorCommonException("ВН " + stockRq.getCustomerID() + " не найден");
                        }
                        //Получить остатки
                        SqlParameterSource ftpParam = new MapSqlParameterSource().addValue("id", customer.getHolderID());
                        List<PartStockLine> partStockLines = namedParameterJdbcTemplate.query(//TODO пустой набор не ошибка?
                                "SELECT * FROM loads WHERE holder_id = :id", //TODO поле master
                                ftpParam, (rs, i) -> new PartStockLine(
                                        rs.getInt("LINENO"),
                                        rs.getString("ARTICLE"),
                                        rs.getString("UPC"),
                                        rs.getString("NAME"),
                                        rs.getInt("QTY")));
                        stockRq.setTimeStamp(new Date()); //текущая дата
                        stockRq.setStockLines(partStockLines);

//                        String xml = xmlMapper.writer().writeValueAsString(stockRq);
//                        System.out.println(xml);
                        //TODO поле detail ? обсудить HELLMAN_STOCK

                        //Поиск/создание суточного заказа
                        String dailyOrderSql = "SELECT sp.id FROM kb_spros sp WHERE sp.n_gruz = 'STOCK'\n" +
                                "AND trunc(sp.dt_zakaz) = trunc(SYSDATE) AND sp.id_zak = ?";
                        String customerId = null;
                        try {
                            customerId = jdbcTemplate.queryForObject(dailyOrderSql, String.class, customer.getId());
                        } catch (EmptyResultDataAccessException e) {
                            //TODO доделать
                            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("tab").usingGeneratedKeyColumns("id");
                            MapSqlParameterSource params = new MapSqlParameterSource();
                            params.addValue("name", "Steve Jobs");
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
//                                    .addValue("email", "steve@apple.com")
//                                    .addValue("address", "USA");
//                            Number newId = simpleJdbcInsert.executeAndReturnKey(params);
                            KeyHolder keyHolder = simpleJdbcInsert.executeAndReturnKeyHolder(params);
//                            customerId = keyHolder.getKeyAs(Integer.class);
                        }
            /*--добавляем событие 4301 в заказ
            INSERT INTO kb_sost
                    (id_obsl, dt_sost, dt_sost_end, id_sost,
                          sost_doc,--ignored
                           sost_prm, id_isp)
            VALUES  (v_id_obsl,SYSDATE,SYSDATE,     KB_USL99770', p_id_file,
                            'Получен запрос PART_STOCK ' || p_id_file, '010277043');*/
                        // добавляем событие 4301 в заказ
                        jdbcTemplate.update( "INSERT INTO kb_sost (id_obsl, dt_sost, dt_sost_end, id_sost,  sost_prm, id_isp)VALUES (?, ?, ?, ?,?,&)",
                                customerId, new Date(),new Date(),"KB_USL99770", new Date(), new Date(), "Получен запрос PART_STOCK","010277043");

                        System.out.println("stop");


                        //ftp.deleteFile(file.getName());//TODO удаляем принятый файл
                    }
                    break;
                    default:
                        break;
                }
            }


            ftp.logout();
        } catch (IOException | //TODO
                MonitorCommonException e) {
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
