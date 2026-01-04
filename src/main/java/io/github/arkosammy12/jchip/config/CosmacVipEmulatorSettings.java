package io.github.arkosammy12.jchip.config;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.emulators.CosmacVipEmulator;
import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.Variant;

import java.util.Optional;

import static io.github.arkosammy12.jchip.util.Variant.COSMAC_VIP;

public class CosmacVipEmulatorSettings extends AbstractEmulatorSettings {

    private final String romTitle;

    private final DisplayAngle displayAngle;
    private final Variant variant;
    private final Chip8Interpreter chip8Interpreter;

    public CosmacVipEmulatorSettings(Jchip jchip, Chip8Interpreter chip8Interpreter, PrimarySettingsProvider settings) {
        super(jchip, settings);

        this.displayAngle = settings.getDisplayAngle().orElse(DisplayAngle.DEG_0);
        this.romTitle = settings.getRomPath().map(path -> path.getFileName().toString()).orElse(null);
        this.variant = settings.getVariant().orElse(COSMAC_VIP);
        this.chip8Interpreter = chip8Interpreter;
    }

    @Override
    public Optional<String> getRomTitle() {
        return Optional.ofNullable(this.romTitle);
    }

    @Override
    public DisplayAngle getDisplayAngle() {
        return this.displayAngle;
    }

    @Override
    public Variant getVariant() {
        return this.variant;
    }

    @Override
    public Emulator getEmulator() {
        return new CosmacVipEmulator(this, this.chip8Interpreter);
    }

    public enum Chip8Interpreter {
        CHIP_8,
        CHIP_8X,
        NONE
    }

}
