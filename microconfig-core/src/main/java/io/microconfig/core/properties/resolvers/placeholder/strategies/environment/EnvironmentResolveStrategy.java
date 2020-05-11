package io.microconfig.core.properties.resolvers.placeholder.strategies.environment;

import io.microconfig.core.environments.Environment;
import io.microconfig.core.environments.EnvironmentRepository;
import io.microconfig.core.environments.repository.EnvironmentException;
import io.microconfig.core.properties.DeclaringComponent;
import io.microconfig.core.properties.Placeholder;
import io.microconfig.core.properties.PlaceholderResolveStrategy;
import io.microconfig.core.properties.Property;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.microconfig.core.properties.ConfigFormat.PROPERTIES;
import static io.microconfig.core.properties.PropertyImpl.property;
import static java.util.Optional.empty;
import static java.util.stream.Stream.of;

@RequiredArgsConstructor
public class EnvironmentResolveStrategy implements PlaceholderResolveStrategy {
    private final EnvironmentRepository environmentRepository;
    private final Map<String, EnvProperty> propertyByKey;

    @Override
    public Optional<Property> resolve(Placeholder placeholder,
                                      DeclaringComponent sourceOfValue,
                                      DeclaringComponent root,
                                      Set<Placeholder> visited) {
        EnvProperty envProperty = propertyByKey.get(placeholder.getKey());
        if (envProperty == null) return empty();

        DeclaringComponent component = placeholder.getReferencedComponent(root.getComponent());
       Stream<Environment> environments = findEnv(placeholder, sourceOfValue, root, visited);
       return environments.map(environment-> envProperty.resolveFor(component.getComponent(), environment))
               .filter(Optional::isPresent)
               .map(Optional::get)
               .map(value -> property(placeholder.getKey(), value, PROPERTIES, component))
               .findFirst();
    }

    //-e cr-dev6 -s cr-atlas-cib-feed
    private Stream<Environment> findEnv(Placeholder p,
                                       DeclaringComponent sourceOfValue,
                                       DeclaringComponent root,
                                       Set<Placeholder> visited) {
        if (p.isSelfReferenced(sourceOfValue)) {
            return of(
                    of(root),
                    visited.stream().map(pl -> pl.getReferencedComponent("")),
                    of(sourceOfValue)
            ).flatMap(Function.identity())
                    .map(DeclaringComponent::getEnvironment)
                    .map(this::getEnvironment)
                    .filter(Objects::nonNull);

        }

        return Stream.of(getEnvironment(p.getReferencedComponent("").getEnvironment())).filter(Objects::nonNull);
    }

    private Environment getEnvironment(String environment) {
        try {
            return environmentRepository.getByName(environment);
        } catch (EnvironmentException e) {
            return null;
        }
    }
}