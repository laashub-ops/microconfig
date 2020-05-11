package io.microconfig.core.properties.resolvers.placeholder.strategies.component.properties;

import io.microconfig.core.environments.EnvironmentRepository;
import io.microconfig.core.properties.repository.ComponentGraph;
import io.microconfig.core.properties.resolvers.placeholder.strategies.component.ComponentProperty;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.of;

@RequiredArgsConstructor
public class ComponentProperties {
    private final ComponentGraph componentGraph;
    private final EnvironmentRepository environmentRepository;
    private final File rootDir;
    private final File destinationComponentDir;

    public Map<String, ComponentProperty> properties() {
        return of(
                new NameProperty(),
                new ConfigDirProperty(componentGraph, environmentRepository),
                new ResultDirProperty(destinationComponentDir),
                new ConfigRootDirProperty(rootDir)
        ).collect(toMap(ComponentProperty::key, identity()));
    }
}