package at.rodrigo.api.gateway.processor;

import at.rodrigo.api.gateway.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

@Slf4j
@Component
public class PathVariableProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {
        String capiContext = getCapiContext(exchange);
        if(capiContext != null) {
            String httpPath = exchange.getIn().getHeader(Exchange.HTTP_PATH).toString();
            log.info(httpPath);
            log.info(capiContext);
            log.info(httpPath.substring(capiContext.length()));
            exchange.getIn().setHeader(Exchange.HTTP_PATH, httpPath.substring(capiContext.length()));
        }


        Map<String, Object> maps = exchange.getIn().getHeaders();
        Iterator<String> keys = maps.keySet().iterator();

        while(keys.hasNext()) {
            String key = keys.next();
            log.info("-------------------------> " + key + ": " + exchange.getIn().getHeader(key));
        }
    }

    private String getCapiContext(Exchange exchange) {
        if(exchange.getIn().getHeader(Constants.CAPI_CONTEXT_HEADER) != null) {
            String capiContext = exchange.getIn().getHeader(Constants.CAPI_CONTEXT_HEADER).toString().startsWith("/") ?
                    exchange.getIn().getHeader(Constants.CAPI_CONTEXT_HEADER).toString() :
                    "/" + exchange.getIn().getHeader(Constants.CAPI_CONTEXT_HEADER).toString();
            exchange.getIn().removeHeader(Constants.CAPI_CONTEXT_HEADER);
            return capiContext;
        }
        return null;
    }
}