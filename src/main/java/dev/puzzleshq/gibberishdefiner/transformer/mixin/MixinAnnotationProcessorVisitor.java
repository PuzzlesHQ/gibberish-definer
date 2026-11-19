package dev.puzzleshq.gibberishdefiner.transformer.mixin;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Objects;

public class MixinAnnotationProcessorVisitor extends AnnotationVisitor {

    private final List<String> parents;

    public MixinAnnotationProcessorVisitor(List<String> parents) {
        super(Opcodes.ASM9);
        this.parents = parents;
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        if (!(Objects.equals(name, "value") || Objects.equals(name, "targets"))) return super.visitArray(name);
        return new MixinAnnotationProcessorVisitor(parents);
    }

    @Override
    public void visit(String name, Object value) {
        if (name == null) {
            if (value instanceof Type) {
                parents.add(((Type) value).getInternalName());
            }

            if (value instanceof String) {
                parents.add((String) value);
            }
        }

        super.visit(name, value);
    }
}
