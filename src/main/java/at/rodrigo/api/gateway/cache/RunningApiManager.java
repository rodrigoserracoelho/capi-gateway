package at.rodrigo.api.gateway.cache;

import at.rodrigo.api.gateway.entity.Path;
import at.rodrigo.api.gateway.entity.RunningApi;
import at.rodrigo.api.gateway.utils.Constants;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class RunningApiManager {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    public void runApi(String routeId, String apiId, Path path) {
        RunningApi runningApi = new RunningApi();
        runningApi.setId(apiId);
        runningApi.setRouteId(routeId);
        runningApi.setDisabled(false);
        runningApi.setFailedCalls(0);
        if(path.isBlockIfInError()) {
            runningApi.setMaxAllowedFailedCalls(path.getMaxAllowedFailedCalls());
        } else {
            runningApi.setMaxAllowedFailedCalls(-1);
        }
        getCachedApi().put(routeId, runningApi);
    }

    private RunningApi getRunningApi(String routeId) {
        if(getCachedApi().containsKey(routeId)) {
            return getCachedApi().get(routeId);
        } else {
            return null;
        }
    }

    public boolean blockApi(String routeId) {
        RunningApi runningApi = getRunningApi(routeId);
        if(runningApi != null) {
            if(runningApi.getFailedCalls() == runningApi.getMaxAllowedFailedCalls()) {
                runningApi.setDisabled(true);
                getCachedApi().put(routeId, runningApi);
                return true;
            } else {
                runningApi.setFailedCalls(runningApi.getFailedCalls() + 1);
                getCachedApi().put(routeId, runningApi);
                return false;
            }
        } else {
            return false;
        }
    }

    private Map<String, RunningApi> getCachedApi() {
        return hazelcastInstance.getMap("runningApi");
    }

    public List<RunningApi> getDisabledRunningApis() {
        List<RunningApi> disabledRunningApis = new ArrayList<>();
        Map<String, RunningApi> runningApis = getCachedApi();
        Iterator<String> i = runningApis.keySet().iterator();
        while(i.hasNext()) {
            String routeId = i.next();
            if(runningApis.get(routeId).isDisabled()) {
                disabledRunningApis.add(runningApis.get(routeId));
            }
        }
        return disabledRunningApis;
    }

    public int count() {
        return getCachedApi().size();
    }

}
