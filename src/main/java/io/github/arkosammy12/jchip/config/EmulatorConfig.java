package io.github.arkosammy12.jchip.config;

import io.github.arkosammy12.jchip.config.database.Chip8Database;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.KeyboardLayout;
import io.github.arkosammy12.jchip.video.BuiltInColorPalette;
import io.github.arkosammy12.jchip.video.ColorPalette;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class EmulatorConfig {

    private final int[] rom;
    private final String romTitle;

    private final Chip8Variant chip8Variant;
    private final KeyboardLayout keyboardLayout;
    private final ColorPalette colorPalette;
    private final DisplayAngle displayAngle;
    private final int instructionsPerFrame;
    private final boolean doVFReset;
    private final boolean doIncrementIndex;
    private final boolean doDisplayWait;
    private final boolean doClipping;
    private final boolean doShiftVXInPlace;
    private final boolean doJumpWithVX;

    public EmulatorConfig(String[] args) throws IOException {
        PrimarySettingsProvider primarySettingsProvider;
        if (args.length > 0) {
            primarySettingsProvider = new CommandLineArgs();
            CommandLine cli = new CommandLine(primarySettingsProvider);
            CommandLine.ParseResult parseResult = cli.parseArgs(args);
            Integer executeHelpResult = CommandLine.executeHelpRequest(parseResult);
            int exitCodeOnUsageHelp = cli.getCommandSpec().exitCodeOnUsageHelp();
            int exitCodeOnVersionHelp = cli.getCommandSpec().exitCodeOnVersionHelp();
            if (executeHelpResult != null) {
                if (executeHelpResult == exitCodeOnUsageHelp) {
                    System.exit(exitCodeOnUsageHelp);
                } else if (executeHelpResult == exitCodeOnVersionHelp) {
                    System.exit(exitCodeOnVersionHelp);
                }
            }
        } else {
            primarySettingsProvider = new UISettingsChooser();
        }

        Path romPath = primarySettingsProvider.getRomPath();
        byte[] rawRom = Files.readAllBytes(romPath);
        this.rom = new int[rawRom.length];
        for (int i = 0; i < rom.length; i++) {
            this.rom[i] = rawRom[i] & 0xFF;
        }
        Chip8Database database = new Chip8Database(rawRom);

        this.romTitle = database.getProgramTitle().orElse(romPath.getFileName().toString());
        this.keyboardLayout = primarySettingsProvider.getKeyboardLayout().orElse(KeyboardLayout.QWERTY);
        this.colorPalette = database.getColorPalette().orElse(primarySettingsProvider.getColorPalette().orElse(BuiltInColorPalette.CADMIUM));
        this.displayAngle = primarySettingsProvider.getDisplayAngle().orElse(database.getDisplayAngle().orElse(DisplayAngle.DEG_0));

        Optional<Chip8Variant> optionalChip8VariantArg = primarySettingsProvider.getChip8Variant();
        if (optionalChip8VariantArg.isPresent()) {
            this.chip8Variant = optionalChip8VariantArg.get();
            Chip8Variant.Quirkset defaultQuirkset = this.chip8Variant.getDefaultQuirkset();
            this.doVFReset = primarySettingsProvider.doVFReset().orElse(defaultQuirkset.doVFReset());
            this.doIncrementIndex = primarySettingsProvider.doIncrementIndex().orElse(defaultQuirkset.doIncrementIndex());
            this.doDisplayWait = primarySettingsProvider.doDisplayWait().orElse(defaultQuirkset.doDisplayWait());
            this.doClipping = primarySettingsProvider.doClipping().orElse(defaultQuirkset.doClipping());
            this.doShiftVXInPlace = primarySettingsProvider.doShiftVXInPlace().orElse(defaultQuirkset.doShiftVXInPlace());
            this.doJumpWithVX = primarySettingsProvider.doJumpWithVX().orElse(defaultQuirkset.doJumpWithVX());
            this.instructionsPerFrame = primarySettingsProvider.getInstructionsPerFrame().orElse(defaultQuirkset.instructionsPerFrame().applyAsInt(this.doDisplayWait));
        } else {
            this.chip8Variant = database.getChip8Variant().orElse(Chip8Variant.CHIP_8);
            Chip8Variant.Quirkset defaultQuirkset = this.chip8Variant.getDefaultQuirkset();
            this.doVFReset = primarySettingsProvider.doVFReset().orElse(database.doVFReset().orElse(defaultQuirkset.doVFReset()));
            this.doIncrementIndex = primarySettingsProvider.doIncrementIndex().orElse(database.doIncrementIndex().orElse(defaultQuirkset.doIncrementIndex()));
            this.doDisplayWait = primarySettingsProvider.doDisplayWait().orElse(database.doDisplayWait().orElse((defaultQuirkset.doDisplayWait())));
            this.doClipping = primarySettingsProvider.doClipping().orElse(database.doClipping().orElse(defaultQuirkset.doClipping()));
            this.doShiftVXInPlace = primarySettingsProvider.doShiftVXInPlace().orElse(database.doShiftVXInPlace().orElse(defaultQuirkset.doShiftVXInPlace()));
            this.doJumpWithVX = primarySettingsProvider.doJumpWithVX().orElse(database.doJumpWithVX().orElse(defaultQuirkset.doJumpWithVX()));
            this.instructionsPerFrame = primarySettingsProvider.getInstructionsPerFrame().orElse(database.getInstructionsPerFrame().orElse(defaultQuirkset.instructionsPerFrame().applyAsInt(this.doDisplayWait)));
        }
    }

    public int[] getRom() {
        return Arrays.copyOf(this.rom, this.rom.length);
    }

    public String getProgramTitle() {
        return this.romTitle;
    }

    public int getInstructionsPerFrame() {
        return this.instructionsPerFrame;
    }

    public ColorPalette getColorPalette() {
        return this.colorPalette;
    }

    public KeyboardLayout getKeyboardLayout() {
        return this.keyboardLayout;
    }

    public DisplayAngle getDisplayAngle() {
        return this.displayAngle;
    }

    public Chip8Variant getConsoleVariant() {
        return this.chip8Variant;
    }

    public boolean doVFReset() {
        return this.doVFReset;
    }

    public boolean doIncrementIndex() {
        return this.doIncrementIndex;
    }

    public boolean doDisplayWait() {
        return this.doDisplayWait;
    }

    public boolean doClipping() {
        return this.doClipping;
    }

    public boolean doShiftVXInPlace() {
        return this.doShiftVXInPlace;
    }

    public boolean doJumpWithVX() {
        return this.doJumpWithVX;
    }

}
