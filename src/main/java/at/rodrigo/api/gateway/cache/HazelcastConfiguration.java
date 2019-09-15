package at.rodrigo.api.gateway.cache;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfiguration {
    @Bean
    public Config hazelCastConfig(){
        Config config = new Config();
        config.setInstanceName("running-apis-instance")
            .addMapConfig(
                    new MapConfig()
                            .setName("running-apis-configuration")
                            .setMaxSizeConfig(new MaxSizeConfig(200, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
                            .setEvictionPolicy(EvictionPolicy.LRU)
                            .setTimeToLiveSeconds(-1));
        return config;
    }
}
