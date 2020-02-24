package at.rodrigo.api.gateway.processor;

import at.rodrigo.api.gateway.cache.ThrottlingManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrafficInspectorProcessor implements Processor {

    @Autowired
    private ThrottlingManager throttlingManager;

    @Override
    public void process(Exchange exchange) {
        throttlingManager.incrementThrottlingByRouteID(exchange.getIn().getBody().toString(), 1);
    }
}
