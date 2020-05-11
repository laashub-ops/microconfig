package io.microconfig.core;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static io.microconfig.core.ClasspathReader.classpathFile;
import static io.microconfig.core.Microconfig.searchConfigsIn;
import static io.microconfig.core.configtypes.ConfigTypeFilters.configType;
import static io.microconfig.core.configtypes.StandardConfigType.APPLICATION;
import static io.microconfig.core.properties.serializers.PropertySerializers.asString;
import static io.microconfig.utils.FileUtils.walk;
import static io.microconfig.utils.IoUtils.readFully;
import static io.microconfig.utils.StringUtils.toUnixPathSeparator;
import static io.microconfig.utils.StringUtils.unixLikePath;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class MicroconfigTest {
    File root = classpathFile("repo");
    Microconfig microconfig = searchConfigsIn(root);

    @TestFactory
    List<DynamicTest> findTests() {
        try (Stream<Path> stream = walk(root.toPath())) {
            return stream.map(Path::toFile)
                    .filter(this::isExpectation)
                    .map(this::toTest)
                    .collect(toList());
        }
    }

    @Test
    void testIncorrectRootDir() {
        assertThrows(IllegalArgumentException.class, () -> searchConfigsIn(new File("missingDir")));
    }

    private boolean isExpectation(File file) {
        String name = file.getName();
        return overriddenTest(file) &&
                (name.startsWith("expect.") || name.startsWith("truncate.") || name.startsWith("exception."));
    }

    private boolean overriddenTest(File file) {
        String testName = System.getProperty("test");
        return testName == null || file.getParentFile().getName().equals(testName);
    }

    private DynamicTest toTest(File expectation) {
        String component = getComponentName(expectation);
        String env = getEnvName(expectation);
        return toTest(expectation, component, env);
    }

    private DynamicTest toTest(File expectation, String component, String env) {
        return dynamicTest(component + "[" + env + "]", () -> {
            String expected = readExpectation(expectation).trim();
            String actual = build(component, env).trim();

            if (expectation.getName().startsWith("truncate.")) {
                actual = actual.substring(0, min(expected.length(), actual.length()));
            }

            assertEquals(
                    toUnixPathSeparator(expected),
                    toUnixPathSeparator(actual)
            );
        });
    }

    private String getComponentName(File expectation) {
        String[] parts = expectation.getName().split("\\.");
        return parts.length == 3 ? parts[2] : expectation.getParentFile().getName();
    }

    private String getEnvName(File expectation) {
        return expectation.getName().split("\\.")[1];
    }

    private String build(String component, String env) {
        try {
            return microconfig.environments().getOrCreateByName(env)
                    .findComponentWithName(component)
                    .getPropertiesFor(configType(APPLICATION))
                    .resolveBy(microconfig.resolver())
                    .first()
                    .save(asString());
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    private String readExpectation(File expectation) {
        return readFully(expectation)
                .replace("${configDir}", toPath(expectation.getParentFile()))
                .replace("${configRoot}", toPath(root))
                .replace("${resultDir}", toPath(new File(root, "build")))
                .replace("${userHome}", toPath(new File(System.getProperty("user.home"))))
                .replace("${space}", " ")
                .replace("#todo", "");
    }

    private String toPath(File file) {
        return unixLikePath(file.getAbsolutePath());
    }
}