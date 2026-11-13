import dev.puzzleshq.gibberishdefiner.JarRemapper;
import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.util.MappingUtil;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.VisitableMappingTree;

import java.io.*;

public class RemappingTest {

    public static void main(String[] args) throws IOException {
        File in = new File("test/server.jar");
        File out = new File("test/server.deobf.jar");
        //noinspection ResultOfMethodCallIgnored
        out.createNewFile();

        JarRemapper.applyBareMapping(in, out, getMapping());
    }

    public static IMapping getMapping() throws IOException {
        VisitableMappingTree mappingTree = MappingUtil.fromFile(new File("test/yarn-1.4.2+build.551-tiny"), MappingFormat.TINY_FILE);

        return MappingUtil.fromMappingTree(mappingTree, "official", "named");
    }

}
