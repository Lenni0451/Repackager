package net.lenni0451.repackager.extensions;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;

public abstract class RepackageExtension {

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

    public RepackageExtension() {
        this.getRemapStrings().convention(false);
        this.getRemapServices().convention(true);
        this.getRemapManifest().convention(true);
        this.getRemoveEmptyDirs().convention(false);
    }

}
