package io.github.arkosammy12.jchip.config;

import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.video.ColorPalette;

import java.util.Optional;

public interface SettingsProvider {

    Optional<Integer> getInstructionsPerFrame();

    Optional<ColorPalette> getColorPalette();

    Optional<DisplayAngle> getDisplayAngle();

    Optional<Chip8Variant> getChip8Variant();

    Optional<Boolean> doVFReset();

    Optional<Boolean> doIncrementIndex();

    Optional<Boolean> doDisplayWait();

    Optional<Boolean> doClipping();

    Optional<Boolean> doShiftVXInPlace();

    Optional<Boolean> doJumpWithVX();

}
