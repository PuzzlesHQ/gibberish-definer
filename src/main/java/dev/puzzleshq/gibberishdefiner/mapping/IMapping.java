package dev.puzzleshq.gibberishdefiner.mapping;

// TODO: Document Class
public interface IMapping {

    String map(String clazz);
    String map(String clazz, String field);
    String map(String clazz, String method, String descriptor);

    IMapping reverse();

}
