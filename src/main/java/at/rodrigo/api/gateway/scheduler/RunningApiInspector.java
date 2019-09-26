package at.rodrigo.api.gateway.scheduler;

import at.rodrigo.api.gateway.cache.RunningApiManager;
import at.rodrigo.api.gateway.entity.RunningApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Scheduled(fixedRate = 60000)
    public void checkDisabledRunningApis() {
        List<RunningApi> disabledRunningApis = runningApiManager.getDisabledRunningApis();
        for(RunningApi runningApi : disabledRunningApis) {
            removeRoute(runningApi.getRouteId());
        }
    }

    private void removeRoute(String routeId) {
        Route directRoute = camelContext.getRoute(routeId);
        if(directRoute != null) {
            try {
                camelContext.getRouteController().stopRoute(routeId);
                camelContext.removeRoute(routeId);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else {
            log.info("Route does not exist: {}" , routeId);
        }
    }
}
