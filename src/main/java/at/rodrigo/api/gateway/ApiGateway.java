package at.rodrigo.api.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
@EnableScheduling
@Slf4j
public class ApiGateway {

    public static void main(String[] args) {
        SpringApplication.run(ApiGateway.class);
    }


    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }


}