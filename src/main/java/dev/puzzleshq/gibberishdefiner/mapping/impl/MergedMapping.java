package dev.puzzleshq.gibberishdefiner.mapping.impl;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.IMergeableMapping;

import java.util.HashMap;
import java.util.Map;

public class MergedMapping implements IMapping {

    Map<String, String> classes;
    /* clazz~field, clazz~field */
    Map<String, String> fields;
    /* clazz~method~descriptor, clazz-method~descriptor*/
    Map<String, String> methods;

    public MergedMapping() {
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

    public void merge(IMergeableMapping mapping) {
        for (Map.Entry<String, String> entry : mapping.getClassEntries()) {
            this.classes.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, String> entry : mapping.getFieldEntries()) {
            this.fields.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, String> entry : mapping.getMethodEntries()) {
            this.methods.put(entry.getKey(), entry.getValue());
        }
    }

}
