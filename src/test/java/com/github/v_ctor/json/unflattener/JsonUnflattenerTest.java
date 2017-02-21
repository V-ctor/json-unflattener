package com.github.v_ctor.json.unflattener;


import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class JsonUnflattenerTest {

    public static final String TEST_VALUE_1 = "value1";
    public static final String TEST_VALUE_2 = "value2";
    public static final String TEST_VALUE_3 = "value3";
    public static final String TEST_VALUE_4 = "value4";

    @Test
    public void MapToJsonTestOneVariable() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a", TEST_VALUE_1);

        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(), "{\"a\":\"" + TEST_VALUE_1 + "\"}");
    }

    @Test(dependsOnMethods = "MapToJsonTestOneVariable")
    public void MapToJsonTestTwoVariables() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a", TEST_VALUE_1);
        data.put("b", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(), "{\"a\":\"" + TEST_VALUE_1 + "\",\"b\":\"" + TEST_VALUE_2 + "\"}");
    }

    @Test//(dependsOnMethods = "MapToJsonTestTwoVariables")
    public void MapToJsonTestOneSubVariable() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a.b", TEST_VALUE_1);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(), "{\"a\":{\"b\":\"" + TEST_VALUE_1 + "\"}}");
    }

    @Test(dependsOnMethods = "MapToJsonTestOneSubVariable")
    public void MapToJsonTestTwoSubVariables() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a.b", TEST_VALUE_1);
        data.put("a.c", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(), "{\"a\":{\"b\":\"" + TEST_VALUE_1 + "\",\"c\":\"" + TEST_VALUE_2 + "\"}}");
    }

    @Test//(dependsOnMethods = "MapToJsonTestArray")
    public void MapToJsonTestTwoSubVariables2() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a.b", TEST_VALUE_1);
        data.put("c.d", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(),
            "{\"a\":{\"b\":\"" + TEST_VALUE_1 + "\"},\"c\":{\"d\":\"" + TEST_VALUE_2 + "\"}}");
    }

    @Test//(dependsOnMethods = "MapToJsonTestTwoSubVariables")
    public void MapToJsonTestThreeSubVariables() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a.b", TEST_VALUE_1);
        data.put("a.c.d", TEST_VALUE_2);
        data.put("a.e", TEST_VALUE_3);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(), "{\"a\":{\"b\":\"" + TEST_VALUE_1 + "\",\"c\":{\"d\":\"" + TEST_VALUE_2 +
            "\"},\"e\":\"" + TEST_VALUE_3 + "\"}}");
    }

    @Test//(dependsOnMethods = "MapToJsonTestTwoSubVariables")
    public void MapToJsonTestThreeSubVariables3() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a.b.c", TEST_VALUE_1);
        data.put("a.b.d", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(() -> data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(), "{\"a\":{\"b\":{\"c\":\"" + TEST_VALUE_1 + "\",\"d\":\"" + TEST_VALUE_2 + "\"}}}");
    }

    @Test//(dependsOnMethods = "MapToJsonTestTwoSubVariables")
    public void MapToJsonTestThreeSubVariables4() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a.b.c", TEST_VALUE_1);
        data.put("a.d.e", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(() -> data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(), "{\"a\":{\"b\":{\"c\":\"" + TEST_VALUE_1 + "\"},\"d\":{\"e\":\"" + TEST_VALUE_2 + "\"}}}");
    }

    @Test//(dependsOnMethods = "MapToJsonTestTwoSubVariables")
    public void MapToJsonTestThreeSubVariables5() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a.b.c.d", TEST_VALUE_1);
        data.put("a.e.f", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(() -> data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(), "{\"a\":{\"b\":{\"c\":{\"d\":\"value1\"}},\"e\":{\"f\":\"" + TEST_VALUE_2 + "\"}}}");
    }

    @Test//(dependsOnMethods = "MapToJsonTestTwoSubVariables")
    public void MapToJsonTestThreeSubVariables6() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a.b.c", TEST_VALUE_1);
        data.put("a.d.e.f", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(() -> data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(),
            "{\"a\":{\"b\":{\"c\":\"" + TEST_VALUE_1 + "\"},\"d\":{\"e\":{\"f\":\"" + TEST_VALUE_2 + "\"}}}}");
    }

    @Test//(dependsOnMethods = "MapToJsonTestThreeSubVariables")
    public void MapToJsonTestThreeSubVariables2() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a.b", TEST_VALUE_1);
        data.put("a.c.d.d.d.d", TEST_VALUE_2);
        data.put("a.e", "1.3");
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        assertEquals(outputStream.toString(),
            "{\"a\":{\"b\":\"" + TEST_VALUE_1 + "\",\"c\":{\"d\":{\"d\":{\"d\":{\"d\":\"" + TEST_VALUE_2 + "\"}}}},\"e\":\"1.3\"}}");
    }

    @Test//(dependsOnMethods = "MapToJsonTestThreeSubVariables2")
    public void MapToJsonTestArraySimple() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0]", TEST_VALUE_1);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(), "{\"a\":[\"" + TEST_VALUE_1 + "\"]}");
    }

    @Test//(dependsOnMethods = "MapToJsonTestThreeSubVariables2")
    public void MapToJsonTestArraySimpleTwoElements() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0]", TEST_VALUE_1);
        data.put("a[1]", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(), "{\"a\":[\"" + TEST_VALUE_1 + "\",\"" + TEST_VALUE_2 + "\"]}");
    }
    @Test//(dependsOnMethods = "MapToJsonTestThreeSubVariables2")
    public void MapToJsonTestArraySimpleThreeElements() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0]", TEST_VALUE_1);
        data.put("a[1]", TEST_VALUE_2);
        data.put("a[2]", TEST_VALUE_3);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(), "{\"a\":[\"" + TEST_VALUE_1 + "\",\"" + TEST_VALUE_2 + "\",\"" + TEST_VALUE_3 + "\"]}");
    }
    @Test//(dependsOnMethods = "MapToJsonTestThreeSubVariables2")
    public void MapToJsonTestTwoArrayaSimple() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0]", TEST_VALUE_1);
        data.put("b[0]", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(), "{\"a\":[\"" + TEST_VALUE_1 + "\"],\"b\":[\"" + TEST_VALUE_2 + "\"]}");
    }

    @Test//(dependsOnMethods = "MapToJsonTestThreeSubVariables2")
    public void MapToJsonTestArrayOneVariable() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].b", TEST_VALUE_1);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(() -> data, outputStream);
        jsonUnflattener.parseToJson();
        assertEquals(outputStream.toString(), "{\"a\":[{\"b\":\"" + TEST_VALUE_1 + "\"}]}");
    }

    @Test//(dependsOnMethods = "MapToJsonTestArrayOneVariable")
    public void MapToJsonTestArrayTwoVariables() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].b", TEST_VALUE_1);
        data.put("a[0].c", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(), "{\"a\":[{\"b\":\"" + TEST_VALUE_1 + "\",\"c\":\"" + TEST_VALUE_2 + "\"}]}");
    }

    @Test//(dependsOnMethods = "MapToJsonTestArrayTwoVariables")
    public void MapToJsonTestArrayTwoElements() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].b", TEST_VALUE_1);
        data.put("a[1].b", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(), "{\"a\":[{\"b\":\"" + TEST_VALUE_1 + "\"},{\"b\":\"" + TEST_VALUE_2 + "\"}]}");
    }

    @Test(dependsOnMethods = "MapToJsonTestArrayTwoElements")
    public void MapToJsonTestArray() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].b[0].c", TEST_VALUE_1);
        data.put("a[0].url", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        assertEquals(outputStream.toString(),
            "{\"a\":[{\"b\":[{\"c\":\"" + TEST_VALUE_1 + "\"}],\"url\":\"" + TEST_VALUE_2 + "\"}]}");
    }

    @Test//(dependsOnMethods = "MapToJsonTestArray")
    public void MapToJsonTestTwoArrays() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].b[0].c", TEST_VALUE_1);
        data.put("a[0].d.e", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(),
            "{\"a\":[{\"b\":[{\"c\":\"value1\"}],\"d\":{\"e\":\"" + TEST_VALUE_2 + "\"}}]}");
    }

    @Test(dependsOnMethods = "MapToJsonTestArray")
    public void MapToJsonTestTwoArrays2() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].b[0].c", TEST_VALUE_1);
        data.put("a[1].b[0].c", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        assertEquals(outputStream.toString(),
            "{\"a\":[{\"b\":[{\"c\":\"" + TEST_VALUE_1 + "\"}]},{\"b\":[{\"c\":\"" + TEST_VALUE_2 + "\"}]}]}");
    }

    @Test(dependsOnMethods = "MapToJsonTestArray")
    public void MapToJsonTestTwoArrays3() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].b[0].c", TEST_VALUE_1);
        data.put("a[1].b[1].c", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        assertEquals(outputStream.toString(),
            "{\"a\":[{\"b\":[{\"c\":\"value1\"}]},{\"b\":[{\"c\":\"" + TEST_VALUE_2 + "\"}]}]}");
    }

    @Test//(dependsOnMethods = "MapToJsonTestArray")
    public void MapToJsonTestTwoArrays5() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].b[0].c", TEST_VALUE_1);
        data.put("a[0].b[1].c", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(() -> data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(),
            "{\"a\":[{\"b\":[{\"c\":\"value1\"},{\"c\":\"" + TEST_VALUE_2 + "\"}]}]}");
    }

    @Test//(dependsOnMethods = "MapToJsonTestArray")
    public void MapToJsonTestTwoArrays7() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].b[0]", TEST_VALUE_1);
        data.put("a[0].b[1]", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(() -> data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(),
            "{\"a\":[{\"b\":[\"" + TEST_VALUE_1 + "\",\"" + TEST_VALUE_2 + "\"]}]}");
    }

    @Test//(dependsOnMethods = "MapToJsonTestArray")
    public void MapToJsonTestTwoArrays8() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].b.c", TEST_VALUE_1);
        data.put("a[0].b.d", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(() -> data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(),
            "{\"a\":[{\"b\":{\"c\":\"" + TEST_VALUE_1 + "\",\"d\":\"" + TEST_VALUE_2 + "\"}}]}");
    }

    @Test//(dependsOnMethods = "MapToJsonTestArray")
    public void MapToJsonTestTwoArrays9() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a.b[0]", TEST_VALUE_1);
        data.put("a.b[1]", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(() -> data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(), "{\"a\":{\"b\":[\"value1\",\"value2\"]}}");
    }

    @Test//(dependsOnMethods = "MapToJsonTestArray")
    public void MapToJsonTestTwoArrays10() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a.b[0].c", TEST_VALUE_1);
        data.put("a.b[1].c", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(() -> data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(), "{\"a\":{\"b\":[{\"c\":\"value1\"},{\"c\":\"value2\"}]}}");
    }

    @Test//(dependsOnMethods = "MapToJsonTestArray")
    public void MapToJsonTestTwoArrays4() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].c", TEST_VALUE_1);
        data.put("d[0].c", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(),
            "{\"a\":[{\"c\":\""+TEST_VALUE_1+"\"}],\"d\":[{\"c\":\""+TEST_VALUE_2+"\"}]}");
    }

    @Test//(dependsOnMethods = "MapToJsonTestArray")
    public void MapToJsonTestTwoArrays6() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].b.c", TEST_VALUE_1);
        data.put("d[0].e[0].ee[0].c", TEST_VALUE_2);
        data.put("f[0].g.c", TEST_VALUE_1);
        data.put("h[0].i[0].c", TEST_VALUE_2);
        data.put("g.c", TEST_VALUE_2);
        data.put("k.l", TEST_VALUE_2);
        data.put("m", TEST_VALUE_2);
        //        data.put("a[0].url", TEST_VALUE_2);
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        assertEquals(outputStream.toString(),
            "{\"a\":[{\"b\":{\"c\":\""+ TEST_VALUE_1 + "\"}}],\"d\":[{\"e\":[{\"ee\":[{\"c\":\"" + TEST_VALUE_2 + "\"}]}]}],\"f\":[{\"g\":{\"c\":\"" + TEST_VALUE_1 + "\"}}]," +
                "\"g\":{\"c\":\"" + TEST_VALUE_2 + "\"},\"h\":[{\"i\":[{\"c\":\"" + TEST_VALUE_2 + "\"}]}],\"k\":{\"l\":\"" + TEST_VALUE_2 + "\"},\"m\":\"" + TEST_VALUE_2 + "\"}");
    }

    @Test(dependsOnMethods = "MapToJsonTestTwoArrays")
    public void MapToJsonTestFull() throws IOException {
        final Map<String, String> data = new HashMap();

        data.put("a[0].url", TEST_VALUE_2);
        data.put("a[0].b[0].c", TEST_VALUE_1);
        data.put("a[0].b[0].d", TEST_VALUE_3);
        data.put("a[0].b[0].e", TEST_VALUE_4);
        data.put("a[0].b[0].f", "");
        data.put("a[0].b[1].c", TEST_VALUE_1);
        data.put("a[0].b[1].g", "10");
        data.put("a[0].b[1].h", "10000");
        data.put("a[0].b[1].e", TEST_VALUE_4);
        data.put("a[0].b[1].f", "");
        data.put("a[0].b[2].c", TEST_VALUE_1);
        data.put("a[0].b[2].g", "20");
        data.put("a[0].b[2].h", "20000");
        data.put("a[0].b[2].e", TEST_VALUE_4);
        data.put("a[0].b[2].f", "");
        data.put("a[1].url", TEST_VALUE_2);
        data.put("a[1].b[0].c", TEST_VALUE_1);
        data.put("a[1].b[0].d", TEST_VALUE_3);

        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        assertEquals(outputStream.toString(),
            "{\"a\":[{\"b\":[{\"c\":\"" + TEST_VALUE_1 + "\",\"d\":\"value3\",\"e\":\"value4\",\"f\":\"\"},{\"c\":\"" + TEST_VALUE_1 + "\",\"e\":\"value4\",\"f\":\"\",\"g\":\"10\",\"h\":\"10000\"},{\"c\":\"" + TEST_VALUE_1 + "\",\"e\":\"value4\",\"f\":\"\",\"g\":\"20\",\"h\":\"20000\"}],\"url\":\"" + TEST_VALUE_2 + "\"},{\"b\":[{\"c\":\"" + TEST_VALUE_1 + "\",\"d\":\"value3\"}],\"url\":\"" + TEST_VALUE_2 + "\"}]}");
    }

    @Test
    public void MapToJsonTest() throws IOException {
        final Map<String, String[]> data = new HashMap();
        data.put("a", new String[] {TEST_VALUE_1});
        data.put("b", new String[] {TEST_VALUE_2});

        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        assertEquals(outputStream.toString(), "{\"a\":\"" + TEST_VALUE_1 + "\",\"b\":\"" + TEST_VALUE_2 + "\"}");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void MapToJsonTestException() throws IOException {
        final Map<String, String[]> data = new HashMap();
        data.put("a", new String[] {TEST_VALUE_1});
        data.put("b", new String[] {TEST_VALUE_1, TEST_VALUE_2});

        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(() -> data, outputStream);
        jsonUnflattener.parseToJson();
    }
}
