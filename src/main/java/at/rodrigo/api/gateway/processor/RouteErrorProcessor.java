package at.rodrigo.api.gateway.processor;


import at.rodrigo.api.gateway.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RouteErrorProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        exchange.getIn().setHeader(Constants.CAPI_INTERNAL_ERROR, cause.getMessage());
        exchange.getIn().setHeader(Constants.CAPI_INTERNAL_ERROR_CLASS_NAME, cause.getClass().getName());
        exchange.getIn().setHeader(Exchange.HTTP_PATH, "");
    }
}