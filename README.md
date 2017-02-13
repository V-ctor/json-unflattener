# json-unflattener

Java utility based on Jackson to unflatten objects from Map to JSON.

#Example
We have flattened JSON Map like
"a[0].b.c" -> "value1"
"k.l" -> "value2"
"h[0].i[0].c" -> "value2"
"d[0].e[0].ee[0].c" -> "value2"
"f[0].g.c" -> "value1"
"g.c" -> "value2"
"m" -> "value2"

then we need to convert it in unflattened JSON
{"a":[{"b":{"c":"value1"}}],"d":[{"e":[{"ee":[{"c":"value2"}]}]}],"f":[{"g":{"c":"value1"}}],"g":{"c":"value2"},"h":[{"i":[{"c":"value2"}]}],"k":{"l":"value2"},"m":"value2"}

#Quick start
```java
        final Map<String, String> data = new HashMap();
        data.put("a[0].c", "value1");
        data.put("d[0].c", "value2");
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(()->data, outputStream);
        jsonUnflattener.parseToJson();
        System.out.println(outputStream);
        //{"a":[{"c":"value1"}],"d":[{"c":"value2"}]}
```
Or converting HttpServletRequest.getParameterMap() . 
getParameterMap returns Map<String, String[]> so let's use lambda to avoid Type Erasure problem and implements two constrauctors for Map<String, String> and Map<String, String[]>.
That's why we pass map in lambda way ()->map

```java
    public String searchGet(Model model, HttpServletRequest request) throws IOException {
        final OutputStream outputStream = new ByteArrayOutputStream();
        final JsonUnflattener jsonUnflattener = new JsonUnflattener(() -> request.getParameterMap(), outputStream);
        jsonUnflattener.parseToJson();

```
