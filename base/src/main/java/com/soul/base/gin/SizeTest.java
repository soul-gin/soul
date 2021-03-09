package com.soul.base.gin;

import org.roaringbitmap.longlong.Roaring64NavigableMap;

import java.util.HashMap;

/**
 * @author gin
 * @date 2021/3/9
 */
public class SizeTest {
    public HashMap<String, String> hashMap = new HashMap<String, String>() {{
        put("0", "1");
        put("1", "1");
        put("9", "1");
        put("39", "1");
        put("199", "1");
        put("2000", "1");
        put("30870", "1");
        put("300000", "1");
        put("6000007", "1");
        put("80000090", "1");
        put("800000000", "1");
        put("4000000000", "1");
        put("56780000000", "1");
        put("500000007770", "1");
        put("600000000000999", "1");
        put("7000000000666000", "1");
        put("70000000321000000", "1");
        put("700000000000000002", "1");
        put("8000000000000000001", "1");
        put("9000372036854775807", "1");
        put("9113372036854775807", "1");
        put("9123372036854775807", "1");
        put("9133372036854775807", "1");
        put("9143372036854775807", "1");
        put("9153372036854775807", "1");
        put("9163372036854775807", "1");
        put("9173372036854775807", "1");
        put("9183372036854775807", "1");
        put("9193372036854775807", "1");
        put("9223372036854775807", "1");
    }};

    public Roaring64NavigableMap roaring64NavigableMap = new Roaring64NavigableMap();

    public HashMap<String, String> getHashMap() {
        return hashMap;
    }

    public Roaring64NavigableMap getRoaring64NavigableMap() {
        roaring64NavigableMap.add(0L);
        roaring64NavigableMap.add(1L);
        roaring64NavigableMap.add(9L);
        roaring64NavigableMap.add(39L);
        roaring64NavigableMap.add(199L);
        roaring64NavigableMap.add(2000L);
        roaring64NavigableMap.add(30870L);
        roaring64NavigableMap.add(300000L);
        roaring64NavigableMap.add(6000007L);
        roaring64NavigableMap.add(80000090L);
        roaring64NavigableMap.add(800000000L);
        roaring64NavigableMap.add(4000000000L);
        roaring64NavigableMap.add(56780000000L);
        roaring64NavigableMap.add(500000007770L);
        roaring64NavigableMap.add(600000000000999L);
        roaring64NavigableMap.add(7000000000666000L);
        roaring64NavigableMap.add(70000000321000000L);
        roaring64NavigableMap.add(700000000000000002L);
        roaring64NavigableMap.add(8000000000000000001L);
        roaring64NavigableMap.add(9000372036854775807L);
        roaring64NavigableMap.add(9113372036854775807L);
        roaring64NavigableMap.add(9123372036854775807L);
        roaring64NavigableMap.add(9133372036854775807L);
        roaring64NavigableMap.add(9143372036854775807L);
        roaring64NavigableMap.add(9153372036854775807L);
        roaring64NavigableMap.add(9163372036854775807L);
        roaring64NavigableMap.add(9173372036854775807L);
        roaring64NavigableMap.add(9183372036854775807L);
        roaring64NavigableMap.add(9193372036854775807L);
        roaring64NavigableMap.add(9223372036854775807L);
        return roaring64NavigableMap;
    }


}
