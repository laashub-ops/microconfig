package io.microconfig.core.properties;

public interface Placeholder {
    DeclaringComponent getReferencedComponent(String defaultComponentName);

    Placeholder withReferencedComponent(DeclaringComponent component);

    String getKey();

    String getDefaultValue();

    boolean isSelfReferenced(DeclaringComponent sourceOfPlaceholder);

    boolean isSelfReferenced();
}