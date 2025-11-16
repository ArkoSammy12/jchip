package io.github.arkosammy12.jchip.config;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.config.database.Chip8Database;
import io.github.arkosammy12.jchip.emulators.*;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.HexSpriteFont;
import io.github.arkosammy12.jchip.util.Variant;
import io.github.arkosammy12.jchip.video.BuiltInColorPalette;
import io.github.arkosammy12.jchip.video.ColorPalette;

import java.util.Optional;
import java.util.function.ToIntFunction;

import static io.github.arkosammy12.jchip.util.Variant.*;

public class Chip8EmulatorSettings extends AbstractEmulatorSettings {

    private final String romTitle;

    private final Variant variant;
    private final ColorPalette colorPalette;
    private final DisplayAngle displayAngle;
    private final HexSpriteFont hexSpriteFont;
    private final int instructionsPerFrame;
    private final boolean doVFReset;
    private final boolean doIncrementIndex;
    private final boolean doDisplayWait;
    private final boolean doClipping;
    private final boolean doShiftVXInPlace;
    private final boolean doJumpWithVX;

    public Chip8EmulatorSettings(JChip jchip) {
        super(jchip);

        PrimarySettingsProvider settings = this.getJChip().getMainWindow().getSettingsBar();
        Chip8Database database = jchip.getDatabase();
        database.fetchDataForRom(this.getRawRom());

        this.romTitle = database.getProgramTitle().orElse(settings.getRomPath().map(path -> path.getFileName().toString()).orElse(null));
        this.colorPalette = settings.getColorPalette().orElse(database.getColorPalette().orElse(BuiltInColorPalette.CADMIUM));
        this.displayAngle = settings.getDisplayAngle().orElse(database.getDisplayAngle().orElse(DisplayAngle.DEG_0));
        this.variant = settings.getVariant().orElse(database.getVariant().orElse(CHIP_8));

        DefaultQuirkSet defaultQuirkset = getDefaultQuirkSet(this.variant).orElse(new DefaultQuirkSet(false, false, false, false, false, false, _ -> 0));
        boolean useVariantQuirks = settings.useVariantQuirks();
        this.hexSpriteFont = getHexSpriteFont(this.variant);

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

    @Override
    public String getProgramTitle() {
        return this.romTitle;
    }

    public int getInstructionsPerFrame() {
        return this.instructionsPerFrame;
    }

    public ColorPalette getColorPalette() {
        return this.colorPalette;
    }

    @Override
    public DisplayAngle getDisplayAngle() {
        return this.displayAngle;
    }

    public HexSpriteFont getHexSpriteFont() {
        return this.hexSpriteFont;
    }

    @Override
    public Variant getVariant() {
        return this.variant;
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

    @Override
    public Emulator getEmulator() {
        return switch (this.variant) {
            case CHIP_8 -> new Chip8Emulator(this);
            case STRICT_CHIP_8 -> new StrictChip8Emulator(this);
            case CHIP_8X -> new Chip8XEmulator(this);
            case SUPER_CHIP_LEGACY -> new SChipEmulator(this, false);
            case SUPER_CHIP_MODERN -> new SChipEmulator(this, true);
            case XO_CHIP -> new XOChipEmulator(this);
            case MEGA_CHIP -> new MegaChipEmulator(this);
            case HYPERWAVE_CHIP_64 -> new HyperWaveChip64Emulator(this);
            default -> throw new IllegalArgumentException();
        };
    }

    private static HexSpriteFont getHexSpriteFont(Variant variant) {
        return switch (variant) {
            case SUPER_CHIP_LEGACY, SUPER_CHIP_MODERN -> new HexSpriteFont(HexSpriteFont.CHIP_48, HexSpriteFont.SCHIP_11_BIG);
            case XO_CHIP, HYPERWAVE_CHIP_64 -> new HexSpriteFont(HexSpriteFont.CHIP_48, HexSpriteFont.OCTO_BIG);
            case MEGA_CHIP -> new HexSpriteFont(HexSpriteFont.CHIP_48, HexSpriteFont.MEGACHIP_8_BIG);
            default -> new HexSpriteFont(HexSpriteFont.CHIP_8_VIP, null);
        };
    }

    private static Optional<DefaultQuirkSet> getDefaultQuirkSet(Variant variant) {
        return switch (variant) {
            case CHIP_8, CHIP_8X -> Optional.of(new DefaultQuirkSet(true, true, true, true, false, false, doDisplayWait -> doDisplayWait ? 15 : 11));
            case SUPER_CHIP_LEGACY -> Optional.of(new DefaultQuirkSet(false, false, true, true, true, true, _ -> 30));
            case SUPER_CHIP_MODERN -> Optional.of(new DefaultQuirkSet(false, false, false, true, true, true, _ -> 30));
            case XO_CHIP, HYPERWAVE_CHIP_64 -> Optional.of(new DefaultQuirkSet(false, true, false, false, false, false, _ -> 1000));
            case MEGA_CHIP -> Optional.of(new DefaultQuirkSet(false, false, false, false, true, false, _ -> 1000));
            default -> Optional.empty();
        };
    }

    private record DefaultQuirkSet(
            boolean doVFReset,
            boolean doIncrementIndex,
            boolean doDisplayWait,
            boolean doClipping,
            boolean doShiftVXInPlace,
            boolean doJumpWithVX,
            ToIntFunction<Boolean> instructionsPerFrame
    ) {

    }

}
