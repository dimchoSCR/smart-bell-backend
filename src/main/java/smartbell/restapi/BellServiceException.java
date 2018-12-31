package smartbell.spring;

import javax.xml.ws.WebServiceException;

public class BellServiceException extends WebServiceException {

    public BellServiceException(String message) {
        super(message);
    }

    public BellServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
