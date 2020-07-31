/*******************************************************************************
 *                                                                              
 *  COPYRIGHT (C) FUJITSU LIMITED - ALL RIGHTS RESERVED.                  
 *
 *  Creation Date: 2014-02-24                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.approval.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.oscm.app.approval.util.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reflects the result of a service call.
 */
public class ServiceResult {
    private static final Logger logger = LoggerFactory.getLogger(ServiceResult.class);

    // Common error codes
    final public static int JSON_OK = 0;
    final public static int JSON_SSH_ERROR = 1;

    private int httpStatus = HttpServletResponse.SC_OK;
    private String htmlOutput;
    private JsonResult jsonResult;

    public void setError(int httpStatusCode, Throwable t) {
        setError(httpStatusCode, t.getMessage());
    }

    public void setError(int httpStatusCode, String text) {
        this.httpStatus = httpStatusCode;

        jsonResult = null;
        JsonResult json = getJson();
        json.begin();
        json.add("status", text);
        json.end();
    }

    public void setStatus(int httpStatusCode) {
        this.httpStatus = httpStatusCode;
    }

    public JsonResult getJson() {
        htmlOutput = null;
        if (jsonResult == null) {
            jsonResult = new JsonResult();
        }
        return jsonResult;
    }

    public void setHTMLOutput(String out) {
        jsonResult = null;
        htmlOutput = out;
    }

    /**
     * Returns cached status information to caller.
     */
    public void sendResult(HttpServletResponse response) {
        logger.debug("ServiceResult.sendResult() HTTP response status code: "
                + httpStatus);
        response.setStatus(httpStatus);
        try {
            if (jsonResult != null) {
                sendJsonResult(response);
            } else if (htmlOutput != null) {
                sendHtmlResult(response);
            }
        } catch (IOException e) {
            logger.error("ServiceResult.sendResult()", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    private void sendHtmlResult(HttpServletResponse response)
            throws IOException {

        logger.debug("HTML-OUT:" + htmlOutput);
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.println("<html>");
            out.println("<head><title>RMP-CTMG-Gateway</title></head>");
            out.println("<body>");
            out.println("<h1>RMP-CTMG-Gateway</h1>");
            out.println("<p>");
            out.println("<p>" + htmlOutput + "</p>");
            out.println("</body></html>");
        }
    }

    private void sendJsonResult(HttpServletResponse response)
            throws IOException {

        logger.debug("JSON-OUT:" + jsonResult.getJson());
        response.setContentType("application/json; charset=UTF-8");
        response.setHeader("cache-control",
                "private, max-age=0, no-cache, no-store");
        response.setHeader("pragma", "no-cache");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter();) {
            out.println(jsonResult.getJson());
        }
    }

}
