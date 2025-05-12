package net.lenni0451.repackager.transforms;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.lenni0451.repackager.Repackager;
import net.lenni0451.repackager.settings.RepackageSettings;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

import java.io.File;

@Slf4j
public abstract class RepackageTransform implements TransformAction<RepackageTransform.Parameters> {

    @InputArtifact
    public abstract Provider<FileSystemLocation> getInputArtifact();

    @Override
    @SneakyThrows
    public void transform(TransformOutputs outputs) {
        File input = this.getInputArtifact().get().getAsFile();
        File output = outputs.file(input.getName().replace(".jar", "-repackaged.jar"));
        Repackager.builder()
                .logger(log)
                .inputFile(input)
                .outputFile(output)
                .relocations(this.getParameters().getRelocations().get())
                .removals(this.getParameters().getRemovals().get())
                .remapStrings(this.getParameters().getRemapStrings().get())
                .remapServices(this.getParameters().getRemapServices().get())
                .remapManifest(this.getParameters().getRemapManifest().get())
                .removeEmptyDirs(this.getParameters().getRemoveEmptyDirs().get())
                .build().run();
    }


    public interface Parameters extends TransformParameters, RepackageSettings {
    }

}
