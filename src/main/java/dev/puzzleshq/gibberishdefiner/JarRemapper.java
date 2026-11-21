package dev.puzzleshq.gibberishdefiner;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.impl.Mapping;
import dev.puzzleshq.gibberishdefiner.transformer.ClassPreprocessor;
import dev.puzzleshq.gibberishdefiner.transformer.ClassSourceFixer;
import dev.puzzleshq.gibberishdefiner.transformer.GibberishClassRemapper;
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

        // Parents that live outside the jar but have entries in the mapping, indexed once up front.
        Map<String, ClassPreprocessor> libraryParents = buildLibraryParents(preprocessorMap, mapping);

        List<ClassPreprocessor> sortedClasses = getTopologicallySortedClasses(preprocessorMap);

        for (ClassPreprocessor entry : sortedClasses) {
            String mappedEntry = remixedMapping.mapClass(entry.name);

            for (ClassPreprocessor parent : allParents(preprocessorMap, libraryParents, entry)) {
                // members the class declares itself (overrides / hidden fields) pick up the parent's name
                for (String method : entry.methods) inheritMethod(remixedMapping, entry.name, mappedEntry, parent.name, method);
                for (String field : entry.fields) inheritField(remixedMapping, entry.name, mappedEntry, parent.name, field);

                // members only the parent declares, but which can be referenced through this class
                for (String method : parent.methods) inheritMethod(remixedMapping, entry.name, mappedEntry, parent.name, method);
                for (String field : parent.fields) inheritField(remixedMapping, entry.name, mappedEntry, parent.name, field);
            }
        }

        return remixedMapping;
    }

    /** member is "name~descriptor". Only writes an entry when the parent's member is actually mapped. */
    private static void inheritMethod(Mapping remixed, String entryName, String mappedEntry, String parentName, String member) {
        int sep = member.indexOf('~');
        if (sep <= 0) return;

        String name = member.substring(0, sep);
        String desc = member.substring(sep + 1);
        if (name.charAt(0) == '<') return; // <init> / <clinit> are never inherited or renamed

        // the class already has its own (or a closer parent's) mapping for this method
        if (!remixed.mapMethod(entryName, name, desc).equals(entryName + "~" + name + "~" + desc)) return;

        String mapped = remixed.mapMethod(parentName, name, desc);
        // unmapped in the parent: there is nothing to inherit, don't write an identity entry
        if (mapped.equals(parentName + "~" + name + "~" + desc)) return;

        String[] split = mapped.split("~");
        remixed.putMethod(entryName, name, desc, mappedEntry, split[1], split[2]);
    }

    /** member is "name" or "name~descriptor". Only writes an entry when the parent's field is actually mapped. */
    private static void inheritField(Mapping remixed, String entryName, String mappedEntry, String parentName, String member) {
        int sep = member.indexOf('~');
        String name = sep < 0 ? member : member.substring(0, sep);

        if (!remixed.mapField(entryName, name).equals(entryName + "~" + name)) return;

        String mapped = remixed.mapField(parentName, name);
        if (mapped.equals(parentName + "~" + name)) return;

        String[] split = mapped.split("~");
        remixed.putField(entryName, name, mappedEntry, split[1]);
    }

    /**
     * Builds one stand-in ClassPreprocessor per parent that is not in the jar but is known to the mapping,
     * filling their members from a single pass over the mapping (instead of one pass per class per parent).
     */
    private static Map<String, ClassPreprocessor> buildLibraryParents(Map<String, ClassPreprocessor> preprocessorMap, IMapping mapping) {
        Map<String, ClassPreprocessor> libraryParents = new HashMap<>();

        for (ClassPreprocessor processor : preprocessorMap.values()) {
            for (String parentName : processor.parents) {
                if (parentName == null) continue;
                if (preprocessorMap.containsKey(parentName) || libraryParents.containsKey(parentName)) continue;
                if (!mapping.hasClass(parentName)) continue;

                ClassPreprocessor proxy = new ClassPreprocessor();
                proxy.name = parentName;
                libraryParents.put(parentName, proxy);
            }
        }

        if (libraryParents.isEmpty()) return libraryParents;

        for (Map.Entry<String, String> fieldEntry : mapping.getFieldEntries()) {
            String key = fieldEntry.getKey();
            int sep = key.indexOf('~');
            if (sep < 0) continue;
            ClassPreprocessor proxy = libraryParents.get(key.substring(0, sep));
            if (proxy != null) proxy.fields.add(key.substring(sep + 1));
        }

        for (Map.Entry<String, String> methodEntry : mapping.getMethodEntries()) {
            String key = methodEntry.getKey();
            int sep = key.indexOf('~');
            if (sep < 0) continue;
            ClassPreprocessor proxy = libraryParents.get(key.substring(0, sep));
            if (proxy != null) proxy.methods.add(key.substring(sep + 1));
        }

        return libraryParents;
    }

    private static List<ClassPreprocessor> allParents(Map<String, ClassPreprocessor> preprocessorMap, Map<String, ClassPreprocessor> libraryParents, ClassPreprocessor entry) {
        return allParents(preprocessorMap, libraryParents, entry, new HashSet<>());
    }

    private static List<ClassPreprocessor> allParents(Map<String, ClassPreprocessor> preprocessorMap, Map<String, ClassPreprocessor> libraryParents, ClassPreprocessor entry, Set<String> visited) {
        List<ClassPreprocessor> a = new ArrayList<>();
        if (entry == null || !visited.add(entry.name)) return a;

        for (String parentName : entry.parents) {
            ClassPreprocessor parent = preprocessorMap.get(parentName);
            if (parent == null) {
                ClassPreprocessor library = libraryParents.get(parentName);
                if (library != null) a.add(library);
                continue;
            }
            a.add(parent);
            a.addAll(allParents(preprocessorMap, libraryParents, parent, visited));
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
        ClassRemapper classRemapper = new GibberishClassRemapper(node, remapper);
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
