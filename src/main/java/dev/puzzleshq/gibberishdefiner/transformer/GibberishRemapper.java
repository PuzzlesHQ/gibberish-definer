package dev.puzzleshq.gibberishdefiner.transformer;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.Remapper;

public class GibberishRemapper extends Remapper {

    public final IMapping mapping;
    public ClassReader reader;
    public String className;

    public GibberishRemapper(IMapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        if (name.charAt(0) == '<') return name;
        return nameOf(mapping.mapMethod(owner, name, descriptor));
    }

    @Override
    public String mapInvokeDynamicMethodName(String name, String descriptor) {
        String method = mapping.mapMethod(className, name, descriptor);
        return method.split("~")[1];
    }

    public String mapRecordComponentName(String owner, String name, String descriptor) {
        return mapFieldName(owner, name, descriptor);
    }

    @Override
    public String mapFieldName(String owner, String name, String descriptor) {
        return nameOf(mapping.mapField(owner, name));
    }

    public String mapPackageName(String name) {
        return name;
    }

    public String mapModuleName(String name) {
        return name;
    }

    public String map(String internalName) {
        return mapping.mapClass(internalName);
    }

    private static String nameOf(String joined) {
        int first = joined.indexOf('~');
        int second = joined.indexOf('~', first + 1);
        return second < 0 ? joined.substring(first + 1) : joined.substring(first + 1, second);
    }

}
