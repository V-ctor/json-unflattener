package com.github.v_ctor.json.unflattener;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Map;

class ParserStateMachine {
    private final ArrayList<Map.Entry<String, Integer>> jsonElementStack = new ArrayList<>();
    private final Deque<Map.Entry<Integer, ParserStates>> parserStateDeque = new ArrayDeque<>();
    private final JsonGenerator generator;

    ParserStateMachine(JsonGenerator generator) {
        this.generator = generator;
    }

    void closeAll() throws IOException {
        while (parserStateDeque.size() > 0) {
            final ParserStates last = parserStateDeque.getLast().getValue();
            last.close(generator);
            parserStateDeque.removeLast();
        }
    }

    void toArrayState(int level, String arrayName, int arrayIndex) throws IOException {
        addEntity(arrayName, arrayIndex);
        openArrayEntity(level, arrayName);
    }

    void fromAnywhereForArrayElement(int level) throws IOException {
        downgradeToLevel(level);
        closeAllToLevel(level);
    }

    void fromAnywhereForArrayObject(int level) throws IOException {
        downgradeToLevel(level);
        closeAllToLevel(level);
        if (parserStateDeque.getLast().getValue().equals(ParserStates.InArrayElement))
            closeEntity();
    }

    void toArrayElement(String arrayName, int arrayIndex) throws IOException {
        addEntity(arrayName, arrayIndex);
    }

    void toArrayObject(int level, String arrayName, int arrayIndex) throws IOException {
        addEntity(arrayName, arrayIndex);
        openArrayElementEntity(level);
    }

    void fromAnywhereForArray(int level) throws IOException {
        downgradeToLevel(level);
        closeAllToLevel(level - 1);
    }

    void fromAnywhereForObject(int level) throws IOException {
        downgradeToLevel(level);
        closeAllToLevel(level - 1);
    }

    void toObject(int level, String name) throws IOException {
        addEntity(name, null);
        openObjectEntity(level, name);
    }

    private void openArrayEntity(int level, String name) throws IOException {
        final ParserStates parserState = ParserStates.InArray;
        parserState.open(generator, name);
        parserStateDeque.addLast(new AbstractMap.SimpleEntry<>(level, parserState));
    }

    void openArrayElementEntity(int level) throws IOException {
        final ParserStates parserState = ParserStates.InArrayElement;
        parserState.open(generator, null);
        parserStateDeque.addLast(new AbstractMap.SimpleEntry<>(level, parserState));
    }

    private ParserStates closeEntity() throws IOException {
        final ParserStates parserState = parserStateDeque.pollLast().getValue();
        parserState.close(generator);
        return parserState;
    }

    private void openObjectEntity(int level, String name) throws IOException {
        final ParserStates parserState = ParserStates.InObject;
        parserState.open(generator, name);
        parserStateDeque.addLast(new AbstractMap.SimpleEntry<>(level, parserState));
    }

    boolean isBranchDeeper(int level) {
        return level <= jsonElementStack.size();
    }

    private void addEntity(String arrayName, Integer arrayIndex) {
        jsonElementStack.add(new AbstractMap.SimpleEntry<>(arrayName, arrayIndex));
    }

    boolean isItSameArray(int level, String arrayName) {
        return jsonElementStack.size() > 0 && jsonElementStack.get(level - 1).getKey().equals(arrayName);
    }

    boolean isItSameArrayElement(int level, int arrayIndex) {
        return jsonElementStack.size() > 0 && /*!*/jsonElementStack.get(level - 1).getValue().equals(arrayIndex);
    }

    boolean isItSameObject(int level, String name) {
        return jsonElementStack.get(level - 1).getKey().equals(name);
    }

    private void downgradeToLevel(int level) {
        while (jsonElementStack.size() > (level - 1)) {
            jsonElementStack.remove(jsonElementStack.size() - 1);
        }
    }

    private void closeAllToLevel(int level) throws IOException {
        while (parserStateDeque.size() > 0 && parserStateDeque.getLast().getKey() > (level)) {
            closeEntity();
        }
    }

    void fromAnywhereToField(int level) throws IOException {
        downgradeToLevel(level);
        closeAllToLevel(level - 1);
    }
}

