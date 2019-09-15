package at.rodrigo.api.gateway.cache;

import at.rodrigo.api.gateway.entity.Path;
import at.rodrigo.api.gateway.entity.RunningApi;
import com.hazelcast.core.HazelcastInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class RunningApiManagerTest {

    @Mock
    private HazelcastInstance mockHazelcastInstance;

    private RunningApiManager runningApiManagerUnderTest = Mockito.mock(RunningApiManager.class);

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void testRunApi() {
        // Setup
        final String routeId = "routeId";
        final String apiId = "apiId";
        final Path path = null;
        when(mockHazelcastInstance.getMap("s")).thenReturn(null);

        // Run the test
        runningApiManagerUnderTest.runApi(routeId, apiId, path);

        // Verify the results
    }

    @Test
    void testBlockApi() {
        // Setup
        final String routeId = "routeId";
        when(mockHazelcastInstance.getMap("s")).thenReturn(null);

        // Run the test
        final boolean result = runningApiManagerUnderTest.blockApi(routeId);

        // Verify the results
        assertTrue(result);
    }

    @Test
    void testGetDisabledRunningApis() {
        // Setup
        final List<RunningApi> expectedResult = Arrays.asList();
        when(mockHazelcastInstance.getMap("s")).thenReturn(null);

        // Run the test
        final List<RunningApi> result = runningApiManagerUnderTest.getDisabledRunningApis();

        // Verify the results
        assertEquals(expectedResult, result);
    }
}
