package at.rodrigo.api.gateway.controller;

import at.rodrigo.api.gateway.cache.RunningApiManager;
import at.rodrigo.api.gateway.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
public class ErrorController {

    @Autowired
    private RunningApiManager runningApiManager;

    @GetMapping(path="/error")
    public ResponseEntity<String> get(HttpServletRequest request) {
        return buildResponse(request);
    }

    @PostMapping(path="/error")
    public ResponseEntity<String> post(HttpServletRequest request) {
        return buildResponse(request);
    }

    @PutMapping(path="/error")
    public ResponseEntity<String> put(HttpServletRequest request) {
        return buildResponse(request);
    }

    @DeleteMapping(path="/error")
    public ResponseEntity<String> delete(HttpServletRequest request) {
        return buildResponse(request);
    }

    private ResponseEntity<String> buildResponse(HttpServletRequest request) {
        JSONObject result = new JSONObject();

        String routeId = request.getHeader(Constants.ROUTE_ID_HEADER);
        if(routeId != null) {
            runningApiManager.blockApi(routeId);
        }

        try {
            if(request.getHeader(Constants.REASON_CODE_HEADER) != null && request.getHeader(Constants.REASON_MESSAGE_HEADER) != null) {
                result.put(Constants.ERROR, request.getHeader(Constants.REASON_MESSAGE_HEADER));
                int returnedCode = Integer.parseInt(request.getHeader(Constants.REASON_CODE_HEADER));
                return new ResponseEntity<>(result.toString(), HttpStatus.valueOf(returnedCode));
            } else {
                result.put(Constants.ERROR, "Bad request");
                return new ResponseEntity<>(result.toString(), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            result.put(Constants.ERROR, e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
