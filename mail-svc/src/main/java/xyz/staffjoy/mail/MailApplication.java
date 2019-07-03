package xyz.staffjoy.mail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MailApplication {

    public static void main(String[] args) {

        SpringApplication.run(MailApplication.class, args);
    }

}

