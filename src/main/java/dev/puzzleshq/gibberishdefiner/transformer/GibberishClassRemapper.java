package dev.puzzleshq.gibberishdefiner.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.MethodRemapper;

public class GibberishClassRemapper extends ClassRemapper {

    private final GibberishRemapper remapper;

    public GibberishClassRemapper(ClassVisitor classVisitor, GibberishRemapper remapper) {
        super(classVisitor, remapper);
        this.remapper = remapper;
    }

    @Override
    protected MethodVisitor createMethodRemapper(MethodVisitor methodVisitor) {
        return new MethodRemapper(api, methodVisitor, remapper) {
            public Object[] remapValues(Object[] args) {
                Object[] mappedArgs = new Object[args.length];
                for (int i = 0; i < args.length; i++) {
                    mappedArgs[i] = remapper.mapValue(args[i]);
                }
                return mappedArgs;
            }

            @Override
            public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
                if (bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory")) {
                    // the functional interface is the return type of the indy descriptor
                    String iface = Type.getReturnType(desc).getInternalName();
                    // the interface method's descriptor is the first bootstrap argument
                    String samDesc = ((Type) bsmArgs[0]).getDescriptor();
                    name = remapper.mapMethodName(iface, name, samDesc);
                }
                super.visitInvokeDynamicInsn(
                        name,
                        remapper.mapMethodDesc(desc),
                        (Handle) remapper.mapValue(bsm),
                        remapValues(bsmArgs)
                );
            }
        };
    }
}