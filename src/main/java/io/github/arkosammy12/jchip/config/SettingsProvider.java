package io.github.arkosammy12.jchip.config;

import io.github.arkosammy12.jchip.util.Variant;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.video.ColorPalette;

import java.util.Optional;

public interface SettingsProvider {

    Optional<Integer> getInstructionsPerFrame();

    Optional<ColorPalette> getColorPalette();

    Optional<DisplayAngle> getDisplayAngle();

    Optional<Variant> getVariant();

    Optional<Boolean> doVFReset();

    Optional<Boolean> doIncrementIndex();

    Optional<Boolean> doDisplayWait();

    Optional<Boolean> doClipping();

    Optional<Boolean> doShiftVXInPlace();

    Optional<Boolean> doJumpWithVX();

}
