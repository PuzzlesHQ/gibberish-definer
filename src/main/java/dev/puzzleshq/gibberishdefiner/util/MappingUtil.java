package dev.puzzleshq.gibberishdefiner.util;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.impl.Mapping;
import dev.puzzleshq.gibberishdefiner.transformer.GibberishRemapper;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.mappingio.tree.VisitableMappingTree;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

// TODO: Document Class
public class MappingUtil {

    public static VisitableMappingTree fromString(String s, MappingFormat format) throws IOException {
        VisitableMappingTree tree = new MemoryMappingTree();
        MappingReader.read(new StringReader(s), format, tree);
        return tree;
    }

    public static VisitableMappingTree fromFile(File file, MappingFormat format) throws IOException {
        VisitableMappingTree tree = new MemoryMappingTree();
        MappingReader.read(new FileReader(file), format, tree);
        return tree;
    }

    public static Mapping fromMappingTree(VisitableMappingTree mappingTree, String namespaceA, String namespaceB) throws IOException {
        int a = mappingTree.getNamespaceId(namespaceA);
        int b = mappingTree.getNamespaceId(namespaceB);

        Mapping mapping = new Mapping();

        for (MappingTree.ClassMapping aClass : mappingTree.getClasses()) {
            String src = aClass.getName(a);
            String dst = aClass.getName(b);

            mapping.putClass(src, dst);
            for (MappingTree.FieldMapping field : aClass.getFields()) {
                String srcField = field.getName(a);
                String dstField = field.getName(b);

                mapping.putField(src, srcField, dst, dstField);
            }

            for (MappingTree.MethodMapping method : aClass.getMethods()) {
                String srcMethod = method.getName(a);
                String srcDesc = method.getDesc(a);

                String dstMethod = method.getName(b);
                String dstDesc = method.getDstDesc(b);

                mapping.putMethod(src, srcMethod, srcDesc, dst, dstMethod, dstDesc);
            }
        }

        return mapping;
    }

    public static String toMethodDescriptor(IMapping mapping, String[] params) {
        StringBuilder desc = new StringBuilder("(");
        for (String clzz : params) {
            desc.append(MappingUtil.toDescriptor(mapping, clzz));
        }
        desc.append(")");
        return desc.toString();
    }

    public static String toMethodDescriptor(String[] params) {
        StringBuilder desc = new StringBuilder("(");
        for (String clzz : params) {
            desc.append(MappingUtil.toDescriptor(clzz));
        }
        desc.append(")");
        return desc.toString();
    }

    public static String toDescriptor(IMapping mapping, final String thing) {
        switch (thing) {
            case "boolean": return "Z";
            case "byte": return "B";
            case "char": return "C";
            case "short": return "S";
            case "int": return "I";
            case "long": return "J";
            case "float": return "F";
            case "double": return "D";
            case "void": return "V";
            default: {
                int bracketCount = thing.length() - thing.replace("[", "").length();
                if (bracketCount != 0){
                    String newString = "";
                    for (int i = 0; i < bracketCount; i++) newString += "[";
                    newString += toDescriptor(thing.replaceAll("[\\[\\]]", ""));
                    return newString;
                }
                return "L" + (mapping.mapClass(thing.replaceAll("\\.", "/"))) + ";";
            }
        }
    }

    public static String toDescriptor(final String thing) {
        switch (thing) {
            case "boolean": return "Z";
            case "byte": return "B";
            case "char": return "C";
            case "short": return "S";
            case "int": return "I";
            case "long": return "J";
            case "float": return "F";
            case "double": return "D";
            case "void": return "V";
            default: {
                int bracketCount = thing.length() - thing.replace("[", "").length();
                if (bracketCount != 0){
                    String newString = "";
                    for (int i = 0; i < bracketCount; i++) newString += "[";
                    newString += toDescriptor(thing.replaceAll("[\\[\\]]", ""));
                    return newString;
                }
                return  "L" + thing.replaceAll("\\.", "/") + ";";
            }
        }
    }

    public static String toProperName(String s) {
        return s.replace('.', '/');
    }

    public static String deobfuscateDescriptor(IMapping mapping, String obDesc) {
        GibberishRemapper remapper = new GibberishRemapper(mapping);
        return remapper.mapDesc(obDesc);
    }
}
