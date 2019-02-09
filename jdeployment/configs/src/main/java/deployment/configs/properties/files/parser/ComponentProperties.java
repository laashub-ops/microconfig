package deployment.configs.properties.files.parser;

import deployment.configs.properties.Property;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ComponentProperties {
    private final String component;
    @Getter
    private final List<Include> includes;
    @Getter
    private final List<Property> properties;

    @Override
    public String toString() {
        return component;
    }
}