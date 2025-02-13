package net.lenni0451.repackager.tasks;

import net.lenni0451.repackager.Repackager;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

public abstract class RepackageTask extends DefaultTask {

    @InputFile
    public abstract RegularFileProperty getJarFile();

    @Optional
    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @Input
    public abstract MapProperty<String, String> getRelocations();

    @Input
    public abstract Property<Boolean> getRemapStrings();

    @Input
    public abstract Property<Boolean> getRemapServices();

    @Input
    public abstract Property<Boolean> getRemapManifest();

    @Input
    public abstract Property<Boolean> getRemoveEmptyDirs();

    public RepackageTask() {
        this.getRemapStrings().convention(false);
        this.getRemapServices().convention(true);
        this.getRemapManifest().convention(true);
        this.getRemoveEmptyDirs().convention(false);
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
