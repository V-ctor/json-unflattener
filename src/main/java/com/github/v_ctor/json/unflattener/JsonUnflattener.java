package com.github.v_ctor.json.unflattener;


import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.v_ctor.json.unflattener.ParserStateEnum.InArrayElement;

@SuppressWarnings("WeakerAccess")
public class JsonUnflattener {
    private Map<String, String> data;
    private final OutputStream out;
    private JsonUnflattenerState jsonUnflattenerState;

    @SuppressWarnings("WeakerAccess")
    private JsonUnflattener(OutputStream out) throws IOException {
        this.out = out;
        final JsonFactory factory = new JsonFactory();
        jsonUnflattenerState = new JsonUnflattenerState(factory.createGenerator(out, JsonEncoding.UTF8));
    }

    public JsonUnflattener(FlatterenMapStringString data, OutputStream out) throws IOException {
        this(out);
        this.data = data.get();
    }

    public JsonUnflattener(FlatterenMapStringStringArray data, OutputStream out) throws IOException {
        this(out);
        this.data = convertMultiValueMapToSingleValue(data);
    }

    private Map<String, String> convertMultiValueMapToSingleValue(FlatterenMapStringStringArray data) {
        Map<String, String> jsonMap;
        jsonMap = data.get().entrySet().stream()
            .filter(
                obj -> {
                    if (!(obj.getValue().length == 1))
                        throw new IllegalArgumentException("Value array must have only one element");
                    return true;
                }
            )
            .collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue()[0]));
        return jsonMap;
    }

    @SuppressWarnings("WeakerAccess")
    public OutputStream parseToJson() throws IOException {
        jsonUnflattenerState.openJson();
        for (String key : new TreeSet<>(data.keySet())) {
            parseKey(key);
        }

        jsonUnflattenerState.closeJson();

        return out;
    }

    private void parseKey(String key) throws IOException {
        final String fullKey = key;
        String keyLeft = getKeyLeft(key);
        ParserState jsonElement = null;

        for (int i = 0;
             isKeyComplex(key) || isKeyElementOfArray(keyLeft);
             key = getKeyRight(key),
                 keyLeft = getKeyLeft(key),
                 i++
            ) {

            if (i <= jsonUnflattenerState.size() - 1) {
                while (jsonUnflattenerState.get(i).getParserStateEnum().equals(InArrayElement)) {
                    i++;
                }
                jsonElement = jsonUnflattenerState.get(i);
                if (!jsonElement.getName().equals(keyLeft)) {
                    //closing entities
                    if (isKeyElementOfArray(keyLeft)) {
                        final String arrayName = getFieldNameFromKey(keyLeft);
                        if (!jsonElement.getName().equals(arrayName)) {
                            jsonUnflattenerState.closeAllToElement(jsonElement);
                            jsonUnflattenerState.addAndOpenInArray(getFieldNameFromKey(keyLeft), getIndexFromKey(keyLeft));
                            if (isKeyComplex(key)) {
                                jsonUnflattenerState.addAndOpenInArrayElement(getFieldNameFromKey(keyLeft));
                            }
                        } else {//the same array
                            final int arrayIndex = getIndexFromKey(keyLeft);
                            if (!jsonElement.getIndex().equals(arrayIndex)) {
                                jsonUnflattenerState.closeAllBeforeArray(jsonElement);
                                jsonUnflattenerState.incLastIndex();
                                if (isKeyComplex(key)) {
                                    jsonUnflattenerState.addAndOpenInArrayElement(getFieldNameFromKey(keyLeft));
                                }
                            }
                        }
                    } else {
                        jsonUnflattenerState.closeAllToElement(jsonElement);
                        jsonUnflattenerState.addAndOpenInObject(keyLeft);
                    }
                }
            } else {
                if (isKeyElementOfArray(keyLeft)) {
                    final String arrayName = getFieldNameFromKey(keyLeft);
                    jsonUnflattenerState.addAndOpenInArray(arrayName, getIndexFromKey(keyLeft));
                    if (isKeyComplex(key)) {
                        jsonUnflattenerState.addAndOpenInArrayElement(getFieldNameFromKey(keyLeft));
                    }
                } else {
                    jsonUnflattenerState.addAndOpenInObject(keyLeft);
                }
            }
        }

        jsonUnflattenerState.closeAllBeforeItem(jsonElement);
        jsonUnflattenerState.updateStack();

        if (key != null) {
            jsonUnflattenerState.writeStringField(key, data.get(fullKey));
        } else
            jsonUnflattenerState.writeString(data.get(fullKey));
    }

    private static boolean isKeyComplex(String key) {
        return key != null && key.split("\\.").length > 1;
    }

    private static String getKeyLeft(String key) {
        if (key == null)
            return null;
        return key.split("\\.")[0];
    }

    private static String getKeyRight(String key) {
        return getSecondPartOfKey(key, getKeyLeft(key));
    }

    private static String getSecondPartOfKey(String key, String subKey) {
        return key.length() > subKey.length() ? key.substring(subKey.length() + 1) : null;
    }

    private static String getFieldNameFromKey(String key) {
        return key.split("\\[")[0];
    }

    private static Matcher getKeyArrayIndexMatcher(String key) {
        final Pattern p = Pattern.compile("(?<=\\[)\\d");
        return p.matcher(key);
    }

    private static boolean isKeyElementOfArray(String key) {
        return key != null && getKeyArrayIndexMatcher(key).find();
    }

    private static int getIndexFromKey(String key) {
        final Matcher keyArrayIndexMatcher = getKeyArrayIndexMatcher(key);
        boolean result = keyArrayIndexMatcher.find();
        assert result;
        return Integer.parseInt(keyArrayIndexMatcher.group());
    }
}
