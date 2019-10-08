package at.rodrigo.api.gateway.scheduler;

import at.rodrigo.api.gateway.cache.RunningApiManager;
import at.rodrigo.api.gateway.entity.RunningApi;
import at.rodrigo.api.gateway.entity.SuspensionType;
import at.rodrigo.api.gateway.utils.CamelUtils;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
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

    @Autowired
    CamelUtils camelUtils;

    @Scheduled(fixedRate = 60000)
    public void checkDisabledRunningApis() {
        log.info("CHECKING FOR APIS TO BLOCK");
        List<RunningApi> disabledRunningApis = runningApiManager.getDisabledRunningApis();
        for(RunningApi runningApi : disabledRunningApis) {
            camelUtils.suspendRoute(runningApi);
            runningApi.setRemoved(true);
            runningApi.setSuspensionType(SuspensionType.ERROR);
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
                    camelContext.getRouteController().resumeRoute(runningApi.getRouteId());
                    runningApi.setRemoved(false);
                    runningApi.setDisabled(false);
                    runningApi.setCountBlockChecks(0);
                    runningApi.setSuspensionType(null);
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
}
