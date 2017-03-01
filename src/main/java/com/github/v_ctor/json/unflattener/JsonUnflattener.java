package com.github.v_ctor.json.unflattener;


import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.v_ctor.json.unflattener.ParserStateEnum.InArray;
import static com.github.v_ctor.json.unflattener.ParserStateEnum.InArrayElement;
import static com.github.v_ctor.json.unflattener.ParserStateEnum.InObject;

@SuppressWarnings("WeakerAccess")
public class JsonUnflattener {
    private Map<String, String> data;
    private final OutputStream out;
    private final JsonGenerator generator;
    private final ArrayList<ParserState> stack = new ArrayList<>();


    @SuppressWarnings("WeakerAccess")
    private JsonUnflattener(OutputStream out) throws IOException {
        this.out = out;
        final JsonFactory factory = new JsonFactory();
        this.generator = factory.createGenerator(out, JsonEncoding.UTF8);
        //        this.parserStateMachine = new ParserStateMachine(generator);
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
            parseKey(key, key);
        }

        ListIterator<ParserState> iterator = stack.listIterator(stack.size());
        while (iterator.hasPrevious()) {
            iterator.previous().close();
        }

        generator.writeEndObject();
        generator.close();

        return out;
    }

    private void parseKey(String key, final String fullKey) throws IOException {
        //        final String keyLeft = getKeyLeft(key);
        //        final String arrayName = getFieldNameFromKey(keyLeft);
        //        final int arrayIndex = getIndexFromKey(keyLeft);
        String keyLeft = getKeyLeft(key);
        ParserState jsonElement = null;
        final ArrayList<ParserState> stackLocal = new ArrayList<>();

        for (/*Iterator<ParserState> iterator = stack.iterator()*/
            int i = 0;
            isKeyComplex(key) || isKeyElementOfArray(keyLeft);
            key = getKeyRight(key),
                keyLeft = getKeyLeft(key),
                i++
            ) {

            if (i <= stack.size() - 1) {
                //                jsonElement = iterator.next();
                jsonElement = stack.get(i);
                if (!jsonElement.getName().equals(keyLeft)) {
                    //закрываем сущности
                    if (isKeyElementOfArray(keyLeft)) {
                        final String arrayName = getFieldNameFromKey(keyLeft);
                        if (!jsonElement.getName().equals(arrayName)) {
                            closeAllToElement(jsonElement);
                            //                            ParserState state = new ParserState(InArray, generator, getFieldNameFromKey(keyLeft), getIndexFromKey(keyLeft));
                            stackLocal.add(new ParserState(InArray, generator, getFieldNameFromKey(keyLeft), getIndexFromKey(keyLeft)));
                            //                            ParserStateEnum.InArray.open(generator, arrayName);
                            if (isKeyComplex(key)) {
                                //                                stackLocal.add(new AbstractMap.SimpleEntry<>(getFieldNameFromKey(keyLeft), null));
                                //                                ParserStateEnum.InObject.open(generator, keyLeft);
                                stackLocal
                                    .add(new ParserState(InObject, generator, getFieldNameFromKey(keyLeft), null));
                            }
                        } else {//the same array
                            final int arrayIndex = getIndexFromKey(keyLeft);
                            if (!jsonElement.getIndex().equals(arrayIndex)) {
/*                                closeAllBeforeElement(jsonElement);
                                stackLocal.add(new AbstractMap.SimpleEntry<>(getFieldNameFromKey(keyLeft), getIndexFromKey(keyLeft))); //!!!*/
                                closeAllBeforeArray(jsonElement);
                                ParserState state = stack.get(stack.size() - 1);
                                state.setIndex(state.getIndex() + 1);
                                //                                stackLocal.add(state);
                                //                                stackLocal.add(new ParserState(getFieldNameFromKey(keyLeft), getIndexFromKey(keyLeft)));
                                if (isKeyComplex(key)) {
                                    //                                    stackLocal.add(new AbstractMap.SimpleEntry<>(getFieldNameFromKey(keyLeft), null));
                                    //                                    ParserStateEnum.InArrayElement.open(generator, keyLeft);
                                    stackLocal.add(
                                        new ParserState(InArrayElement, generator, getFieldNameFromKey(keyLeft), null));
                                }
                            }
                        }
                    } else {
                        closeAllToElement(jsonElement);
                        //                        stackLocal.add(new AbstractMap.SimpleEntry<>(keyLeft, null));
                        //                        InObject.open(generator, keyLeft);
                        stackLocal.add(new ParserState(InObject, generator, keyLeft, null));
                    }
                }
            } else {
                if (isKeyElementOfArray(keyLeft)) {
                    final String arrayName = getFieldNameFromKey(keyLeft);
                    //                    stackLocal.add(new AbstractMap.SimpleEntry<>(arrayName, getIndexFromKey(keyLeft)));
                    //                    InArray.open(generator, arrayName);
                    stackLocal.add(new ParserState(InArray, generator, arrayName, getIndexFromKey(keyLeft)));

                    if (isKeyComplex(key)) {
                        //                        stackLocal.add(new AbstractMap.SimpleEntry<>(getFieldNameFromKey(keyLeft), null));
                        //                        InArrayElement.open(generator, keyLeft);
                        stackLocal.add(new ParserState(InArrayElement, generator, getFieldNameFromKey(keyLeft), null));
                    }
                } else {
                    //                    stackLocal.add(new AbstractMap.SimpleEntry<>(keyLeft, null));
                    //                    InObject.open(generator, keyLeft);
                    stackLocal.add(new ParserState(InObject, generator, keyLeft, null));
                }
            }

            //            final String keyRight = getKeyRight(key);
            //            key = keyRight;
        }

        if (stackLocal.size() == 0 /*&& key==null*/) {
            closeAllBeforeArray(jsonElement);
        }
        stack.addAll(stackLocal);

        if (key != null) {
/*            if (stack.get(stack.size() - 1).getParserStateEnum().equals(InArray)) {
                stack.add(new ParserState(InArrayElement, generator, getFieldNameFromKey(keyLeft), null));
            }*/
            generator.writeStringField(key, data.get(fullKey));
        } else
            generator.writeString(data.get(fullKey));

    }

    private void closeAllToElement(ParserState jsonElement) throws IOException {
        if (jsonElement == null)
            return;
        for (ListIterator<ParserState> iteratorReverse = stack.listIterator(stack.size());
             iteratorReverse.hasPrevious(); ) {
/*            final ParserState jsonElementReverse = iteratorReverse.previous();
            //            if (jsonElement != jsonElementReverse  *//*|| !iteratorReverse.hasPrevious()*//*) {
            //                            jsonElementReverse.
            ParserStates.InObject.close(generator);
            //            }
            iteratorReverse.remove();*/


            final ParserState element = iteratorReverse.previous();
/*            if (element.getIndex() == null) {
                InObject.close(generator);
            } else {
                InArray.close(generator);
            }*/
            element.close();
            iteratorReverse.remove();


            if (jsonElement == element) {
                break;
            }
        }
    }

    private void closeAllBeforeElement(ParserState jsonElement) throws IOException {
        if (jsonElement == null)
            return;
        for (ListIterator<ParserState> iteratorReverse = stack.listIterator(stack.size());
             iteratorReverse.hasPrevious(); ) {
            final ParserState jsonElementReverse = iteratorReverse.previous();
            iteratorReverse.remove();
            if (jsonElement == jsonElementReverse) {
                break;
            }
            if (jsonElement != jsonElementReverse  /*|| !iteratorReverse.hasPrevious()*/) {
                //                            jsonElementReverse.
                //                InObject.close(generator);
                jsonElementReverse.close();
            }
        }
    }

    private void closeAllBeforeArray(ParserState jsonElement) throws IOException {
        if (jsonElement == null)
            return;
        for (ListIterator<ParserState> iteratorReverse = stack.listIterator(stack.size());
             iteratorReverse.hasPrevious(); ) {
            final ParserState jsonElementReverse = iteratorReverse.previous();
/*            if (jsonElementReverse.getParserStateEnum().equals(ParserStateEnum.InArray))
                break;*/
            if (jsonElement == jsonElementReverse) {
                break;
            }
            iteratorReverse.remove();
            if (jsonElement != jsonElementReverse  /*|| !iteratorReverse.hasPrevious()*/) {
                //                            jsonElementReverse.
                //                InObject.close(generator);
                jsonElementReverse.close();
            }
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
        if (key == null)
            return false;
        return key.split("\\.").length > 1;
    }

    private static String getKeyLeft(String key) {
        if (key == null)
            return null;
        return key.split("\\.")[0];
    }

    private static String getKeyRight(String key) {
        //        assert isKeyComplex(key);
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
        if (key == null)
            return false;
        return getKeyArrayIndexMatcher(key).find();
    }

    private static int getIndexFromKey(String key) {
        final Matcher keyArrayIndexMatcher = getKeyArrayIndexMatcher(key);
        boolean result = keyArrayIndexMatcher.find();
        assert result;
        return Integer.parseInt(keyArrayIndexMatcher.group());
    }
}
