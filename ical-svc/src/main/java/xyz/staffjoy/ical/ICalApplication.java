package xyz.staffjoy.ical;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import xyz.staffjoy.common.config.StaffjoyWebConfig;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients(basePackages = {"xyz.staffjoy.company"})
@Import(value = StaffjoyWebConfig.class)
public class ICalApplication {
    public static void main(String[] args) {
        SpringApplication.run(ICalApplication.class, args);
    }
}
