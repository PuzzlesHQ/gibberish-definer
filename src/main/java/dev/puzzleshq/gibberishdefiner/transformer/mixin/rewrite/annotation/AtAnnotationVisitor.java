package dev.puzzleshq.gibberishdefiner.transformer.mixin.rewrite.annotation;

import dev.puzzleshq.gibberishdefiner.transformer.GibberishRemapper;
import dev.puzzleshq.gibberishdefiner.transformer.mixin.rewrite.MixinMethodParser;
import dev.puzzleshq.gibberishdefiner.transformer.mixin.rewrite.MixinRewriter;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;
import java.util.Objects;

public class AtAnnotationVisitor extends AnnotationVisitor {

    private final MixinRewriter rewriter;
    private final GibberishRemapper remapper;

    public AtAnnotationVisitor(AnnotationVisitor av, MixinRewriter rewriter, GibberishRemapper remapper) {
        super(Opcodes.ASM9, av);
        this.rewriter = rewriter;
        this.remapper = remapper;
    }

    @Override
    public void visit(String name, Object value) {
        if (Objects.equals(name, "target") && value instanceof String) {
            MixinMethodParser.ParsedMethod parsed = MixinMethodParser.parse((String) value);

            String own = parsed.getOwner();
            String mName = parsed.getName();
            String desc = parsed.getDescriptor();

            if (desc == null) {
                boolean found = false;
                for (Map.Entry<String, String> methodEntry : remapper.mapping.getMethodEntries()) {
                    String[] split = methodEntry.getKey().split("~");
                    if (!rewriter.parents.contains(remapper.map(own))) continue;
                    if (!split[1].equals(mName)) continue;
                    String out = remapper.mapping.mapMethod(split[0], mName, split[2]);
                    String[] newSplit = out.split("~");
                    super.visit(name, "L" + newSplit[0] + ";" + newSplit[1] + newSplit[2]);
                    found = true;
                }
                if (!found) super.visit(name, value);
                return;
            }
            String out = remapper.mapping.mapMethod(parsed.getOwner(), parsed.getName(), parsed.getDescriptor());
            String[] newSplit = out.split("~");
            super.visit(name, "L" + newSplit[0] + ";" + newSplit[1] + newSplit[2]);
            return;
        }

        super.visit(name, value);
    }
}
