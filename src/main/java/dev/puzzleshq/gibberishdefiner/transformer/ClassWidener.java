package dev.puzzleshq.gibberishdefiner.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ClassWidener extends ClassVisitor {

    String className;

    public ClassWidener(ClassVisitor classVisitor) {
        super(Opcodes.ASM9, classVisitor);
    }


    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);

        this.className = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
//        if ((access & Opcodes.ACC_PRIVATE) != 0) access &= ~Opcodes.ACC_PRIVATE;
//        if ((access & Opcodes.ACC_PROTECTED) != 0) access &= ~Opcodes.ACC_PROTECTED;
//        access |= Opcodes.ACC_PUBLIC;

        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
//        if ((access & Opcodes.ACC_PRIVATE) != 0) access &= ~Opcodes.ACC_PRIVATE;
//        if ((access & Opcodes.ACC_PROTECTED) != 0) access &= ~Opcodes.ACC_PROTECTED;
//        access |= Opcodes.ACC_PUBLIC;

        return super.visitField(access, name, descriptor, signature, value);
    }
}
