package com.github.v_ctor.json.unflattener;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class ObjectMapperJsonUnflattener extends ObjectMapper {

    public <T> T readValue(Map<String, String> data, Class<T> valueType) throws IOException {
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(() -> data, outputStream);
        return getT(valueType, outputStream, jsonUnflattener);
    }

    public <T> T readValue(FlatterenMapStringString data, Class<T> valueType) throws IOException {
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(data, outputStream);
        return getT(valueType, outputStream, jsonUnflattener);
    }

    public <T> T readValue(FlatterenMapStringStringArray data, Class<T> valueType) throws IOException {
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(data, outputStream);
        return getT(valueType, outputStream, jsonUnflattener);
    }

    private <T> T getT(Class<T> valueType, OutputStream outputStream, JsonUnflattener jsonUnflattener) throws IOException {
        jsonUnflattener.parseToJson();
        final ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(outputStream.toString(), valueType);
    }

}
