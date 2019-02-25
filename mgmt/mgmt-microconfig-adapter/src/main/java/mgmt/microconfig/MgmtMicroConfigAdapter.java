package mgmt.microconfig;

import io.microconfig.commands.*;
import io.microconfig.commands.factory.MicroconfigFactory;
import io.microconfig.commands.postprocessors.CopyTemplatesPostProcessor;
import io.microconfig.commands.postprocessors.UpdateSecretsPostProcessor;
import io.microconfig.templates.CopyTemplatesServiceImpl;

import java.io.File;
import java.util.List;

import static io.microconfig.commands.factory.ConfigType.*;
import static io.microconfig.templates.RelativePathResolver.empty;
import static io.microconfig.templates.TemplatePattern.defaultPattern;
import static java.util.Arrays.asList;

public class MgmtMicroConfigAdapter {
    public static void execute(String env, List<String> groups, File root, File componentsDir, List<String> components) {
        Command command = newBuildPropertiesCommand(root, componentsDir);
        BuildConfigMain.execute(command, env, groups, components);
    }

    private static Command newBuildPropertiesCommand(File repoDir, File componentsDir) {
        MicroconfigFactory factory = MicroconfigFactory.init(repoDir, componentsDir);

        BuildConfigCommand serviceCommon = factory.newBuildCommand(SERVICE, copyTemplatesPostProcessor());
        factory = factory.withServiceInnerDir(".mgmt");
        return new CompositeCommand(asList(
                serviceCommon,
                factory.newBuildCommand(PROCESS, new WebappPostProcessor()),
                factory.newBuildCommand(ENV),
                factory.newBuildCommand(LOG4j),
                factory.newBuildCommand(LOG4J2),
                factory.newBuildCommand(SAP),
                factory.newBuildCommand(SECRET, new UpdateSecretsPostProcessor(factory.getConfigIo())),
                new GenerateComponentListCommand(componentsDir, factory.getEnvironmentProvider()),
                new CopyHelpFilesCommand(factory.getEnvironmentProvider(), factory.getComponentTree(), componentsDir.toPath())
        ));
    }

    private static BuildConfigPostProcessor copyTemplatesPostProcessor() {
        return new CopyTemplatesPostProcessor(
                new CopyTemplatesServiceImpl(defaultPattern().toBuilder().templatePrefix("mgmt.template.").build(), empty())
        );
    }
}
