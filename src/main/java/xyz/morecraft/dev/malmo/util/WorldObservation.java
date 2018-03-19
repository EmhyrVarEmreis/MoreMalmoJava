package xyz.morecraft.dev.malmo.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.msr.malmo.TimestampedString;
import com.microsoft.msr.malmo.TimestampedStringVector;
import com.microsoft.msr.malmo.WorldState;
import lombok.Getter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    public static WorldObservation fromWorldState(WorldState worldState) throws IOException {
        final TimestampedStringVector observations = worldState.getObservations();
        for (int i = 0; i < observations.size(); i++) {
            final TimestampedString o = observations.get(i);
            if (Objects.nonNull(o)) {
                return new WorldObservation(o);
            }
        }
        return null;
    }

    public IntPoint3D getPos() {
        return new IntPoint3D(
                (double) map.get("XPos"),
                (double) map.get("YPos"),
                (double) map.get("ZPos")
        );
    }

    public boolean isAlive() {
        return (boolean) map.get("IsAlive");
    }

    public int getFood() {
        return (int) map.get("Food");
    }

    public int getDamageDealt() {
        return (int) map.get("DamageDealt");
    }

    public int getDamageTaken() {
        return (int) map.get("DamageTaken");
    }

    public int getDistanceTravelled() {
        return (int) map.get("DistanceTravelled");
    }

    public double getLife() {
        return (double) map.get("Life");
    }

    public int getMobsKilled() {
        return (int) map.get("MobsKilled");
    }

    public String getName() {
        return (String) map.get("Name");
    }

    public double getPitch() {
        return (double) map.get("Pitch");
    }

    public int getPlayersKilled() {
        return (int) map.get("PlayersKilled");
    }

    public int getScore() {
        return (int) map.get("Score");
    }

    public int getTimeAlive() {
        return (int) map.get("TimeAlive");
    }

    public int getTotalTime() {
        return (int) map.get("TotalTime");
    }

    public int getWorldTime() {
        return (int) map.get("WorldTime");
    }

    public int getXP() {
        return (int) map.get("XP");
    }

    public double getYaw() {
        return (double) map.get("Yaw");
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

    public double getDistance(String name) {
        return Double.class.cast(map.get("distanceFrom" + name));
    }

}
