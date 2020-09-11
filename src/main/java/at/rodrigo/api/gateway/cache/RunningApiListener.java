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

import at.rodrigo.api.gateway.entity.RunningApi;
import at.rodrigo.api.gateway.utils.CamelUtils;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.MapEvent;
import com.hazelcast.map.listener.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RunningApiListener implements
        EntryAddedListener<String, RunningApi>,
        EntryRemovedListener<String, RunningApi>,
        EntryUpdatedListener<String, RunningApi>,
        EntryEvictedListener<String, RunningApi>,
        EntryLoadedListener<String, RunningApi>,
        MapEvictedListener,
        MapClearedListener {

    @Autowired
    CamelUtils camelUtils;

    @Override
    public void entryAdded( EntryEvent<String, RunningApi> event ) {
        //log.info( "Entry Added:" + event );
    }

    @Override
    public void entryRemoved( EntryEvent<String, RunningApi> event ) {
        //log.info( "Entry Removed:" + event );
    }

    @Override
    public void entryUpdated( EntryEvent<String, RunningApi> event ) {
        if(event.getMergingValue() != null) {
            log.info("MERGING NOT NULL: {}", event.getMergingValue().isRemoved());
        }
        if(event.getOldValue() != null) {
            log.info("OLD VALUE NOT NULL: {}", event.getOldValue().isRemoved());
        }
        if(event.getValue() != null) {
            log.info("VALUE NOT NULL: {}", event.getValue().isRemoved());
        }
        RunningApi runningApi = event.getValue();
        if(runningApi.isRemoved() && runningApi.isDisabled()) {
            log.info( "API detected for suspension: {}", runningApi.getRouteId());
            camelUtils.suspendRoute(runningApi);
            camelUtils.addSuspendedRoute(runningApi);
        } else {
            log.info( "API detected for reactivation: {}", runningApi.getRouteId());
            camelUtils.suspendRoute(runningApi);
            camelUtils.addActiveRoute(runningApi);
        }
    }

    @Override
    public void entryEvicted( EntryEvent<String, RunningApi> event ) {
        //log.info( "Entry Evicted:" + event );
    }

    @Override
    public void entryLoaded( EntryEvent<String, RunningApi> event ) {
        //log.info( "Entry Loaded:" + event );
    }

    @Override
    public void mapEvicted( MapEvent event ) {
        //log.info( "Map Evicted:" + event );
    }

    @Override
    public void mapCleared( MapEvent event ) {
        //log.info( "Map Cleared:" + event );
    }
}