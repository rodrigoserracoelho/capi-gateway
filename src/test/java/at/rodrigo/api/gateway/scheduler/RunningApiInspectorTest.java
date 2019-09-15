package at.rodrigo.api.gateway.scheduler;

import at.rodrigo.api.gateway.cache.RunningApiManager;
import org.apache.camel.CamelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RunningApiInspectorTest {

    private RunningApiInspector runningApiInspectorUnderTest;

    @BeforeEach
    void setUp() {
        runningApiInspectorUnderTest = new RunningApiInspector();
        runningApiInspectorUnderTest.camelContext = mock(CamelContext.class);
        runningApiInspectorUnderTest.runningApiManager = mock(RunningApiManager.class);
    }

    @Test
    void testCheckDisabledRunningApis() throws Exception {
        // Setup
        when(runningApiInspectorUnderTest.runningApiManager.getDisabledRunningApis()).thenReturn(Arrays.asList());
        when(runningApiInspectorUnderTest.camelContext.getRoute("id")).thenReturn(null);
        when(runningApiInspectorUnderTest.camelContext.getRouteController()).thenReturn(null);
        when(runningApiInspectorUnderTest.camelContext.removeRoute("routeId")).thenReturn(false);

        // Run the test
        runningApiInspectorUnderTest.checkDisabledRunningApis();

        // Verify the results
    }

    @Test
    void testCheckDisabledRunningApis_CamelContextThrowsException() throws Exception {
        // Setup
        when(runningApiInspectorUnderTest.runningApiManager.getDisabledRunningApis()).thenReturn(Arrays.asList());
        when(runningApiInspectorUnderTest.camelContext.getRoute("id")).thenReturn(null);
        when(runningApiInspectorUnderTest.camelContext.getRouteController()).thenReturn(null);
        when(runningApiInspectorUnderTest.camelContext.removeRoute("routeId")).thenThrow(Exception.class);

        // Run the test
        runningApiInspectorUnderTest.checkDisabledRunningApis();

        // Verify the results
    }
}
