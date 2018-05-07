package xyz.morecraft.dev.malmo.util;

import cz.adamh.utils.NativeUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Util {

    public static void ensureMalmoXsdPath() {
        final String malmoXsdPath = System.getenv("MALMO_XSD_PATH");
        if (Objects.isNull(malmoXsdPath) || malmoXsdPath.isEmpty()) {
            error("Env property MALMO_XSD_PATH is not set");
        }
        File f = new File(malmoXsdPath);
        if (!(f.exists() && f.isDirectory())) {
            error("Env property MALMO_XSD_PATH is incorrect");
        }
    }

    public static void loadMalmoLib() {
        try {
            NativeUtils.loadLibraryFromJar("/lib/malmo-0.34.0.dll");
        } catch (IOException e) {
            error(e.getMessage());
        }
    }

    private static void error(String e) {
        System.err.println("---\nERROR: " + e + "\n---");
        System.exit(-1);
    }

}
