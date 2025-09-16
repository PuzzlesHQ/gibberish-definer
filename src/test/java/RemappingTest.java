import dev.puzzleshq.gibberishdefiner.GibberishDefiner;
import dev.puzzleshq.gibberishdefiner.JarRemapper;
import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.mapping.impl.Mapping;
import java.io.*;
import java.util.Objects;

public class RemappingTest {

    public static void main(String[] args) throws IOException {
//        File in = new File("test/1.14.4obf.jar");
        File in = new File("test/1.8.9.jar");
//        File out = new File("test/1.14.4debof.jar");
        File out = new File("test/1.8.9deobf.jar");
        //noinspection ResultOfMethodCallIgnored
        out.createNewFile();

        JarRemapper.applySkeletonMapping(in, out, getMapping());
    }

    public static IMapping getMapping() throws IOException {
        GibberishDefiner.initDefaultFormats();

        InputStream stream = Main.class.getResourceAsStream("1.8.9.csrg");
//        InputStream stream = Main.class.getResourceAsStream("1.14.4-client-proguard.cgmbin");
//        InputStream stream = Main.class.getResourceAsStream("mapping.txt");
        byte[] bytes = Objects.requireNonNull(stream).readAllBytes();
        stream.close();

//        IMapping mapping = GibberishDefiner.getFormat("proguard").assemble(new Mapping(), bytes).reverse();
//        IMapping mapping = GibberishDefiner.getFormat("cgibberbin").assemble(new Mapping(), bytes).reverse();
        IMapping mapping = GibberishDefiner.getFormat("csrg").assemble(new Mapping(), bytes);

        return mapping;
    }

}
