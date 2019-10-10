package at.rodrigo.api.gateway.scheduler;

import at.rodrigo.api.gateway.cache.RunningApiManager;
import at.rodrigo.api.gateway.cache.ThrottlingManager;
import at.rodrigo.api.gateway.entity.RunningApi;
import at.rodrigo.api.gateway.entity.SuspensionType;
import at.rodrigo.api.gateway.entity.ThrottlingPolicy;
import at.rodrigo.api.gateway.utils.CamelUtils;
import com.hazelcast.core.IMap;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
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
    CompositeMeterRegistry meterRegistry;

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
                if(throttlingPolicy.isApplyPerPath()) {
                    List<String> routes = throttlingPolicy.getRouteID();
                    for(String routeID : routes) {
                        RunningApi runningApi = runningApiManager.getRunningApi(routeID);
                        if(!runningApi.isRemoved()) {
                            Counter counter = meterRegistry.get(routeID).counter();
                            Iterator<Measurement> iterator = counter.measure().iterator();
                            while(iterator.hasNext()) {
                                Calendar throttlingExpirationTime = Calendar.getInstance();
                                throttlingExpirationTime.add(Calendar.MILLISECOND, throttlingPolicy.getPeriodForMaxCalls());

                                Measurement measurement = iterator.next();
                                int value = (int) measurement.getValue();

                                if(throttlingPolicy.getThrottlingExpiration() == null) {
                                    throttlingPolicy.setThrottlingExpiration(throttlingExpirationTime.getTime());
                                    throttlingPolicy.setStartCountingAt(value);
                                    throttlingPolicy.setTotalCalls(value);
                                } else {
                                    if(executionTime.before(throttlingPolicy.getThrottlingExpiration())) {
                                        int realTimeCalls = throttlingPolicy.getTotalCalls() - throttlingPolicy.getStartCountingAt();
                                        if(realTimeCalls >= throttlingPolicy.getMaxCallsAllowed()) {
                                            camelUtils.suspendRoute(runningApi);
                                            runningApi.setRemoved(true);
                                            runningApi.setDisabled(true);
                                            runningApi.setSuspensionType(SuspensionType.THROTTLING);
                                            runningApi.setSuspensionMessage("Your route was suspended because its configured to accept: " + throttlingPolicy.getMaxCallsAllowed() + " calls during a period of " + throttlingPolicy.getPeriodForMaxCalls());
                                            camelUtils.addSuspendedRoute(runningApi);
                                            runningApiManager.saveRunningApi(runningApi);
                                        } else {
                                            throttlingPolicy.setTotalCalls(value);
                                        }
                                    } else {
                                        throttlingPolicy.setThrottlingExpiration(throttlingExpirationTime.getTime());
                                        throttlingPolicy.setStartCountingAt(value);
                                        throttlingPolicy.setTotalCalls(value);
                                    }
                                }
                            }
                            throttlingManager.saveThrottlingPolicy(policyID, throttlingPolicy);

                        } else if(throttlingPolicy.getThrottlingExpiration().before(executionTime) && runningApi.getSuspensionType().equals(SuspensionType.THROTTLING)) {
                            //Remove suspended route
                            camelUtils.suspendRoute(runningApi);
                            throttlingPolicy.setThrottlingExpiration(null);
                            runningApi.setDisabled(false);
                            runningApi.setRemoved(false);
                            camelUtils.addActiveRoute(runningApi);
                            runningApiManager.saveRunningApi(runningApi);
                            throttlingManager.saveThrottlingPolicy(policyID, throttlingPolicy);
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
