package at.rodrigo.api.gateway.controller;

import at.rodrigo.api.gateway.cache.RunningApiManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class ErrorControllerTest {

    @Mock
    private RunningApiManager mockRunningApiManager;

    @InjectMocks
    private ErrorController errorControllerUnderTest;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    /*@Test
    void testGet() {
        // Setup
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        final ResponseEntity<String> expectedResult = new ResponseEntity<>("{\"error\":\"Bad request\"}", HttpStatus.BAD_REQUEST);
        when(mockRunningApiManager.blockApi("routeId")).thenReturn(false);

        // Run the test
        final ResponseEntity<String> result = errorControllerUnderTest.get(request);

        // Verify the results
        assertEquals(expectedResult, result);
    }*/

    /*@Test
    void testPost() {
        // Setup
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        final ResponseEntity<String> expectedResult = new ResponseEntity<>("{\"error\":\"Bad request\"}", HttpStatus.BAD_REQUEST);
        when(mockRunningApiManager.blockApi("routeId")).thenReturn(false);

        // Run the test
        final ResponseEntity<String> result = errorControllerUnderTest.post(request);

        // Verify the results
        assertEquals(expectedResult, result);
    }*/

    /*@Test
    void testPut() {
        // Setup
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        final ResponseEntity<String> expectedResult = new ResponseEntity<>("{\"error\":\"Bad request\"}", HttpStatus.BAD_REQUEST);
        when(mockRunningApiManager.blockApi("routeId")).thenReturn(false);

        // Run the test
        final ResponseEntity<String> result = errorControllerUnderTest.put(request);

        // Verify the results
        assertEquals(expectedResult, result);
    }*/

    /*@Test
    void testDelete() {
        // Setup
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        final ResponseEntity<String> expectedResult = new ResponseEntity<>("{\"error\":\"Bad request\"}", HttpStatus.BAD_REQUEST);
        when(mockRunningApiManager.blockApi("routeId")).thenReturn(false);

        // Run the test
        final ResponseEntity<String> result = errorControllerUnderTest.delete(request);

        // Verify the results
        assertEquals(expectedResult, result);
    }*/
}
