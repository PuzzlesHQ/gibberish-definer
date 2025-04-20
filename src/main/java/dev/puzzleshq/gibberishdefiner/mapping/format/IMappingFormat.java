package dev.puzzleshq.gibberishdefiner.mapping.format;

import dev.puzzleshq.gibberishdefiner.GibberishDefiner;
import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.IMutableMapping;
import dev.puzzleshq.gibberishdefiner.mapping.impl.Mapping;

// TODO: Document Class
public interface IMappingFormat {

    IMapping assemble(IMutableMapping mapping, String contents);
    String name();

    static IMapping fromString(IMutableMapping mapping, String formatType, String contents) {
        IMappingFormat format = GibberishDefiner.getFormat(formatType);
        return format.assemble(mapping, contents);
    }

    static IMapping fromString(String formatType, String contents) {
        IMappingFormat format = GibberishDefiner.getFormat(formatType);
        return format.assemble(new Mapping(), contents);
    }

}
