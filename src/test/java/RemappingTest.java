import dev.puzzleshq.gibberishdefiner.JarRemapper;
import dev.puzzleshq.gibberishdefiner.mapping.IMapping;
import dev.puzzleshq.gibberishdefiner.util.MappingUtil;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.VisitableMappingTree;

import java.io.*;

public class RemappingTest {

    public static void main(String[] args) throws IOException {
//        File in = new File("test/server.jar");
//        File out = new File("test/server.deobf.jar");
//        //noinspection ResultOfMethodCallIgnored
//        out.createNewFile();
//
//        JarRemapper.applyBareMapping(in, out, getMapping());

        File client = new File("test/minecraft-1.14.4-client.jar");

        File in = new File("test/test-project-1.0-SNAPSHOT.jar");
        File out = new File("test/test-project-1.0-SNAPSHOT-reobfuscated.jar");

        IMapping finishedMapping = JarRemapper.createFinishedMapping(client, getMapping());
        System.out.println(finishedMapping.mapClass("net/minecraft/entity/projectile/ProjectileEntity"));
        JarRemapper.applyBareMapping(in, out, finishedMapping);
    }

    public static IMapping getMapping() throws IOException {
        VisitableMappingTree mappingTree = MappingUtil.fromFile(new File("test/1.14.4-client-proguard.txt"), MappingFormat.PROGUARD_FILE);

        return MappingUtil.fromMappingTree(mappingTree, "source", "target");
    }

}
