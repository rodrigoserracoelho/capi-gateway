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

    /*@Bean
    MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }*/

    /*@Bean
    public MeterRegistryCustomizer<MeterRegistry> commonTags() {
        return r -> r.config().commonTags("application", "CAPI");
    }*/

    /*@Bean
    CamelContextConfiguration contextConfiguration() {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext context) {
                log.info("---------------------------------------Configuring Camel metrics on all routes---------------------------------------------------");
                MetricsRoutePolicyFactory fac = new MetricsRoutePolicyFactory();
                fac.setMetricsRegistry(metricRegistry());
                context.addRoutePolicyFactory(fac);
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {
                // noop
            }
        };
    }*/

}