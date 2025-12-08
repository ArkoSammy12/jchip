package io.github.arkosammy12.jchip.config;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.config.database.Chip8Database;
import io.github.arkosammy12.jchip.emulators.*;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.DisplayNameProvider;
import io.github.arkosammy12.jchip.util.HexSpriteFont;
import io.github.arkosammy12.jchip.util.Variant;
import io.github.arkosammy12.jchip.video.BuiltInColorPalette;
import io.github.arkosammy12.jchip.video.ColorPalette;
import picocli.CommandLine;

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
    private final MemoryIncrementQuirk memoryIncrementQuirk;
    private final boolean doDisplayWait;
    private final boolean doClipping;
    private final boolean doShiftVXInPlace;
    private final boolean doJumpWithVX;

    public Chip8EmulatorSettings(Jchip jchip) {
        super(jchip);

        PrimarySettingsProvider settings = this.getJchip().getMainWindow().getSettingsBar();
        Chip8Database database = jchip.getDatabase();
        database.fetchDataForRom(this.getRawRom());

        this.romTitle = database.getProgramTitle().orElse(settings.getRomPath().map(path -> path.getFileName().toString()).orElse(null));
        this.colorPalette = settings.getColorPalette().orElse(database.getColorPalette().orElse(BuiltInColorPalette.CADMIUM));
        this.displayAngle = settings.getDisplayAngle().orElse(database.getDisplayAngle().orElse(DisplayAngle.DEG_0));
        this.variant = settings.getVariant().orElse(database.getVariant().orElse(CHIP_8));

        DefaultQuirkSet defaultQuirkset = getDefaultQuirkSet(this.variant).orElse(new DefaultQuirkSet(false, MemoryIncrementQuirk.NONE, false, false, false, false, _ -> 0));
        boolean useVariantQuirks = settings.useVariantQuirks();
        this.hexSpriteFont = getHexSpriteFont(this.variant);

        if (useVariantQuirks) {
            this.doVFReset = defaultQuirkset.doVFReset();
            this.memoryIncrementQuirk = defaultQuirkset.memoryIncrementQuirk();
            this.doDisplayWait = defaultQuirkset.doDisplayWait();
            this.doClipping = defaultQuirkset.doClipping();
            this.doShiftVXInPlace = defaultQuirkset.doShiftVXInPlace();
            this.doJumpWithVX = defaultQuirkset.doJumpWithVX();
            this.instructionsPerFrame = defaultQuirkset.instructionsPerFrame().applyAsInt(this.doDisplayWait);
        } else {
            this.doVFReset = settings.doVFReset().orElse(database.doVFReset().orElse(defaultQuirkset.doVFReset()));
            this.memoryIncrementQuirk = settings.getMemoryIncrementQuirk().orElse(database.getMemoryIncrementQuirk().orElse(defaultQuirkset.memoryIncrementQuirk()));
            this.doDisplayWait = settings.doDisplayWait().orElse(database.doDisplayWait().orElse((defaultQuirkset.doDisplayWait())));
            this.doClipping = settings.doClipping().orElse(database.doClipping().orElse(defaultQuirkset.doClipping()));
            this.doShiftVXInPlace = settings.doShiftVXInPlace().orElse(database.doShiftVXInPlace().orElse(defaultQuirkset.doShiftVXInPlace()));
            this.doJumpWithVX = settings.doJumpWithVX().orElse(database.doJumpWithVX().orElse(defaultQuirkset.doJumpWithVX()));
            this.instructionsPerFrame = settings.getInstructionsPerFrame().orElse(database.getInstructionsPerFrame().orElse(defaultQuirkset.instructionsPerFrame().applyAsInt(this.doDisplayWait)));
        }
    }

    @Override
    public Optional<String> getRomTitle() {
        return Optional.ofNullable(this.romTitle);
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

    public MemoryIncrementQuirk getMemoryIncrementQuirk() {
        return this.memoryIncrementQuirk;
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
            case CHIP_8, CHIP_48 -> new Chip8Emulator(this);
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
            case CHIP_48 -> new HexSpriteFont(HexSpriteFont.CHIP_48, null);
            case SUPER_CHIP_LEGACY, SUPER_CHIP_MODERN -> new HexSpriteFont(HexSpriteFont.CHIP_48, HexSpriteFont.SCHIP_11_BIG);
            case XO_CHIP, HYPERWAVE_CHIP_64 -> new HexSpriteFont(HexSpriteFont.CHIP_48, HexSpriteFont.OCTO_BIG);
            case MEGA_CHIP -> new HexSpriteFont(HexSpriteFont.CHIP_48, HexSpriteFont.MEGACHIP_8_BIG);
            default -> new HexSpriteFont(HexSpriteFont.CHIP_8_VIP, null);
        };
    }

    private static Optional<DefaultQuirkSet> getDefaultQuirkSet(Variant variant) {
        return switch (variant) {
            case CHIP_8, CHIP_8X -> Optional.of(new DefaultQuirkSet(true, MemoryIncrementQuirk.INCREMENT_X_1, true, true, false, false, doDisplayWait -> doDisplayWait ? 15 : 11));
            case CHIP_48 -> Optional.of(new DefaultQuirkSet(false, MemoryIncrementQuirk.INCREMENT_X, true, true, true, true, doDisplayWait -> doDisplayWait ? 15 : 11));
            case SUPER_CHIP_LEGACY -> Optional.of(new DefaultQuirkSet(false, MemoryIncrementQuirk.NONE, true, true, true, true, _ -> 30));
            case SUPER_CHIP_MODERN -> Optional.of(new DefaultQuirkSet(false, MemoryIncrementQuirk.NONE, false, true, true, true, _ -> 30));
            case XO_CHIP, HYPERWAVE_CHIP_64 -> Optional.of(new DefaultQuirkSet(false, MemoryIncrementQuirk.INCREMENT_X_1, false, false, false, false, _ -> 1000));
            case MEGA_CHIP -> Optional.of(new DefaultQuirkSet(false, MemoryIncrementQuirk.NONE, false, false, true, false, _ -> 3000));
            default -> Optional.empty();
        };
    }

    public enum MemoryIncrementQuirk implements DisplayNameProvider {
        NONE("None", "none"),
        INCREMENT_X("Increment by X", "increment-x"),
        INCREMENT_X_1("Increment by X + 1", "increment-x-1");

        private final String name;
        private final String identifier;

        MemoryIncrementQuirk(String name, String identifier) {
            this.name = name;
            this.identifier = identifier;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public String getDisplayName() {
            return this.name;
        }

        public static MemoryIncrementQuirk getMemoryIncrementQuirkForIdentifier(String identifier) {
            for (MemoryIncrementQuirk memoryIncrementQuirk : MemoryIncrementQuirk.values()) {
                if (memoryIncrementQuirk.identifier.equals(identifier)) {
                    return memoryIncrementQuirk;
                }
            }
            throw new IllegalArgumentException("Invalid memory increment quirk value: " + identifier + "!");
        }

        public static class Converter implements CommandLine.ITypeConverter<MemoryIncrementQuirk> {

            @Override
            public MemoryIncrementQuirk convert(String value) {
                return getMemoryIncrementQuirkForIdentifier(value);
            }

        }

    }

    private record DefaultQuirkSet(
            boolean doVFReset,
            MemoryIncrementQuirk memoryIncrementQuirk,
            boolean doDisplayWait,
            boolean doClipping,
            boolean doShiftVXInPlace,
            boolean doJumpWithVX,
            ToIntFunction<Boolean> instructionsPerFrame
    ) {

    }

}
