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

    private String name;
    private Integer index;

    abstract void open(JsonGenerator generator, String fieldName) throws IOException;

    abstract void close(JsonGenerator generator) throws IOException;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public void setNameIndex(String name, Integer index){
        this.name = name;
        this.index = index;
    }
}
