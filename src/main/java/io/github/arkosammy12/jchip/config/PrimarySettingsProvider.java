package io.github.arkosammy12.jchip.config;

import io.github.arkosammy12.jchip.util.KeyboardLayout;

import java.nio.file.Path;
import java.util.Optional;

public interface PrimarySettingsProvider extends SettingsProvider {

    Optional<Path> getRomPath();

    Optional<byte[]> getRawRom();

    boolean useVariantQuirks();

    Optional<KeyboardLayout> getKeyboardLayout();

}
