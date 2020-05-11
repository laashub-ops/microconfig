package io.microconfig.core.properties;

import java.util.Optional;
import java.util.Set;

public interface PlaceholderResolveStrategy {
    Optional<Property> resolve(Placeholder placeholder,
                               DeclaringComponent sourceOfValue,
                               DeclaringComponent root,
                               Set<Placeholder> visited);
}