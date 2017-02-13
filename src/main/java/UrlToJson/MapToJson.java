package UrlToJson;


import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
public class MapToJson {
    private  Map<String, String> data;
    private final OutputStream out;
    private final JsonGenerator generator;
    private final ParserStateMachine parserStateMachine;

    private int level;

    @SuppressWarnings("WeakerAccess")
    private MapToJson(OutputStream out) throws IOException {
        this.out = out;
        final JsonFactory factory = new JsonFactory();
        this.generator = factory.createGenerator(out, JsonEncoding.UTF8);
        this.parserStateMachine = new ParserStateMachine(generator);
    }
    public interface MapStringStringRef extends Supplier<Map<String, String>> {}
    public MapToJson(MapStringStringRef data, OutputStream out) throws IOException {
        this(out);
        this.data = data.get();
    }

    public interface MapStringStringArrayRef extends Supplier<Map<String, String[]>> {}
    public MapToJson(MapStringStringArrayRef data, OutputStream out) throws IOException {
        this(out);
        this.data = convertMultiValueMapToSingleValue(data);
    }

    private Map<String, String> convertMultiValueMapToSingleValue(MapStringStringArrayRef data) {
        Map<String, String> jsonMap = new HashMap<>();
        data.get().entrySet().forEach(
            (obj) -> {
                System.out.println(obj);
                if (!(obj.getValue().length == 1))
                    throw new IllegalArgumentException("More than one value for parameter");
                jsonMap.put(obj.getKey(), obj.getValue()[0]);
            }
        );
        return jsonMap;
    }

    @SuppressWarnings("WeakerAccess")
    public OutputStream parseToJson() throws IOException {
        generator.writeStartObject();
        for (String key : new TreeSet<>(data.keySet())) {
            level = 0;
            parseKey(key, key);
        }
        parserStateMachine.closeAll();

        generator.writeEndObject();

        generator.close();

        return out;
    }

    private void parseKey(String key, String fullKey) throws IOException {
        level++;
        if (isKeyComplex(key)) {
            final String keyLeft = getKeyLeft(key);
            final String keyRight = getKeyRight(key);

            if (isKeyElementOfArray(keyLeft)) {
                final String arrayName = getFieldNameFromKey(keyLeft);
                final int arrayIndex = getIndexFromKey(keyLeft);

                if (!parserStateMachine.isBranchDeeper(level)) { //is it new array?
                    parserStateMachine.toArrayState(level, arrayName, arrayIndex);
                } else {//or just new array element?
                    if (parserStateMachine.isItSameArray(level, arrayName)) {  //same array
                        if (parserStateMachine.isItSameArrayElement(level, arrayIndex)) { // but new array element
                            parserStateMachine.fromAnywhereForArrayElement(level);

                            parserStateMachine.toArrayElement(level, arrayName, arrayIndex);
                        }
                    } else { //new array
                        parserStateMachine.fromAnywhereForArray(level);

                        parserStateMachine.toArrayState(level, arrayName, arrayIndex);
                    }
                }
            } else {//key is object
                if (!parserStateMachine.isBranchDeeper(level)) {
                    parserStateMachine.toObject(level, keyLeft);
                } else {
                    if (!parserStateMachine.isItSameObject(level, keyLeft)) {
                        parserStateMachine.fromAnywhereForObject(level);

                        parserStateMachine.toObject(level, keyLeft);
                    }
                }
            }
            parseKey(keyRight, fullKey);
        } else {//simple field
            parserStateMachine.fromAnywhereToField(level);
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

    private static Matcher getKeyArrayIndexMatcher(String key) {
        final Pattern p = Pattern.compile("(?<=\\[)\\d");
        return p.matcher(key);
    }

    private static boolean isKeyElementOfArray(String key) {
        return getKeyArrayIndexMatcher(key).find();
    }

    private static int getIndexFromKey(String key) {
        final Matcher keyArrayIndexMatcher = getKeyArrayIndexMatcher(key);
        boolean result = keyArrayIndexMatcher.find();
        assert result;
        return Integer.parseInt(keyArrayIndexMatcher.group());
    }
}
