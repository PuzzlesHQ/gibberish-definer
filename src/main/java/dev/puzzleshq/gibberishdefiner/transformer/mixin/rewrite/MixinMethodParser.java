package dev.puzzleshq.gibberishdefiner.transformer.mixin.rewrite;

public class MixinMethodParser {

    public static ParsedMethod parse(String methodRef) {
        if (methodRef == null || methodRef.isEmpty()) {
            return null;
        }

        String owner = null;
        String name;
        String descriptor = null;

        // 1. Check if it's a fully qualified reference containing a class owner (e.g. "Lnet/minecraft/client/Minecraft;runTick()V" or "net/minecraft/client/Minecraft.runTick(...)")
        if (methodRef.startsWith("L")) {
            // JVM internal descriptor format: Lowner;name+descriptor
            int semicolonIdx = methodRef.indexOf(';');
            owner = methodRef.substring(1, semicolonIdx);
            methodRef = methodRef.substring(semicolonIdx + 1);
        } else if (methodRef.contains(".")) {
            int lastDot = methodRef.lastIndexOf('.');
            owner = methodRef.substring(0, lastDot).replace('.', '/');
            methodRef = methodRef.substring(lastDot + 1);
        }

        // 2. Separate method name from descriptor if present (e.g. "runTick()V" or "runTick(I)V")
        int parenIdx = methodRef.indexOf('(');
        if (parenIdx != -1) {
            name = methodRef.substring(0, parenIdx);
            descriptor = methodRef.substring(parenIdx);
        } else {
            name = methodRef;
        }

        return new ParsedMethod(owner, name, descriptor);
    }

    public static class ParsedMethod {
        private final String owner;
        private final String name;
        private final String descriptor;

        public ParsedMethod(String owner, String name, String descriptor) {
            this.owner = owner;
            this.name = name;
            this.descriptor = descriptor;
        }

        public String getOwner() { return owner; }
        public String getName() { return name; }
        public String getDescriptor() { return descriptor; }
    }
}