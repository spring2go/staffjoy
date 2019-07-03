package xyz.staffjoy.whoami;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import xyz.staffjoy.common.config.StaffjoyRestConfig;

@Import(value = StaffjoyRestConfig.class)
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients(basePackages = {"xyz.staffjoy.company", "xyz.staffjoy.account"})
public class WhoAmIApplication {
    public static void main(String[] args) {
        SpringApplication.run(WhoAmIApplication.class, args);
    }
}
