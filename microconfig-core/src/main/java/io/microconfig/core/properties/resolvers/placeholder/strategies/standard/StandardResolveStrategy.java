package io.microconfig.core.properties.resolvers.placeholder.strategies.standard;

import io.microconfig.core.environments.EnvironmentRepository;
import io.microconfig.core.properties.*;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.Set;

import static io.microconfig.core.configtypes.ConfigTypeFilters.configTypeWithName;
import static java.util.function.Function.identity;
import static java.util.stream.Stream.of;

@RequiredArgsConstructor
public class StandardResolveStrategy implements PlaceholderResolveStrategy {
    private final EnvironmentRepository environmentRepository;

    @Override
    public Optional<Property> resolve(Placeholder p,
                                      DeclaringComponent sourceOfValue,
                                      DeclaringComponent root,
                                      Set<Placeholder> visited) {
        return of(
                of(root),
                visited.stream().map(v -> v.getReferencedComponent("this")), //todo
                of(sourceOfValue)
        ).flatMap(identity())
                .map(DeclaringComponentImpl::copyOf).distinct()//for correct distinct
                .map(c -> doResolve(c.getComponent(), p.getKey(), c.getEnvironment(), c.getConfigType())) //in some cases can't be override
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private Optional<Property> doResolve(String component, String key, String environment, String configType) {
        return environmentRepository.getOrCreateByName(environment)
                .getOrCreateComponentWithName(component)
                .getPropertiesFor(configTypeWithName(configType))
                .getPropertyWithKey(key);
    }
}