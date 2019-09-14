package at.rodrigo.api.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import org.json.JSONObject;

@RestController
@Slf4j
public class ErrorController {

    @RequestMapping( path="/error", method= RequestMethod.GET)
    public ResponseEntity<String> get(HttpServletRequest request) {

        JSONObject result = new JSONObject();
        result.put("error", "permission denied");
        log.info(request.getHeader("routeId"));

        return new ResponseEntity<>(result.toString(), HttpStatus.BAD_REQUEST);
    }

    @RequestMapping( path="/error", method=RequestMethod.POST)
    public ResponseEntity<String> post(HttpServletRequest request) {

        JSONObject result = new JSONObject();
        result.put("error", "permission denied");
        return new ResponseEntity<>(result.toString(), HttpStatus.BAD_REQUEST);
    }

    @RequestMapping( path="/error", method=RequestMethod.PUT)
    public ResponseEntity<String> put(HttpServletRequest request) {

        JSONObject result = new JSONObject();
        result.put("error", "permission denied");
        log.info(request.getHeader("routeId"));
        return new ResponseEntity<>(result.toString(), HttpStatus.BAD_REQUEST);
    }


}
