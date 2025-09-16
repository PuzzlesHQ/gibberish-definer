package dev.puzzleshq.gibberishdefiner.transformer;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.IMutableMapping;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class ClassSourceFixer extends ClassVisitor {

    IMapping mapping;
    public String className;

    public ClassSourceFixer(IMapping mapping, ClassVisitor visitor) {
        super(Opcodes.ASM9, visitor);

        this.mapping = mapping;
    }

    @Override
    public void visitEnd() {
        super.visitSource(mapping.mapSource(className), null);
        super.visitEnd();
    }
}
