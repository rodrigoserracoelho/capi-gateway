package at.rodrigo.api.gateway.cache;

import at.rodrigo.api.gateway.entity.Path;
import at.rodrigo.api.gateway.entity.RunningApi;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
@Slf4j
public class RunningApiManager {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    //@Autowired
    //private RunningApiListener runningApiListener;

    @PostConstruct
    public void addListener() {
        getCachedApi().addEntryListener(new RunningApiListener(), true );
    }

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

    private IMap<String, RunningApi> getCachedApi() {
        return hazelcastInstance.getMap(CacheConstants.RUNNING_API_IMAP_NAME);
    }

    public List<RunningApi> getDisabledRunningApis() {
        List<RunningApi> disabledRunningApis = new ArrayList<>();
        IMap<String, RunningApi> runningApis = getCachedApi();
        Iterator<String> i = runningApis.keySet().iterator();
        while(i.hasNext()) {
            String routeId = i.next();
            if(runningApis.get(routeId).isDisabled() && !runningApis.get(routeId).isRemoved()) {
                disabledRunningApis.add(runningApis.get(routeId));
            }
        }
        return disabledRunningApis;
    }

    public void removeRunningApi(RunningApi runningApi) {
        this.getCachedApi().remove(runningApi);
    }

    public void saveRunningApi(RunningApi runningApi) {
        this.getCachedApi().put(runningApi.getRouteId(), runningApi);
    }

    public int count() {
        return getCachedApi().size();
    }

}
