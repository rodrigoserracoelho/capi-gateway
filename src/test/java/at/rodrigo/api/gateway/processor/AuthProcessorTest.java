package at.rodrigo.api.gateway.processor;

import at.rodrigo.api.gateway.security.JWSChecker;
import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.text.ParseException;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class AuthProcessorTest {

    @Mock
    private JWSChecker mockJwsChecker;

    private AuthProcessor authProcessorUnderTest = Mockito.mock(AuthProcessor.class);

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void testProcess() throws Exception {
        // Setup
        final Exchange exchange = null;
        when(mockJwsChecker.getAlgorithm("token")).thenReturn(null);

        // Run the test
        authProcessorUnderTest.process(exchange);

        // Verify the results

    }

    @Test
    void testProcess_JWSCheckerThrowsParseException() throws Exception {
        // Setup
        final Exchange exchange = null;
        when(mockJwsChecker.getAlgorithm("token")).thenThrow(ParseException.class);

        // Run the test
        authProcessorUnderTest.process(exchange);

        // Verify the results
    }
}
