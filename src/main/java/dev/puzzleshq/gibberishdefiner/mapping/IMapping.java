package dev.puzzleshq.gibberishdefiner.mapping;

import java.util.Map;

// TODO: Document Class
public interface IMapping {

    String mapClass(String clazz);
    String mapField(String clazz, String field);
    String mapMethod(String clazz, String method, String descriptor);

    IMapping reverse();

    String mapSource(String name);

    Iterable<? extends Map.Entry<String, String>> getClassEntries();
    Iterable<? extends Map.Entry<String, String>> getFieldEntries();
    Iterable<? extends Map.Entry<String, String>> getMethodEntries();
    Iterable<? extends Map.Entry<String, String>> getSourceEntries();

}
