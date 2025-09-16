import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class GetManifests {

    public static void main(String[] args) throws Exception {
        File baseDir = new File("test/minecraft");
        String urlBase = "https://piston-meta.mojang.com/v1/packages/";

        InputStream stream = new FileInputStream(new File(baseDir, "version_manifest_v2.json"));
        JsonObject versionManifest = JsonValue.readHjson(new String(stream.readAllBytes())).asObject();
        stream.close();

        JsonArray versions = versionManifest.get("versions").asArray();
        for (JsonValue value : versions) {
            JsonObject version = value.asObject();
            String id = version.get("id").asString();
            File manifest = new File(baseDir, "manifests/" + id + ".json");
            if (manifest.exists()) continue;
            System.out.println("Getting manifest for version " + id);

            String sha1 = version.get("sha1").asString();

            URL url = new URL(urlBase + sha1 + "/" + id + ".json");
            stream = url.openStream();
            byte[] bytes = stream.readAllBytes();
            stream.close();

            FileOutputStream outputStream = new FileOutputStream(manifest);
            outputStream.write(bytes);
            outputStream.close();
        }
    }

}
