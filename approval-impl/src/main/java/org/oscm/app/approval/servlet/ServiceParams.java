/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.approval.servlet;

import java.util.ArrayList;
import java.util.Map;

/**
 * Defines the service parameters for the REST API call
 */
public class ServiceParams {
    enum MODE {
        GET, POST, PUT, DELETE
    };

    private MODE mode;
    private String[] path;
    private Map<?, ?> parameters;

    /**
     * Constructor
     */
    public ServiceParams(MODE mode, String[] path, Map<?, ?> parameters) {
        this.mode = mode;
        this.path = path;
        this.parameters = parameters;
    }

    /**
     * @return the mode
     */
    public MODE getMode() {
        return mode;
    }

    /**
     * @return the path
     */
    public String[] getPaths() {
        return path;
    }

    /**
     * @return the specified path part (or "" if not defined)
     */
    public String getPath(int index) {
        return (path.length > index) ? path[index] : "";
    }

    /**
     * Returns the specified parameter or ""
     */
    public String getParameter(String key) {
        return getParameter(key, 0);
    }

    /**
     * Returns the specified parameter or ""
     */
    public String getParameter(String key, int index) {
        if (parameters.containsKey(key)) {
            String[] values = (String[]) parameters.get(key);
            return values[index];
        }
        return "";
    }

    /**
     * Returns a set with all parameter keys
     */
    public String[] getParameterKeys() {
        ArrayList<String> list = new ArrayList<String>();
        for (Object key : parameters.keySet()) {
            list.add(key.toString());
        }
        return list.toArray(new String[list.size()]);
    }

}
