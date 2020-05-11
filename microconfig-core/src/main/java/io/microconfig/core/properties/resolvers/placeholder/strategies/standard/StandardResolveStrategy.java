package io.microconfig.core.properties.resolvers.placeholder.strategies.standard;

import io.microconfig.core.environments.EnvironmentRepository;
import io.microconfig.core.properties.*;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.microconfig.core.configtypes.ConfigTypeFilters.configTypeWithName;
import static java.util.stream.Stream.of;

@RequiredArgsConstructor
public class StandardResolveStrategy implements PlaceholderResolveStrategy {
    private final EnvironmentRepository environmentRepository;

    @Override
    public Optional<Property> resolve(Placeholder p,
                                      DeclaringComponent sourceOfValue,
                                      DeclaringComponent root,
                                      Set<Placeholder> visited) {
        return overridePriority(p, sourceOfValue, root, visited)
                .map(c -> doResolve(c, p.getKey())) //in some cases can't be override
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private Stream<DeclaringComponent> overridePriority(Placeholder p,
                                                        DeclaringComponent sourceOfValue,
                                                        DeclaringComponent root,
                                                        Set<Placeholder> visited) {
        if (!p.isSelfReferenced() && !p.referencedTo(sourceOfValue)) {
            return Stream.of(p.getReferencedComponent(""));
        }

        return of(
                of(root),
                visited.stream().map(v -> v.getReferencedComponent("this")), //todo
                of(sourceOfValue)
        ).flatMap(Function.identity())
                .map(DeclaringComponentImpl::copyOf)//for correct distinct
                .distinct();
    }

    private Optional<Property> doResolve(DeclaringComponent c, String key) {
        return environmentRepository.getOrCreateByName(c.getEnvironment())
                .getOrCreateComponentWithName(c.getComponent())
                .getPropertiesFor(configTypeWithName(c.getConfigType()))
                .getPropertyWithKey(key);
    }
}