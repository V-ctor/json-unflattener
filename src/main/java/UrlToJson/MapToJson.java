package UrlToJson;


import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapToJson {
    private final ArrayList<Map.Entry<String, Integer>> jsonElementStack = new ArrayList();
    private final Deque<Map.Entry<Integer, ParserState>> parserStateDeque = new ArrayDeque();
    private final Map<String, String> data;
    private final JsonFactory factory = new JsonFactory();
    private final OutputStream out = new ByteArrayOutputStream();
    private final JsonGenerator generator = factory.createGenerator(out, JsonEncoding.UTF8);

    private int level;

    public MapToJson(Map<String, String> data) throws IOException {
        this.data = data;
    }

    public OutputStream parseToJson() throws IOException {
        generator.writeStartObject();
        for (String key : new TreeSet<String>(data.keySet())) {
            level = 0;
            parseKey(key, key);
        }
        while (parserStateDeque.size() > 0) {
            ParserState last = parserStateDeque.getLast().getValue();
            last.close(generator);
            parserStateDeque.removeLast();
        }

        generator.writeEndObject();

        generator.close();
        System.out.println(out);

        return out;
    }

    private void parseKey(String key, String fullKey) throws IOException {
        level++;
        if (isKeyComplex(key)) {
            final String keyLeft = getKeyLeft(key);
            final String keyRight = getKeyRight(key);

            if (isKeyElementOfArray(keyLeft)) {
                final String arrayName = getFieldNameFromKey(keyLeft);
                final Integer arrayIndex = getIndexFromKey(keyLeft);

                if (level > jsonElementStack.size()) { //is it new array?
                    jsonElementStack.add(new AbstractMap.SimpleEntry(arrayName, arrayIndex));
                    openArrayEntity(arrayName);

                    openArrayElementEntity();

                } else {//or just new array element?
                    if (jsonElementStack.size() > 0 && jsonElementStack.get(level - 1).getKey().equals(arrayName)) {  //same array
                        if (jsonElementStack.size() > 0 &&
                            !jsonElementStack.get(level - 1).getValue().equals(arrayIndex)) { // but new array element

                            while (jsonElementStack.size() > (level - 1)) {
                                jsonElementStack.remove(jsonElementStack.size() - 1);

                            }
                            jsonElementStack.add(new AbstractMap.SimpleEntry(arrayName, arrayIndex));

                            while (parserStateDeque.size() > 0 && parserStateDeque.getLast().getKey() > (level)) {
                                closeEntity();
                            }
                            closeEntity();

                            openArrayElementEntity();
                        }
                    } else { //new array
                        while (jsonElementStack.size() > (level - 1)) {
                            jsonElementStack.remove(jsonElementStack.size() - 1);

                        }
                        jsonElementStack.add(new AbstractMap.SimpleEntry(arrayName, arrayIndex));
                        while (parserStateDeque.size() > 0 && parserStateDeque.getLast().getKey() > (level - 1)) {
                            closeEntity();
                        }

                        openArrayEntity(arrayName);

                        openArrayElementEntity();
                    }
                }

                parseKey(keyRight, fullKey);

            } else {//key is object
                if (level > jsonElementStack.size()) {

                    jsonElementStack.add(new AbstractMap.SimpleEntry(keyLeft, null));
                    openObjectEntity(keyLeft);
                } else {
                    if (!jsonElementStack.get(level - 1).getKey().equals(keyLeft)) {

                        while (jsonElementStack.size() > (level - 1)) {
                            jsonElementStack.remove(jsonElementStack.size() - 1);

                        }
                        jsonElementStack.add(new AbstractMap.SimpleEntry(keyLeft, null));

                        while (parserStateDeque.size() > 0 && parserStateDeque.getLast().getKey() >= (level)) {
                            //                            closeEntityCompletely();
                            /*ParserState parserState =*/
                            closeEntity();
                        }
                        if (parserStateDeque.size() > 0 && parserStateDeque.getLast().getValue().equals(ParserState.InArrayElement)) {
                            closeEntity();
                            openArrayElementEntity();
                        }

                        openObjectEntity(keyLeft);
                    }

                }

                parseKey(keyRight, fullKey);
            }

        } else {//simple field
            while (jsonElementStack.size() > (level - 1)) {
                closeEntityCompletely();
                jsonElementStack.remove(jsonElementStack.size() - 1);
            }
            generator.writeStringField(key, data.get(fullKey));
        }
        level--;
    }

    private static boolean isKeyComplex(String key) {
        return key.split("\\.").length > 1;
    }

    private static String getKeyLeft(String key) {
        return key.split("\\.")[0];
    }

    private static String getKeyRight(String key) {
        assert isKeyComplex(key);
        return getSecondPartOfKey(key, getKeyLeft(key));
    }

    private static String getSecondPartOfKey(String key, String subKey) {
        return key.length() > subKey.length() ? key.substring(subKey.length() + 1) : null;
    }

    private static String getFieldNameFromKey(String key) {
        return key.split("\\[")[0];
    }

    private static boolean isKeyElementOfArray(String key) {
        return getIndexFromKey(key) != null;
    }

    private ParserState closeEntity() throws IOException {
        ParserState parserState = parserStateDeque.pollLast().getValue();
        parserState.close(generator);
        return parserState;
    }

    private void closeEntityCompletely() throws IOException {
        if (closeEntity().equals(ParserState.InArrayElement)) {
            ParserState parserState = parserStateDeque.pollLast().getValue();
            assert parserState.equals(ParserState.InArray);
            parserState.close(generator);
        }
    }

    private void openArrayEntity(String keyLeft) throws IOException {
        ParserState parserState = ParserState.InArray;
        parserState.open(generator, keyLeft);
        parserStateDeque.addLast(new AbstractMap.SimpleEntry(level, parserState));
    }

    private void openObjectEntity(String keyLeft) throws IOException {
        ParserState parserState = ParserState.InObject;
        parserState.open(generator, keyLeft);
        parserStateDeque.addLast(new AbstractMap.SimpleEntry(level, parserState));
    }

    private void openArrayElementEntity() throws IOException {
        ParserState parserState = ParserState.InArrayElement;
        parserState.open(generator, null);
        parserStateDeque.addLast(new AbstractMap.SimpleEntry(level, parserState));
    }

    private void closeArrayIfItsNecessary() throws IOException {

        if (parserStateDeque.getLast().equals(ParserState.InArray)) {
            generator.writeEndArray();
            parserStateDeque.removeLast();
        }
    }

    private static Integer getIndexFromKey(String key) {
        final Pattern p = Pattern.compile("(?<=\\[)\\d");
        final Matcher m = p.matcher(key);
        if (m.find()) {
            String matchIndex = m.group();
            int index = Integer.parseInt(matchIndex);
            return index;
        }
        return null;
    }

}
