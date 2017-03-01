package com.github.v_ctor.json.unflattener;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public class ParserState {
    private ParserStateEnum parserStateEnum;
    private JsonGenerator generator;
    private String name;
    private Integer index;

    public ParserState(ParserStateEnum parserStateEnum, JsonGenerator generator, String name, Integer index) throws IOException {
        this.parserStateEnum = parserStateEnum;
        this.generator = generator;
        this.name = name;
        this.index = index;

        parserStateEnum.open(generator, name);
    }

    public ParserState(String name, Integer index) throws IOException {
        this.name = name;
        this.index = index;
    }

    public ParserStateEnum getParserStateEnum() {
        return parserStateEnum;
    }

    public void setParserStateEnum(ParserStateEnum parserStateEnum) {
        this.parserStateEnum = parserStateEnum;
    }

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

    public void close() throws IOException {
        if (parserStateEnum != null)
            parserStateEnum.close(generator);
    }
}
