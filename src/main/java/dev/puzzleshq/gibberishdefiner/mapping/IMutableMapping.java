package dev.puzzleshq.gibberishdefiner.mapping;

// TODO: Document Class
public interface IMutableMapping extends IMapping {

    void put(String clazz, String clazz2);
    void put(String clazz, String field, String clazz2, String field2);
    void put(String clazz, String method, String descriptor, String clazz2, String method2, String descriptor2);

    IMapping reverse();

}
