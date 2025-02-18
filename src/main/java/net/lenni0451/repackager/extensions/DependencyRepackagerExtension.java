package net.lenni0451.repackager.extensions;

import net.lenni0451.repackager.settings.RepackageSettings;
import net.lenni0451.repackager.transforms.RepackageTransform;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

import java.util.Map;

public abstract class DependencyRepackagerExtension implements RepackageSettings {

    private final Project project;

    public DependencyRepackagerExtension(final Project project) {
        this.project = project;
        this.initDefaults();
    }

    @Input
    public abstract Property<Configuration> getConfiguration();

    public void register(final Configuration configuration, final Map<String, String> relocations, final boolean remapStrings, final boolean remapServices, final boolean remapManifest, final boolean removeEmptyDirs) {
        this.project.afterEvaluate(project -> {
            String jarType = "repackaged-jar-" + configuration.getName();
            for (Dependency dependency : configuration.getAllDependencies()) {
                if (!(dependency instanceof ModuleDependency)) continue;
                ModuleDependency moduleDependency = (ModuleDependency) dependency;
                moduleDependency.attributes(attributeContainer -> attributeContainer.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, jarType));
            }
            project.getDependencies().registerTransform(RepackageTransform.class, transform -> {
                transform.getParameters().getRelocations().set(relocations);
                transform.getParameters().getRemapStrings().set(remapStrings);
                transform.getParameters().getRemapServices().set(remapServices);
                transform.getParameters().getRemapManifest().set(remapManifest);
                transform.getParameters().getRemoveEmptyDirs().set(removeEmptyDirs);

                transform.getFrom().attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE);
                transform.getTo().attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, jarType);
            });
        });
    }

}
