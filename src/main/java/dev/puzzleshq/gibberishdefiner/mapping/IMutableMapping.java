package dev.puzzleshq.gibberishdefiner.mapping;

// TODO: Document Class
public interface IMutableMapping extends IMapping {

    void putClass(String clazz, String clazz2);
    void putField(String clazz, String field, String clazz2, String field2);
    void putMethod(String clazz, String method, String descriptor, String clazz2, String method2, String descriptor2);

    void putSource(String name, String source);

    IMapping reverse();

}
