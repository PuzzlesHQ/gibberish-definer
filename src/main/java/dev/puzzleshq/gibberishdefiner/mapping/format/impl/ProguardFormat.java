package dev.puzzleshq.gibberishdefiner.mapping.format.impl;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.IMutableMapping;
import dev.puzzleshq.gibberishdefiner.mapping.format.IMappingFormat;
import dev.puzzleshq.gibberishdefiner.util.MappingUtil;

import java.util.*;
import java.util.regex.Pattern;

public class ProguardFormat implements IMappingFormat {

    private static final Pattern SPLITTER = Pattern.compile(" -> ");
    private static final Pattern FIELD_PATTERN = Pattern.compile("[0-9a-zA-Z_$.-]+ [0-9a-zA-Z_$]+ -> [0-9a-zA-Z_$]+");
    private static final Pattern METHOD_COLON_PATTERN = Pattern.compile("[0-9]+:[0-9]+:");

    @Override
    public IMapping assemble(final IMutableMapping mapping, final String contents) {
        String[] lines = contents.replace("\r", "").split("\n");

        int lineIndex = 1;
        boolean isTrackingClass = false;
        ProguardClass trackingClass = null;
        Queue<ProguardClass> foundClasses = new LinkedList<>();

        String currentLine;
        for (;;) {
            if (!isTrackingClass) {
                String classDefinition = lines[lineIndex++].replace(":", "");
                String classSourceDefinition = lines[lineIndex++];
                String sourceName = classSourceDefinition.replace("# {\"fileName\":\"", "").replace("\",\"id\":\"sourceFile\"}", "");

                String[] splitName = ProguardFormat.SPLITTER.split(classDefinition);
                trackingClass = new ProguardClass(splitName[0], splitName[1], sourceName);
                mapping.put(splitName[0], splitName[1]);
                foundClasses.add(trackingClass);

                isTrackingClass = lineIndex != lines.length && lines[lineIndex].startsWith("    ");
                if (isTrackingClass) trackingClass.startLine = lineIndex;

                continue;
            }
            currentLine = lines[lineIndex];
            if (currentLine.startsWith("    ")) {
                trackingClass.endLine = ++lineIndex;
                if (lineIndex == lines.length) break;
                continue;
            }
            isTrackingClass = false;
        }
        isTrackingClass = false;

        System.out.printf("Found %d classes.\n", foundClasses.size());

        String[] parts, buffer, paramBuffer;

        while (true) {
            if (isTrackingClass) {
                // Start Class Tracking
                if (lineIndex == trackingClass.endLine) {
                    isTrackingClass = false;
                    continue;
                }
                currentLine = lines[lineIndex++].replaceFirst(" {4}", "");

                boolean isField = FIELD_PATTERN.matcher(currentLine).matches();
                if (!isField) currentLine = METHOD_COLON_PATTERN.matcher(currentLine).replaceAll("");
                parts = SPLITTER.split(currentLine);
                buffer = parts[0].split(" ");

                if (isField) {
                    mapping.put(
                            trackingClass.deobfuscatedClassName, buffer[1],
                            trackingClass.obfuscatedClassName, parts[1]
                    );
                    continue;
                }
                String descriptor = MappingUtil.toDescriptor(buffer[0]);
                String obfuscatedDescriptor = MappingUtil.toDescriptor(mapping, buffer[0]);
                buffer = buffer[1].replace("(", " ").replace(")", "").split(" ");

                if (buffer.length < 2) {
                    descriptor = "()" + descriptor;
                    obfuscatedDescriptor = "()" + obfuscatedDescriptor;
                } else {
                    paramBuffer = buffer[1].split(",");
                    descriptor = MappingUtil.toMethodDescriptor(paramBuffer) + descriptor;
                    obfuscatedDescriptor = MappingUtil.toMethodDescriptor(mapping, paramBuffer) + obfuscatedDescriptor;
                }

                mapping.put(
                        trackingClass.deobfuscatedClassName, buffer[0], descriptor,
                        trackingClass.obfuscatedClassName, parts[1], obfuscatedDescriptor
                );
                continue;
            }

            if (foundClasses.isEmpty()) break;

            trackingClass = foundClasses.poll();
            isTrackingClass = (trackingClass.startLine & trackingClass.endLine) != -1;
            lineIndex = trackingClass.startLine;
        }
        return mapping;
    }

    @Override
    public String name() {
        return "Proguard Mapping Format";
    }

    public static class ProguardClass {

        private final String deobfuscatedClassName;
        private final String obfuscatedClassName;

        private final String sourceFileName;

        public int startLine = -1;
        public int endLine = -1;

        public ProguardClass(String deobfName, String obfName, String sourceName) {
            this.deobfuscatedClassName = deobfName;
            this.obfuscatedClassName = obfName;
            this.sourceFileName = sourceName;
        }

        public String getDeobfuscatedClassName() {
            return deobfuscatedClassName;
        }

        public String getObfuscatedClassName() {
            return obfuscatedClassName;
        }

        public String getSourceFileName() {
            return sourceFileName;
        }
    }
}
