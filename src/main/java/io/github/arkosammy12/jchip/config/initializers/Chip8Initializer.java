package io.github.arkosammy12.jchip.config.initializers;

import io.github.arkosammy12.jchip.config.settings.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.Variant;
import io.github.arkosammy12.jchip.emulators.video.ColorPalette;

import java.util.Optional;

public interface Chip8Initializer {

    Optional<Integer> getInstructionsPerFrame();

    Optional<ColorPalette> getColorPalette();

    Optional<DisplayAngle> getDisplayAngle();

    Optional<Variant> getVariant();

    Optional<Boolean> doVFReset();

    Optional<Chip8EmulatorSettings.MemoryIncrementQuirk> getMemoryIncrementQuirk();

    Optional<Boolean> doDisplayWait();

    Optional<Boolean> doClipping();

    Optional<Boolean> doShiftVXInPlace();

    Optional<Boolean> doJumpWithVX();

}
