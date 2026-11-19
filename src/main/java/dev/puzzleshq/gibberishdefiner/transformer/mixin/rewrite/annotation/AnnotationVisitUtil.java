package dev.puzzleshq.gibberishdefiner.transformer.mixin.rewrite.annotation;

import dev.puzzleshq.gibberishdefiner.transformer.mixin.rewrite.MixinRewriter;
import org.objectweb.asm.AnnotationVisitor;

public class AnnotationVisitUtil {

    public static AnnotationVisitor visitAnnotation(MixinRewriter rewriter, String descriptor, AnnotationVisitor av) {
        switch (descriptor) {
            case "Lorg/spongepowered/asm/mixin/injection/At;": return new AtAnnotationVisitor(av, rewriter, rewriter.remapper);
            case "Lorg/spongepowered/asm/mixin/injection/Inject;": return new InjectAnnotationVisitor(av, rewriter, rewriter.remapper);
            case "Lorg/spongepowered/asm/mixin/injection/Redirect;": return new RedirectAnnotationVisitor(av, rewriter, rewriter.remapper);
            case "Lorg/spongepowered/asm/mixin/Mixin;": return new MixinAnnotationVisitor(av, rewriter, rewriter.parents);
        }
        return av;
    }

}
