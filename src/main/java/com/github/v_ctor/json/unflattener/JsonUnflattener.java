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
            parseKey(key);
        }

        ListIterator<ParserState> iterator = stack.listIterator(stack.size());
        while (iterator.hasPrevious()) {
            iterator.previous().close();
        }

        generator.writeEndObject();
        generator.close();

        return out;
    }

    private void parseKey(String key) throws IOException {
        final String fullKey = key;
        String keyLeft = getKeyLeft(key);
        ParserState jsonElement = null;
        final ArrayList<ParserState> stackLocal = new ArrayList<>();

        for (int i = 0;
             isKeyComplex(key) || isKeyElementOfArray(keyLeft);
             key = getKeyRight(key),
                 keyLeft = getKeyLeft(key),
                 i++
            ) {

            if (i <= stack.size() - 1) {
                while (stack.get(i).getParserStateEnum().equals(InArrayElement)) {
                    i++;
                }
                jsonElement = stack.get(i);
                if (!jsonElement.getName().equals(keyLeft)) {
                    //closing entities
                    if (isKeyElementOfArray(keyLeft)) {
                        final String arrayName = getFieldNameFromKey(keyLeft);
                        if (!jsonElement.getName().equals(arrayName)) {
                            closeAllToElement(jsonElement);
                            stackLocal.add(new ParserState(InArray, generator, getFieldNameFromKey(keyLeft), getIndexFromKey(keyLeft)));
                            if (isKeyComplex(key)) {
                                stackLocal
                                    .add(new ParserState(InArrayElement, generator, getFieldNameFromKey(keyLeft), null));
                            }
                        } else {//the same array
                            final int arrayIndex = getIndexFromKey(keyLeft);
                            if (!jsonElement.getIndex().equals(arrayIndex)) {
                                closeAllBeforeArray(jsonElement);
                                ParserState state = stack.get(stack.size() - 1);
                                state.incIndex();
                                if (isKeyComplex(key)) {
                                    stackLocal.add(
                                        new ParserState(InArrayElement, generator, getFieldNameFromKey(keyLeft), null));
                                }
                            }
                        }
                    } else {
                        closeAllToElement(jsonElement);
                        stackLocal.add(new ParserState(InObject, generator, keyLeft, null));
                    }
                }
            } else {
                if (isKeyElementOfArray(keyLeft)) {
                    final String arrayName = getFieldNameFromKey(keyLeft);
                    stackLocal.add(new ParserState(InArray, generator, arrayName, getIndexFromKey(keyLeft)));
                    if (isKeyComplex(key)) {
                        stackLocal.add(new ParserState(InArrayElement, generator, getFieldNameFromKey(keyLeft), null));
                    }
                } else {
                    stackLocal.add(new ParserState(InObject, generator, keyLeft, null));
                }
            }
        }

        if (stackLocal.size() == 0) {
            closeAllBeforeItem(jsonElement);
        }
        stack.addAll(stackLocal);

        if (key != null) {
            generator.writeStringField(key, data.get(fullKey));
        } else
            generator.writeString(data.get(fullKey));

    }

    private void closeAllToElement(ParserState jsonElement) throws IOException {
        if (jsonElement == null)
            return;
        for (ListIterator<ParserState> iteratorReverse = stack.listIterator(stack.size());
             iteratorReverse.hasPrevious(); ) {
            final ParserState element = iteratorReverse.previous();
            element.close();
            iteratorReverse.remove();

            if (jsonElement == element) {
                break;
            }
        }
    }

    private void closeAllBeforeArray(ParserState jsonElement) throws IOException {
        if (jsonElement == null)
            return;
        for (ListIterator<ParserState> iteratorReverse = stack.listIterator(stack.size());
             iteratorReverse.hasPrevious(); ) {
            final ParserState jsonElementReverse = iteratorReverse.previous();
            if (jsonElement == jsonElementReverse) {
                break;
            }
            iteratorReverse.remove();
            jsonElementReverse.close();
        }
    }

    private void closeAllBeforeItem(ParserState jsonElement) throws IOException {
        for (ListIterator<ParserState> iteratorReverse = stack.listIterator(stack.size());
             iteratorReverse.hasPrevious(); ) {
            final ParserState jsonElementReverse = iteratorReverse.previous();
            if (jsonElementReverse.getParserStateEnum().equals(ParserStateEnum.InArrayElement)) {
                if (stack.get(iteratorReverse.previousIndex()) == jsonElement) {
                    break;
                }
            }
            if (jsonElement == jsonElementReverse) {
                break;
            }
            iteratorReverse.remove();
            jsonElementReverse.close();
        }
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
