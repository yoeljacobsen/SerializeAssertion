package com.emet.customassertion.serialize;

import com.l7tech.policy.assertion.ext.CustomAssertion;
import com.l7tech.policy.assertion.ext.ServiceInvocation;
import com.l7tech.policy.assertion.ext.ServiceRequest;
import com.l7tech.policy.assertion.ext.ServiceResponse;

import sun.misc.BASE64Encoder;
import javax.xml.stream.*;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * An assertion which serializes an object to a BASE64 string.
 */
public class SerializeCustomAssertionServiceInvocation extends ServiceInvocation {
    private static final Logger logger = Logger.getLogger(SerializeCustomAssertionServiceInvocation.class.getName());

    private static final String SYSTEM_ID_MESSAGE = "http://layer7tech.com/message"; // Dummy system identifier used to identify errors parsing a message.

    private static final XMLReporter SILENT_REPORTER = new XMLReporter() {
        @Override
        public void report( final String message, final String errorType, final Object relatedInformation, final Location location ) throws XMLStreamException {
            throw new XMLStreamException(message, location);
        }
    };

    private static final XMLResolver FAILING_RESOLVER = new XMLResolver() {
        @Override
        public Object resolveEntity( final String publicID, final String systemID, final String baseURI, final String namespace ) throws XMLStreamException {
            throw new XMLStreamException("External entity access forbidden '"+systemID+"' relative to '"+baseURI+"'.");
        }
    };

    // Utility regexes, only needed if performBusinessLogicValidation == true
    private static final Pattern PAT_COMMA = Pattern.compile(Pattern.quote(","));
    private static final Pattern PAT_COLON = Pattern.compile(Pattern.quote(":"));

    private SerializeCustomAssertion data;

    /**
     * when the server side policy is created, the ssg will pass the custom assertion containing the data
     * (if any) that was entered by the administrator
     */
    public void setCustomAssertion(CustomAssertion customAssertion) {
        super.setCustomAssertion(customAssertion);
        assert(customAssertion instanceof SerializeCustomAssertion);
        data = (SerializeCustomAssertion)customAssertion;
    }

    /**
     * if this assertion is invoked BEFORE routing occurs, this method will be called otherwise. onResponse will
     * be called instead.
     */
    public void onRequest(ServiceRequest serviceRequest) throws IOException, GeneralSecurityException {
        handleInvocation(serviceRequest);
    }

    /**
     * if this assertion is invoked AFTER routing occurs, this method will be called. otherwise onRequest will
     * be called instead.
     */
    public void onResponse(ServiceResponse serviceResponse) throws IOException, GeneralSecurityException {
        handleInvocation(serviceResponse);
    }

    private void handleInvocation(Object ctx) throws IOException {
        try {
            handleInvocationThrows(ctx);
        } catch (Exception e) {
            final String msg = "Error serializing to BASE64: " + e.getMessage();
            logger.log(Level.WARNING, msg, e);
            throw new IOException(msg, e);
        }
    }

    private void handleInvocationThrows(Object ctx) throws Exception {
        String inputVarName = data.getInputVarName();

        String outputVarName = data.getOutputVarName();

        Object inputVal = getVariable(ctx, inputVarName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(inputVal);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        byte[] outputBytes = baos.toByteArray();
        String outputString = new BASE64Encoder().encode(outputBytes);

        setVariable(ctx, outputVarName, outputString);
    }


    private Object getVariable(Object serviceReqOrResp, String varname) {
        if (serviceReqOrResp instanceof ServiceRequest) {
            ServiceRequest serviceRequest = (ServiceRequest) serviceReqOrResp;
            return serviceRequest.getVariable(varname);
        } else if (serviceReqOrResp instanceof ServiceResponse) {
            ServiceResponse serviceResponse = (ServiceResponse) serviceReqOrResp;
            return serviceResponse.getVariable(varname);
        } else {
            throw new ClassCastException("serviceReqOrResp argument must be either ServiceRequest or ServiceResponse");
        }
    }

    private void setVariable(Object serviceReqOrResp, String varname, Object value) {
        if (serviceReqOrResp instanceof ServiceRequest) {
            ServiceRequest serviceRequest = (ServiceRequest) serviceReqOrResp;
            serviceRequest.setVariable(varname, value);
        } else if (serviceReqOrResp instanceof ServiceResponse) {
            ServiceResponse serviceResponse = (ServiceResponse) serviceReqOrResp;
            serviceResponse.setVariable(varname, value);
        } else {
            throw new ClassCastException("serviceReqOrResp argument must be either ServiceRequest or ServiceResponse");
        }
    }

}
