package io.microconfig.core.properties.resolvers.placeholder;

import io.microconfig.core.properties.*;
import io.microconfig.core.properties.resolvers.RecursiveResolver;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.microconfig.core.properties.resolvers.placeholder.PlaceholderBorders.findPlaceholderIn;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.of;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class PlaceholderResolver implements RecursiveResolver {
    private final PlaceholderResolveStrategy strategy;
    @With(PRIVATE)
    private final Set<Placeholder> visited;

    public PlaceholderResolver(PlaceholderResolveStrategy strategy) {
        this(strategy, emptySet());
    }

    @Override
    public Optional<Statement> findStatementIn(CharSequence line) {
        return findPlaceholderIn(line).map(PlaceholderStatement::new);
    }

    @RequiredArgsConstructor
    private class PlaceholderStatement implements Statement {
        private final PlaceholderBorders borders;

        @Override
        public String resolveFor(DeclaringComponent sourceOfValue, DeclaringComponent root) {
            Placeholder placeholder = borders.toPlaceholder(sourceOfValue.getConfigType(), sourceOfValue.getEnvironment());
            return resolve(placeholder, sourceOfValue, root);
        }

        private String resolve(Placeholder p, DeclaringComponent sourceOfValue, DeclaringComponent root) {
            try {
                Property resolved = strategy.resolve(p, sourceOfValue, root, visited)
                        .orElseThrow(() -> new ResolveException(sourceOfValue, root, "Can't resolve " + p));
                return resolved.resolveBy(currentResolverWithVisited(p), root).getValue();
            } catch (RuntimeException e) {
                String defaultValue = p.getDefaultValue();
                if (defaultValue != null) return defaultValue;
                throw e;
            }
        }

        private PlaceholderResolver currentResolverWithVisited(Placeholder placeholder) {
            Set<Placeholder> updated = new LinkedHashSet<>(visited);
            if (updated.add(placeholder)) {
                return withVisited(unmodifiableSet(updated));
            }

            throw new IllegalStateException("Found cyclic dependencies:\n" +
                    updated.stream().map(Placeholder::toString).collect(joining(" -> "))
            );
        }

        @Override
        public int getStartIndex() {
            return borders.getStartIndex();
        }

        @Override
        public int getEndIndex() {
            return borders.getEndIndex();
        }

        @Override
        public String toString() {
            return borders.toString();
        }
    }
}