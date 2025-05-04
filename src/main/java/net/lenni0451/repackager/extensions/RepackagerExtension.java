package net.lenni0451.repackager.extensions;

import net.lenni0451.repackager.settings.RepackageSettings;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;

public abstract class RepackagerExtension implements RepackageSettings {

    private final Project project;

    public RepackagerExtension(final Project project) {
        this.project = project;
        this.initDefaults();
    }

    @InputFile
    public abstract RegularFileProperty getJarFile();

    @Optional
    @InputFile
    public abstract RegularFileProperty getOutputFile();

}
