package at.rodrigo.api.gateway.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component
public class AuthProcessor implements Processor {
    public void process(Exchange exchange) throws Exception {

        System.out.println(exchange.getFromEndpoint());
        System.out.println(exchange.getFromRouteId());

        exchange.getIn().setHeader("VALID", false);

        if(exchange.getIn().getHeader("Authorization") != null) {
            String token = exchange.getIn().getHeader("Authorization").toString();
            if(token.startsWith("ROD")) {
                exchange.getIn().setHeader("VALID", true);
            }
        }
    }
}
