package at.rodrigo.api.gateway.controller;


import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

//@Controller
//@Slf4j
public class SpringErrorController { //extends AbstractErrorController {

    /*public SpringErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }

    private static final String PATH = "/error";

    @RequestMapping(value= "/error", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> handleError(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, false);
        errorAttributes.remove("message");
        errorAttributes.remove("exception");
        return errorAttributes;
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }*/

}