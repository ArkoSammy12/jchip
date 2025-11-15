package io.github.arkosammy12.jchip.config;

import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.util.Variant;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.KeyboardLayout;
import io.github.arkosammy12.jchip.video.BuiltInColorPalette;
import io.github.arkosammy12.jchip.video.ColorPalette;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.Optional;

@CommandLine.Command(
        name = "jchip",
        mixinStandardHelpOptions = true,
        version = Main.VERSION_STRING,
        description = "Initializes jchip with the desired configurations and starts emulation."
)
public class CLIArgs implements PrimarySettingsProvider {

    @CommandLine.Option(
            names = {"--rom", "-r"},
            required = true,
            description = "The path of the file containing the raw binary ROM data."
    )
    private Path romPath;

    @CommandLine.Option(
            names = {"--variant", "-v"},
            converter = Variant.Converter.class,
            defaultValue = CommandLine.Option.NULL_VALUE,
            description = "Select the desired CHIP-8 variant or leave unspecified."
    )
    private Optional<Variant> chip8Variant;

    @CommandLine.Option(
            names = {"--instructions-per-frame", "-i"},
            defaultValue = CommandLine.Option.NULL_VALUE,
            description = "Set the desired IPF or leave unspecified.."
    )
    private Optional<Integer> instructionsPerFrame;

    @CommandLine.Option(
            names = {"--color-palette", "-c"},
            converter = BuiltInColorPalette.Converter.class,
            defaultValue = CommandLine.Option.NULL_VALUE,
            description = "Select the desired display color palette or leave unspecified."
    )
    private Optional<ColorPalette> colorPalette;

    @CommandLine.Option(
            names = {"--keyboard-layout", "-k"},
            defaultValue = "qwerty",
            converter = KeyboardLayout.Converter.class,
            description = "Select the desired keyboard layout configuration for using the CHIP-8 keypad."
    )
    private Optional<KeyboardLayout> keyboardLayout;

    @CommandLine.Option(
            names = "--use-variant-quirks",
            negatable = true,
            defaultValue = "false",
            description = "Force the used quirks to be of the variant used to run the current ROM."
    )
    private Boolean useVariantQuirks;

    @CommandLine.Option(
            names = {"-a", "--angle"},
            converter = DisplayAngle.Converter.class,
            defaultValue = CommandLine.Option.NULL_VALUE,
            description = "Select the screen rotation or leave unspecified."
    )
    private Optional<DisplayAngle> displayAngle;

    @CommandLine.Option(
            names = "--vf-reset",
            negatable = true,
            defaultValue = CommandLine.Option.NULL_VALUE,
            description = "Toggle the VF Reset quirk or leave unspecified."
    )
    private Optional<Boolean> doVFReset;

    @CommandLine.Option(
            names = "--increment-i",
            negatable = true,
            defaultValue = CommandLine.Option.NULL_VALUE,
            description = "Toggle the Increment Index quirk or leave unspecified."
    )
    private Optional<Boolean> doIncrementIndex;

    @CommandLine.Option(
            names = "--display-wait",
            negatable = true,
            defaultValue = CommandLine.Option.NULL_VALUE,
            description = "Toggle the Display Wait quirk or leave unspecified."
    )
    private Optional<Boolean> doDisplayWait;

    @CommandLine.Option(
            names = "--clipping",
            negatable = true,
            defaultValue = CommandLine.Option.NULL_VALUE,
            description = "Toggle the Clipping quirk or leave unspecified."
    )
    private Optional<Boolean> doClipping;

    @CommandLine.Option(
            names = "--shift-vx-in-place",
            negatable = true,
            defaultValue = CommandLine.Option.NULL_VALUE,
            description = "Toggle the Shift VX in Place quirk or leave unspecified."
    )
    private Optional<Boolean> doShiftVXInPlace;

    @CommandLine.Option(
            names = "--jump-with-vx",
            negatable = true,
            defaultValue = CommandLine.Option.NULL_VALUE,
            description = "Toggle the Jump with VX quirk or leave unspecified."
    )
    private Optional<Boolean> doJumpWithVX;

    @Override
    public Optional<byte[]> getRawRom() {
        return Optional.of(EmulatorSettings.getRawRom(convertToAbsolutePathIfNeeded(romPath)));
    }

    @Override
    public Optional<Path> getRomPath() {
        return Optional.of(convertToAbsolutePathIfNeeded(romPath));
    }

    @Override
    public boolean useVariantQuirks() {
        return this.useVariantQuirks;
    }

    @Override
    public Optional<Integer> getInstructionsPerFrame() {
        return this.instructionsPerFrame;
    }

    @Override
    public Optional<ColorPalette> getColorPalette() {
        return this.colorPalette;
    }

    public Optional<KeyboardLayout> getKeyboardLayout() {
        return this.keyboardLayout;
    }

    @Override
    public Optional<DisplayAngle> getDisplayAngle() {
        return this.displayAngle;
    }

    @Override
    public Optional<Variant> getVariant() {
        return this.chip8Variant;
    }

    @Override
    public Optional<Boolean> doVFReset() {
        return this.doVFReset;
    }

    @Override
    public Optional<Boolean> doIncrementIndex() {
        return this.doIncrementIndex;
    }

    @Override
    public Optional<Boolean> doDisplayWait() {
        return this.doDisplayWait;
    }

    @Override
    public Optional<Boolean> doClipping() {
        return this.doClipping;
    }

    @Override
    public Optional<Boolean> doShiftVXInPlace() {
        return this.doShiftVXInPlace;
    }

    @Override
    public Optional<Boolean> doJumpWithVX() {
        return this.doJumpWithVX;
    }

    public static Path convertToAbsolutePathIfNeeded(Path path) {
        if (path == null) {
            return null;
        }
        return path.isAbsolute() ? path : path.toAbsolutePath();
    }

}
