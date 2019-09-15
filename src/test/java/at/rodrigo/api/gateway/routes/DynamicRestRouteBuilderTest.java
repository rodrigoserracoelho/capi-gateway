package at.rodrigo.api.gateway.routes;

import at.rodrigo.api.gateway.cache.RunningApiManager;
import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.processor.AuthProcessor;
import org.apache.camel.CamelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class DynamicRestRouteBuilderTest {

    @Mock
    private CamelContext mockContext;
    @Mock
    private AuthProcessor mockAuthProcessor;
    @Mock
    private RunningApiManager mockRunningApiManager;
    @Mock
    private Api mockApi;

    private DynamicRestRouteBuilder dynamicRestRouteBuilderUnderTest;

    @BeforeEach
    void setUp() {
        initMocks(this);
        dynamicRestRouteBuilderUnderTest = new DynamicRestRouteBuilder(mockContext, mockAuthProcessor, mockRunningApiManager, "apiGatewayErrorEndpoint", mockApi);
    }

    @Test
    void testConfigure() throws Exception {
        // Setup
        when(mockApi.getPaths()).thenReturn(Arrays.asList());
        when(mockApi.isSecured()).thenReturn(false);
        when(mockApi.getContext()).thenReturn("result");
        when(mockApi.getJwsEndpoint()).thenReturn("result");
        when(mockApi.getEndpoint()).thenReturn("result");
        when(mockApi.getName()).thenReturn("result");
        when(mockApi.getId()).thenReturn("result");

        // Run the test
        dynamicRestRouteBuilderUnderTest.configure();

        // Verify the results
        verify(mockRunningApiManager).runApi("routeId", "apiId", null);
    }

    @Test
    void testConfigure_ThrowsException() throws Exception {
        // Setup
        when(mockApi.getPaths()).thenReturn(Arrays.asList());
        when(mockApi.isSecured()).thenReturn(false);
        when(mockApi.getContext()).thenReturn("result");
        when(mockApi.getJwsEndpoint()).thenReturn("result");
        when(mockApi.getEndpoint()).thenReturn("result");
        when(mockApi.getName()).thenReturn("result");
        when(mockApi.getId()).thenReturn("result");

        // Run the test
        assertThrows(Exception.class, () -> {
            dynamicRestRouteBuilderUnderTest.configure();
        });
        verify(mockRunningApiManager).runApi("routeId", "apiId", null);
    }
}
