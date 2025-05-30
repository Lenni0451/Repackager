package net.lenni0451.repackager;

import net.lenni0451.repackager.extensions.DependencyRepackagerExtension;
import net.lenni0451.repackager.extensions.RepackagerExtension;
import net.lenni0451.repackager.tasks.RepackageTask;
import net.lenni0451.repackager.transforms.RepackageTransform;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;

public class RepackagerPlugin implements Plugin<Project> {

    @Override
    public void apply(Project target) {
        RepackagerExtension repackagerExtension = target.getExtensions().create("repackager", RepackagerExtension.class, target);
        DependencyRepackagerExtension dependencyRepackagerExtension = target.getExtensions().create("dependencyRepackager", DependencyRepackagerExtension.class, target);

        target.afterEvaluate(project -> {
            if (repackagerExtension.getJarFile().isPresent()) {
                RepackageTask task = target.getTasks().register("repackageTask", RepackageTask.class, thiz -> {
                    thiz.getJarFile().set(repackagerExtension.getJarFile());
                    thiz.getOutputFile().set(repackagerExtension.getOutputFile());
                    thiz.getRelocations().set(repackagerExtension.getRelocations());
                    thiz.getRemovals().set(repackagerExtension.getRemovals());
                    thiz.getRemapStrings().set(repackagerExtension.getRemapStrings());
                    thiz.getRemapServices().set(repackagerExtension.getRemapServices());
                    thiz.getRemapManifest().set(repackagerExtension.getRemapManifest());
                    thiz.getRemoveEmptyDirs().set(repackagerExtension.getRemoveEmptyDirs());
                }).get();

                Task buildTask = project.getTasks().findByName("build");
                if (buildTask != null) buildTask.finalizedBy(task);
            }
        });
        target.afterEvaluate(project -> {
            if (dependencyRepackagerExtension.getConfiguration().isPresent()) {
                Configuration configuration = dependencyRepackagerExtension.getConfiguration().get();
                String jarType = "repackaged-jar-" + configuration.getName();
                for (Dependency dependency : configuration.getAllDependencies()) {
                    if (!(dependency instanceof ModuleDependency)) continue;
                    ModuleDependency moduleDependency = (ModuleDependency) dependency;
                    moduleDependency.attributes(attributeContainer -> attributeContainer.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, jarType));
                }
                target.getDependencies().registerTransform(RepackageTransform.class, transform -> {
                    transform.getParameters().getRelocations().set(dependencyRepackagerExtension.getRelocations());
                    transform.getParameters().getRemovals().set(dependencyRepackagerExtension.getRemovals());
                    transform.getParameters().getRemapStrings().set(dependencyRepackagerExtension.getRemapStrings());
                    transform.getParameters().getRemapServices().set(dependencyRepackagerExtension.getRemapServices());
                    transform.getParameters().getRemapManifest().set(dependencyRepackagerExtension.getRemapManifest());
                    transform.getParameters().getRemoveEmptyDirs().set(dependencyRepackagerExtension.getRemoveEmptyDirs());

                    transform.getFrom().attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE);
                    transform.getTo().attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, jarType);
                });
            }
        });
    }

}
