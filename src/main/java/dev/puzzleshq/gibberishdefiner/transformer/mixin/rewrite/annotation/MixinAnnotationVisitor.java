package dev.puzzleshq.gibberishdefiner.transformer.mixin.rewrite.annotation;

import dev.puzzleshq.gibberishdefiner.transformer.GibberishRemapper;
import dev.puzzleshq.gibberishdefiner.transformer.mixin.rewrite.MixinRewriter;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Objects;

public class MixinAnnotationVisitor extends AnnotationVisitor {

    private final MixinRewriter rewriter;
    private final List<String> parents;

    public MixinAnnotationVisitor(AnnotationVisitor visitor, MixinRewriter rewriter, List<String> parents) {
        super(Opcodes.ASM9, visitor);
        this.rewriter = rewriter;
        this.parents = parents;
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        if (!(Objects.equals(name, "value") || Objects.equals(name, "targets"))) return super.visitArray(name);
        return new MixinAnnotationVisitor(super.visitArray(name), rewriter, parents);
    }

    @Override
    public void visit(String name, Object value) {
        if (name == null) {
            if (value instanceof Type) {
                parents.add(((Type) value).getInternalName());
            }

            if (value instanceof String) {
                String remapped = (String) value;
                parents.add(remapped);
                super.visit(name, remapped);
                return;
            }
        }

        super.visit(name, value);
    }
}
