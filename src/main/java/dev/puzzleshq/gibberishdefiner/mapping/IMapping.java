package dev.puzzleshq.gibberishdefiner.mapping;

import java.util.Map;

// TODO: Document Class
public interface IMapping {

    String mapClass(String clazz);
    String mapField(String clazz, String field, String descriptor);
    String mapMethod(String clazz, String method, String descriptor);

    IMapping reverse();

    String mapSource(String name);

}
