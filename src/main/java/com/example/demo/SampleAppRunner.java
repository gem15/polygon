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

            FTPFileFilter filter = ftpFile -> (ftpFile.isFile() && ftpFile.getName().endsWith(".xml"));
//            xmlMapper = new XmlMapper();

            ftp.changeWorkingDirectory("/in");
//            System.out.println("Current directory is " + ftp.printWorkingDirectory());
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
                        SqlParameterSource ftpParam = new MapSqlParameterSource().addValue("id",customer.getHolderID() );
                        List<PartStockLine> partStockLines = namedParameterJdbcTemplate.query(//TODO пустой набор не ошибка?
                                "SELECT * FROM loads WHERE holder_id = :id",
                                ftpParam, (rs, i) -> new PartStockLine(
                                        rs.getInt("LINENO"),
                                        rs.getString("ARTICLE"),
                                        rs.getString("UPC"),
                                        rs.getString("NAME"),
                                        rs.getInt("QTY")));
                        stockRq.setTimeStamp(new Date()); //текущая дата
                        stockRq.setStockLines(partStockLines);

                        String xml = xmlMapper.writer().writeValueAsString(stockRq);
                        System.out.println(xml);


                        //                        ftp.deleteFile(file.getName());//TODO удаляем принятый файл
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
