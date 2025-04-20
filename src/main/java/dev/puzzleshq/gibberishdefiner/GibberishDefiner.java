package dev.puzzleshq.gibberishdefiner;

import dev.puzzleshq.gibberishdefiner.mapping.format.IMappingFormat;
import dev.puzzleshq.gibberishdefiner.mapping.format.impl.ProguardFormat;

import java.util.HashMap;
import java.util.Map;

// TODO: Document Class
public class GibberishDefiner {

    private static final Map<String, IMappingFormat> formatMap = new HashMap<>();

    public static void initDefaultFormats() {
        register("proguard", new ProguardFormat());
    }

    public static void register(String type, IMappingFormat format) {
        formatMap.put(type, format);
    }

    public static IMappingFormat getFormat(String type) {
        return formatMap.get(type);
    }

}
