package io.microconfig.core.properties.resolvers.placeholder.strategies.composite;

import io.microconfig.core.properties.DeclaringComponent;
import io.microconfig.core.properties.Placeholder;
import io.microconfig.core.properties.PlaceholderResolveStrategy;
import io.microconfig.core.properties.Property;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.microconfig.utils.StreamUtils.findFirstResult;

@RequiredArgsConstructor
public class CompositeResolveStrategy implements PlaceholderResolveStrategy {
    private final List<PlaceholderResolveStrategy> strategies;

    public static PlaceholderResolveStrategy composite(List<PlaceholderResolveStrategy> strategies) {
        return new CompositeResolveStrategy(strategies);
    }

    @Override
    public Optional<Property> resolve(Placeholder placeholder,
                                      DeclaringComponent sourceOfValue,
                                      DeclaringComponent root, Set<Placeholder> visited) {
        return findFirstResult(strategies, s -> s.resolve(placeholder, sourceOfValue, root, visited));
    }
}