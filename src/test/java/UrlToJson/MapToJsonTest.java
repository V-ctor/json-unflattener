package UrlToJson;


import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class MapToJsonTest {

    public static final String TEST_VALUE_1 = "value1";
    public static final String TEST_VALUE_2 = "value2";
    public static final String TEST_VALUE_3 = "value3";
    public static final String TEST_VALUE_4 = "value4";

    @Test
    public void MapToJsonTestOneVariable() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a", TEST_VALUE_1);

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(), "{\"a\":\"" + TEST_VALUE_1 + "\"}");
    }

    @Test
    public void MapToJsonTestTwoVariables() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a", TEST_VALUE_1);
        data.put("b", TEST_VALUE_2);

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(), "{\"a\":\"" + TEST_VALUE_1 + "\",\"b\":\"" + TEST_VALUE_2 + "\"}");
    }

    @Test
    public void MapToJsonTestOneSubVariable() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a.b", TEST_VALUE_1);
        //        data.put("b", "search.domain");

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(), "{\"a\":{\"b\":\"" + TEST_VALUE_1 + "\"}}");
    }

    @Test
    public void MapToJsonTestTwoSubVariables() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a.b", TEST_VALUE_1);
        data.put("a.c", TEST_VALUE_2);
        //        data.put("b", "search.domain");

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(), "{\"a\":{\"b\":\"" + TEST_VALUE_1 + "\",\"c\":\"" + TEST_VALUE_2 + "\"}}");
    }

    @Test
    public void MapToJsonTestThreeSubVariables() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a.b", TEST_VALUE_1);
        data.put("a.c.d", TEST_VALUE_2);
        data.put("a.e", "1.3");

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(), "{\"a\":{\"b\":\"" + TEST_VALUE_1 + "\",\"c\":{\"d\":\"" + TEST_VALUE_2 +
            "\"},\"e\":\"1.3\"}}");
    }

    @Test
    public void MapToJsonTestThreeSubVariables2() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a.b", TEST_VALUE_1);
        data.put("a.c.d.d.d.d", TEST_VALUE_2);
        data.put("a.e", "1.3");

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(),
            "{\"a\":{\"b\":\"" + TEST_VALUE_1 + "\",\"c\":{\"d\":{\"d\":{\"d\":{\"d\":\"" + TEST_VALUE_2 + "\"}}}},\"e\":\"1.3\"}}");
    }

    @Test
    public void MapToJsonTestArrayOneVariable() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].b", TEST_VALUE_1);

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(), "{\"a\":[{\"b\":\"" + TEST_VALUE_1 + "\"}]}");
    }

    @Test
    public void MapToJsonTestArrayTwoVariables() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].b", TEST_VALUE_1);
        data.put("a[0].c", TEST_VALUE_2);

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(), "{\"a\":[{\"b\":\"" + TEST_VALUE_1 + "\",\"c\":\"" + TEST_VALUE_2 + "\"}]}");
    }

    @Test
    public void MapToJsonTestArrayTwoElements() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].b", TEST_VALUE_1);
        data.put("a[1].b", TEST_VALUE_2);

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(), "{\"a\":[{\"b\":\"" + TEST_VALUE_1 + "\"},{\"b\":\"" + TEST_VALUE_2 + "\"}]}");
    }

    @Test
    public void MapToJsonTestArray() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].b[0].c", TEST_VALUE_1);
        data.put("a[0].url", TEST_VALUE_2);

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(),
            "{\"a\":[{\"b\":[{\"c\":\"" + TEST_VALUE_1 + "\"}],\"url\":\"" + TEST_VALUE_2 + "\"}]}");
    }

    @Test
    public void MapToJsonTestTwoArrays() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].b[0].c", TEST_VALUE_1);
        data.put("a[1].b[0].c", TEST_VALUE_2);
        //        data.put("a[0].url", TEST_VALUE_2);

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(),
            "{\"a\":[{\"b\":[{\"c\":\""+ TEST_VALUE_1 + "\"}]},{\"b\":[{\"c\":\"" + TEST_VALUE_2 + "\"}]}]}");
    }

    @Test
    public void MapToJsonTestFull() throws IOException {
        final Map<String, String> data = new HashMap();

        data.put("a[0].url", TEST_VALUE_2);
        data.put("a[0].b[0].c", TEST_VALUE_1);
        data.put("a[0].b[0].d", TEST_VALUE_3);
        data.put("a[0].b[0].e", TEST_VALUE_4);
        data.put("a[0].b[0].f", "");
        data.put("a[0].b[1].c", TEST_VALUE_1 );
        data.put("a[0].b[1].g", "10");
        data.put("a[0].b[1].h", "10000");
        data.put("a[0].b[1].e", TEST_VALUE_4);
        data.put("a[0].b[1].f", "");
        data.put("a[0].b[2].c", TEST_VALUE_1 );
        data.put("a[0].b[2].g", "20");
        data.put("a[0].b[2].h", "20000");
        data.put("a[0].b[2].e", TEST_VALUE_4);
        data.put("a[0].b[2].f", "");
        data.put("a[1].url", TEST_VALUE_2);
        data.put("a[1].b[0].c", TEST_VALUE_1);
        data.put("a[1].b[0].d", TEST_VALUE_3);


        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(), "{\"a\":[{\"b\":[{\"c\":\"value1\",\"d\":\"value3\",\"e\":\"value4\",\"f\":\"\"},{\"c\":\"value1\",\"e\":\"value4\",\"f\":\"\",\"g\":\"10\",\"h\":\"10000\"},{\"c\":\"value1\",\"e\":\"value4\",\"f\":\"\",\"g\":\"20\",\"h\":\"20000\"}],\"url\":\"value2\"},{\"b\":[{\"c\":\"value1\",\"d\":\"value3\"}],\"url\":\"value2\"}]}");
    }
}
