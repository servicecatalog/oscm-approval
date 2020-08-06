/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.approval.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oscm.app.approval.servlet.ServiceParams.MODE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for services.
 */
public abstract class ServiceBase extends HttpServlet {
    private static final long serialVersionUID = -611754152437032466L;

    private static final Logger logger = LoggerFactory.getLogger(ServiceBase.class);

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handleServiceCall(MODE.GET, req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handleServiceCall(MODE.POST, req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handleServiceCall(MODE.DELETE, req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handleServiceCall(MODE.PUT, req, resp);
    }

    /**
     * Handle all incoming requests similary
     */
    private void handleServiceCall(MODE mode, HttpServletRequest req,
            HttpServletResponse resp) {
        logger.debug(getRequestString(req));
        ServiceResult result;
        try {
            // Split path into elements
            String[] path = splitPath(req.getPathInfo());

            // Invoke service method
            ServiceParams params = new ServiceParams(mode, path,
                    req.getParameterMap());
            result = doService(params, req.getReader(), req.getUserPrincipal().getName());
            if (result == null) {
                result = new ServiceResult();
                result.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

        } catch (Throwable t) {
            result = new ServiceResult();
            result.setError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    t.getMessage());
        }

        // And return result back to client
        result.sendResult(resp);

    }

    /**
     * Main method
     */
    public abstract ServiceResult doService(ServiceParams params,
            BufferedReader reader, String userid) throws Exception;

    /**
     * Split given path info into segments
     */
    private String[] splitPath(String path) {
        List<String> list = new ArrayList<String>();
        if (path != null) {
            String pathes[] = path.split("/");
            for (String elm : pathes) {
                if (elm.trim().length() > 0)
                    list.add(elm.trim());
            }
        }

        return list.toArray(new String[list.size()]);
    }

    /**
     * Returns whole request as string
     */
    private String getRequestString(HttpServletRequest req) {
        StringBuffer buf = new StringBuffer();
        buf.append(req.getMethod());
        buf.append(' ');
        if (req.getPathInfo() != null) {
            buf.append(req.getPathInfo());
        } else {
            buf.append('/');
        }
        if (req.getQueryString() != null) {
            buf.append(" Q:");
            buf.append(req.getQueryString());
        }
        return buf.toString();
    }

}
