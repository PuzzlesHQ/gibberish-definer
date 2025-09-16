package dev.puzzleshq.gibberishdefiner.mapping.format.writing.impl;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class GibberMappingStr {

    private static class GibberClass {
        String obf;
        String deobf;

        public Queue<Map.Entry<String, String>> fieldEntries = new ConcurrentLinkedDeque<>();
        public Queue<Map.Entry<String, String>> methodEntries = new ConcurrentLinkedDeque<>();

        public GibberClass(String obf, String deobf) {
            this.obf = obf;
            this.deobf = deobf;
        }

    }

    public void export(IMapping mapping, File file) throws IOException {
        Map<String, GibberClass> gibberClassMap = new HashMap<>();

        String[] strings;
        String[] strings2;

        for (Map.Entry<String, String> classEntry : mapping.getClassEntries()) {
            GibberClass gibberClass = new GibberClass(classEntry.getKey(), classEntry.getValue());
            gibberClassMap.put(classEntry.getKey(), gibberClass);
        }

        for (Map.Entry<String, String> fieldEntry : mapping.getFieldEntries()) {
            strings = fieldEntry.getKey().split("~");
            String clazz = strings[0];
            GibberClass gibberClass = gibberClassMap.get(clazz);
            gibberClass.fieldEntries.add(fieldEntry);
        }

        for (Map.Entry<String, String> methodEntry : mapping.getMethodEntries()) {
            strings = methodEntry.getKey().split("~");
            String clazz = strings[0];
            GibberClass gibberClass = gibberClassMap.get(clazz);
            gibberClass.methodEntries.add(methodEntry);
        }

        ByteArrayOutputStream poolStream = new ByteArrayOutputStream();
        PrintWriter poolWriter = new PrintWriter(poolStream);

        poolWriter.println("Magic Number: 0x7171E5, Version: 0");

        ByteArrayOutputStream codeStream = new ByteArrayOutputStream();
        PrintWriter codeWriter = new PrintWriter(codeStream);

        for (GibberClass gibberClass : gibberClassMap.values()) {
            codeWriter.println("BEGIN_CLASS " + gibberClass.obf + " | " + gibberClass.deobf);

            for (Map.Entry<String, String> fieldEntry : gibberClass.fieldEntries) {
                strings = fieldEntry.getKey().split("~");
                strings2 = fieldEntry.getValue().split("~");

                codeWriter.println("DECLARE_FIELD " + strings[1] + " | " + strings2[1]);
            }

            for (Map.Entry<String, String> fieldEntry : gibberClass.methodEntries) {
                strings = fieldEntry.getKey().split("~");
                strings2 = fieldEntry.getValue().split("~");

                codeWriter.println("DECLARE_METHOD " + strings[1] + "<>" + strings[2] + " | " + strings2[1] + "<>" + strings2[2]);
            }

            codeWriter.println("END_CLASS");
        }
        codeWriter.close();

        poolStream.write(codeStream.toByteArray());
        codeStream.close();

        FileOutputStream stream = new FileOutputStream(file);
        stream.write(poolStream.toByteArray());
        stream.close();
        poolStream.close();
    }

}
