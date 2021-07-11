package com.example.demo;

import com.example.demo.jsonData.PartStock;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
public class SampleAppRunner implements ApplicationRunner {

    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    FTPClient ftp;

    XmlMapper xmlMapper;

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
            xmlMapper = new XmlMapper();

            //TODO read and delete files
            ftp.changeWorkingDirectory("/in");
            System.out.println("Current directory is " + ftp.printWorkingDirectory());
            FTPFile[] listFile = ftp.listFiles("/in", filter);
            for (FTPFile file : listFile) {
                String dir = file.getName().substring(0, 1).toUpperCase();
                switch (dir) {
                    case ("P"): {
                        InputStream remoteInput=ftp.retrieveFileStream(file.getName()); //файл преобразуем в поток
                        PartStock stockRq = xmlMapper.readValue(remoteInput, PartStock.class); //  десериализуем (из потока создаём объект)

                        //проверка ВН
                        String sql = "SELECT count(*) FROM MyTable WHERE Param = ?";
                        boolean exists = false;
                        int count = jdbcTemplate.queryForObject(sql, Integer.class,stockRq.getCustomerID());
                        exists = count > 0;

                        ftp.deleteFile(file.getName());//удаляем принятый файл
                    }
                    break;
                    default:
                        break;
                }
            }


            ftp.logout();
        } catch (IOException |
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
