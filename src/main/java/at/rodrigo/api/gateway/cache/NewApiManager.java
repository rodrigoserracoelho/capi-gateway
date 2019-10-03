package at.rodrigo.api.gateway.cache;

import at.rodrigo.api.gateway.entity.Api;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class NewApiManager {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private NewApiListener newApiListener;

    @PostConstruct
    public void addListener() {
        getCachedApi().addEntryListener( newApiListener, true );
    }

    private IMap<String, Api> getCachedApi() {
        return hazelcastInstance.getMap(CacheConstants.API_IMAP_NAME);
    }

}
