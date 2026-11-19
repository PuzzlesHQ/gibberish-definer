package dev.puzzleshq.gibberishdefiner.transformer.mixin.rewrite.annotation;

import dev.puzzleshq.gibberishdefiner.transformer.GibberishRemapper;
import dev.puzzleshq.gibberishdefiner.transformer.mixin.rewrite.MixinRewriter;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MixinMethodVisitor extends MethodVisitor {

    private final GibberishRemapper remapper;
    private MixinRewriter rewriter;

    public MixinMethodVisitor(MethodVisitor methodVisitor, GibberishRemapper remapper, MixinRewriter rewriter) {
        super(Opcodes.ASM9, methodVisitor);
        this.remapper = remapper;
        this.rewriter = rewriter;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return AnnotationVisitUtil.visitAnnotation(rewriter, descriptor, super.visitAnnotation(descriptor, visible));
    }
}
