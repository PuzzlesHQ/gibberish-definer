package dev.puzzleshq.gibberishdefiner.mapping;

import java.util.Map;

// TODO: Document Class
public interface IMergeableMapping extends IMapping {

    Iterable<? extends Map.Entry<String, String>> getClassEntries();
    Iterable<? extends Map.Entry<String, String>> getFieldEntries();
    Iterable<? extends Map.Entry<String, String>> getMethodEntries();

}
