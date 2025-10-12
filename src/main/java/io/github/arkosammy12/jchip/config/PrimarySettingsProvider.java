package io.github.arkosammy12.jchip.config;

import io.github.arkosammy12.jchip.util.KeyboardLayout;

import java.nio.file.Path;
import java.util.Optional;

public interface PrimarySettingsProvider extends SettingsProvider {

    Path getRomPath();

    Optional<KeyboardLayout> getKeyboardLayout();

}
