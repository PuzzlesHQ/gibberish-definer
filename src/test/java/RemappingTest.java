import dev.puzzleshq.gibberishdefiner.GibberishDefiner;
import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.IMergeableMapping;
import dev.puzzleshq.gibberishdefiner.mapping.impl.Mapping;
import dev.puzzleshq.gibberishdefiner.transformer.ClassDeduper;
import dev.puzzleshq.gibberishdefiner.transformer.ClassPreprocessor;
import dev.puzzleshq.gibberishdefiner.transformer.ClassSourceFixer;
import dev.puzzleshq.gibberishdefiner.transformer.GibberishRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class RemappingTest {

    public static void main(String[] args) throws IOException {
//        File f = new File("test/deobfuscated.jar");
        File f = new File("test/1.21.1obf.jar");
        File o = new File("test/1.21.1debof.jar");
//        File o = new File("test/1.21.1reobf.jar");
        o.createNewFile();
        RemappingTest.transform(f, o, getMapping());
//        RemappingTest.transform(f, o, getMapping().reverse());
    }

    public static IMapping getMapping() throws IOException {
        GibberishDefiner.initDefaultFormats();

        InputStream stream = Main.class.getResourceAsStream("mapping.txt");
        byte[] bytes = stream.readAllBytes();
        stream.close();

        IMergeableMapping mapping = (IMergeableMapping) GibberishDefiner.getFormat("proguard").assemble(new Mapping(), new String(bytes));
        mapping = (IMergeableMapping) mapping.reverse();

//        for (Map.Entry<String, String> e : mapping.getClassEntries()) {
//            System.out.println(e.getKey() + " | " + e.getValue());
//        }
//        System.out.println("----------------------------------------------");
//        for (Map.Entry<String, String> e : mapping.getFieldEntries()) {
//            System.out.println(e.getKey() + " | " + e.getValue());
//        }
//        System.out.println("----------------------------------------------");

        return mapping;
    }

    public static void transform(File in, File out, IMapping mapping) throws IOException {
        GibberishDefiner.initDefaultFormats();
        GibberishRemapper remapper = new GibberishRemapper(mapping);

        Map<String, ClassPreprocessor> preprocessorMap = new HashMap<>();

        try {
            FileInputStream stream = new FileInputStream(in);
            byte[] bytes = stream.readAllBytes();
            stream.close();
            ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(bytes));

            ZipEntry entry = input.getNextEntry();
            while (entry != null) {
                String entryName = entry.getName();

                if (entryName.contains("module-info") || entryName.contains("package-info")) {
                } else if (entryName.endsWith(".class")) {
                    ClassReader reader = new ClassReader(input.readAllBytes());
                    ClassPreprocessor preprocessor = new ClassPreprocessor();

                    preprocessorMap.put(reader.getClassName(), preprocessor);
                    reader.accept(preprocessor, 0);
                }

                entry = input.getNextEntry();
            }

            input.close();

            mapping = remixMapping(preprocessorMap, mapping);

            input = new ZipInputStream(new ByteArrayInputStream(bytes));
            ZipOutputStream output = new ZipOutputStream(new FileOutputStream(out));
            entry = input.getNextEntry();
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
    }

    private static IMapping remixMapping(Map<String, ClassPreprocessor> preprocessorMap, IMapping mapping) {
        Mapping mapping1 = null;
        if (mapping instanceof Mapping) mapping1 = (Mapping) mapping;

        for (Map.Entry<String, ClassPreprocessor> entry : preprocessorMap.entrySet()) {
            for (ClassPreprocessor parent : allParents(preprocessorMap, entry.getValue())) {
                for (String method : parent.methods) {
                    String mapO = mapping.mapMethod(entry.getKey(), method.split("~")[0], method.split("~")[1]);
                    if (!mapO.equals(entry.getKey() + "~" + method.split("~")[0] + "~" + method.split("~")[1])) continue;

                    String map = mapping.mapMethod(parent.name, method.split("~")[0], method.split("~")[1]);
                    mapping1.putMethod(
                            entry.getKey(), method.split("~")[0], method.split("~")[1],
                            mapping.mapClass(entry.getKey()), map.split("~")[1], map.split("~")[2]
                    );
                }
                for (String field : parent.fields) {
                    String mapO = mapping.mapField(entry.getKey(), field.split("~")[0], field.split("~")[1]);
                    if (!mapO.equals(entry.getKey() + "~" + field.split("~")[0] + "~" + field.split("~")[1])) continue;

                    String map = mapping.mapField(parent.name, field.split("~")[0], field.split("~")[1]);
                    mapping1.putField(
                            entry.getKey(), field.split("~")[0], field.split("~")[1],
                            mapping.mapClass(entry.getKey()), map.split("~")[1], map.split("~")[2]
                    );
                }
            }
        }
        return mapping;
    }

    private static List<ClassPreprocessor> allParents(Map<String, ClassPreprocessor> preprocessorMap, ClassPreprocessor entry) {
        List<ClassPreprocessor> a = new ArrayList<>();
        for (String parent : entry.parents) {
            ClassPreprocessor preprocessor = preprocessorMap.get(parent);
            if (preprocessor == null) continue;
            a.addAll(allParents(preprocessorMap, preprocessorMap.get(preprocessor.name)));
            a.add(preprocessor);
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

        return fixSource(bytes, writer.toByteArray(), mapping);
    }

    private static byte[] fixSource(byte[] old, byte[] bytes, IMapping mapping) throws IOException {
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);

        ClassSourceFixer classSourceFixer = new ClassSourceFixer(mapping, writer);
        classSourceFixer.className = reader.getClassName();

        reader.accept(classSourceFixer, 0);

        return deduper(old, writer.toByteArray(), mapping);
    }

    private static byte[] deduper(byte[] ud, byte[] bytes, IMapping mapping) throws IOException {
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode(Opcodes.ASM9);
        ClassReader reader2 = new ClassReader(ud);
        ClassNode node2 = new ClassNode(Opcodes.ASM9);

        reader.accept(node, 0);
        reader2.accept(node2, 0);

//        if (node.name.contains("ConfirmLinkScreen")) {
//            for (MethodNode method : node.methods) {
//                System.err.println(node.name + " " + method.name + "~" + method.desc + " " + method.instructions.size());
//            }
//            for (MethodNode method : node2.methods) {
//                System.err.println(getMapping().mapMethod(node2.name, method.name, method.desc));
//                System.err.println(node2.name + " " + method.name + "~" + method.desc + " " + method.instructions.size());
//            }
//        }

//        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
//
//        ClassDeduper classDeduper = new ClassDeduper(writer);
//
//        reader.accept(classDeduper, ClassReader.EXPAND_FRAMES);

        return bytes;
//        return writer.toByteArray();
    }

}
