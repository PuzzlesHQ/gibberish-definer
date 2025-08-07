package dev.puzzleshq.gibberishdefiner.transformer;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.Remapper;

public class GibberishRemapper extends Remapper {

    IMapping mapping;
    public ClassReader reader;
    public String className;

    public GibberishRemapper(IMapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        String method = mapping.mapMethod(owner, name, descriptor);
        return method.split("~")[1];
    }

    @Override
    public String mapInvokeDynamicMethodName(String name, String descriptor) {
        String method = mapping.mapMethod(className, name, descriptor);
        return method.split("~")[1];
    }

    public String mapRecordComponentName(String owner, String name, String descriptor) {
        return mapFieldName(owner, name, descriptor);
    }

    public String mapFieldName(String owner, String name, String descriptor) {
        String method = mapping.mapField(owner, name, descriptor);
        return method.split("~")[1];
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

}
