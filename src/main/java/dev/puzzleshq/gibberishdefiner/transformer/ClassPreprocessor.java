package dev.puzzleshq.gibberishdefiner.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

public class ClassPreprocessor extends ClassVisitor {

    public final List<String> methods = new ArrayList<>();
    public final List<String> fields = new ArrayList<>();
    public final List<String> parents = new ArrayList<>();

    public String name;

    public ClassPreprocessor() {
        super(Opcodes.ASM9);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);

        this.name = name;

        if (!superName.equals("java/lang/Object")) parents.add(superName);
        if (interfaces != null && interfaces.length != 0) parents.addAll(List.of(interfaces));
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
//        if ((access & Opcodes.ACC_STATIC) == 0 && (access & Opcodes.ACC_PRIVATE) == 0)
            fields.add(name + "~" + descriptor);
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        methods.add(name + "~" + descriptor);
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

}
