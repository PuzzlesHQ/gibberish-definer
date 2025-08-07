package dev.puzzleshq.gibberishdefiner.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashSet;
import java.util.Set;

public class ClassDeduper extends ClassVisitor {

    String className;

    public ClassDeduper(ClassVisitor classVisitor) {
        super(Opcodes.ASM9, classVisitor);
    }

    Set<String> methods = new HashSet<>();
    Set<String> fields = new HashSet<>();

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);

        this.className = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
//        if (this.className.contains("ConfirmLinkScreen")) {
//            System.err.println(name + " " + descriptor);
//
//        }
        if (methods.contains(name + "~" + descriptor)) System.err.println(this.className + " " + name + " " + descriptor);
        methods.add(name + "~" + descriptor);
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
//        if (fields.contains(name + "~" + descriptor)) return null;
//        fields.add(name + "~" + descriptor);
        return super.visitField(access, name, descriptor, signature, value);
    }
}
