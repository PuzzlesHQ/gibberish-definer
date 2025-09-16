import dev.puzzleshq.gibberishdefiner.GibberishDefiner;
import dev.puzzleshq.gibberishdefiner.JarRemapper;
import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.format.writing.impl.GibberMappingCompressedBinary;
import dev.puzzleshq.gibberishdefiner.mapping.format.writing.impl.GibberMappingStr;
import dev.puzzleshq.gibberishdefiner.mapping.impl.Mapping;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class GibberMapTest {

    public static void main(String[] args) throws IOException {
        File in = new File("test/1.21.1obf.jar");

        File skeletonGibberMapping = new File("test/gibber/1.21.1-skeleton-gibber.gmbin");
        File meatyGibberMapping = new File("test/gibber/1.21.1-meaty-gibber.gmbin");

        File compressedSkeletonGibberMapping = new File("test/gibber/1.21.1-skeleton-gibber.cgmbin");
        File compressedMeatyGibberMapping = new File("test/gibber/1.21.1-meaty-gibber.cgmbin");

        File stringSkeletonGibberMapping = new File("test/gibber/1.21.1-skeleton-gibber.gmstr");
        File stringMeatyGibberMapping = new File("test/gibber/1.21.1-meaty-gibber.gmstr");

        skeletonGibberMapping.getParentFile().mkdirs();

        compressedSkeletonGibberMapping.createNewFile();
        compressedMeatyGibberMapping.createNewFile();
        skeletonGibberMapping.createNewFile();
        meatyGibberMapping.createNewFile();
        stringSkeletonGibberMapping.createNewFile();
        stringMeatyGibberMapping.createNewFile();

        IMapping mapping = getMapping();
        IMapping contextMapping = JarRemapper.createContextMapping(in, mapping);

        GibberMappingCompressedBinary gibberMappingCompressedBinary = new GibberMappingCompressedBinary();
        GibberMappingStr gibberMappingStr = new GibberMappingStr();

        System.out.println("Writing " + compressedSkeletonGibberMapping);
        gibberMappingCompressedBinary.export(mapping, compressedSkeletonGibberMapping);
        System.out.println("Writing " + stringSkeletonGibberMapping);
        gibberMappingStr.export(mapping, stringSkeletonGibberMapping);

        System.out.println("Writing " + stringMeatyGibberMapping);
        gibberMappingStr.export(contextMapping, stringMeatyGibberMapping);
        System.out.println("Writing " + compressedMeatyGibberMapping);
        gibberMappingCompressedBinary.export(contextMapping, compressedMeatyGibberMapping);
    }

    public static IMapping getMapping() throws IOException {
        GibberishDefiner.initDefaultFormats();

        InputStream stream = Main.class.getResourceAsStream("mapping.txt");
        byte[] bytes = Objects.requireNonNull(stream).readAllBytes();
        stream.close();

        IMapping mapping = GibberishDefiner.getFormat("proguard").assemble(new Mapping(), bytes);
        mapping = mapping.reverse();

        return mapping;
    }


}
