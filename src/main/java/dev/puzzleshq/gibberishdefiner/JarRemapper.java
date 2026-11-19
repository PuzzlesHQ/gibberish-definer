package dev.puzzleshq.gibberishdefiner;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.impl.Mapping;
import dev.puzzleshq.gibberishdefiner.transformer.ClassPreprocessor;
import dev.puzzleshq.gibberishdefiner.transformer.ClassSourceFixer;
import dev.puzzleshq.gibberishdefiner.transformer.GibberishRemapper;
import dev.puzzleshq.gibberishdefiner.transformer.mixin.rewrite.MixinRewriter;
import dev.puzzleshq.gibberishdefiner.util.JavaUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class JarRemapper {

    public static IMapping createFinishedMapping(File inputJar, IMapping mapping) {
        return createFinishedMapping(inputJar, mapping, false);
    }

    public static IMapping createFinishedMapping(File inputJar, IMapping mapping, boolean reverse) {
        if (reverse) mapping = mapping.reverse();

        Map<String, ClassPreprocessor> preprocessorMap = new HashMap<>();

        try {
            FileInputStream stream = new FileInputStream(inputJar);
            byte[] bytes = JavaUtils.readAllBytes(stream);
            stream.close();
            ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(bytes));

            ZipEntry entry = input.getNextEntry();
            while (entry != null) {
                String entryName = entry.getName();

                if (entryName.endsWith(".class") && !(entryName.contains("module-info") || entryName.contains("package-info"))) {
//                    if (mapping.mapClass(entryName.replace(".class", "")).equals(entryName.replace(".class", ""))) {
//                        entry = input.getNextEntry();
//                        continue;
//                    }

                    ClassReader reader = new ClassReader(JavaUtils.readAllBytes(input));
                    ClassPreprocessor preprocessor = new ClassPreprocessor();

                    String className = reader.getClassName();
                    preprocessorMap.put(className, preprocessor);
                    reader.accept(preprocessor, 0);
                }

                entry = input.getNextEntry();
            }

            input.close();

            return remixMapping(preprocessorMap, mapping);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] applyMappingToClass(byte[] clazz, GibberishRemapper gibberishRemapper) throws IOException {
        return transformClass(clazz, gibberishRemapper.mapping, gibberishRemapper);
    }

    public static IMapping applyBareMapping(File inputJar, File outputJar, IMapping mapping) {
        return applyBareMapping(inputJar, outputJar, mapping, false);
    }

    public static IMapping applyBareMapping(File inputJar, File outputJar, IMapping mapping, boolean reverse) {
        if (reverse) mapping = mapping.reverse();

        Map<String, ClassPreprocessor> preprocessorMap = new HashMap<>();

        try {
            FileInputStream stream = new FileInputStream(inputJar);
            byte[] bytes = JavaUtils.readAllBytes(stream);
            stream.close();
            ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(bytes));

            ZipEntry entry = input.getNextEntry();
            while (entry != null) {
                String entryName = entry.getName();

                if (entryName.contains("module-info") || entryName.contains("package-info")) {}
                else if (entryName.endsWith(".class")) {
                    ClassReader reader = new ClassReader(JavaUtils.readAllBytes(input));
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
            ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(outputJar.toPath()));
            entry = input.getNextEntry();
            while (entry != null) {
                String entryName = entry.getName();

                if (entryName.contains("module-info") || entryName.contains("package-info")) {
                    byte[] classBytes = transformClass(JavaUtils.readAllBytes(input), remixedMapping, remapper);
                    if (classBytes != null) {
                        entry = new ZipEntry(remixedMapping.mapClass(entryName.replace(".class", "")) + ".class");
                        output.putNextEntry(entry);
                        output.write(JavaUtils.readAllBytes(input));
                    }
                } else if (entryName.endsWith(".class")) {
                    byte[] classBytes = transformClass(JavaUtils.readAllBytes(input), remixedMapping, remapper);
                    if (classBytes != null) {
                        entry = new ZipEntry(remixedMapping.mapClass(entryName.replace(".class", "")) + ".class");
                        output.putNextEntry(entry);
                        output.write(classBytes);
                    }
                } else {
                    if (!entryName.contains(".RSA") && !entryName.contains(".SF")) {
                        output.putNextEntry(entry);
                        output.write(JavaUtils.readAllBytes(input));
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

    public static IMapping applyMapping(File inputJar, File outputJar, IMapping mapping) {
        return applyMapping(inputJar, outputJar, mapping, false);
    }

    public static IMapping applyMapping(File inputJar, File outputJar, IMapping mapping, boolean reverse) {
        if (reverse) mapping = mapping.reverse();

        GibberishRemapper remapper = new GibberishRemapper(mapping);

        try {
            FileInputStream stream = new FileInputStream(inputJar);
            byte[] bytes = JavaUtils.readAllBytes(stream);
            stream.close();

            ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(bytes));
            ZipOutputStream output = new ZipOutputStream(new FileOutputStream(outputJar));
            ZipEntry entry = input.getNextEntry();
            while (entry != null) {
                String entryName = entry.getName();

                if (entryName.contains("module-info") || entryName.contains("package-info")) {
                    byte[] classBytes = transformClass(JavaUtils.readAllBytes(input), mapping, remapper);
                    if (classBytes != null) {
                        entry = new ZipEntry(mapping.mapClass(entryName.replace(".class", "")) + ".class");
                        output.putNextEntry(entry);
                        output.write(JavaUtils.readAllBytes(input));
                    }
                } else if (entryName.endsWith(".class")) {
                    byte[] classBytes = transformClass(JavaUtils.readAllBytes(input), mapping, remapper);
                    if (classBytes != null) {
                        entry = new ZipEntry(mapping.mapClass(entryName.replace(".class", "")) + ".class");
                        output.putNextEntry(entry);
                        output.write(classBytes);
                    }
                } else {
                    if (!entryName.contains(".RSA") && !entryName.contains(".SF")) {
                        output.putNextEntry(entry);
                        output.write(JavaUtils.readAllBytes(input));
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

    public static List<ClassPreprocessor> getTopologicallySortedClasses(Map<String, ClassPreprocessor> preprocessorMap) {
        List<ClassPreprocessor> sorted = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        for (ClassPreprocessor preprocessor : preprocessorMap.values()) {
            sortNode(preprocessor, preprocessorMap, sorted, visited);
        }

        return sorted;
    }

    private static void sortNode(ClassPreprocessor current, Map<String, ClassPreprocessor> map, List<ClassPreprocessor> sorted, Set<String> visited) {
        if (current == null || visited.contains(current.name)) return;

        visited.add(current.name);

        for (String parentName : current.parents) {
            ClassPreprocessor parent = map.get(parentName);
            if (parent != null) {
                sortNode(parent, map, sorted, visited);
            }
        }

        sorted.add(current);
    }
    
    private static IMapping remixMapping(Map<String, ClassPreprocessor> preprocessorMap, IMapping mapping) {
        Mapping remixedMapping = new Mapping();
        remixedMapping.merge(mapping);

        List<ClassPreprocessor> sortedClasses = getTopologicallySortedClasses(preprocessorMap);

        for (ClassPreprocessor entry : sortedClasses) {
            for (ClassPreprocessor parent : allParents(mapping, preprocessorMap, entry)) {
                for (String method : entry.methods) {
                    String mapO = remixedMapping.mapMethod(entry.name, method.split("~")[0], method.split("~")[1]);
                    if (!mapO.equals(entry.name + "~" + method.split("~")[0] + "~" + method.split("~")[1])) {
                        continue;
                    }
                    String map = remixedMapping.mapMethod(parent.name, method.split("~")[0], method.split("~")[1]);

                    remixedMapping.putMethod(
                            entry.name, method.split("~")[0], method.split("~")[1],
                            remixedMapping.mapClass(entry.name), map.split("~")[1], map.split("~")[2]
                    );
                }
                for (String field : entry.fields) {
                    String mapO = remixedMapping.mapField(entry.name, field.split("~")[0]);
                    if (!mapO.equals(entry.name + "~" + field.split("~")[0])) continue;

                    String map = remixedMapping.mapField(parent.name, field.split("~")[0]);
                    remixedMapping.putField(
                            entry.name, field.split("~")[0],
                            remixedMapping.mapClass(entry.name), map.split("~")[1]
                    );
                }
                for (String method : parent.methods) {
                    String mapO = remixedMapping.mapMethod(entry.name, method.split("~")[0], method.split("~")[1]);
                    if (!mapO.equals(entry.name + "~" + method.split("~")[0] + "~" + method.split("~")[1])) {
                        continue;
                    }
                    String map = remixedMapping.mapMethod(parent.name, method.split("~")[0], method.split("~")[1]);

                    remixedMapping.putMethod(
                            entry.name, method.split("~")[0], method.split("~")[1],
                            remixedMapping.mapClass(entry.name), map.split("~")[1], map.split("~")[2]
                    );
                }
                for (String field : parent.fields) {
                    String mapO = remixedMapping.mapField(entry.name, field.split("~")[0]);
                    if (!mapO.equals(entry.name + "~" + field.split("~")[0])) continue;

                    String map = remixedMapping.mapField(parent.name, field.split("~")[0]);
                    remixedMapping.putField(
                            entry.name, field.split("~")[0],
                            remixedMapping.mapClass(entry.name), map.split("~")[1]
                    );
                }
            }
        }

        return remixedMapping;
    }

    private static List<ClassPreprocessor> allParents(IMapping mapping, Map<String, ClassPreprocessor> preprocessorMap, ClassPreprocessor entry) {
        return allParents(mapping, preprocessorMap, entry, new HashSet<>());
    }

    private static List<ClassPreprocessor> allParents(IMapping mapping, Map<String, ClassPreprocessor> preprocessorMap, ClassPreprocessor entry, Set<String> visited) {
        List<ClassPreprocessor> a = new ArrayList<>();
        if (entry == null || !visited.add(entry.name)) return a;

        for (String parentName : entry.parents) {
            ClassPreprocessor parent = preprocessorMap.get(parentName);
            if (parent == null) {
                if (!mapping.hasClass(parentName)) continue;
                ClassPreprocessor proc = new ClassPreprocessor();
                for (Map.Entry<String, String> fieldEntry : mapping.getFieldEntries()) {
                    String[] split = fieldEntry.getKey().split("~");
                    if (!Objects.equals(split[0], parentName)) continue;
                    proc.fields.add(split[1]);
                }
                for (Map.Entry<String, String> methodEntry : mapping.getMethodEntries()) {
                    String[] split = methodEntry.getKey().split("~");
                    if (!Objects.equals(split[0], parentName)) continue;
                    StringBuilder newThing = new StringBuilder();
                    for (int i = 1; i < split.length; i++) {
                        newThing.append(split[i]);
                        if (i < split.length - 1) {
                            newThing.append("~");
                        }
                    }
                    proc.methods.add(newThing.toString());
                }
                proc.name = parentName;
                a.add(proc);

                continue;
            }
            a.add(parent);
            a.addAll(allParents(mapping, preprocessorMap, parent, visited));
        }
        return a;
    }

    private static int makePublic(int access) {
        if ((access & Opcodes.ACC_PRIVATE) != 0) return access; // leave private alone
        return (access & ~(Opcodes.ACC_PROTECTED)) | Opcodes.ACC_PUBLIC;
    }

    private static byte[] transformClass(byte[] bytes, IMapping mapping, GibberishRemapper remapper) throws IOException {
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(reader, 0);

        ClassNode node = new ClassNode();
        ClassRemapper classRemapper = new ClassRemapper(node, remapper);
        remapper.className = reader.getClassName();
        remapper.reader = reader;

        boolean isMixin = false;

        reader.accept(classRemapper, 0);

        if (node.invisibleAnnotations != null) {
            for (AnnotationNode invisibleTypeAnnotation : node.invisibleAnnotations) {
                if ("Lorg/spongepowered/asm/mixin/Mixin;".equals(invisibleTypeAnnotation.desc)) {
                    isMixin = true;
                    break;
                }
            }
        }

        node.access = makePublic(node.access);

        for (InnerClassNode inner : node.innerClasses) {
            inner.access = makePublic(inner.access);
        }
        for (FieldNode field : node.fields) {
            field.access = makePublic(field.access);
        }
        for (MethodNode method : node.methods) {
            method.access = makePublic(method.access);
        }

        if (isMixin) {
            MixinRewriter rewriter = new MixinRewriter(remapper, writer);
            node.accept(rewriter);
            System.out.println(rewriter.parents);
        } else {
            node.accept(writer);
        }

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
