/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.approval.json;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JSONMapper {

    private static final Logger log = LoggerFactory.getLogger(JSONMapper.class);

    public static String toJSON(TriggerProcessData data) {
        log.debug("");
        ObjectMapper mapper = new ObjectMapper();
        String json = "";
        try {
            json = mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert TriggerProcessData to JSON.", e);
        }
        return json;
    }

    public static TriggerProcessData toTriggerProcessData(String json) {
        log.debug("");
        final ObjectMapper mapper = new ObjectMapper();

        // to enable standard indentation ("pretty-printing"):
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // to allow serialization of "empty" POJOs (no properties to serialize)
        // (without this setting, an exception is thrown in those cases)
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // to write java.util.Date, Calendar as number (timestamp):
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // DeserializationFeature for changing how JSON is read as POJOs:

        // to prevent exception when encountering unknown property:
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // to allow coercion of JSON empty String ("") to null Object value:
        mapper.enable(
                DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        final ObjectReader reader = mapper.reader(TriggerProcessData.class);
        TriggerProcessData data = null;

        try {
            data = reader.readValue(json);
        } catch (Exception e) {
            log.error("Failed to convert JSON to TriggerProcessData. " + json,
                    e);
        }

        return data;
    }
}
