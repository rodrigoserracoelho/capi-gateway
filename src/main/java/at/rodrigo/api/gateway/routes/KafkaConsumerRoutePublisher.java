package at.rodrigo.api.gateway.routes;

import at.rodrigo.api.gateway.entity.KafkaRoute;
import at.rodrigo.api.gateway.processor.KafkaRouteProcessor;
import at.rodrigo.api.gateway.repository.KafkaRouteRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class KafkaConsumerRoutePublisher extends RouteBuilder {

    @Autowired
    private KafkaRouteRepository kafkaRouteRepository;

    @Autowired
    private KafkaRouteProcessor kafkaRouteProcessor;

    @Override
    public void configure() {

        List<KafkaRoute> kafkaRoutes = kafkaRouteRepository.findAll();

        for(KafkaRoute kafkaRoute : kafkaRoutes) {
            from("kafka:" + kafkaRoute.getTopicName() + "?brokers=" + kafkaRoute.getBrokerEndpoint())
                    .process(kafkaRouteProcessor)
                    .to(kafkaRoute.getHttpEndpoint());
        }
    }
}