package net.lenni0451.repackager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import net.lenni0451.repackager.utils.ASMUtils;
import net.lenni0451.repackager.utils.PackageRemapper;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Stream;

@Builder
@AllArgsConstructor
public class Repackager {

    private final Logger logger;
    private final File inputFile;
    private final File outputFile;
    private final Map<String, String> relocations;
    private final boolean remapStrings;
    private final boolean remapServices;
    private final boolean remapManifest;
    private final boolean removeEmptyDirs;

    public void run() throws Throwable {
        if (this.relocations.isEmpty()) throw new IllegalArgumentException("Repackage task requires at least one relocation");

        File file;
        if (this.inputFile.equals(this.outputFile)) {
            //Repackaging in-place breaks the gradle jar cache
            //But it's still possible for convenience
            this.logger.warn("Repackaging in-place is not recommended, consider changing the output file");
            file = this.inputFile;
        } else {
            Files.copy(this.inputFile.toPath(), this.outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            file = this.outputFile;
        }

        PackageRemapper remapper = new PackageRemapper(this.relocations);
        try (FileSystem fileSystem = FileSystems.newFileSystem(file.toPath(), null)) {
            this.remap(fileSystem, remapper);
            if (this.removeEmptyDirs) this.removeEmptyDirectories(fileSystem);
        }
    }

    private void remap(final FileSystem fileSystem, final PackageRemapper remapper) throws IOException {
        try (Stream<Path> paths = Files.walk(fileSystem.getPath("/"))) {
            paths.forEach(path -> {
                try {
                    if (!Files.isRegularFile(path)) return;

                    String pathString = path.toString();
                    boolean slash = pathString.startsWith("/");
                    if (slash) pathString = pathString.substring(1);
                    if (path.getFileName().toString().toLowerCase().endsWith(".class")) {
                        byte[] bytes = Files.readAllBytes(path);
                        ClassNode node = ASMUtils.remap(ASMUtils.fromBytes(bytes), remapper);
                        if (this.remapStrings) this.remapStrings(node, remapper);
                        Files.write(path, ASMUtils.toBytes(node));
                        this.logger.debug("Remapped class: {}", path);
                    } else if (pathString.toLowerCase().startsWith("meta-inf/")) {
                        if (this.remapServices && pathString.startsWith("meta-inf/services/")) {
                            String serviceName = path.toString().substring(19);
                            String remappedServiceName = ASMUtils.remap(remapper, serviceName);
                            String serviceImpl = new String(Files.readAllBytes(path)).trim();
                            String remappedServiceImpl = ASMUtils.remap(remapper, serviceImpl);
                            if (!serviceImpl.equals(remappedServiceImpl)) {
                                Files.write(path, remappedServiceImpl.getBytes(StandardCharsets.UTF_8));
                                this.logger.info("Remapped service implementation: {} -> {}", serviceImpl, remappedServiceImpl);
                            }
                            if (!serviceName.equals(remappedServiceName)) {
                                Path newPath = fileSystem.getPath((slash ? "/" : "") + "META-INF/services/" + remappedServiceName);
                                Files.move(path, newPath);
                                this.logger.info("Remapped service name: {} -> {}", serviceName, remappedServiceName);
                            }
                        } else if (this.remapManifest && path.getFileName().toString().equalsIgnoreCase("manifest.mf")) {
                            Manifest manifest = new Manifest(Files.newInputStream(path));
                            List<Attributes> allAttributes = new ArrayList<>();
                            allAttributes.add(manifest.getMainAttributes());
                            allAttributes.addAll(manifest.getEntries().values());

                            boolean modified = false;
                            for (Attributes attributes : allAttributes) {
                                for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
                                    String value = entry.getValue().toString();
                                    String remappedValue = ASMUtils.remap(remapper, value);
                                    if (remappedValue != null && !value.equals(remappedValue)) {
                                        entry.setValue(remappedValue);
                                        modified = true;
                                    }
                                }
                            }

                            if (modified) {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                manifest.write(baos);
                                Files.write(path, baos.toByteArray());
                                this.logger.info("Remapped manifest: {}", path);
                            }
                        }
                    }

                    if (pathString.toLowerCase(Locale.ROOT).startsWith("meta-inf/versions/")) {
                        String prefix = pathString.substring(0, pathString.indexOf('/', 18) + 1);
                        String suffix = pathString.substring(pathString.indexOf('/', 18) + 1);
                        String remappedSuffix = remapper.mapUnchecked(suffix);
                        if (remappedSuffix != null && !suffix.equals(remappedSuffix)) {
                            Path newPath = fileSystem.getPath((slash ? "/" : "") + prefix + remappedSuffix);
                            Files.createDirectories(newPath.getParent());
                            Files.move(path, newPath);
                            this.logger.debug("Remapped versioned file: {} -> {}", path, newPath);
                        }
                    } else {
                        String remappedPath = remapper.mapUnchecked(pathString);
                        if (remappedPath != null && !pathString.equals(remappedPath)) {
                            Path newPath = fileSystem.getPath((slash ? "/" : "") + remappedPath);
                            Files.createDirectories(newPath.getParent());
                            Files.move(path, newPath);
                            this.logger.debug("Remapped file: {} -> {}", path, newPath);
                        }
                    }
                } catch (Throwable t) {
                    throw new IllegalStateException("Failed to remap file: " + path, t);
                }
            });
        }
    }

    private void removeEmptyDirectories(final FileSystem fileSystem) throws IOException {
        try (Stream<Path> paths = Files.walk(fileSystem.getPath("/"))) {
            paths.sorted((p1, p2) -> p2.toString().length() - p1.toString().length()).forEach(path -> {
                try {
                    if (!Files.isDirectory(path)) return;
                    try (Stream<Path> dirStream = Files.list(path)) {
                        if (dirStream.anyMatch(p -> true)) return;
                        Files.delete(path);
                        this.logger.debug("Removed empty directory: {}", path);
                    }
                } catch (Throwable t) {
                    throw new IllegalStateException("Failed to remove empty directory: " + path, t);
                }
            });
        }
    }

    private void remapStrings(final ClassNode node, final PackageRemapper remapper) {
        for (FieldNode field : node.fields) {
            if (field.value instanceof String) {
                String s = (String) field.value; //Field default values
                s = ASMUtils.remap(remapper, s);
                if (s != null) field.value = s;
            }
        }
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof LdcInsnNode) {
                    LdcInsnNode ldc = (LdcInsnNode) instruction;
                    if (ldc.cst instanceof String) {
                        String s = (String) ldc.cst;
                        s = ASMUtils.remap(remapper, s);
                        if (s != null) ldc.cst = s;
                    }
                }
            }
        }
    }

}
