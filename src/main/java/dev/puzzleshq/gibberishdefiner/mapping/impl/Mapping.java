package dev.puzzleshq.gibberishdefiner.mapping.impl;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.IMutableMapping;

import java.util.HashMap;
import java.util.Map;

public class Mapping implements IMapping, IMutableMapping {

    Map<String, String> classes;
    /* clazz~field, clazz~field */
    Map<String, String> fields;
    /* clazz~method~descriptor, clazz-method~descriptor*/
    Map<String, String> methods;

    public Mapping() {
        classes = new HashMap<>();
        fields = new HashMap<>();
        methods = new HashMap<>();
    }

    @Override
    public String map(String clazz) {
        if (!classes.containsKey(clazz)) return clazz;
        return classes.get(clazz);
    }

    @Override
    public String map(String clazz, String field) {
        String key = clazz + "~" + field;

        if (!fields.containsKey(key)) return key;
        return fields.get(key);
    }

    @Override
    public String map(String clazz, String method, String descriptor) {
        String key = clazz + "~" + method + "~" + descriptor;

        if (!methods.containsKey(key)) return key;
        return methods.get(key);
    }

    @Override
    public void put(String clazz, String clazz2) {
        this.classes.put(clazz, clazz2);
    }

    @Override
    public void put(String clazz, String field, String clazz2, String field2) {
        String key = clazz + "~" + field;
        String value = clazz2 + "~" + field2;
        this.methods.put(key, value);
    }

    @Override
    public void put(String clazz, String method, String descriptor, String clazz2, String method2, String descriptor2) {
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

        return mapping;
    }
}
