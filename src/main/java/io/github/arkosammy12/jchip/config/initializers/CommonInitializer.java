package io.github.arkosammy12.jchip.config.initializers;

import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.KeyboardLayout;
import io.github.arkosammy12.jchip.util.Variant;

import java.nio.file.Path;
import java.util.Optional;

public interface CommonInitializer {

    Optional<Path> getRomPath();

    Optional<byte[]> getRawRom();

    Optional<Variant> getVariant();

    Optional<DisplayAngle> getDisplayAngle();

    Optional<KeyboardLayout> getKeyboardLayout();

    Optional<Boolean> useVariantQuirks();

}
