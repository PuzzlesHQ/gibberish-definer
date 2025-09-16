package dev.puzzleshq.gibberishdefiner.mapping.format.parsing;

import dev.puzzleshq.gibberishdefiner.GibberishDefiner;
import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.IMutableMapping;
import dev.puzzleshq.gibberishdefiner.mapping.impl.Mapping;

// TODO: Document Class
public interface IMappingFormat {

    IMapping assemble(IMutableMapping mapping, byte[] contents);
    String name();

    static IMapping fromBytes(IMutableMapping mapping, String formatType, byte[] contents) {
        IMappingFormat format = GibberishDefiner.getFormat(formatType);
        return format.assemble(mapping, contents);
    }

    static IMapping fromBytes(String formatType, byte[] contents) {
        IMappingFormat format = GibberishDefiner.getFormat(formatType);
        return format.assemble(new Mapping(), contents);
    }

}
