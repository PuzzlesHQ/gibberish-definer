package dev.puzzleshq.gibberishdefiner.mapping.format.writing.impl;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.zip.GZIPOutputStream;

public class GibberMappingCompressedBinary {

    private static class StringPool {

        public List<String> constantPool = new ArrayList<>();

        public StringPool() {}

        public int add(String str) {
            int idx = constantPool.indexOf(str);
            if (idx == -1) {
                idx = constantPool.size();
                constantPool.add(str);
            }
            return idx;
        }

        public void write(DataOutputStream dataOutputStream) throws IOException {
            dataOutputStream.writeInt(constantPool.size());
            for (String s : constantPool) {
                dataOutputStream.writeUTF(s);
            }
        }
    }

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
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(poolStream);
        DataOutputStream poolDataStream = new DataOutputStream(gzipOutputStream);
        poolDataStream.writeByte(0x71); // Magic Number
        poolDataStream.writeByte(0x71); // Magic Number
        poolDataStream.writeByte(0xE5); // Magic Number

        poolDataStream.writeByte(0x00); // Version
        poolDataStream.writeByte(0x00); // Version

        ByteArrayOutputStream codeStream = new ByteArrayOutputStream();
        GZIPOutputStream codeGZipStream = new GZIPOutputStream(codeStream);
        DataOutputStream codeDataStream = new DataOutputStream(codeGZipStream);

        StringPool classPool = new StringPool();
        StringPool fieldPool = new StringPool();
        StringPool methodPool = new StringPool();

        for (GibberClass gibberClass : gibberClassMap.values()) {
            int classObfNameIdx = classPool.add(gibberClass.obf);
            int classDeObfNameIdx = classPool.add(gibberClass.deobf);

            codeDataStream.writeByte(1);
            codeDataStream.writeInt(classObfNameIdx);
            codeDataStream.writeInt(classDeObfNameIdx);

            for (Map.Entry<String, String> fieldEntry : gibberClass.fieldEntries) {
                strings = fieldEntry.getKey().split("~");
                strings2 = fieldEntry.getValue().split("~");

                int fieldObfNameIdx = fieldPool.add(strings[1]);
                int fieldDeObfNameIdx = fieldPool.add(strings2[1]);

                codeDataStream.writeByte(3);
                codeDataStream.writeInt(fieldObfNameIdx);
                codeDataStream.writeInt(fieldDeObfNameIdx);
            }

            for (Map.Entry<String, String> fieldEntry : gibberClass.methodEntries) {
                strings = fieldEntry.getKey().split("~");
                strings2 = fieldEntry.getValue().split("~");

                int methodObfNameIdx = methodPool.add(strings[1]);
                int methodDeObfNameIdx = methodPool.add(strings2[1]);

                int methodObfDescIdx = methodPool.add(strings[2]);
                int methodDeObfDescIdx = methodPool.add(strings2[2]);

                codeDataStream.writeByte(4);
                codeDataStream.writeInt(methodObfNameIdx);
                codeDataStream.writeInt(methodObfDescIdx);
                codeDataStream.writeInt(methodDeObfNameIdx);
                codeDataStream.writeInt(methodDeObfDescIdx);
            }

            codeDataStream.writeByte(2);
        }
        codeDataStream.close();

        classPool.write(poolDataStream);
        fieldPool.write(poolDataStream);
        methodPool.write(poolDataStream);

        byte[] code = codeStream.toByteArray();
        codeStream.close();

        poolDataStream.writeInt(code.length);
        poolDataStream.write(code);
        poolDataStream.close();

        FileOutputStream stream = new FileOutputStream(file);
        stream.write(poolStream.toByteArray());
        stream.close();
        poolStream.close();
    }

}
