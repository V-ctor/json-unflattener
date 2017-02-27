package com.github.v_ctor.json.unflattener;


import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class JsonUnflattener {
    private Map<String, String> data;
    private final OutputStream out;
    private final JsonGenerator generator;
    private final ParserStateMachine parserStateMachine;
    private final ArrayList<Map.Entry<String, Integer>> stack = new ArrayList<>();

    //    private int level;

    @SuppressWarnings("WeakerAccess")
    private JsonUnflattener(OutputStream out) throws IOException {
        this.out = out;
        final JsonFactory factory = new JsonFactory();
        this.generator = factory.createGenerator(out, JsonEncoding.UTF8);
        this.parserStateMachine = new ParserStateMachine(generator);
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
        generator.writeStartObject();
        for (String key : new TreeSet<>(data.keySet())) {
            //            level = 0;
            parseKey(key, key);
        }
        parserStateMachine.closeAll();

        generator.writeEndObject();

        generator.close();

        return out;
    }

    private void parseKey(String key, String fullKey) throws IOException {
        //        final String keyLeft = getKeyLeft(key);
        //        final String arrayName = getFieldNameFromKey(keyLeft);
        //        final int arrayIndex = getIndexFromKey(keyLeft);
        String keyLeft = getKeyLeft(key);
        Map.Entry<String, Integer> jsonElement = null;
        final ArrayList<Map.Entry<String, Integer>> stackLocal = new ArrayList<>();

        for (/*Iterator<Map.Entry<String, Integer>> iterator = stack.iterator()*/int i = 0; isKeyComplex(key) || isKeyElementOfArray(keyLeft);
                                                                                 keyLeft = getKeyLeft(key), i++) {
            if (i <= stack.size() - 1) {
//                jsonElement = iterator.next();
                jsonElement = stack.get(i);
                if (!jsonElement.getKey().equals(keyLeft)) {
                    //закрываем сущности
                    closeAllToElement(jsonElement);
                    if (isKeyElementOfArray(keyLeft)) {
                        stackLocal.add(new AbstractMap.SimpleEntry<>(getFieldNameFromKey(keyLeft), getIndexFromKey(keyLeft)));
                        ParserStates.InArray.open(generator, keyLeft);
                    } else {
                        stackLocal.add(new AbstractMap.SimpleEntry<>(keyLeft, null));
                        ParserStates.InObject.open(generator, keyLeft);
                    }
                }
            } else {
                if (isKeyElementOfArray(keyLeft)) {
                    final String arrayName = getFieldNameFromKey(keyLeft);
                    stackLocal.add(new AbstractMap.SimpleEntry<>(arrayName, getIndexFromKey(keyLeft)));
                    ParserStates.InArray.open(generator, arrayName);
                } else {
                    stackLocal.add(new AbstractMap.SimpleEntry<>(keyLeft, null));
                    ParserStates.InObject.open(generator, keyLeft);
                }

            }

            final String keyRight = getKeyRight(key);
            key = keyRight;
        }

        if (stackLocal.size() == 0) {
            closeAllBeforeElement(jsonElement);
        }
        stack.addAll(stackLocal);
        generator.writeStringField(key, data.get(fullKey));
    }

    private void closeAllToElement(Map.Entry<String, Integer> jsonElement) throws IOException {
        if (jsonElement == null)
            return;
        for (ListIterator<Map.Entry<String, Integer>> iteratorReverse = stack.listIterator(stack.size());
             iteratorReverse.hasPrevious(); ) {
            final Map.Entry<String, Integer> jsonElementReverse = iteratorReverse.previous();
            //            if (jsonElement != jsonElementReverse  /*|| !iteratorReverse.hasPrevious()*/) {
            //                            jsonElementReverse.
            ParserStates.InObject.close(generator);
            //            }
            iteratorReverse.remove();
            if (jsonElement == jsonElementReverse) {
                break;
            }
        }
    }

    private void closeAllBeforeElement(Map.Entry<String, Integer> jsonElement) throws IOException {
        if (jsonElement == null)
            return;
        for (ListIterator<Map.Entry<String, Integer>> iteratorReverse = stack.listIterator(stack.size());
             iteratorReverse.hasPrevious(); ) {
            final Map.Entry<String, Integer> jsonElementReverse = iteratorReverse.previous();
            if (jsonElement == jsonElementReverse) {
                break;
            }
            if (jsonElement != jsonElementReverse  /*|| !iteratorReverse.hasPrevious()*/) {
                //                            jsonElementReverse.
                ParserStates.InObject.close(generator);
            }
            iteratorReverse.remove();
        }
    }
   /*     level++;

        if (isKeyElementOfArray(keyLeft)) {

            if (!parserStateMachine.isBranchDeeper(level)) { //is it new array?
                 parserStateMachine.toArrayState(level, arrayName, arrayIndex);
                if (isKeyComplex(key)) {
                    parserStateMachine.openArrayElementEntity(level);
                    final String keyRight = getKeyRight(key);
                    parseKey(keyRight, fullKey);
                } else {
                    generator.writeString(data.get(fullKey));
                }

            } else {
                if (parserStateMachine.isItSameArray(level, arrayName)) {  //same array
                    if (parserStateMachine.isItSameArrayElement(level, arrayIndex)) { // same array element

                        if (isKeyComplex(key)) {
                            final String keyRight = getKeyRight(key);
                            parseKey(keyRight, fullKey);
                        }
                        else {
                            parserStateMachine.fromAnywhereForArrayElement(level);
                            parserStateMachine.toArrayElement(arrayName, arrayIndex);
                            generator.writeString(data.get(fullKey));
                        }
                    } else { //new array element
                        parserStateMachine.fromAnywhereForArrayObject(level);

                        if (isKeyComplex(key)) {
                            parserStateMachine.toArrayObject(level, arrayName, arrayIndex);
                            final String keyRight = getKeyRight(key);
                            parseKey(keyRight, fullKey);
                        }
                        else {
                            parserStateMachine.toArrayElement(arrayName, arrayIndex);
                            generator.writeString(data.get(fullKey));
                        }

                    }
                } else { //new array
                    parserStateMachine.fromAnywhereForArray(level);
                    parserStateMachine.toArrayState(level, arrayName, arrayIndex);

                    if (isKeyComplex(key)) {
                        parserStateMachine.openArrayElementEntity(level);
                        final String keyRight = getKeyRight(key);
                        parseKey(keyRight, fullKey);
                    }
                    else {
                        parserStateMachine.toArrayElement(arrayName, arrayIndex);
                        generator.writeString(data.get(fullKey));
                    }

                }
            }
        } else {//key is not array element
                if (isKeyComplex(key)) {
                    if (!parserStateMachine.isBranchDeeper(level)) {
                        parserStateMachine.toObject(level, keyLeft);
                    } else {
                        if (!parserStateMachine.isItSameObject(level, keyLeft)) {
                            parserStateMachine.fromAnywhereForObject(level);

                            parserStateMachine.toObject(level, keyLeft);
                        }
                    }
                    final String keyRight = getKeyRight(key);
                    parseKey(keyRight, fullKey);
                }else {//simple field
                    parserStateMachine.fromAnywhereToField(level);
                    generator.writeStringField(key, data.get(fullKey));
                }
        }
        level--;*/


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
