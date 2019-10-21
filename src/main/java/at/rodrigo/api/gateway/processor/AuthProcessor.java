package at.rodrigo.api.gateway.processor;

import at.rodrigo.api.gateway.security.JWSChecker;
import at.rodrigo.api.gateway.utils.Constants;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.List;

@Component
@Slf4j
public class AuthProcessor implements Processor {

   @Autowired
   private JWSChecker jwsChecker;

   @Autowired
   JWKSet jwkSet;

    public void process(Exchange exchange) {
        boolean validCall = false;
        try {
            String apiID = exchange.getIn().getHeader(Constants.API_ID_HEADER).toString();
            String jwtToken = exchange.getIn().getHeader(Constants.AUTHORIZATION_HEADER).toString().substring("Bearer ".length());
            exchange.getIn().removeHeader(Constants.AUTHORIZATION_HEADER);
            exchange.getIn().removeHeader(Constants.BLOCK_IF_IN_ERROR_HEADER);
            exchange.getIn().removeHeader(Constants.API_ID_HEADER);

            if(apiID != null && jwtToken != null) {
                ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();
                JWKSource keySource = new ImmutableJWKSet(jwkSet);
                JWSAlgorithm expectedJWSAlg = jwsChecker.getAlgorithm(jwtToken);
                JWSKeySelector keySelector = new JWSVerificationKeySelector(expectedJWSAlg, keySource);
                jwtProcessor.setJWSKeySelector(keySelector);
                JWTClaimsSet claimsSet = jwtProcessor.process(jwtToken, null);
                if(claimsSet != null) {
                    List<String> authorities = (List<String>) claimsSet.getClaim("authorities");
                    if(authorities != null && authorities.contains(apiID)) {
                        validCall = true;
                    }
                }
                if(!validCall) {
                    exchange.getIn().setHeader(Constants.REASON_CODE_HEADER, HttpStatus.FORBIDDEN.value());
                    exchange.getIn().setHeader(Constants.REASON_MESSAGE_HEADER, "You are not subscribed to this API");
                    exchange.setException(null);
                }
            } else {
                exchange.getIn().setHeader(Constants.REASON_CODE_HEADER, HttpStatus.BAD_REQUEST.value());
                exchange.getIn().setHeader(Constants.REASON_MESSAGE_HEADER, "Invalid token was provided");
                exchange.setException(null);
            }
        } catch(ParseException pex) {
            exchange.getIn().setHeader(Constants.REASON_CODE_HEADER, HttpStatus.BAD_REQUEST.value());
            exchange.getIn().setHeader(Constants.REASON_MESSAGE_HEADER, "Invalid token was provided");
            exchange.setException(null);
        } catch(Exception e) {
            exchange.getIn().setHeader(Constants.REASON_CODE_HEADER, HttpStatus.FORBIDDEN.value());
            exchange.getIn().setHeader(Constants.REASON_MESSAGE_HEADER, "Invalid Keys");
            exchange.setException(null);
        } finally {
            exchange.getIn().setHeader(Constants.VALID_HEADER, validCall);
        }
    }
}
