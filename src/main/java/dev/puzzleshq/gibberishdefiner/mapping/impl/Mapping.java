package dev.puzzleshq.gibberishdefiner.mapping.impl;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.IMutableMapping;

import java.util.HashMap;
import java.util.Map;

public class Mapping implements IMapping, IMutableMapping, Cloneable {

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

    protected Mapping(
            Map<String, String> classes,
            Map<String, String> fields,
            Map<String, String> methods,
            Map<String, String> sourceMap
    ) {
        this.classes = classes;
        this.fields = fields;
        this.methods = methods;
        this.sourceMap = sourceMap;
    }

    @Override
    public String mapClass(String clazz) {
        if (!classes.containsKey(clazz)) return clazz;
        return classes.get(clazz);
    }

    @Override
    public String mapField(String clazz, String field) {
        String key = clazz + "~" + field;

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
    public void putField(String clazz, String field, String clazz2, String field2) {
        String key = clazz + "~" + field;
        String value = clazz2 + "~" + field2;
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

        mapping.sourceMap.putAll(this.sourceMap);

        return mapping;
    }

    public void merge(IMapping mapping) {
        for (Map.Entry<String, String> entry : mapping.getClassEntries()) {
            this.classes.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, String> entry : mapping.getFieldEntries()) {
            this.fields.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, String> entry : mapping.getMethodEntries()) {
            this.methods.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, String> entry : mapping.getSourceEntries()) {
            this.sourceMap.put(entry.getKey(), entry.getValue());
        }
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

    @Override
    public Mapping clone() {
        Map<String, String> cloneClasses = new HashMap<>(classes);
        Map<String, String> cloneFields = new HashMap<>(fields);
        Map<String, String> cloneMethods = new HashMap<>(methods);
        Map<String, String> cloneSourceMap = new HashMap<>(sourceMap);

        return new Mapping(
                cloneClasses,
                cloneFields,
                cloneMethods,
                cloneSourceMap
        );
    }
}
