package at.rodrigo.api.gateway.processor;

import at.rodrigo.api.gateway.utils.CamelUtils;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.search.RequiredSearch;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MetricsProcessor implements Processor {

    @Autowired
    private CompositeMeterRegistry meterRegistry;

    @Autowired
    private CamelUtils camelUtils;

    @Override
    public void process(Exchange exchange) {
        if(exchange.getIn().getHeader("CamelServletContextPath") != null && exchange.getIn().getHeader(Exchange.HTTP_METHOD) != null) {
            String metricName = camelUtils.normalizeRouteId(exchange.getIn().getHeader("CamelServletContextPath").toString().substring(1) + "-" + exchange.getIn().getHeader("CamelHttpMethod"));
            log.info(metricName);
            RequiredSearch s = meterRegistry.get(metricName);
            s.counter().increment();
        }
    }
}
