package deployment.configs.engine.util;

import java.io.File;
import java.net.URL;

public class TestUtils {
    public static File getFile(String name) {
        URL resource = TestUtils.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalArgumentException("File is not exists: " + name);
        }
        return new File(resource.getFile());
    }
}
