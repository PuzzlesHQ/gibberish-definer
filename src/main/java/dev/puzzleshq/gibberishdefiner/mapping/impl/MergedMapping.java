package dev.puzzleshq.gibberishdefiner.mapping.impl;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.IMergeableMapping;

import java.util.HashMap;
import java.util.Map;

public class MergedMapping implements IMapping {

    Map<String, String> classes;
    /* clazz~field~descriptor, clazz-field~descriptor*/
    Map<String, String> fields;
    /* clazz~method~descriptor, clazz-method~descriptor*/
    Map<String, String> methods;

    Map<String, String> sourceMap;

    public MergedMapping() {
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
    public String mapSource(String name) {
        return sourceMap.get(name);
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

        for (Map.Entry<String, String> entry : mapping.getSourceEntries()) {
            this.sourceMap.put(entry.getKey(), entry.getValue());
        }
    }

}
