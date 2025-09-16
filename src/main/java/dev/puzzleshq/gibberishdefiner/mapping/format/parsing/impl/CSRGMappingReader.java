package dev.puzzleshq.gibberishdefiner.mapping.format.parsing.impl;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.IMutableMapping;
import dev.puzzleshq.gibberishdefiner.mapping.format.parsing.IMappingFormat;
import dev.puzzleshq.gibberishdefiner.util.MappingUtil;

import java.util.Scanner;

public class CSRGMappingReader implements IMappingFormat {

    public IMapping assemble(IMutableMapping mapping, byte[] bytes) {
        String data = new String(bytes);

        Scanner scanner = new Scanner(data);

        String[] strings;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            strings = line.split(" ");
            if (strings.length == 2) {
                mapping.putClass(strings[0], strings[1]);
                continue;
            }

            if (strings.length == 3) {
                String clazz = strings[0];
                String obField = strings[1];
                String deField = strings[2];

                mapping.putField(clazz, obField, mapping.mapClass(clazz), deField);
                continue;
            }

            if (strings.length == 4) {
                String clazz = strings[0];
                String obMethod = strings[1];
                String obDesc = strings[2];
                String deMethod = strings[3];

                System.out.println(MappingUtil.deobfuscateDescriptor(mapping, obDesc));
                mapping.putMethod(clazz, obMethod, obDesc, mapping.mapClass(clazz), deMethod, MappingUtil.deobfuscateDescriptor(mapping, obDesc));
                continue;
            }

        }
        return mapping;
    }

    @Override
    public String name() {
        return "CSRG Mapping Format";
    }

}
