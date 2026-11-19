package dev.puzzleshq.gibberishdefiner.transformer;

import dev.puzzleshq.gibberishdefiner.transformer.mixin.MixinAnnotationProcessorVisitor;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassPreprocessor extends ClassVisitor {

    public final List<String> methods = new ArrayList<>();
    public final List<String> fields = new ArrayList<>();
    public final List<String> parents = new ArrayList<>();

    public boolean isMixin = false;

    public String name;

    public ClassPreprocessor() {
        super(Opcodes.ASM9);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        if ("Lorg/spongepowered/asm/mixin/Mixin;".equals(descriptor)) {
            isMixin = true;
        }
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);

        this.name = name;

        if (interfaces != null && interfaces.length != 0) parents.addAll(Arrays.asList(interfaces));
        parents.add(superName);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if ((access & Opcodes.ACC_STATIC) == 0 || (access & Opcodes.ACC_PRIVATE) == 0)
            fields.add(name + "~" + descriptor);
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if ((access & Opcodes.ACC_STATIC) == 0 || (access & Opcodes.ACC_PRIVATE) == 0)
            methods.add(name + "~" + descriptor);
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (descriptor.equals("Lorg/spongepowered/asm/mixin/Mixin;")) {
            return new MixinAnnotationProcessorVisitor(parents);
        }
        return super.visitAnnotation(descriptor, visible);
    }

}
