package at.rodrigo.api.gateway.cache;

import at.rodrigo.api.gateway.entity.Path;
import at.rodrigo.api.gateway.entity.RunningApi;
import at.rodrigo.api.gateway.entity.Verb;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.TestHazelcastInstanceFactory;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunningApiManagerTest extends HazelcastTestSupport {

    private static TestHazelcastInstanceFactory testInstanceFactory = new TestHazelcastInstanceFactory();

    @Test
    void testRunApi() {
        // Setup
        RunningApiManager runningApiManagerUnderTest = new RunningApiManager();
        ReflectionTestUtils.setField(runningApiManagerUnderTest, "hazelcastInstance", testInstanceFactory.newHazelcastInstance());

        String routeId = "super_unsafe_internal_GET";
        String apiId = UUID.randomUUID().toString();
        Path path = new Path();
        path.setPath("/internal");
        path.setVerb(Verb.GET);
        path.setBlockIfInError(true);
        path.setMaxAllowedFailedCalls(0);

         // Run the test
        runningApiManagerUnderTest.runApi(routeId, apiId, path);

        // Verify the results
        assertNotNull(runningApiManagerUnderTest);
    }

    @Test
    void testBlockApi() {
        // Setup
        RunningApiManager runningApiManagerUnderTest = new RunningApiManager();
        ReflectionTestUtils.setField(runningApiManagerUnderTest, "hazelcastInstance", testInstanceFactory.newHazelcastInstance());

        final String routeId = "super_unsafe_internal_GET";
        final String apiId = UUID.randomUUID().toString();
        final Path path = new Path();
        path.setPath("/internal");
        path.setVerb(Verb.GET);
        path.setBlockIfInError(true);
        path.setMaxAllowedFailedCalls(0);

        runningApiManagerUnderTest.runApi(routeId, apiId, path);

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

        final String routeId = "super_unsafe_internal_GET";
        final String apiId = UUID.randomUUID().toString();
        final Path path = new Path();
        path.setPath("/internal");
        path.setVerb(Verb.GET);
        path.setBlockIfInError(true);
        path.setMaxAllowedFailedCalls(0);

        runningApiManagerUnderTest.runApi(routeId, apiId, path);
        runningApiManagerUnderTest.blockApi(routeId);

        // Run the test
        List<RunningApi> result = runningApiManagerUnderTest.getDisabledRunningApis();

        // Verify the results
        assertEquals("super_unsafe_internal_GET", result.get(0).getRouteId());
    }

}
