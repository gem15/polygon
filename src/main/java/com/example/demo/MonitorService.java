package com.example.demo;

import com.example.demo.jsonData.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Deprecated
@Slf4j
@Service
public class MonitorService {

    JdbcTemplate jdbcTemplate;

    @Transactional
    public Customer findCustomerByClientId(int clientId) {
        return null;
    }


}
