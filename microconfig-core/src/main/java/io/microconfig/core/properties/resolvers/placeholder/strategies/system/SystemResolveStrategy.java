package io.microconfig.core.properties.resolvers.placeholder.strategies.system;

import io.microconfig.core.properties.*;
import io.microconfig.utils.Os;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

import static io.microconfig.core.properties.ConfigFormat.PROPERTIES;
import static io.microconfig.core.properties.PropertyImpl.property;
import static io.microconfig.utils.StringUtils.escape;
import static io.microconfig.utils.StringUtils.unixLikePath;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
public class SystemResolveStrategy implements PlaceholderResolveStrategy {
    public static final String ENV_OS_SOURCE = "env";
    public static final String SYSTEM_SOURCE = "system";

    private final String type;
    private final UnaryOperator<String> resolver;

    public static PlaceholderResolveStrategy systemPropertiesResolveStrategy() {
        return new SystemResolveStrategy(SYSTEM_SOURCE, System::getProperty);
    }

    public static PlaceholderResolveStrategy envVariablesResolveStrategy() {
        return new SystemResolveStrategy(ENV_OS_SOURCE, System::getenv);
    }

    @Override
    public Optional<Property> resolve(Placeholder placeholder,
                                      DeclaringComponent sourceOfValue,
                                      DeclaringComponent root, Set<Placeholder> visited) {
        DeclaringComponent component = placeholder.getReferencedComponent(root.getComponent());
        if (!type.equals(component.getComponent())) return empty();

        return ofNullable(resolver.apply(placeholder.getKey()))
                .map(v -> escapeOnWindows(v, placeholder.getKey()))
                .map(v -> property(placeholder.getKey(), v, PROPERTIES, component));
    }

    private String escapeOnWindows(String value, String key) {
        if (!Os.isWindows()) return value;
        return "user.home".equals(key) ? unixLikePath(value) : escape(value);
    }
}