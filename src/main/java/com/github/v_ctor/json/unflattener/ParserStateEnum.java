package com.github.v_ctor.json.unflattener;


import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public enum ParserStateEnum {
    InObject {
        @Override void open(JsonGenerator generator, String objectName) throws IOException {
            generator.writeObjectFieldStart(objectName);
        }

        @Override void close(JsonGenerator generator) throws IOException {
            generator.writeEndObject();
        }
    }, InArray {
        @Override
        void open(JsonGenerator generator, String arrayName) throws IOException {
            generator.writeFieldName(arrayName);
            generator.writeStartArray();

        }

        @Override void close(JsonGenerator generator) throws IOException {
            generator.writeEndArray();
        }

    }, InArrayElement {
        @Override void open(JsonGenerator generator, String objectName) throws IOException {
            generator.writeStartObject();
        }

        @Override void close(JsonGenerator generator) throws IOException {
            generator.writeEndObject();
        }
    };

    abstract void open(JsonGenerator generator, String fieldName) throws IOException;

    abstract void close(JsonGenerator generator) throws IOException;
}
