package at.rodrigo.api.gateway.scheduler;

import at.rodrigo.api.gateway.cache.RunningApiManager;
import at.rodrigo.api.gateway.cache.ThrottlingManager;
import at.rodrigo.api.gateway.entity.RunningApi;
import at.rodrigo.api.gateway.entity.SuspensionType;
import at.rodrigo.api.gateway.entity.ThrottlingPolicy;
import at.rodrigo.api.gateway.utils.CamelUtils;
import com.hazelcast.core.IMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Component
@Slf4j
public class ThrottlingInspector {

    @Autowired
    RunningApiManager runningApiManager;

    @Autowired
    ThrottlingManager throttlingManager;

    @Autowired
    CamelUtils camelUtils;

    @Scheduled(fixedRateString = "${api.gateway.api.throttling.inspector.period}")
    public void count() {

        Date executionTime = Calendar.getInstance().getTime();
        IMap<String, ThrottlingPolicy> throttlingPolicies = throttlingManager.getThrottlingPolicies();
        Iterator<String> throttlingIterator = throttlingPolicies.keySet().iterator();

        try {
            while(throttlingIterator.hasNext()) {
                String policyID = throttlingIterator.next();
                ThrottlingPolicy throttlingPolicy = throttlingPolicies.get(policyID);

                if(throttlingPolicy.getThrottlingExpiration() == null) {
                    Calendar throttlingExpirationTime = Calendar.getInstance();
                    throttlingExpirationTime.add(Calendar.MILLISECOND, throttlingPolicy.getPeriodForMaxCalls());
                    throttlingPolicy.setThrottlingExpiration(throttlingExpirationTime.getTime());
                    throttlingManager.saveThrottlingPolicy(policyID, throttlingPolicy);
                }

                if(throttlingPolicy.isApplyPerPath()) {
                    List<String> routes = throttlingPolicy.getRouteID();
                    for(String routeID : routes) {
                        RunningApi runningApi = runningApiManager.getRunningApi(routeID);
                        if(!runningApi.isRemoved()) {
                            if(throttlingPolicy.getTotalCalls() >= throttlingPolicy.getMaxCallsAllowed() && !runningApi.isRemoved()) {
                                Calendar throttlingExpirationTime = Calendar.getInstance();
                                throttlingExpirationTime.add(Calendar.MILLISECOND, throttlingPolicy.getPeriodForMaxCalls());

                                throttlingPolicy.setThrottlingExpiration(throttlingExpirationTime.getTime());
                                throttlingManager.saveThrottlingPolicy(policyID, throttlingPolicy);
                                runningApi.setRemoved(true);
                                runningApi.setDisabled(true);
                                runningApi.setSuspensionType(SuspensionType.THROTTLING);
                                runningApi.setSuspensionMessage("Your route was suspended because its configured to accept: " + throttlingPolicy.getMaxCallsAllowed() + " calls during a period of " + throttlingPolicy.getPeriodForMaxCalls());
                                runningApiManager.saveRunningApi(runningApi);
                                camelUtils.suspendRoute(runningApi);
                                camelUtils.addSuspendedRoute(runningApi);
                            } else {
                                if(throttlingPolicy.getThrottlingExpiration() != null && throttlingPolicy.getThrottlingExpiration().before(executionTime)) {
                                    throttlingPolicy.setTotalCalls(0);

                                    Calendar throttlingExpirationTime = Calendar.getInstance();
                                    throttlingExpirationTime.add(Calendar.MILLISECOND, throttlingPolicy.getPeriodForMaxCalls());
                                    throttlingPolicy.setThrottlingExpiration(throttlingExpirationTime.getTime());
                                    throttlingManager.saveThrottlingPolicy(policyID, throttlingPolicy);
                                }
                            }
                        } else if(throttlingPolicy.getThrottlingExpiration().before(executionTime) && runningApi.getSuspensionType().equals(SuspensionType.THROTTLING)) {
                            throttlingPolicy.setThrottlingExpiration(null);
                            throttlingPolicy.setTotalCalls(0);
                            throttlingManager.saveThrottlingPolicy(policyID, throttlingPolicy);
                            runningApi.setDisabled(false);
                            runningApi.setRemoved(false);
                            runningApiManager.saveRunningApi(runningApi);
                            //Remove suspended route
                            camelUtils.suspendRoute(runningApi);
                            camelUtils.addActiveRoute(runningApi);
                        }
                    }

                } else {
                    //apply per api
                }
            }
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
