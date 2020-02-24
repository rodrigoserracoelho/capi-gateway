package at.rodrigo.api.gateway.security;

import at.rodrigo.api.gateway.processor.TrafficInspectorProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaTrafficInspector extends RouteBuilder {

    @Value("${api.gateway.traffic.inspector.enabled}")
    private boolean trafficInspectorEnabled;

    @Value("${api.gateway.traffic.inspector.kafka.topic}")
    private String trafficInspectorKafkaTopic;

    @Value("${api.gateway.traffic.inspector.kafka.broker}")
    private String trafficInspectorKafkaBroker;

    @Value("${api.gateway.traffic.inspector.kafka.groupId}")
    private String trafficInspectorKafkaGroupId;

    @Autowired
    private TrafficInspectorProcessor trafficInspectorProcessor;

    @Override
    public void configure() {
        if(trafficInspectorEnabled) {
            log.info("Starting Traffic Inspector Route...");
            from("kafka:" + trafficInspectorKafkaTopic + "?brokers=" + trafficInspectorKafkaBroker + "&groupId=" + trafficInspectorKafkaGroupId)
               .process(trafficInspectorProcessor);
        }
    }
}

