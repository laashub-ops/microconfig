package io.microconfig.configs.properties.files.parser;

import deployment.util.StringUtils;
import io.microconfig.configs.properties.Property;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

/**
 * supported format #include componentName[optionalEnv]
 */
@EqualsAndHashCode(of = {"componentName", "env"})
public class Include {
    final static Pattern PATTERN = Pattern.compile("[#@][iI]nclude\\s+(?<comp>[\\w\\d\\s_-]+)(\\[(?<env>.+)])?(\\s*@without:(?<without>.+))?");

    @Getter
    private final String componentName;
    @Getter
    private final String env;
    private final Optional<String> without;

    public static Include parse(String line, String defaultEnv) {
        return new Include(line, defaultEnv);
    }

    static boolean isInclude(String line) {
        String lower = line.toLowerCase();
        return lower.startsWith("#include") || lower.startsWith("#@include");
    }

    private Include(String line, String defaultEnv) {
        Matcher matcher = PATTERN.matcher(line);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Can't parse include directive: " + line + ". Supported format: #include componentName[optionalEnv]");
        }

        this.componentName = requireNonNull(matcher.group("comp")).trim();
        this.env = requireNonNull(ofNullable(matcher.group("env")).orElse(defaultEnv)).trim();
        this.without = ofNullable(matcher.group("without"));
    }

    public Map<String, Property> removeExcluded(Map<String, Property> includedProperties) {
        if (!without.isPresent()) return includedProperties;

        Map<String, Property> copy = new LinkedHashMap<>(includedProperties);
        copy.keySet().removeIf(p -> StringUtils.like(p, without.get().trim()));
        return copy;
    }

    @Override
    public String toString() {
        return "#include " + componentName + "[" + env + "]";
    }
}