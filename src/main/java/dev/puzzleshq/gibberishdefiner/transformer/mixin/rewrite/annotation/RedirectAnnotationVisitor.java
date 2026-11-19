package dev.puzzleshq.gibberishdefiner.transformer.mixin.rewrite.annotation;

import dev.puzzleshq.gibberishdefiner.transformer.GibberishRemapper;
import dev.puzzleshq.gibberishdefiner.transformer.mixin.rewrite.MixinMethodParser;
import dev.puzzleshq.gibberishdefiner.transformer.mixin.rewrite.MixinRewriter;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class RedirectAnnotationVisitor extends AnnotationVisitor {

    private final String arrayName;
    private final MixinRewriter rewriter;
    private final GibberishRemapper remapper;

    public RedirectAnnotationVisitor(AnnotationVisitor av, MixinRewriter rewriter, GibberishRemapper remapper) {
        super(Opcodes.ASM9, av);
        this.rewriter = rewriter;
        this.remapper = remapper;
        this.arrayName = null;
    }

    public RedirectAnnotationVisitor(AnnotationVisitor av, MixinRewriter rewriter, String arrayName, GibberishRemapper remapper) {
        super(Opcodes.ASM9, av);
        this.rewriter = rewriter;
        this.arrayName = arrayName;
        this.remapper = remapper;
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        return new RedirectAnnotationVisitor(super.visitArray(name), rewriter, name, remapper);
    }

    @Override
    public void visit(String name, Object value) {
        if (Objects.equals(arrayName, "method") && value instanceof String) {
            String methodString = (String) value;

            MixinMethodParser.ParsedMethod parsed = MixinMethodParser.parse(methodString);

            if (parsed.getOwner() != null && parsed.getDescriptor() != null) {
                super.visit(name, remapper.mapMethodName(parsed.getOwner(), parsed.getName(), parsed.getDescriptor()));
                return;
            }

            String own = parsed.getOwner();
            String desc = parsed.getDescriptor();
            String mName = parsed.getName();

            boolean found = false;
            if (own != null) {
                if (desc == null) {
                    for (Map.Entry<String, String> methodEntry : remapper.mapping.getMethodEntries()) {
                        String[] split = methodEntry.getKey().split("~");
                        if (!rewriter.parents.contains(remapper.map(own))) continue;
                        if (!split[1].equals(mName)) continue;
                        String out = remapper.mapping.mapMethod(split[0], mName, split[2]);
                        String[] newSplit = out.split("~");
                        super.visit(name, newSplit[1] + newSplit[2]);
                        found = true;
                    }
                    if (!found) super.visit(name, value);
                    return;
                }
                String out = remapper.mapping.mapMethod(own, mName, desc);
                String[] newSplit = out.split("~");
                super.visit(name, newSplit[1] + newSplit[2]);
                return;
            }
            if (desc == null) {
                for (Map.Entry<String, String> methodEntry : remapper.mapping.getMethodEntries()) {
                    String[] split = methodEntry.getKey().split("~");
                    if (!rewriter.parents.contains(remapper.map(split[0]))) continue;
                    if (!split[1].equals(mName)) continue;
                    String out = remapper.mapping.mapMethod(split[0], mName, split[2]);
                    String[] newSplit = out.split("~");
                    super.visit(name, newSplit[1] + newSplit[2]);
                    found = true;
                }
                if (!found) super.visit(name, value);
                return;
            }
            for (Map.Entry<String, String> methodEntry : remapper.mapping.getMethodEntries()) {
                String[] split = methodEntry.getKey().split("~");
                if (!rewriter.parents.contains(remapper.map(split[0]))) continue;
                if (!split[1].equals(mName)) continue;
                if (!split[2].equals(desc)) continue;
                String out = remapper.mapping.mapMethod(split[0], mName, desc);
                String[] newSplit = out.split("~");
                System.out.println(Arrays.toString(newSplit));
                super.visit(name, newSplit[1] + newSplit[2]);
                found = true;
            }
            if (!found) super.visit(name, value);
            return;
        }

        super.visit(name, value);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        return AnnotationVisitUtil.visitAnnotation(rewriter, descriptor, super.visitAnnotation(name, descriptor));
    }

}
