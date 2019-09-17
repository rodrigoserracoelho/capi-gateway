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
        final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Ik1VUkdSVFEyUmtJMk1qZzJOVE0wTTBFMVJESTJNalpETVRNeVJUTTNRakJEUTBVd09EZ3hSZyJ9.eyJpc3MiOiJodHRwczovL3JvZHJpZ29jb2VsaG8uYXV0aDAuY29tLyIsInN1YiI6IjBQSm5McXJTTWdsMUxsa0hMTkdBcFRRMHpXY0I3ZWRoQGNsaWVudHMiLCJhdWQiOiJodHRwczovL2NhcGkuZ2F0ZXdheS5hcGkiLCJpYXQiOjE1Njg0MTI5ODcsImV4cCI6MTU2ODQ5OTM4NywiYXpwIjoiMFBKbkxxclNNZ2wxTGxrSExOR0FwVFEweldjQjdlZGgiLCJndHkiOiJjbGllbnQtY3JlZGVudGlhbHMifQ.Xv7aVxGDe2aeETasG_5f_-9cKOEvVWdx97R4l0quHYGrtAvX-93laHsDf_k1hE5DIWNcoP8B0BlIEH2X8iaWyy81xYa_1rRg5KheXpmrcMTa5mtkUeeyy57CkfY3cdwEQviMuRdN7uW3KuzrmfuqWgW5zd3MW26O49UmBEUTaTiZwhrnkyMobXWWf6-HANerSZCTp8xhAvmyIxeReMo3-Y25HltYbvWaNqBK3PhNfQiR2vygkJY1bx-Ej2Nsv8uMkPBI-5o7zKxvogZRM4dQiecpxYJQeMgt6waoB4AgDISnBLWNLGGadTH8_3vPrLvor-ftvjt9Q71A9L_MC99uyQ";
        final JWSAlgorithm expectedResult = JWSAlgorithm.RS256;

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
