package net.lenni0451.repackager.settings;

import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;

import java.util.Collections;

public interface RepackageSettings {

    @Input
    MapProperty<String, String> getRelocations();

    @Input
    SetProperty<String> getRemovals();

    @Input
    Property<Boolean> getRemapStrings();

    @Input
    Property<Boolean> getRemapServices();

    @Input
    Property<Boolean> getRemapManifest();

    @Input
    Property<Boolean> getRemoveEmptyDirs();

    default void initDefaults() {
        this.getRelocations().convention(Collections.emptyMap());
        this.getRemovals().convention(Collections.emptySet());
        this.getRemapStrings().convention(false);
        this.getRemapServices().convention(true);
        this.getRemapManifest().convention(true);
        this.getRemoveEmptyDirs().convention(false);
    }

}
