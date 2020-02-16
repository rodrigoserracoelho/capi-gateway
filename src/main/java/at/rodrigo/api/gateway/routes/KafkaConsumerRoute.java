package at.rodrigo.api.gateway.routes;

import at.rodrigo.api.gateway.utils.CamelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaConsumerRoute extends RouteBuilder {

    @Autowired
    private CamelUtils camelUtils;

    @Override
    public void configure() {

        log.info("Starting configuration of Kafka Consumer Routes");
        from("kafka:test?brokers=localhost:9092")
                //.log("Message received from Kafka :" + body()) // ${body}")
                .to("http://capi.ecdevops.eu:9010/exposed?message= " + test(exchangeProperty("KAFKA_MSG")))
                .log("Message received from Kafka : ${body}")
                .log("    on the topic ${headers[kafka.TOPIC]}")
                .log("    on the partition ${headers[kafka.PARTITION]}")
                .log("    with the offset ${headers[kafka.OFFSET]}")
                .log("    with the key ${headers[kafka.KEY]}")
        ;
    }

    private String test(ValueBuilder body) {
        log.info(body.toString());
        return "M-"+body.toString();
    }


}