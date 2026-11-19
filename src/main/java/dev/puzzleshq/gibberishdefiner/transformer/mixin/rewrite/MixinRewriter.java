package dev.puzzleshq.gibberishdefiner.transformer.mixin.rewrite;

import dev.puzzleshq.gibberishdefiner.transformer.GibberishRemapper;
import dev.puzzleshq.gibberishdefiner.transformer.mixin.rewrite.annotation.AnnotationVisitUtil;
import dev.puzzleshq.gibberishdefiner.transformer.mixin.rewrite.annotation.MixinAnnotationVisitor;
import dev.puzzleshq.gibberishdefiner.transformer.mixin.rewrite.annotation.MixinMethodVisitor;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MixinRewriter extends ClassVisitor {

    public final List<String> parents = new ArrayList<>();
    public final GibberishRemapper remapper;

    public MixinRewriter(GibberishRemapper remapper, ClassVisitor writer) {
        super(Opcodes.ASM9, writer);
        this.remapper = remapper;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        System.out.println("rewriting mixin '" + name + "'");
        if (interfaces != null && interfaces.length != 0) {
            parents.addAll(Arrays.asList(interfaces));
        }
        if (superName != null) {
            parents.add(superName);
        }

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return new MixinMethodVisitor(super.visitMethod(access, name, descriptor, signature, exceptions), remapper, this);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return AnnotationVisitUtil.visitAnnotation(this, descriptor, super.visitAnnotation(descriptor, visible));
    }

}
