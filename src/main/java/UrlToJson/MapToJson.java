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
    private final ArrayList<Map.Entry<String, Integer>> stack = new ArrayList();
    private final Deque<ParserState> parserStateDeque = new ArrayDeque();
    //    private final Map<String, String> data = new HashMap();
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
//                            level = 0;
            parseKey(key, key);
        }
        while (parserStateDeque.size() > 0) {
            ParserState last = parserStateDeque.getLast();
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
        final String[] subKeys = key.split("\\.");
        final String keyLeft = subKeys[0];
        final String keyRight = getSecondPartOfKey(key, keyLeft);
        if (subKeys.length > 1) {

            final Integer index = getIndexFromKey(keyLeft);
            if (index != null) {
                final String fieldName = getFieldNameFromKey(keyLeft);

                if (level > parserStateDeque.size()) {
                    stack.add(new AbstractMap.SimpleEntry(fieldName, index));
                    openArrayEntity(fieldName);

                    openArrayElementEntity();
                    level++;

                } else /*if (level == parserStateDeque.size()) */{
                    if (stack.get(stack.size() - 1).getKey().equals(fieldName)) {
                        if (!stack.get(stack.size() - 1).getValue().equals(index)) {
                            stack.remove(stack.size() - 1);
                            closeEntity();

                            openArrayElementEntity();
                            stack.add(new AbstractMap.SimpleEntry(fieldName, index));
                        }
                    }
                }

                parseKey(keyRight, fullKey);

            } else {
                if (level > stack.size() || !stack.get(level - 1).getKey().equals(keyLeft)) {
                    stack.add(new AbstractMap.SimpleEntry(keyLeft, null));
                    openObjectEntity(keyLeft);
                }
                parseKey(keyRight, fullKey);
            }

            //            generator.writeObjectFieldStart(keyLeft);
            //            generator.writeEndObject();
        } else {
            //            closeArrayIfItsNecessary();
            while (parserStateDeque.size() > (level - 1)) {
//                closeEntity();
                closeEntityCompletely();
                stack.remove(stack.size() - 1);
            }
            generator.writeStringField(key, data.get(fullKey));
        }
        level--;
    }

    private ParserState closeEntity() throws IOException {
        ParserState parserState = parserStateDeque.pollLast();
        parserState.close(generator);
        return parserState;
    }

    private void closeEntityCompletely() throws IOException {
        if (closeEntity().equals(ParserState.InArrayElement)){
            ParserState parserState = parserStateDeque.pollLast();
            assert parserState.equals(ParserState.InArray);
            parserState.close(generator);
        }
    }

    private void openArrayEntity(String keyLeft) throws IOException {
        ParserState parserState = ParserState.InArray;
        parserState.open(generator, keyLeft);
        parserStateDeque.addLast(parserState);
    }

    private void openObjectEntity(String keyLeft) throws IOException {
        ParserState parserState = ParserState.InObject;
        parserState.open(generator, keyLeft);
        parserStateDeque.addLast(parserState);
    }

    private void openArrayElementEntity() throws IOException {
        ParserState parserState = ParserState.InArrayElement;
        parserState.open(generator, null);
        parserStateDeque.addLast(parserState);
    }

    private String getSecondPartOfKey(String key, String subKey) {
        return key.length() > subKey.length() ? key.substring(subKey.length() + 1) : null;
    }


    private String getFieldNameFromKey(String key) {
        return key.split("\\[")[0];
    }

    private void closeArrayIfItsNecessary() throws IOException {

        if (parserStateDeque.getLast().equals(ParserState.InArray)) {
            generator.writeEndArray();
            parserStateDeque.removeLast();
        }
    }

    private Integer getIndexFromKey(String key) {
        final Pattern p = Pattern.compile("(?<=\\[)\\d");
        final Matcher m = p.matcher(key);
        if (m.find()) {
            String matchIndex = m.group();
            int index = Integer.parseInt(matchIndex);
            return index;
        }
        return null;
    }

    private boolean isComplexKey(String key) {
        final String[] subKeys = key.split("\\.");
        return subKeys.length > 1;
    }

}
