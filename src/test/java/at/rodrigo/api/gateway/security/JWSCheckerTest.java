package at.rodrigo.api.gateway.security;

import com.nimbusds.jose.JWSAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JWSCheckerTest {

    private JWSChecker jwsCheckerUnderTest;

    @BeforeEach
    void setUp() {
        jwsCheckerUnderTest = new JWSChecker();
    }

    @Test
    void testGetAlgorithm() throws Exception {
        // Setup
        final String token = "token";
        final JWSAlgorithm expectedResult = null;

        // Run the test
        final JWSAlgorithm result = jwsCheckerUnderTest.getAlgorithm(token);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    void testGetAlgorithm_ThrowsParseException() throws Exception {
        // Setup
        final String token = "token";

        // Run the test
        assertThrows(ParseException.class, () -> {
            jwsCheckerUnderTest.getAlgorithm(token);
        });
    }
}
