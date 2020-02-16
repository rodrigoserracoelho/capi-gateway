package at.rodrigo.api.gateway.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.UUID;

@Slf4j
@Component
public class StarterProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {

        exchange.getIn().setHeader("CALL_ID", UUID.randomUUID().toString());
        exchange.getIn().setHeader("START_TIME", LocalTime.now());

    }
}
