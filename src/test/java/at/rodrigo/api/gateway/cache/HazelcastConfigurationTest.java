package at.rodrigo.api.gateway.cache;

import com.hazelcast.config.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HazelcastConfigurationTest {

    private HazelcastConfiguration hazelcastConfigurationUnderTest;

    @BeforeEach
    void setUp() {
        hazelcastConfigurationUnderTest = new HazelcastConfiguration();
    }

    @Test
    void testHazelCastConfig() {
        // Setup
        final Config expectedResult = null;

        // Run the test
        final Config result = hazelcastConfigurationUnderTest.hazelCastConfig();

        // Verify the results
        assertEquals(expectedResult, result);
    }
}
