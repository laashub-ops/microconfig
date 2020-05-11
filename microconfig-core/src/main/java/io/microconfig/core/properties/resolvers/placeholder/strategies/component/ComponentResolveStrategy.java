package io.microconfig.core.properties.resolvers.placeholder.strategies.component;

import io.microconfig.core.properties.DeclaringComponent;
import io.microconfig.core.properties.Placeholder;
import io.microconfig.core.properties.PlaceholderResolveStrategy;
import io.microconfig.core.properties.Property;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.microconfig.core.properties.ConfigFormat.PROPERTIES;
import static io.microconfig.core.properties.PropertyImpl.property;
import static java.util.Optional.empty;

@RequiredArgsConstructor
public class ComponentResolveStrategy implements PlaceholderResolveStrategy {
    private final Map<String, ComponentProperty> propertyByKey;

    @Override
    public Optional<Property> resolve(Placeholder placeholder,
                                      DeclaringComponent sourceOfValue,
                                      DeclaringComponent root,
                                      Set<Placeholder> visited) {
        ComponentProperty componentProperty = propertyByKey.get(placeholder.getKey());
        if (componentProperty == null) return empty();

        DeclaringComponent component = placeholder.getReferencedComponent(root.getComponent());
        return componentProperty.resolveFor(component.getComponent(), component.getEnvironment())
                .map(value -> property(placeholder.getKey(), value, PROPERTIES, component));
    }
}