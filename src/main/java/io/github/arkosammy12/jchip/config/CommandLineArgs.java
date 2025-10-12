package io.github.arkosammy12.jchip.config;

import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.util.Chip8Variant;
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
        description = "Runs a CHIP-8 rom with various configurable options."
)
public class CommandLineArgs implements PrimarySettingsProvider {

    @CommandLine.Option(names = {"--rom", "-r"}, required = true)
    private Path romPath;

    @CommandLine.Option(names = {"--variant", "-v"}, converter = Chip8Variant.Converter.class, defaultValue = CommandLine.Option.NULL_VALUE)
    private Optional<Chip8Variant> chip8Variant;

    @CommandLine.Option(names = {"--instructions-per-frame", "-i"}, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Integer> instructionsPerFrame;

    @CommandLine.Option(names = {"--color-palette", "-c"}, converter = BuiltInColorPalette.Converter.class, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<ColorPalette> colorPalette;

    @CommandLine.Option(names = {"--keyboard-layout", "-k"}, defaultValue = "qwerty", converter = KeyboardLayout.Converter.class, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<KeyboardLayout> keyboardLayout;

    @CommandLine.Option(names = {"-a", "--angle"}, converter = DisplayAngle.Converter.class, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<DisplayAngle> displayAngle;

    @CommandLine.Option(names = "--vf-reset", negatable = true, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Boolean> doVFReset;

    @CommandLine.Option(names = "--increment-i", negatable = true, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Boolean> doIncrementIndex;

    @CommandLine.Option(names = "--display-wait", negatable = true, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Boolean> doDisplayWait;

    @CommandLine.Option(names = "--clipping", negatable = true, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Boolean> doClipping;

    @CommandLine.Option(names = "--shift-vx-in-place", negatable = true, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Boolean> doShiftVXInPlace;

    @CommandLine.Option(names = "--jump-with-vx", negatable = true, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Boolean> doJumpWithVX;

    @Override
    public Path getRomPath() {
        return convertToAbsolutePathIfNeeded(romPath);
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
    public Optional<Chip8Variant> getChip8Variant() {
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
