package net.lenni0451.repackager.tasks;

import net.lenni0451.repackager.Repackager;
import net.lenni0451.repackager.settings.RepackageSettings;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class RepackageTask extends DefaultTask implements RepackageSettings {

    @InputFile
    public abstract RegularFileProperty getJarFile();

    @Optional
    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    public RepackageTask() {
        this.initDefaults();
    }

    @TaskAction
    public void run() throws Throwable {
        Repackager.builder()
                .logger(this.getLogger())
                .inputFile(this.getJarFile().get().getAsFile())
                .outputFile(this.getOutputFile().isPresent() ? this.getOutputFile().get().getAsFile() : this.getJarFile().get().getAsFile())
                .relocations(this.getRelocations().get())
                .remapStrings(this.getRemapStrings().get())
                .remapServices(this.getRemapServices().get())
                .remapManifest(this.getRemapManifest().get())
                .removeEmptyDirs(this.getRemoveEmptyDirs().get())
                .build().run();
    }

}
