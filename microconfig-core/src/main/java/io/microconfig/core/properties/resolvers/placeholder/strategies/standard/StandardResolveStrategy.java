package io.microconfig.core.properties.resolvers.placeholder.strategies.standard;

import io.microconfig.core.environments.EnvironmentRepository;
import io.microconfig.core.properties.*;
import io.microconfig.core.properties.resolvers.placeholder.PlaceholderImpl;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.microconfig.core.configtypes.ConfigTypeFilters.configTypeWithName;
import static java.util.function.Function.identity;
import static java.util.stream.Stream.of;

@RequiredArgsConstructor
public class StandardResolveStrategy implements PlaceholderResolveStrategy {
    private final EnvironmentRepository environmentRepository;

    @Override
    public Optional<Property> resolve(Placeholder placeholder,
                                      DeclaringComponent sourceOfValue,
                                      DeclaringComponent root,
                                      Set<Placeholder> visited) {
        DeclaringComponent component = placeholder.getReferencedComponent(root.getComponent());
        return environmentRepository.getOrCreateByName(component.getEnvironment())
                .getOrCreateComponentWithName(component.getComponent())
                .getPropertiesFor(configTypeWithName(component.getConfigType()))
                .getPropertyWithKey(placeholder.getKey());
    }

//    private boolean canBeOverridden(PlaceholderImpl p, DeclaringComponent sourceOfValue) {
//        return p.isSelfReferenced() ||
//                (p.referencedTo(sourceOfValue) && !nonOverridableKeys.contains(p.getKey()));
//    }
//
//    private String overrideByParents(PlaceholderImpl p, DeclaringComponent sourceOfValue, DeclaringComponent root) {
//        return
//                Function < DeclaringComponent,String > tryResolveFor = override -> {
//            try {
//                return resolve(p.overrideBy(override), root);
//            } catch (RuntimeException e) {
//                return null;
//            }
//        };
//
//        return of(
//                of(root),
//                visited.stream().map(PlaceholderImpl::getReferencedComponent),
//                of(sourceOfValue)
//        ).flatMap(identity())
//                .map(DeclaringComponentImpl::copyOf).distinct()//for correct distinct
//                .map(tryResolveFor)
//                .filter(Objects::nonNull)
//                .findFirst()
//                .orElseThrow(() -> new ResolveException(sourceOfValue, root, "Can't resolve placeholder " + this));
//    }
}