/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *          http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

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
