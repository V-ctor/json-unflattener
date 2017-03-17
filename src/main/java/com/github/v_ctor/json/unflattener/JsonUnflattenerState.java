package com.github.v_ctor.json.unflattener;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

import static com.github.v_ctor.json.unflattener.ParserStateEnum.InArray;
import static com.github.v_ctor.json.unflattener.ParserStateEnum.InArrayElement;
import static com.github.v_ctor.json.unflattener.ParserStateEnum.InObject;

class JsonUnflattenerState {
    private final JsonGenerator generator;
    private final ArrayList<ParserState> stack = new ArrayList<>();
    private final ArrayList<ParserState> stackNew = new ArrayList<>();
    private int i = 0;

    JsonUnflattenerState(JsonGenerator generator) {
        this.generator = generator;
    }

    void openJson() throws IOException {
        generator.writeStartObject();
    }

    void closeJson() throws IOException {
        ListIterator<ParserState> iterator = stack.listIterator(stack.size());
        while (iterator.hasPrevious()) {
            iterator.previous().close();
        }

        generator.writeEndObject();
        generator.close();
    }

    ParserState getCurrent(int i) {
        return stack.get(i);
    }

    int size() {
        return stack.size();
    }

    void addAndOpenInArray(final String name, final Integer index) throws IOException {
        stackNew.add(new ParserState(InArray, generator, name, index));
    }

    void addAndOpenInArrayElement(final String name) throws IOException {
        stackNew.add(new ParserState(InArrayElement, generator, name, null));
    }

    void addAndOpenInObject(final String name) throws IOException {
        stackNew.add(new ParserState(InObject, generator, name, null));
    }

    void incLastIndex() {
        ParserState state = stack.get(stack.size() - 1);
        state.incIndex();
    }

    void writeStringField(final String key, final String value) throws IOException {
        generator.writeStringField(key, value);
    }

    void writeString(final String value) throws IOException {
        generator.writeString(value);
    }

    void updateStack() {
        stack.addAll(stackNew);
        stackNew.clear();
    }

    void resetIterator() {
        i = 0;
    }

    boolean hasNext() {
        return i <= (stack.size() - 1);
    }

    private boolean notLast() {
        return i < (stack.size() - 1);
    }

    void incPosition() {
        i++;
    }

    private ParserState getCurrent() {
        return stack.get(i);
    }

    /*
    Get next element skipping InArrayElement
     */
    ParserState getNext() {
        ParserState jsonElement;
        while (notLast() &&
            getCurrent().getParserStateEnum().equals(ParserStateEnum.InArrayElement)) {

            incPosition();
        }
        jsonElement = getCurrent();
        return jsonElement;
    }

    void closeAllToElement(ParserState jsonElement) throws IOException {
        if (jsonElement == null)
            return;
        for (ListIterator<ParserState> iteratorReverse = stack.listIterator(stack.size());
             iteratorReverse.hasPrevious(); ) {
            final ParserState element = iteratorReverse.previous();
            if (element.getParserStateEnum().equals(InArrayElement)
                && (element == jsonElement))
                break;
            element.close();
            iteratorReverse.remove();

            if (jsonElement == element) {
                break;
            }
        }
    }

    void closeAllBeforeArray(ParserState jsonElement) throws IOException {
        if (jsonElement == null)
            return;
        for (ListIterator<ParserState> iteratorReverse = stack.listIterator(stack.size());
             iteratorReverse.hasPrevious(); ) {
            final ParserState jsonElementReverse = iteratorReverse.previous();
            if (jsonElement == jsonElementReverse) {
                break;
            }
            iteratorReverse.remove();
            jsonElementReverse.close();
        }
    }

    void closeAllBeforeItem(ParserState jsonElement) throws IOException {
        if (stackNew.size() != 0) {
            return;
        }

        for (ListIterator<ParserState> iteratorReverse = stack.listIterator(stack.size());
             iteratorReverse.hasPrevious(); ) {
            final ParserState jsonElementReverse = iteratorReverse.previous();
            if (jsonElementReverse.getParserStateEnum().equals(ParserStateEnum.InArrayElement)) {
                if (stack.get(iteratorReverse.previousIndex()) == jsonElement) {
                    break;
                }
            }
            if (jsonElement == jsonElementReverse) {
                break;
            }
            iteratorReverse.remove();
            jsonElementReverse.close();
        }
    }
}
