# Repackager
Gradle plugin for repackaging the build output and dependencies.\
Dependencies are remapped before they are imported into the IDE. This allows you to use multiple versions of the same library in your project.

## Installation
To use this plugin, you need to add my maven server to your plugin repositories:
```groovy
pluginManagement {
    repositories {
        maven {
            name = "lenni0451 releases"
            url = "https://maven.lenni0451.net/releases"
        }
    }
}
```

After adding the repository, you can add the plugin to your project:
```groovy
plugins {
    id "net.lenni0451.repackager" version "x.x.x"
}
```

## Configuration
### Extensions
Configure build output repackaging:
```groovy
repackager {
    jarFile = jar.archiveFile //The input jar file
    outputFile = file("$buildDir/libs/${project.name}-repackaged.jar") //The output jar file (optional, default: override input)
    relocations = [ //All relocated packages
            "net": "repackaged.net", //Relocate all classes in packages starting with "net" to "repackaged.net"
            "org.": "repackaged.org." //Relocate all classes in the "org" package to the "repackaged.org" package
    ]
    remapStrings = true //Search for matches in strings and remap them (optional, default: false)
    remapServices = true //Remap service providers (optional, default: true)
    remapManifest = true //Remap the manifest file (optional, default: true)
    removeEmptyDirs = true //Remove empty directories after repackaging (optional, default: false)
}
```

Configure dependency repackaging:
```groovy
dependencyRepackager {
    configuration = configurations.transformed //The configuration to search for dependencies
    relocations = [ //All relocated packages
            "net": "repackaged.net", //Relocate all classes in packages starting with "net" to "repackaged.net"
            "org.": "repackaged.org." //Relocate all classes in the "org" package to the "repackaged.org" package
    ]
    remapStrings = true //Search for matches in strings and remap them (optional, default: false)
    remapServices = true //Remap service providers (optional, default: true)
    remapManifest = true //Remap the manifest file (optional, default: true)
    removeEmptyDirs = true //Remove empty directories after repackaging (optional, default: false)
}
```

### Tasks
#### build output
If you want more control over the repackaging task, you can register the task yourself:
```groovy
tasks.register("repackage", net.lenni0451.repackager.tasks.RepackageTask) {
    jarFile = jar.archiveFile
    outputFile = file("$buildDir/libs/${project.name}-repackaged.jar")
    relocations = [
                    "net": "repackaged.net",
                    "org.": "repackaged.org."
    ]
    remapStrings = true
    remapServices = true
    remapManifest = true
    removeEmptyDirs = true
}
```

#### dependencies
Because dependency remapping is a more involved process, a register method is provided:
```groovy
dependencyRepackager.register(
        /* configuration = */ configurations.transformedLib,
        /* relocations = */ [
                        "net": "repackaged.net",
                        "org.": "repackaged.org."
        ],
        /* remapStrings = */ false,
        /* remapServices = */ true,
        /* remapManifest = */ true,
        /* removeEmptyDirs = */ false
)
```
This is the recommended (and only) way if you want to repackage multiple configurations.
