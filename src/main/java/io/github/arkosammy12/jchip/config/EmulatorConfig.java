package io.github.arkosammy12.jchip.config;

import io.github.arkosammy12.jchip.config.database.Chip8Database;
import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.exceptions.EmulatorException;
import io.github.arkosammy12.jchip.ui.SettingsMenu;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.KeyboardLayout;
import io.github.arkosammy12.jchip.video.BuiltInColorPalette;
import io.github.arkosammy12.jchip.video.ColorPalette;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class EmulatorConfig {

    private final int[] rom;
    private final String romTitle;
    private final JChip jchip;

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

    public EmulatorConfig(JChip jchip) throws IOException {
        this.jchip = jchip;

        SettingsMenu settings = this.jchip.getMainWindow().getSettingsMenu();
        Path romPath = settings.getRomPath();
        if (romPath == null) {
            throw new EmulatorException("ROM path cannot be null!");
        }
        byte[] rawRom = Files.readAllBytes(romPath);
        this.rom = new int[rawRom.length];
        for (int i = 0; i < rom.length; i++) {
            this.rom[i] = rawRom[i] & 0xFF;
        }
        Chip8Database database = jchip.getDatabase();
        database.fetchDataForRom(rawRom);

        this.romTitle = database.getProgramTitle().orElse(romPath.getFileName().toString());
        this.keyboardLayout = settings.getKeyboardLayout().orElse(KeyboardLayout.QWERTY);
        this.colorPalette = settings.getColorPalette().orElse(database.getColorPalette().orElse(BuiltInColorPalette.CADMIUM));
        this.displayAngle = settings.getDisplayAngle().orElse(database.getDisplayAngle().orElse(DisplayAngle.DEG_0));

        this.chip8Variant = settings.getChip8Variant().orElse(database.getChip8Variant().orElse(Chip8Variant.CHIP_8));
        Chip8Variant.Quirkset defaultQuirkset = this.chip8Variant.getDefaultQuirkset();
        boolean useVariantQuirks = settings.useVariantQuirks();

        if (useVariantQuirks) {
            this.doVFReset = defaultQuirkset.doVFReset();
            this.doIncrementIndex = defaultQuirkset.doIncrementIndex();
            this.doDisplayWait = defaultQuirkset.doDisplayWait();
            this.doClipping = defaultQuirkset.doClipping();
            this.doShiftVXInPlace = defaultQuirkset.doShiftVXInPlace();
            this.doJumpWithVX = defaultQuirkset.doJumpWithVX();
            this.instructionsPerFrame = defaultQuirkset.instructionsPerFrame().applyAsInt(this.doDisplayWait);
        } else {
            this.doVFReset = settings.doVFReset().orElse(database.doVFReset().orElse(defaultQuirkset.doVFReset()));
            this.doIncrementIndex = settings.doIncrementIndex().orElse(database.doIncrementIndex().orElse(defaultQuirkset.doIncrementIndex()));
            this.doDisplayWait = settings.doDisplayWait().orElse(database.doDisplayWait().orElse((defaultQuirkset.doDisplayWait())));
            this.doClipping = settings.doClipping().orElse(database.doClipping().orElse(defaultQuirkset.doClipping()));
            this.doShiftVXInPlace = settings.doShiftVXInPlace().orElse(database.doShiftVXInPlace().orElse(defaultQuirkset.doShiftVXInPlace()));
            this.doJumpWithVX = settings.doJumpWithVX().orElse(database.doJumpWithVX().orElse(defaultQuirkset.doJumpWithVX()));
            this.instructionsPerFrame = settings.getInstructionsPerFrame().orElse(database.getInstructionsPerFrame().orElse(defaultQuirkset.instructionsPerFrame().applyAsInt(this.doDisplayWait)));
        }
    }

    public JChip getJChip() {
        return this.jchip;
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

    public Chip8Variant getVariant() {
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
