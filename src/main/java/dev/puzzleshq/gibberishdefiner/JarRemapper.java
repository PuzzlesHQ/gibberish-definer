package dev.puzzleshq.gibberishdefiner;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.impl.Mapping;
import dev.puzzleshq.gibberishdefiner.transformer.ClassPreprocessor;
import dev.puzzleshq.gibberishdefiner.transformer.ClassSourceFixer;
import dev.puzzleshq.gibberishdefiner.transformer.GibberishRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class JarRemapper {

    public static IMapping createContextMapping(File inputJar, IMapping mapping) {
        return createContextMapping(inputJar, mapping, false);
    }

    public static IMapping createContextMapping(File inputJar, IMapping mapping, boolean reverse) {
        if (reverse) mapping = mapping.reverse();

        Map<String, ClassPreprocessor> preprocessorMap = new HashMap<>();

        try {
            FileInputStream stream = new FileInputStream(inputJar);
            byte[] bytes = stream.readAllBytes();
            stream.close();
            ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(bytes));

            ZipEntry entry = input.getNextEntry();
            while (entry != null) {
                String entryName = entry.getName();

                if (entryName.endsWith(".class") && !(entryName.contains("module-info") || entryName.contains("package-info"))) {
                    if (mapping.mapClass(entryName.replace(".class", "")).equals(entryName.replace(".class", ""))) {
                        entry = input.getNextEntry();
                        continue;
                    }

                    ClassReader reader = new ClassReader(input.readAllBytes());
                    ClassPreprocessor preprocessor = new ClassPreprocessor();

                    preprocessorMap.put(reader.getClassName(), preprocessor);
                    reader.accept(preprocessor, 0);
                }

                entry = input.getNextEntry();
            }

            input.close();

            mapping = remixMapping(preprocessorMap, mapping);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return mapping;
    }

    public static IMapping applySkeletonMapping(File inputJar, File outputJar, IMapping mapping) {
        return applySkeletonMapping(inputJar, outputJar, mapping, false);
    }

    public static IMapping applySkeletonMapping(File inputJar, File outputJar, IMapping mapping, boolean reverse) {
        if (reverse) mapping = mapping.reverse();

        Map<String, ClassPreprocessor> preprocessorMap = new HashMap<>();

        try {
            FileInputStream stream = new FileInputStream(inputJar);
            byte[] bytes = stream.readAllBytes();
            stream.close();
            ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(bytes));

            ZipEntry entry = input.getNextEntry();
            while (entry != null) {
                String entryName = entry.getName();

                if (entryName.contains("module-info") || entryName.contains("package-info")) {}
                else if (entryName.endsWith(".class")) {
//                    if (mapping.mapClass(entryName.replace(".class", "")).equals(entryName.replace(".class", ""))) {
//                        entry = input.getNextEntry();
//                        continue;
//                    }
                    ClassReader reader = new ClassReader(input.readAllBytes());
                    ClassPreprocessor preprocessor = new ClassPreprocessor();

                    reader.accept(preprocessor, 0);
                    preprocessorMap.put(reader.getClassName(), preprocessor);
                }

                entry = input.getNextEntry();
            }

            input.close();

            IMapping remixedMapping = remixMapping(preprocessorMap, mapping);
            GibberishRemapper remapper = new GibberishRemapper(remixedMapping);

            input = new ZipInputStream(new ByteArrayInputStream(bytes));
            ZipOutputStream output = new ZipOutputStream(new FileOutputStream(outputJar));
            entry = input.getNextEntry();
            while (entry != null) {
                String entryName = entry.getName();

                if (entryName.contains("module-info") || entryName.contains("package-info")) {
                    byte[] classBytes = transformClass(input.readAllBytes(), remixedMapping, remapper);
                    if (classBytes != null) {
                        entry = new ZipEntry(remixedMapping.mapClass(entryName.replace(".class", "")) + ".class");
                        output.putNextEntry(entry);
                        output.write(input.readAllBytes());
                    }
                } else if (entryName.endsWith(".class")) {
                    byte[] classBytes = transformClass(input.readAllBytes(), remixedMapping, remapper);
                    if (classBytes != null) {
                        entry = new ZipEntry(remixedMapping.mapClass(entryName.replace(".class", "")) + ".class");
                        output.putNextEntry(entry);
                        output.write(classBytes);
                    }
                } else {
                    if (!entryName.contains(".RSA") && !entryName.contains(".SF")) {
                        output.putNextEntry(entry);
                        output.write(input.readAllBytes());
                    }
                }

                entry = input.getNextEntry();
            }
            input.close();
            output.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return mapping;
    }

    public static IMapping applyContextMapping(File inputJar, File outputJar, IMapping mapping) {
        return applyContextMapping(inputJar, outputJar, mapping, false);
    }

    public static IMapping applyContextMapping(File inputJar, File outputJar, IMapping mapping, boolean reverse) {
        if (reverse) mapping = mapping.reverse();

        GibberishRemapper remapper = new GibberishRemapper(mapping);

        try {
            FileInputStream stream = new FileInputStream(inputJar);
            byte[] bytes = stream.readAllBytes();
            stream.close();

            ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(bytes));
            ZipOutputStream output = new ZipOutputStream(new FileOutputStream(outputJar));
            ZipEntry entry = input.getNextEntry();
            while (entry != null) {
                String entryName = entry.getName();

                if (entryName.contains("module-info") || entryName.contains("package-info")) {
                    byte[] classBytes = transformClass(input.readAllBytes(), mapping, remapper);
                    if (classBytes != null) {
                        entry = new ZipEntry(mapping.mapClass(entryName.replace(".class", "")) + ".class");
                        output.putNextEntry(entry);
                        output.write(input.readAllBytes());
                    }
                } else if (entryName.endsWith(".class")) {
                    byte[] classBytes = transformClass(input.readAllBytes(), mapping, remapper);
                    if (classBytes != null) {
                        entry = new ZipEntry(mapping.mapClass(entryName.replace(".class", "")) + ".class");
                        output.putNextEntry(entry);
                        output.write(classBytes);
                    }
                } else {
                    if (!entryName.contains(".RSA") && !entryName.contains(".SF")) {
                        output.putNextEntry(entry);
                        output.write(input.readAllBytes());
                    }
                }

                entry = input.getNextEntry();
            }
            input.close();
            output.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return mapping;
    }

    private static IMapping remixMapping(Map<String, ClassPreprocessor> preprocessorMap, IMapping mapping) {
        Mapping remixedMapping = new Mapping();
        remixedMapping.merge(mapping);

        for (Map.Entry<String, ClassPreprocessor> entry : preprocessorMap.entrySet()) {
            for (ClassPreprocessor parent : allParents(preprocessorMap, entry.getValue())) {
                for (String method : parent.methods) {
                    String mapO = remixedMapping.mapMethod(entry.getKey(), method.split("~")[0], method.split("~")[1]);
                    if (!mapO.equals(entry.getKey() + "~" + method.split("~")[0] + "~" + method.split("~")[1])) {
                        continue;
                    }
                    String map = remixedMapping.mapMethod(parent.name, method.split("~")[0], method.split("~")[1]);

                    remixedMapping.putMethod(
                            entry.getKey(), method.split("~")[0], method.split("~")[1],
                            remixedMapping.mapClass(entry.getKey()), map.split("~")[1], map.split("~")[2]
                    );
                }
                for (String field : parent.fields) {
                    String mapO = remixedMapping.mapField(entry.getKey(), field.split("~")[0]);
                    if (!mapO.equals(entry.getKey() + "~" + field.split("~")[0])) continue;

                    String map = remixedMapping.mapField(parent.name, field.split("~")[0]);
                    remixedMapping.putField(
                            entry.getKey(), field.split("~")[0],
                            remixedMapping.mapClass(entry.getKey()), map.split("~")[1]
                    );
                }
            }
        }

        return remixedMapping;
    }

    private static List<ClassPreprocessor> allParents(Map<String, ClassPreprocessor> preprocessorMap, ClassPreprocessor entry) {
        List<ClassPreprocessor> a = new ArrayList<>();
        for (String parent : entry.parents) {
            ClassPreprocessor preprocessor = preprocessorMap.get(parent);
            if (preprocessor == null) continue;
            a.add(preprocessor);
            a.addAll(allParents(preprocessorMap, preprocessorMap.get(preprocessor.name)));
        }
        return a;
    }

    private static byte[] transformClass(byte[] bytes, IMapping mapping, GibberishRemapper remapper) throws IOException {
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);

        ClassRemapper classRemapper = new ClassRemapper(writer, remapper);
        remapper.className = reader.getClassName();
        remapper.reader = reader;

        reader.accept(classRemapper, 0);

        return fixSource(writer.toByteArray(), mapping);
    }

    private static byte[] fixSource(byte[] bytes, IMapping mapping) {
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);

        ClassSourceFixer classSourceFixer = new ClassSourceFixer(mapping, writer);
        classSourceFixer.className = reader.getClassName();

        reader.accept(classSourceFixer, 0);

        return writer.toByteArray();
    }

}
