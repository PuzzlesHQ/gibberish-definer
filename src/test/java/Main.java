import dev.puzzleshq.gibberishdefiner.GibberishDefiner;
import dev.puzzleshq.gibberishdefiner.mapping.IMergeableMapping;
import dev.puzzleshq.gibberishdefiner.mapping.impl.Mapping;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        GibberishDefiner.initDefaultFormats();

        InputStream stream = Main.class.getResourceAsStream("mapping.txt");
        byte[] bytes = stream.readAllBytes();
        stream.close();

        IMergeableMapping mapping = (IMergeableMapping) GibberishDefiner.getFormat("proguard").assemble(new Mapping(), new String(bytes));
        for (Map.Entry<String, String> entry : mapping.getMethodEntries()) {
            if (entry.getKey().split("~")[0].equals("net/minecraft/CrashReport"))
                System.out.println(entry.getKey() + " | " + entry.getValue());
        }
    }

}
