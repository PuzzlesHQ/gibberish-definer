package dev.puzzleshq.gibberishdefiner.mapping.format.parsing.impl;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.IMutableMapping;
import dev.puzzleshq.gibberishdefiner.mapping.format.parsing.IMappingFormat;

import java.util.Scanner;

public class SRGMappingReader implements IMappingFormat {

    public IMapping assemble(IMutableMapping mapping, byte[] bytes) {
        String data = new String(bytes);

        Scanner scanner = new Scanner(data);

        String[] strings;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (line.startsWith("CL: ")) {
                line = line.replace("CL: ", "");
                strings = line.split(" ");

                mapping.putClass(strings[0], strings[1]);
                continue;
            }
            if (line.startsWith("FD: ")) {
                line = line.replace("FD: ", "");
                strings = line.split(" ");
                String[] f0 = strings[0].split("/");
                String[] f1 = strings[1].split("/");

                String obFieldName = f0[f0.length - 1];
                String deobFieldName = f1[f1.length - 1];

                mapping.putField(
                        (strings[0] + " ").replace("/" + obFieldName + " ", ""),
                        obFieldName,
                        (strings[1] + " ").replace("/" + deobFieldName + " ", ""),
                        deobFieldName
                );
                continue;
            }
            if (line.startsWith("MD: ")) {
                line = line.replace("MD: ", "");
                strings = line.split(" ");
                String[] m0 = strings[0].split("/");
                String[] m1 = strings[2].split("/");

                String obFieldName = m0[m0.length - 1];
                String deobFieldName = m1[m1.length - 1];

                mapping.putMethod(
                        (strings[0] + " ").replace("/" + obFieldName + " ", ""), // className
                        obFieldName,
                        strings[1], // descriptor
                        (strings[2] + " ").replace("/" + deobFieldName + " ", ""), // className
                        deobFieldName,
                        strings[3] // descriptor
                );
                continue;
            }
        }
        return mapping;
    }

    @Override
    public String name() {
        return "SRG Mapping Format";
    }

}
