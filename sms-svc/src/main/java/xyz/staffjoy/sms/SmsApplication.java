package xyz.staffjoy.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SmsApplication {

    public static void main(String[] args) {

        SpringApplication.run(SmsApplication.class, args);
    }
}

