package dev.puzzleshq.gibberishdefiner;

import dev.puzzleshq.gibberishdefiner.mapping.format.parsing.impl.CSRGMappingReader;
import dev.puzzleshq.gibberishdefiner.mapping.format.parsing.impl.SRGMappingReader;
import dev.puzzleshq.gibberishdefiner.mapping.format.parsing.IMappingFormat;
import dev.puzzleshq.gibberishdefiner.mapping.format.parsing.impl.CGibberBinFormat;
import dev.puzzleshq.gibberishdefiner.mapping.format.parsing.impl.ProguardFormat;

import java.util.HashMap;
import java.util.Map;

// TODO: Document Class
public class GibberishDefiner {

    private static final Map<String, IMappingFormat> formatMap = new HashMap<>();

    public static void initDefaultFormats() {
        register("proguard", new ProguardFormat());
        register("cgibberbin", new CGibberBinFormat());
        register("srg", new SRGMappingReader());
        register("csrg", new CSRGMappingReader());
    }

    public static void register(String type, IMappingFormat format) {
        formatMap.put(type, format);
    }

    public static IMappingFormat getFormat(String type) {
        return formatMap.get(type);
    }

}
