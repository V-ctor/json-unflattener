package com.github.v_ctor.json.unflattener;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

class ParserState {
    private ParserStateEnum parserStateEnum;
    private JsonGenerator generator;
    private String name;
    private Integer index;

    ParserState(ParserStateEnum parserStateEnum, JsonGenerator generator, String name, Integer index) throws IOException {
        this.parserStateEnum = parserStateEnum;
        this.generator = generator;
        this.name = name;
        this.index = index;

        parserStateEnum.open(generator, name);
    }

    ParserStateEnum getParserStateEnum() {
        return parserStateEnum;
    }

    String getName() {
        return name;
    }

    Integer getIndex() {
        return index;
    }

    void incIndex() {
        this.index++;
    }

    void close() throws IOException {
        if (parserStateEnum != null)
            parserStateEnum.close(generator);
    }
}
