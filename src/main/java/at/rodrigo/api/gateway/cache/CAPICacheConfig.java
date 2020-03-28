package at.rodrigo.api.gateway.cache;

import com.hazelcast.config.*;
import com.hazelcast.spi.properties.GroupProperty;
import com.hazelcast.zookeeper.ZookeeperDiscoveryProperties;
import com.hazelcast.zookeeper.ZookeeperDiscoveryStrategyFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CAPICacheConfig extends Config {

    public CAPICacheConfig(String environment, boolean zookeeperDiscoveryEnabled, String zookeeperHost, String zookeeperPath, String zookeeperGroupKey) {

        super();

        if(zookeeperDiscoveryEnabled) {
            getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            setProperty(GroupProperty.DISCOVERY_SPI_ENABLED.getName(), "true");
            setProperty("connection-timeout-seconds", "30");

            DiscoveryStrategyConfig discoveryStrategyConfig = new DiscoveryStrategyConfig(new ZookeeperDiscoveryStrategyFactory());
            discoveryStrategyConfig.addProperty(ZookeeperDiscoveryProperties.ZOOKEEPER_URL.key(), zookeeperHost);
            discoveryStrategyConfig.addProperty(ZookeeperDiscoveryProperties.ZOOKEEPER_PATH.key(), zookeeperPath);
            discoveryStrategyConfig.addProperty(ZookeeperDiscoveryProperties.GROUP.key(), zookeeperGroupKey);
            getNetworkConfig().getJoin().getDiscoveryConfig().addDiscoveryStrategyConfig(discoveryStrategyConfig);
        }

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
