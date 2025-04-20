package dev.puzzleshq.gibberishdefiner.util;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;

// TODO: Document Class
public class MappingUtil {

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
        return switch (thing) {
            case "boolean" -> "Z";
            case "byte" -> "B";
            case "char" -> "C";
            case "short" -> "S";
            case "int" -> "I";
            case "long" -> "J";
            case "float" -> "F";
            case "double" -> "D";
            case "void" -> "V";
            default -> {
                int bracketCount = thing.length() - thing.replace("[", "").length();
                if (bracketCount != 0){
                    String newString = "";
                    for (int i = 0; i < bracketCount; i++) newString += "[";
                    newString += toDescriptor(thing.replaceAll("[\\[\\]]", ""));
                    yield newString;
                }
                yield "L" + (mapping.map(thing.replaceAll("\\.", "/"))) + ";";
            }
        };
    }

    public static String toDescriptor(final String thing) {
        return switch (thing) {
            case "boolean" -> "Z";
            case "byte" -> "B";
            case "char" -> "C";
            case "short" -> "S";
            case "int" -> "I";
            case "long" -> "J";
            case "float" -> "F";
            case "double" -> "D";
            case "void" -> "V";
            default -> {
                int bracketCount = thing.length() - thing.replace("[", "").length();
                if (bracketCount != 0){
                    String newString = "";
                    for (int i = 0; i < bracketCount; i++) newString += "[";
                    newString += toDescriptor(thing.replaceAll("[\\[\\]]", ""));
                    yield newString;
                }
                yield "L" + thing.replaceAll("\\.", "/") + ";";
            }
        };
    }

}
