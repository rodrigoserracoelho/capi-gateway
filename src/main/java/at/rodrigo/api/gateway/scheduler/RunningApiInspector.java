package at.rodrigo.api.gateway.scheduler;

import at.rodrigo.api.gateway.cache.RunningApiManager;
import at.rodrigo.api.gateway.entity.RunningApi;
import at.rodrigo.api.gateway.processor.AuthProcessor;
import at.rodrigo.api.gateway.routes.DynamicPathRouteBuilder;
import at.rodrigo.api.gateway.utils.CamelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class RunningApiInspector {

    @Autowired
    CamelContext camelContext;

    @Autowired
    RunningApiManager runningApiManager;

    @Value("${api.gateway.error.endpoint}")
    private String apiGatewayErrorEndpoint;

    @Autowired
    private AuthProcessor authProcessor;

    @Autowired
    private CamelUtils camelUtils;


    @Scheduled(fixedRate = 60000)
    public void checkDisabledRunningApis() {
        log.info("CHECKING FOR APIS TO BLOCK");
        List<RunningApi> disabledRunningApis = runningApiManager.getDisabledRunningApis();
        for(RunningApi runningApi : disabledRunningApis) {
            removeRoute(runningApi);
            runningApi.setRemoved(true);
            runningApiManager.saveRunningApi(runningApi);
        }
    }

    @Scheduled(fixedRate = 60000)
    public void checkRunningApisToUnblock() {
        log.info("CHECKING FOR APIS TO UNBLOCK");
        List<RunningApi> removeddRunningApis = runningApiManager.getRemovedRunningApis();
        for(RunningApi runningApi : removeddRunningApis) {
            if(runningApi.getCountBlockChecks() == runningApi.getUnblockAfterMinutes()) {
                try {
                    camelContext.addRoutes(new DynamicPathRouteBuilder(camelContext, authProcessor, runningApiManager, camelUtils, apiGatewayErrorEndpoint, runningApi));
                    runningApi.setRemoved(false);
                    runningApi.setDisabled(false);
                    runningApi.setCountBlockChecks(0);
                    runningApiManager.saveRunningApi(runningApi);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            } else {
                runningApi.setCountBlockChecks(runningApi.getCountBlockChecks() + 1);
                runningApiManager.saveRunningApi(runningApi);
            }
        }
    }

    private void removeRoute(RunningApi runningApi) {
        Route routeToRemove = camelContext.getRoute(runningApi.getRouteId());
        if(routeToRemove != null) {
            try {
                log.info("Removing route: {}", routeToRemove);
                camelContext.getRouteController().stopRoute(runningApi.getRouteId());
                camelContext.removeRoute(runningApi.getRouteId());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else {
            log.info("Route does not exist: {}" , runningApi.getRouteId());
        }
    }
}
