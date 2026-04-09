package az.matcha.company;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MatchaCompanyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatchaCompanyApplication.class, args);
    }
}
