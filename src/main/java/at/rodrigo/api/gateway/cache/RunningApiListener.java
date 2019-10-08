package at.rodrigo.api.gateway.cache;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.MapEvent;
import com.hazelcast.map.listener.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RunningApiListener implements
        EntryAddedListener<String, String>,
        EntryRemovedListener<String, String>,
        EntryUpdatedListener<String, String>,
        EntryEvictedListener<String, String>,
        EntryLoadedListener<String,String>,
        MapEvictedListener,
        MapClearedListener {
    @Override
    public void entryAdded( EntryEvent<String, String> event ) {
        //log.info( "Entry Added:" + event );
    }

    @Override
    public void entryRemoved( EntryEvent<String, String> event ) {
        //log.info( "Entry Removed:" + event );
    }

    @Override
    public void entryUpdated( EntryEvent<String, String> event ) {
        //log.info( "Entry Updated:" + event );
    }

    @Override
    public void entryEvicted( EntryEvent<String, String> event ) {
        //log.info( "Entry Evicted:" + event );
    }

    @Override
    public void entryLoaded( EntryEvent<String, String> event ) {
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