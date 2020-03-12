package at.rodrigo.api.gateway.routes;


import at.rodrigo.api.gateway.cache.CacheConstants;
import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.repository.ApiRepository;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class SwaggerRouteStarter implements InitializingBean {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Override
    public void afterPropertiesSet()  {
        log.info("Starting configuration of Swagger Routes");
        List<Api> apiList = apiRepository.findAllBySwagger(true);
        for(Api api : apiList) {
            try {
                hazelcastInstance.getMap(CacheConstants.API_IMAP_NAME).put(api.getId(), api);
            } catch(Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}