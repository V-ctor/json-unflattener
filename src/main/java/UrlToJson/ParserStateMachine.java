package UrlToJson;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Map;

public class ParserStateMachine {
    private final ArrayList<Map.Entry<String, Integer>> jsonElementStack = new ArrayList();
    private final Deque<Map.Entry<Integer, ParserStates>> parserStateDeque = new ArrayDeque();
    private final JsonGenerator generator;

    public ParserStateMachine(JsonGenerator generator) {
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
        openArrayElementEntity(level);
    }

    void fromAnywhereForArrayElement(int level) throws IOException {
        downgradeToLevel(level);
        closeAllToLevel(level);
        closeEntity();
    }

    void toArrayElement(int level, String arrayName, int arrayIndex) throws IOException {
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
        closeAndOpenIfLastIsArrayElement(level);
    }

    void toObject(int level, String name) throws IOException {
        addEntity(name, null);
        openObjectEntity(level, name);
    }

    void addStateArray(int level, String fieldName, Integer index) throws IOException {
        jsonElementStack.add(new AbstractMap.SimpleEntry(fieldName, index));
        openArrayEntity(level, fieldName);

        openArrayElementEntity(level);
    }

    void openArrayEntity(int level, String name) throws IOException {
        ParserStates parserState = ParserStates.InArray;
        parserState.open(generator, name);
        parserStateDeque.addLast(new AbstractMap.SimpleEntry(level, parserState));
    }

    void openArrayElementEntity(int level) throws IOException {
        ParserStates parserState = ParserStates.InArrayElement;
        parserState.open(generator, null);
        parserStateDeque.addLast(new AbstractMap.SimpleEntry(level, parserState));
    }

    ParserStates closeEntity() throws IOException {
        ParserStates parserState = parserStateDeque.pollLast().getValue();
        parserState.close(generator);
        return parserState;
    }

    void openObjectEntity(int level, String name) throws IOException {
        ParserStates parserState = ParserStates.InObject;
        parserState.open(generator, name);
        parserStateDeque.addLast(new AbstractMap.SimpleEntry(level, parserState));
    }

    private void closeEntityCompletely() throws IOException {
        if (closeEntity().equals(ParserStates.InArrayElement)) {
            ParserStates parserState = parserStateDeque.pollLast().getValue();
            assert parserState.equals(ParserStates.InArray);
            parserState.close(generator);
        }
    }

    boolean isBranchDeeper(int level) {
        return level <= jsonElementStack.size();
    }

    void addEntity(String arrayName, Integer arrayIndex) {
        jsonElementStack.add(new AbstractMap.SimpleEntry(arrayName, arrayIndex));
    }

    boolean isItSameArray(int level, String arrayName) {
        return jsonElementStack.size() > 0 && jsonElementStack.get(level - 1).getKey().equals(arrayName);
    }

    boolean isItSameArrayElement(int level, int arrayIndex) {
        return jsonElementStack.size() > 0 && !jsonElementStack.get(level - 1).getValue().equals(arrayIndex);
    }

    boolean isItSameObject(int level, String name) {
        return jsonElementStack.get(level - 1).getKey().equals(name);
    }

    void downgradeToLevel(int level) {
        while (jsonElementStack.size() > (level - 1)) {
            jsonElementStack.remove(jsonElementStack.size() - 1);
        }
    }

    void closeAllToLevel(int level) throws IOException {
        while (parserStateDeque.size() > 0 && parserStateDeque.getLast().getKey() > (level)) {
            closeEntity();
        }
    }

    private boolean isLastState(ParserStates parserState) {
        return parserStateDeque.size() > 0 && parserStateDeque.getLast().getValue().equals(parserState);
    }

    void closeAndOpenIfLastIsArrayElement(int level) throws IOException {
        if (isLastState(ParserStates.InArrayElement)) {
            closeEntity();
            openArrayElementEntity(level);
        }
    }

    void fromAnywhereToField(int level) throws IOException {
        downgradeToLevel(level);
        closeAllToLevel(level - 1);
        if (isLastState(ParserStates.InArray)) {
            closeEntity();
        }
    }
}

