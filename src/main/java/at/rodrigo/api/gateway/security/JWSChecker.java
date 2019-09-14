package at.rodrigo.api.gateway.security;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.Header;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.ParseException;

@Slf4j
@Component
public class JWSChecker {

    public JWSAlgorithm getAlgorithm(String token) throws ParseException {
        JWT jwtToken = JWTParser.parse(token);
        Header tokenHeader = jwtToken.getHeader();
        Algorithm algorithm = tokenHeader.getAlgorithm();
        if(algorithm.getName().equals(JWSAlgorithm.RS256.getName())) {
            return JWSAlgorithm.RS256;
        } else if(algorithm.getName().equals(JWSAlgorithm.EdDSA.getName())) {
            return JWSAlgorithm.EdDSA;
        } else {
            throw new ParseException("Unsupported Algorithm", 0);
        }
    }
}