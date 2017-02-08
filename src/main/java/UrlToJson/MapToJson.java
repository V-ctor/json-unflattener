package UrlToJson;


import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapToJson {
    private final Map<String, String> data;
    private final JsonFactory factory = new JsonFactory();
    private final OutputStream out = new ByteArrayOutputStream();
    private final JsonGenerator generator = factory.createGenerator(out, JsonEncoding.UTF8);
    private final ParserStateMachine parserStateMachine = new ParserStateMachine(generator);

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
        parserStateMachine.closeAll();

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

                if (!parserStateMachine.isBranchDeeper(level)) { //is it new array?
                    parserStateMachine.addEntity(arrayName, arrayIndex);
                    parserStateMachine.openArrayEntity(level, arrayName);

                    parserStateMachine.openArrayElementEntity(level);

                } else {//or just new array element?
                    if (parserStateMachine.isItSameArray(level, arrayName)) {  //same array
                        if (parserStateMachine.isItSameArrayElement(level, arrayIndex)) { // but new array element

                            parserStateMachine.downgradeToLevel(level);
                            parserStateMachine.addEntity(arrayName, arrayIndex);

                            parserStateMachine.closeAllToLevel(level);
                            parserStateMachine.closeEntity();

                            parserStateMachine.openArrayElementEntity(level);
                        }
                    } else { //new array
                        parserStateMachine.downgradeToLevel(level);
                        parserStateMachine.addEntity(arrayName, arrayIndex);
                        parserStateMachine.closeAllToLevel(level - 1);

                        parserStateMachine.openArrayEntity(level, arrayName);

                        parserStateMachine.openArrayElementEntity(level);
                    }
                }

                parseKey(keyRight, fullKey);

            } else {//key is object
                if (!parserStateMachine.isBranchDeeper(level)) {

                    parserStateMachine.addEntity(keyLeft,null);
                    parserStateMachine.openObjectEntity(level, keyLeft);
                } else {
                    if (!parserStateMachine.isItSameObject(level, keyLeft)) {

                        parserStateMachine.downgradeToLevel(level);
                        parserStateMachine.addEntity(keyLeft,null);

                        parserStateMachine.closeAllToLevel(level-1);

                        parserStateMachine.closeAndOpenIfLastIsArrayElement(level);

                        parserStateMachine.openObjectEntity(level, keyLeft);
                    }

                }

                parseKey(keyRight, fullKey);
            }

        } else {//simple field
            parserStateMachine.closeForField(level);
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
