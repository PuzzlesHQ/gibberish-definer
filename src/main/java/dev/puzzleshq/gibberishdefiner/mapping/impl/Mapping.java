package dev.puzzleshq.gibberishdefiner.mapping.impl;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.IMergeableMapping;
import dev.puzzleshq.gibberishdefiner.mapping.IMutableMapping;

import java.util.HashMap;
import java.util.Map;

public class Mapping implements IMapping, IMutableMapping, IMergeableMapping {

    Map<String, String> classes;
    /* clazz~field~descriptor, clazz-field~descriptor*/
    Map<String, String> fields;
    /* clazz~method~descriptor, clazz~method~descriptor*/
    Map<String, String> methods;

    Map<String, String> sourceMap;

    public Mapping() {
        classes = new HashMap<>();
        fields = new HashMap<>();
        methods = new HashMap<>();
        sourceMap = new HashMap<>();
    }

    @Override
    public String mapClass(String clazz) {
        if (!classes.containsKey(clazz)) return clazz;
        return classes.get(clazz);
    }

    @Override
    public String mapField(String clazz, String field, String descriptor) {
        String key = clazz + "~" + field + "~" + descriptor;

        if (!fields.containsKey(key)) return key;
        return fields.get(key);
    }

    @Override
    public String mapMethod(String clazz, String method, String descriptor) {
        String key = clazz + "~" + method + "~" + descriptor;

        if (!methods.containsKey(key)) return key;
        return methods.get(key);
    }

    @Override
    public void putClass(String clazz, String clazz2) {
        this.classes.put(clazz, clazz2);
    }

    @Override
    public void putField(String clazz, String field, String descriptor, String clazz2, String field2, String descriptor2) {
        String key = clazz + "~" + field + "~" + descriptor;
        String value = clazz2 + "~" + field2 + "~" + descriptor2;
        this.fields.put(key, value);
    }

    @Override
    public void putMethod(String clazz, String method, String descriptor, String clazz2, String method2, String descriptor2) {
        String key = clazz + "~" + method + "~" + descriptor;
        String value = clazz2 + "~" + method2 + "~" + descriptor2;
        this.methods.put(key, value);
    }

    @Override
    public IMapping reverse() {
        Mapping mapping = new Mapping();
        for (Map.Entry<String, String> clazz : this.classes.entrySet()) {
            mapping.classes.put(clazz.getValue(), clazz.getKey());
        }

        for (Map.Entry<String, String> field : this.fields.entrySet()) {
            mapping.fields.put(field.getValue(), field.getKey());
        }

        for (Map.Entry<String, String> method : this.methods.entrySet()) {
            mapping.methods.put(method.getValue(), method.getKey());
        }

        for (Map.Entry<String, String> method : this.sourceMap.entrySet()) {
            mapping.sourceMap.put(method.getKey(), method.getValue());
        }

        return mapping;
    }

    @Override
    public String mapSource(String name) {
        return this.sourceMap.get(name);
    }

    @Override
    public void putSource(String name, String source) {
        this.sourceMap.put(name, source);
    }

    @Override
    public Iterable<? extends Map.Entry<String, String>> getClassEntries() {
        return this.classes.entrySet();
    }

    @Override
    public Iterable<? extends Map.Entry<String, String>> getFieldEntries() {
        return this.fields.entrySet();
    }

    @Override
    public Iterable<? extends Map.Entry<String, String>> getMethodEntries() {
        return this.methods.entrySet();
    }

    @Override
    public Iterable<? extends Map.Entry<String, String>> getSourceEntries() {
        return this.sourceMap.entrySet();
    }
}
