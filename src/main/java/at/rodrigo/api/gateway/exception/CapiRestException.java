package at.rodrigo.api.gateway.exception;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class CapiRestException {

    private String routeID;
    private String errorMessage;
    private int errorCode;
    private String exception;
    private String internalExceptionMessage;
    private String zipkinTraceID;




}
