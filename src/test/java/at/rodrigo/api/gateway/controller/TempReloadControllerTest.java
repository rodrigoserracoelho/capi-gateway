package at.rodrigo.api.gateway.controller;

import at.rodrigo.api.gateway.cache.RunningApiManager;
import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.processor.AuthProcessor;
import org.apache.camel.CamelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class TempReloadControllerTest {

    @Mock
    private AuthProcessor mockAuthProcessor;
    @Mock
    private CamelContext mockCamelContext;
    @Mock
    private RunningApiManager mockRunningApiManager;

    @InjectMocks
    private TempReloadController tempReloadControllerUnderTest;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void testGet() throws Exception {
        // Setup
        final String context = "context";
        final String path = "path";
        final String verb = "verb";
        final HttpServletRequest request = null;
        final ResponseEntity<String> expectedResult = null;
        when(mockCamelContext.getRoute("id")).thenReturn(null);
        when(mockCamelContext.getRouteController()).thenReturn(null);
        when(mockCamelContext.removeRoute("routeId")).thenReturn(false);

        // Run the test
        final ResponseEntity<String> result = tempReloadControllerUnderTest.get(context, path, verb, request);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    void testGet_CamelContextThrowsException() throws Exception {
        // Setup
        final String context = "context";
        final String path = "path";
        final String verb = "verb";
        final HttpServletRequest request = null;
        final ResponseEntity<String> expectedResult = null;
        when(mockCamelContext.getRoute("id")).thenReturn(null);
        when(mockCamelContext.getRouteController()).thenReturn(null);
        when(mockCamelContext.removeRoute("routeId")).thenThrow(Exception.class);

        // Run the test
        final ResponseEntity<String> result = tempReloadControllerUnderTest.get(context, path, verb, request);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    void testGet1() throws Exception {
        // Setup
        final Api api = null;
        final HttpServletRequest request = null;
        final ResponseEntity<String> expectedResult = null;

        // Run the test
        final ResponseEntity<String> result = tempReloadControllerUnderTest.get(api, request);

        // Verify the results
        assertEquals(expectedResult, result);
        verify(mockCamelContext).addRoutes(null);
    }

    @Test
    void testGet_CamelContextThrowsException1() throws Exception {
        // Setup
        final Api api = null;
        final HttpServletRequest request = null;
        final ResponseEntity<String> expectedResult = null;
        doThrow(Exception.class).when(mockCamelContext).addRoutes(null);

        // Run the test
        final ResponseEntity<String> result = tempReloadControllerUnderTest.get(api, request);

        // Verify the results
        assertEquals(expectedResult, result);
    }
}
