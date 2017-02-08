package UrlToJson;


import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class MapToJsonTest {

    @Test
    public void MapToJsonTestOneVariable() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a", "com.victor.domain.search");

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(), "{\"a\":\"com.victor.domain.search\"}");
    }

    @Test
    public void MapToJsonTestTwoVariables() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a", "com.victor.domain.search");
        data.put("b", "search.domain");

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(), "{\"a\":\"com.victor.domain.search\",\"b\":\"search.domain\"}");
    }

    @Test
    public void MapToJsonTestOneSubVariable() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a.b", "com.victor.domain.search");
        //        data.put("b", "search.domain");

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(), "{\"a\":{\"b\":\"com.victor.domain.search\"}}");
    }

    @Test
    public void MapToJsonTestTwoSubVariables() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a.b", "com.victor.domain.search");
        data.put("a.c", "search.domain");
        //        data.put("b", "search.domain");

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(), "{\"a\":{\"b\":\"com.victor.domain.search\",\"c\":\"search.domain\"}}");
    }

    @Test
    public void MapToJsonTestThreeSubVariables() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a.b", "com.victor.domain.search");
        data.put("a.c.d", "search.domain");
        data.put("a.e", "1.3");

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(), "{\"a\":{\"b\":\"com.victor.domain.search\",\"c\":{\"d\":\"search.domain\"},\"e\":\"1.3\"}}");
    }

    @Test
    public void MapToJsonTestThreeSubVariables2() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a.b", "com.victor.domain.search");
        data.put("a.c.d.d.d.d", "search.domain");
        data.put("a.e", "1.3");

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(),
            "{\"a\":{\"b\":\"com.victor.domain.search\",\"c\":{\"d\":{\"d\":{\"d\":{\"d\":\"search.domain\"}}}},\"e\":\"1.3\"}}");
    }

    @Test
    public void MapToJsonTestArrayOneVariable() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].b", "com.victor.domain.search");

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(), "{\"a\":[{\"b\":\"com.victor.domain.search\"}]}");
    }

    @Test
    public void MapToJsonTestArrayTwoVariables() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].b", "com.victor.domain.search");
        data.put("a[0].c", "search.domain");

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(), "{\"a\":[{\"b\":\"com.victor.domain.search\",\"c\":\"search.domain\"}]}");
    }

    @Test
    public void MapToJsonTestArrayTwoElements() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].b", "com.victor.domain.search");
        data.put("a[1].b", "search.domain");

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(), "{\"a\":[{\"b\":\"com.victor.domain.search\"},{\"b\":\"search.domain\"}]}");
    }

    @Test
    public void MapToJsonTestArray() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].b[0].c", "com.victor.domain.search.SearchRuleText");
        data.put("a[0].url", "https://www.avito.ru");

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(),
            "{\"a\":[{\"b\":[{\"c\":\"com.victor.domain.search.SearchRuleText\"}],\"url\":\"https://www.avito.ru\"}]}");
    }

    @Test
    public void MapToJsonTestTwoArrays() throws IOException {
        final Map<String, String> data = new HashMap();
        data.put("a[0].b[0].c", "com.victor.domain.search.SearchRuleText");
        data.put("a[1].b[0].c", "com.victor.domain.search.SearchRuleText");
//        data.put("a[0].url", "https://www.avito.ru");

        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(),
            "{\"a\":[{\"b\":[{\"c\":\"com.victor.domain.search.SearchRuleText\"}],\"url\":\"https://www.avito.ru\"}]}");
    }

    @Test
    public void MapToJsonTestFull() throws IOException {
        final Map<String, String> data = new HashMap();

        data.put("a[0].url", "https://www.avito.ru");
        data.put("a[0].b[0].c", "com.victor.domain.search.SearchRuleText");
        data.put("a[0].b[0].textFilter", "compact");
        data.put("a[0].b[0].attributeName", "class");
        data.put("a[0].b[0].attributeValue", "");
        data.put("a[0].b[1].c", "com.victor.domain.search.SearchRulePrice");
        data.put("a[0].b[1].minPrice", "10");
        data.put("a[0].b[1].maxPrice", "10000");
        data.put("a[0].b[1].attributeName", "class");
        data.put("a[0].b[1].attributeValue", "");
        data.put("a[0].b[2].c", "com.victor.domain.search.SearchRulePrice");
        data.put("a[0].b[2].minPrice", "20");
        data.put("a[0].b[2].maxPrice", "20000");
        data.put("a[0].b[2].attributeName", "class");
        data.put("a[0].b[2].attributeValue", "");
        data.put("a[1].url", "https://www.avito.ru");
        data.put("a[1].b[0].c", "com.victor.domain.search.SearchRuleText");
        data.put("a[1].b[0].textFilter", "compact");


        final MapToJson mapToJson = new MapToJson(data);
        final OutputStream outputStream = mapToJson.parseToJson();
        assertEquals(outputStream.toString(), "{\"a\":[{\"b\":\"com.victor.domain.search\"},{\"b\":\"search.domain\"}]}");
    }
}
