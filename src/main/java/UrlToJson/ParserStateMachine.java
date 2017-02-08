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

    int getCurrentLevel() {
        return jsonElementStack.size();
    }

    void addStateArray(int level, String fieldName, Integer index) throws IOException {
        jsonElementStack.add(new AbstractMap.SimpleEntry(fieldName, index));
        openArrayEntity(level, fieldName);

        openArrayElementEntity(level);
    }

    void openArrayEntity(int level, String keyLeft) throws IOException {
        ParserStates parserState = ParserStates.InArray;
        parserState.open(generator, keyLeft);
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

    void openObjectEntity(int level, String keyLeft) throws IOException {
        ParserStates parserState = ParserStates.InObject;
        parserState.open(generator, keyLeft);
        parserStateDeque.addLast(new AbstractMap.SimpleEntry(level, parserState));
    }

    void closeAndOpenIfLastIsArrayElement(int level) throws IOException {
        if (parserStateDeque.size() > 0 && parserStateDeque.getLast().getValue().equals(ParserStates.InArrayElement)) {
            closeEntity();
            openArrayElementEntity(level);
        }
    }

    void closeForField(int level) throws IOException {
        while (jsonElementStack.size() > (level - 1)) {
            closeEntityCompletely();
            jsonElementStack.remove(jsonElementStack.size() - 1);
        }
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
}

