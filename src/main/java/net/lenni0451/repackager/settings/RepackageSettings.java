package net.lenni0451.repackager.settings;

import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

public interface RepackageSettings {

    @Input
    MapProperty<String, String> getRelocations();

    @Input
    Property<Boolean> getRemapStrings();

    @Input
    Property<Boolean> getRemapServices();

    @Input
    Property<Boolean> getRemapManifest();

    @Input
    Property<Boolean> getRemoveEmptyDirs();

    default void initDefaults() {
        this.getRemapStrings().convention(false);
        this.getRemapServices().convention(true);
        this.getRemapManifest().convention(true);
        this.getRemoveEmptyDirs().convention(false);
    }

}
