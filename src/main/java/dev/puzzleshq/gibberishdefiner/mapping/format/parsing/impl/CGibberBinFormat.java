package dev.puzzleshq.gibberishdefiner.mapping.format.parsing.impl;

import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.IMutableMapping;
import dev.puzzleshq.gibberishdefiner.mapping.format.parsing.IMappingFormat;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class CGibberBinFormat implements IMappingFormat {

    private static class GibberClass {
        String obf;
        String deobf;

        public GibberClass(String obf, String deobf) {
            this.obf = obf;
            this.deobf = deobf;
        }

    }

    private static class StringPool {

        public String[] constantPool;

        public StringPool() {}

        public String get(int i) {
            return constantPool[i];
        }

        public void read(DataInputStream dataOutputStream) throws IOException {
            int count = dataOutputStream.readInt();
            constantPool = new String[count];
            for (int i = 0; i < count; i++) {
                constantPool[i] = dataOutputStream.readUTF();
            }
        }
    }

    @Override
    public IMapping assemble(IMutableMapping mapping, byte[] contents) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(contents);
            GZIPInputStream stream = new GZIPInputStream(byteArrayInputStream);
            DataInputStream dataInputStream = new DataInputStream(stream);

            int[] magic = new int[3];
            magic[0] = dataInputStream.readUnsignedByte();
            magic[1] = dataInputStream.readUnsignedByte();
            magic[2] = dataInputStream.readUnsignedByte();
            if (magic[0] != 0x71 || magic[1] != 0x71 || magic[2] != 0xE5) throw new IllegalArgumentException("Invalid Magic");
            int[] version = new int[2];
            version[0] = dataInputStream.readUnsignedByte();
            version[1] = dataInputStream.readUnsignedByte();

            StringPool classPool = new StringPool();
            StringPool fieldPool = new StringPool();
            StringPool methodPool = new StringPool();

            classPool.read(dataInputStream);
            fieldPool.read(dataInputStream);
            methodPool.read(dataInputStream);

            int codeSize = dataInputStream.readInt();

            readCodeSectorV0(dataInputStream.readNBytes(codeSize), mapping, classPool, fieldPool, methodPool);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return mapping;
    }

    private void readCodeSectorV0(byte[] bytes, IMutableMapping mapping, StringPool classPool, StringPool fieldPool, StringPool methodPool) throws Exception {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        GZIPInputStream stream = new GZIPInputStream(byteArrayInputStream);
        DataInputStream dataInputStream = new DataInputStream(stream);

        GibberClass tracking = null;

        for (;;) {
            try {
                int opcode = dataInputStream.readByte();
                if (opcode == 1) {
                    int idx0 = dataInputStream.readInt();
                    int idx1 = dataInputStream.readInt();

                    tracking = new GibberClass(classPool.get(idx0), classPool.get(idx1));
                    mapping.putClass(tracking.obf, tracking.deobf);
                    continue;
                }
                if (opcode == 2) {
                    //noinspection all
                    assert tracking != null;
                    tracking = null;
                    continue;
                }
                if (opcode == 3) {
                    //noinspection all
                    assert tracking != null;

                    int fieldObfNameIdx = dataInputStream.readInt();
                    int fieldDeObfNameIdx = dataInputStream.readInt();
                    mapping.putField(
                            tracking.obf,
                            fieldPool.get(fieldObfNameIdx),
                            tracking.deobf,
                            fieldPool.get(fieldDeObfNameIdx)
                    );
                    continue;
                }
                if (opcode == 4) {
                    //noinspection all
                    assert tracking != null;

                    int methodObfNameIdx = dataInputStream.readInt();
                    int methodObfDescIdx = dataInputStream.readInt();
                    int methodDeObfNameIdx = dataInputStream.readInt();
                    int methodDeObfDescIdx = dataInputStream.readInt();
                    mapping.putMethod(
                            tracking.obf,
                            methodPool.get(methodObfNameIdx),
                            methodPool.get(methodObfDescIdx),
                            tracking.deobf,
                            methodPool.get(methodDeObfNameIdx),
                            methodPool.get(methodDeObfDescIdx)
                    );
                }
            } catch (EOFException e) {
                break;
            }
        }
    }

    @Override
    public String name() {
        return "Compressed Binary Gibber Format";
    }
}
