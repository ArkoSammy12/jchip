package io.github.arkosammy12.jchip.config;

import java.nio.file.Path;
import java.util.Optional;

public interface PrimarySettingsProvider extends SettingsProvider {

    Optional<Path> getRomPath();

    Optional<byte[]> getRawRom();

    boolean useVariantQuirks();

}
