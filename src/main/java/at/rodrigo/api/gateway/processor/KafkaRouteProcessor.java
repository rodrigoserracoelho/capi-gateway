package at.rodrigo.api.gateway.processor;


import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaRouteProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {
        exchange.getIn().setHeader("MSG", exchange.getIn().getBody().toString());
    }
}
