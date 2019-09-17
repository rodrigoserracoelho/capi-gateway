package at.rodrigo.api.gateway.controller;

import at.rodrigo.api.gateway.cache.RunningApiManager;
import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.processor.AuthProcessor;
import at.rodrigo.api.gateway.routes.DynamicRestRouteBuilder;
import at.rodrigo.api.gateway.routes.SimpleRestRouter;
import at.rodrigo.api.gateway.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
public class TempReloadController {

    @Value("${api.gateway.error.endpoint}")
    private String apiGatewayErrorEndpoint;

    @Autowired
    private AuthProcessor authProcessor;

    @Autowired
    CamelContext camelContext;

    @Autowired
    RunningApiManager runningApiManager;

    @RequestMapping( path="/reload/{context}/{path}/{verb}", method= RequestMethod.DELETE)
    public ResponseEntity<String> get(@PathVariable String context, @PathVariable String path, @PathVariable String verb, HttpServletRequest request) {

        String directRouteId = Constants.DIRECT_ROUTE_PREFIX + context.toLowerCase() + "/" + path.toLowerCase() + "-" + verb.toUpperCase();
        String restRouteId = Constants.REST_ROUTE_PREFIX  + context.toLowerCase() + "/" + path.toLowerCase() + "-" + verb.toUpperCase();

        Route directRoute = camelContext.getRoute(directRouteId);
        if(directRoute != null) {
            try {
                camelContext.getRouteController().stopRoute(directRouteId);
                camelContext.removeRoute(directRouteId);

                camelContext.getRouteController().stopRoute(restRouteId);
                camelContext.removeRoute(restRouteId);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else {
            log.info("Route does not exist: {}" , directRouteId);
        }


        JSONObject result = new JSONObject();
        result.put(Constants.RESULT, "removed");
        result.put("directRoute", directRouteId);
        result.put("restRoute", restRouteId);

        return new ResponseEntity<>(result.toString(), HttpStatus.OK);
    }

    @RequestMapping( path="/reload", method= RequestMethod.POST)
    public ResponseEntity<String> post(@RequestBody Api api, HttpServletRequest request) {
        JSONObject result = new JSONObject();
        try {
            camelContext.addRoutes(new DynamicRestRouteBuilder(camelContext, authProcessor, runningApiManager, apiGatewayErrorEndpoint, api));
            result.put(Constants.RESULT, "created");
        } catch (Exception e) {
            result.put(Constants.RESULT, "error");
            result.put(Constants.API, api);
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(result.toString(), HttpStatus.BAD_REQUEST);
        }
        result.put(Constants.API, api);
        return new ResponseEntity<>(result.toString(), HttpStatus.OK);
    }




}
