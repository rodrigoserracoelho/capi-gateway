package at.rodrigo.api.gateway.routes;

import at.rodrigo.api.gateway.cache.RunningApiManager;
import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.processor.AuthProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@SpringBootTest
class SimpleRestRouterTest {

    @Mock
    private AuthProcessor mockAuthProcessor;

    @Mock
    private RunningApiManager mockRunningApiManager;

    private SimpleRestRouter simpleRestRouterUnderTest = Mockito.mock(SimpleRestRouter.class);

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void testConfigure() {
        // Setup

        // Run the test
        simpleRestRouterUnderTest.configure();

        // Verify the results
        verify(mockRunningApiManager).runApi("routeId", "apiId", null);
    }

    @Test
    void testAddRoute() {
        // Setup
        final Api api = null;

        // Run the test
        simpleRestRouterUnderTest.addRoute(api);

        // Verify the results
        verify(mockRunningApiManager).runApi("routeId", "apiId", null);
    }
}
