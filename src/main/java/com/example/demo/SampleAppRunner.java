package com.example.demo;

import com.exanple.demo.utils.MonitorCommonException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class SampleAppRunner implements ApplicationRunner {

    @Autowired
    FTPClient ftp;

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


            ftp.logout();
        } catch (IOException | MonitorCommonException e) {
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
