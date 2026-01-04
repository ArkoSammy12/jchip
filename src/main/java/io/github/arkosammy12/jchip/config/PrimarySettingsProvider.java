package io.github.arkosammy12.jchip.config;

import io.github.arkosammy12.jchip.util.KeyboardLayout;

import java.nio.file.Path;
import java.util.Optional;

public interface PrimarySettingsProvider extends Chip8SettingsInitializer {

    Optional<Path> getRomPath();

    Optional<byte[]> getRawRom();

    boolean useVariantQuirks();

    Optional<KeyboardLayout> getKeyboardLayout();

}
