package at.rodrigo.api.gateway.cache;

import com.hazelcast.config.*;

public class CAPICacheConfig extends Config {

    public CAPICacheConfig(String environment) {
        super();
        GroupConfig groupConfig = new GroupConfig();
        groupConfig.setName(environment);
        setInstanceName("running-apis-instance")
                .setGroupConfig(groupConfig)
                .addMapConfig(
                        new MapConfig()
                                .setName("running-apis-configuration")
                                .setMaxSizeConfig(new MaxSizeConfig(200, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
                                .setEvictionPolicy(EvictionPolicy.LRU)
                                .setTimeToLiveSeconds(-1));
    }
}
