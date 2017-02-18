package com.github.v_ctor.json.unflattener;


import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class ObjectMapperJsonUnflattenerTest {

    public static final String TEST_VALUE_1 = "value1";
    public static final String TEST_VALUE_2 = "value2";
    public static final String TEST_VALUE_3 = "value3";
    public static final String TEST_VALUE_4 = "value4";

    @Test
    public void testReadValue() throws Exception {

    }

    @Test
    public void testReadValue1() throws Exception {
        HashMap<String, String> map = new HashMap<>();
        map.put("i", "10");
        map.put("str", TEST_VALUE_1);
        map.put("intArr[0]", "100");
        map.put("intArr[1]", "200");
        map.put("intArr[2]", "300");
        map.put("strArr[0]", TEST_VALUE_2);
        map.put("strArr[1]", TEST_VALUE_3);
        map.put("strArr[2]", TEST_VALUE_4);

        Entity expected = new Entity();
        expected.i = 10;
        expected.str = TEST_VALUE_1;
        expected.intArr.add(100);
        expected.intArr.add(200);
        expected.intArr.add(300);
        expected.strArr[0] = TEST_VALUE_2;
        expected.strArr[1] = TEST_VALUE_3;
        expected.strArr[2] = TEST_VALUE_4;

        ObjectMapperJsonUnflattener mapper = new ObjectMapperJsonUnflattener();
        Entity actual = mapper.readValue(() -> map, Entity.class);

        assertEquals(actual, expected);
    }

    @Test
    public void testReadValue2() throws Exception {

    }

    private static class Entity {
        int i;
        String str;
        List<Integer> intArr = new ArrayList<>();
        String strArr[] = new String[3];

        public Entity() {
        }

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }

        public List<Integer> getIntArr() {
            return intArr;
        }

        public void setIntArr(List<Integer> intArr) {
            this.intArr = intArr;
        }

        public String[] getStrArr() {
            return strArr;
        }

        public void setStrArr(String[] strArr) {
            this.strArr = strArr;
        }


    }
}

