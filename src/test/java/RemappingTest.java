import dev.puzzleshq.gibberishdefiner.JarRemapper;
import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.util.MappingUtil;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.VisitableMappingTree;

import java.io.*;

public class RemappingTest {

    public static void main(String[] args) throws IOException {
//        File in = new File("test/1.14.4obf.jar");
        File in = new File("test/server.jar");
//        File out = new File("test/1.14.4debof.jar");
        File out = new File("test/server.deobf.jar");
        //noinspection ResultOfMethodCallIgnored
        out.createNewFile();

        JarRemapper.applyBareMapping(in, out, getMapping());
    }

    public static IMapping getMapping() throws IOException {
//        InputStream stream = Main.class.getResourceAsStream("test/yarn-1.4.2+build.551-tiny");
//        InputStream stream = Main.class.getResourceAsStream("1.14.4-client-proguard.cgmbin");

//        MappingWriter.create(new File("test/yarn-1.4.2+build.551-tiny2").toPath(), new MappingFormat)


//        IMapping mapping = GibberishDefiner.getFormat("proguard").assemble(new Mapping(), bytes).reverse();
//        IMapping mapping = GibberishDefiner.getFormat("cgibberbin").assemble(new Mapping(), bytes).reverse();
        VisitableMappingTree mappingTree = MappingUtil.fromFile(new File("test/yarn-1.4.2+build.551-tiny"), MappingFormat.TINY_FILE);
        IMapping mapping = MappingUtil.fromMappingTree(mappingTree, "official", "named");

        return mapping;
    }

}
