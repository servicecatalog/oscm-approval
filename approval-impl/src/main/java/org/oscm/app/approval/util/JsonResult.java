/*******************************************************************************
 *                                                                              
 *  COPYRIGHT (C) FUJITSU LIMITED - ALL RIGHTS RESERVED. 
 *       
 *  Creation Date: 2013-02-06                                                       
 *                                                                              
 *******************************************************************************/
package org.oscm.app.approval.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Create a JSON structure.
 */
public class JsonResult {
    private ByteArrayOutputStream out;
    private JsonGenerator g;

    /**
     * Constructor
     */
    public JsonResult() {
        try {
            out = new ByteArrayOutputStream();
            g = new JsonFactory().createGenerator(out, JsonEncoding.UTF8);
        } catch (IOException ioe) {
            // Ignore
        }
    }

    public void begin() {
        try {
            g.writeStartObject();
        } catch (IOException ioe) {
            // Ignore
        }
    }

    public void begin(String subelm) {
        try {
            if (subelm != null && subelm.length() > 0) {
                g.writeFieldName(subelm);
            }
            g.writeStartObject();
        } catch (IOException ioe) {
            // Ignore
        }
    }

    public void beginArray(String subelm) {
        try {
            g.writeArrayFieldStart(subelm);
        } catch (IOException ioe) {
            // Ignore
        }
    }

    public void beginArray() {
        try {
            g.writeStartArray();
        } catch (IOException ioe) {
            // Ignore
        }
    }

    public void endArray() {
        try {
            g.writeEndArray();
        } catch (IOException ioe) {
            // Ignore
        }
    }

    public void end() {
        try {
            g.writeEndObject();
        } catch (IOException ioe) {
            // Ignore
        }
    }

    // public void end(String status) {
    // try {
    // if (status != null) {
    // this.add("result", status);
    // }
    // g.writeEndObject();
    // } catch (IOException ioe) {
    // // Ignore
    // }
    // }

    public void add(String key, String value) {
        try {
            g.writeStringField(key, value);
        } catch (IOException ioe) {
            // Ignore
        }
    }

    public void add(String key, boolean value) {
        try {
            g.writeBooleanField(key, value);
        } catch (IOException ioe) {
            // Ignore
        }
    }

    public void add(String key, long value) {
        try {
            g.writeNumberField(key, value);
        } catch (IOException ioe) {
            // Ignore
        }
    }

    public void add(String key, BigDecimal value) {
        try {
            g.writeNumberField(key, value);
        } catch (IOException ioe) {
            // Ignore
        }
    }

    public String getJson() {
        try {
            g.close();
            return out.toString();
        } catch (IOException ioe) {
            return ioe.toString();
        }
    }
}
