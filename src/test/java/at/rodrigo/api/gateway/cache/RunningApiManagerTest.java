package at.rodrigo.api.gateway.cache;

import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.entity.Path;
import at.rodrigo.api.gateway.entity.RunningApi;
import at.rodrigo.api.gateway.entity.Verb;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.TestHazelcastInstanceFactory;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunningApiManagerTest extends HazelcastTestSupport {

    private static TestHazelcastInstanceFactory testInstanceFactory = new TestHazelcastInstanceFactory();

    @Test
    void testRunApi() {
        // Setup
        RunningApiManager runningApiManagerUnderTest = new RunningApiManager();
        ReflectionTestUtils.setField(runningApiManagerUnderTest, "hazelcastInstance", testInstanceFactory.newHazelcastInstance());

        Api api = new Api();
        String routeId = "super_unsafe_internal_GET";
        String apiId = UUID.randomUUID().toString();
        Path path = new Path();
        path.setPath("/internal");
        path.setVerb(Verb.GET);
        api.setBlockIfInError(true);
        api.setMaxAllowedFailedCalls(0);

        List<Path> paths = new ArrayList<>();
        paths.add(path);
        api.setPaths(paths);

         // Run the test
        runningApiManagerUnderTest.runApi(routeId, api, path.getPath(), path.getVerb());

        // Verify the results
        assertNotNull(runningApiManagerUnderTest);
    }

    @Test
    void testBlockApi() {
        // Setup
        RunningApiManager runningApiManagerUnderTest = new RunningApiManager();
        ReflectionTestUtils.setField(runningApiManagerUnderTest, "hazelcastInstance", testInstanceFactory.newHazelcastInstance());

        Api api = new Api();
        final String routeId = "super_unsafe_internal_GET";
        final String apiId = UUID.randomUUID().toString();
        final Path path = new Path();
        path.setPath("/internal");
        path.setVerb(Verb.GET);
        api.setBlockIfInError(true);
        api.setMaxAllowedFailedCalls(0);

        List<Path> paths = new ArrayList<>();
        paths.add(path);
        api.setPaths(paths);

        runningApiManagerUnderTest.runApi(routeId, api, path.getPath(), path.getVerb());

        // Run the test
        final boolean result = runningApiManagerUnderTest.blockApi(routeId);

        // Verify the results
        assertTrue(result);

    }

    @Test
    void testGetDisabledRunningApis() {
        // Setup
        RunningApiManager runningApiManagerUnderTest = new RunningApiManager();
        ReflectionTestUtils.setField(runningApiManagerUnderTest, "hazelcastInstance", testInstanceFactory.newHazelcastInstance());

        Api api = new Api();
        final String routeId = "super_unsafe_internal_GET";
        final String apiId = UUID.randomUUID().toString();
        final Path path = new Path();
        path.setPath("/internal");
        path.setVerb(Verb.GET);
        api.setBlockIfInError(true);
        api.setMaxAllowedFailedCalls(0);

        List<Path> paths = new ArrayList<>();
        paths.add(path);
        api.setPaths(paths);

        runningApiManagerUnderTest.runApi(routeId, api, path.getPath(), path.getVerb());
        runningApiManagerUnderTest.blockApi(routeId);

        // Run the test
        List<RunningApi> result = runningApiManagerUnderTest.getDisabledRunningApis();

        // Verify the results
        assertEquals("super_unsafe_internal_GET", result.get(0).getRouteId());
    }

}
