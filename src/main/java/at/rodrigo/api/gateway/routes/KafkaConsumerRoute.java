package at.rodrigo.api.gateway.routes;

import at.rodrigo.api.gateway.processor.KafkaRouteProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaConsumerRoute extends RouteBuilder {

    @Autowired
    private KafkaRouteProcessor kafkaRouteProcessor;

    @Override
    public void configure() {

        from("kafka:test?brokers=localhost:9092").process(kafkaRouteProcessor)
         .to("http://localhost:9010/req");


                /*.log("Message received from Kafka : ${body}")
                .log("    on the topic ${headers[kafka.TOPIC]}")
                .log("    on the partition ${headers[kafka.PARTITION]}")
                .log("    with the offset ${headers[kafka.OFFSET]}")
                .log("    with the key ${headers[kafka.KEY]}")*/
        ;
    }




}