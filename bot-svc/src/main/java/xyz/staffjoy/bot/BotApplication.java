package xyz.staffjoy.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@EnableFeignClients(basePackages = {"xyz.staffjoy.mail", "xyz.staffjoy.sms", "xyz.staffjoy.company", "xyz.staffjoy.account"})
public class BotApplication {
    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }
}
