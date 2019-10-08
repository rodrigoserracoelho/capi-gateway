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
import org.apache.camel.CamelContext;
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
    CamelContext camelContext;

    @Autowired
    CompositeMeterRegistry meterRegistry;

    @Autowired
    RunningApiManager runningApiManager;

    @Autowired
    ThrottlingManager throttlingManager;

    @Autowired
    CamelUtils camelUtils;

    @Scheduled(fixedRate = 15000)
    public void count() {
        log.info("Throttling Inspector started at: " + Calendar.getInstance().getTime().toString());
        Date executionTime = Calendar.getInstance().getTime();
        IMap<String, ThrottlingPolicy> throttlingPolicies = throttlingManager.getThrottlingPolicies();
        Iterator<String> throttlingIterator = throttlingPolicies.keySet().iterator();
        log.info("------------------------------------------------------------------------------------------------------------------------");
        try {
            while(throttlingIterator.hasNext()) {
                String policyID = throttlingIterator.next();
                ThrottlingPolicy throttlingPolicy = throttlingPolicies.get(policyID);
                if(throttlingPolicy.isApplyPerPath()) {
                    List<String> routes = throttlingPolicy.getRouteID();
                    for(String routeID : routes) {
                        log.info("P_ID: " + policyID + " - R_ID: " + routeID);
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
                                    log.info("NO CALENDAR INFO, START ONE");
                                    throttlingPolicy.setThrottlingExpiration(throttlingExpirationTime.getTime());
                                    throttlingPolicy.setStartCountingAt(value);
                                    throttlingPolicy.setTotalCalls(value);
                                } else {
                                    if(executionTime.before(throttlingPolicy.getThrottlingExpiration())) {
                                        int realTimeCalls = throttlingPolicy.getTotalCalls() - throttlingPolicy.getStartCountingAt();
                                        if(realTimeCalls >= throttlingPolicy.getMaxCallsAllowed()) {
                                            log.info("BLOCK THIS ROUTE");
                                            log.info(throttlingPolicy.getTotalCalls()+"");
                                            log.info(throttlingPolicy.getStartCountingAt()+"");
                                            log.info(throttlingPolicy.getMaxCallsAllowed()+"");
                                            runningApi.setRemoved(true);
                                            runningApi.setDisabled(true);
                                            runningApi.setSuspensionType(SuspensionType.THROTTLING);
                                            camelUtils.suspendRoute(runningApi);
                                            runningApiManager.saveRunningApi(runningApi);
                                        } else {
                                            log.info("DONT BLOCK THIS ROUTE");
                                            throttlingPolicy.setTotalCalls(value);
                                        }
                                    } else {
                                        log.info("STARTING NEW PERIOD AND INCREMENTING NUMBER");
                                        throttlingPolicy.setThrottlingExpiration(throttlingExpirationTime.getTime());
                                        throttlingPolicy.setStartCountingAt(value);
                                        throttlingPolicy.setTotalCalls(value);
                                    }
                                }
                            }
                            throttlingManager.saveThrottlingPolicy(policyID, throttlingPolicy);

                        } else if(throttlingPolicy.getThrottlingExpiration().before(executionTime) && runningApi.getSuspensionType().equals(SuspensionType.THROTTLING)) {
                            log.info("re-enabling route....");
                            throttlingPolicy.setThrottlingExpiration(null);
                            runningApi.setDisabled(false);
                            runningApi.setRemoved(false);
                            camelContext.getRouteController().resumeRoute(runningApi.getRouteId());
                            runningApiManager.saveRunningApi(runningApi);
                            throttlingManager.saveThrottlingPolicy(policyID, throttlingPolicy);
                        }
                    }

                } else {
                    //apply per api
                }
            }
            log.info("------------------------------------------------------------------------------------------------------------------------");

        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
