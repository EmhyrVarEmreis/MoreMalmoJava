package xyz.morecraft.dev.malmo.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.msr.malmo.TimestampedString;
import lombok.Getter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class WorldObservation {

    private final static ObjectMapper objectMapper = Jackson.OBJECT_MAPPER;

    @Getter
    private TimestampedString v;
    @Getter
    private Map<String, Object> map;

    public WorldObservation(TimestampedString v) throws IOException {
        this.v = v;
        this.map = objectMapper.readValue(v.getText(), new TypeReference<TreeMap<String, Object>>() {
        });
    }

    public String[] getGrid(String name, int x) {
        final Object o = map.get(name);
        final String[] t = new String[x];
        if (o instanceof List) {
            List l = List.class.cast(o);
            int i = 0;
            for (Object oo : l) {
                final String s = String.class.cast(oo);
                t[i++] = s;
            }
            return t;
        }
        return null;
    }

    public String[][][] getGrid(String name, int x, int y, int z) {
        final Object o = map.get(name);
        final String[][][] t = new String[y][z][x];
        if (o instanceof List) {
            List l = List.class.cast(o);
            int f = x * z;
            int i = 0;
            int d = 0;
            for (Object oo : l) {
                final String s = String.class.cast(oo);
                d = i % f;
                t[i / f][d / z][d % x] = s;
                i++;
            }
            return t;
        }
        return null;
    }

    public float getDistance(String name) {
        return (float) Double.class.cast(map.get("distanceFrom" + name)).doubleValue();
    }

}
