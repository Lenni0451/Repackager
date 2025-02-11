package net.lenni0451.repackager;

import net.lenni0451.repackager.extensions.RepackageExtension;
import net.lenni0451.repackager.tasks.RepackageTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class RepackagerPlugin implements Plugin<Project> {

    @Override
    public void apply(Project target) {
        RepackageExtension extension = target.getExtensions().create("repackage", RepackageExtension.class);
        RepackageTask task = target.getTasks().register("repackageTask", RepackageTask.class, thiz -> {
            thiz.getJarFile().set(extension.getJarFile());
            thiz.getOutputFile().set(extension.getOutputFile());
            thiz.getRelocations().set(extension.getRelocations());
            thiz.getRemapStrings().set(extension.getRemapStrings());
            thiz.getRemapServices().set(extension.getRemapServices());
            thiz.getRemapManifest().set(extension.getRemapManifest());
            thiz.getRemoveEmptyDirs().set(extension.getRemoveEmptyDirs());
        }).get();

        target.afterEvaluate(project -> {
            Task buildTask = project.getTasks().findByName("build");
            if (buildTask != null) buildTask.finalizedBy(task);
        });
    }

}
