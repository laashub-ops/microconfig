package io.microconfig.core.properties.resolvers.placeholder;

import io.microconfig.core.properties.DeclaringComponent;
import io.microconfig.core.properties.DeclaringComponentImpl;
import io.microconfig.core.properties.Placeholder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;

import static lombok.AccessLevel.PACKAGE;

@EqualsAndHashCode(exclude = "defaultValue")
@RequiredArgsConstructor(access = PACKAGE)
public class PlaceholderImpl implements Placeholder {
    private static final String SELF_REFERENCE = "this";

    
    private final String configType;
    @With
    private final String component;
    @With
    private final String environment;
    @Getter
    private final String key;
    @Getter
    private final String defaultValue;

    @Override
    public DeclaringComponent getReferencedComponent(String defaultComponentName) {
        return new DeclaringComponentImpl(configType, isSelfReferenced() ? defaultComponentName : component, environment);
    }

    @Override
    public Placeholder withReferencedComponent(DeclaringComponent component) {
        return new PlaceholderImpl(configType, component.getComponent(), component.getEnvironment(), key, defaultValue); //todo type?
    }

    @Override
    public boolean isSelfReferenced(DeclaringComponent sourceOfValue) {
        return isSelfReferenced() || referencedTo(sourceOfValue);
    }

    @Override
    public boolean isSelfReferenced() {
        return component.equals(SELF_REFERENCE);
    }

    private boolean referencedTo(DeclaringComponent sourceOfValue) {
        return component.equals(sourceOfValue.getComponent()) && environment.equals(sourceOfValue.getEnvironment());  //todo type?
    }

    @Override
    public String toString() {
        return "${" +
                component +
                "[" + environment + "]" +
                "@" +
                key +
                (defaultValue == null ? "" : ":" + defaultValue) +
                "}";
    }
}