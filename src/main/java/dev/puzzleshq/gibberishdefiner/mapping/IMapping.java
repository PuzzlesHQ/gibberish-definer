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

    void putClass(String clazz, String clazz2);
    void putField(String clazz, String field, String clazz2, String field2);
    void putMethod(String clazz, String method, String descriptor, String clazz2, String method2, String descriptor2);

    void putSource(String name, String source);

}
