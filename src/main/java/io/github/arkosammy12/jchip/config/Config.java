package io.github.arkosammy12.jchip.config;

import com.google.errorprone.annotations.Var;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.Variant;
import io.github.arkosammy12.jchip.video.BuiltInColorPalette;
import io.github.arkosammy12.jchip.video.ColorPalette;
import io.github.arkosammy12.monkeyconfig.base.ConfigManager;
import io.github.arkosammy12.monkeyconfig.base.Setting;
import io.github.arkosammy12.monkeyconfig.builders.ConfigManagerBuilderKt;
import io.github.arkosammy12.monkeyconfig.managers.ConfigManagerUtils;
import io.github.arkosammy12.monkeyconfig.types.ListType;
import io.github.arkosammy12.monkeyconfig.types.StringType;
import io.github.arkosammy12.monkeyconfig.util.ElementPath;
import kotlin.OptionalExpectation;
import kotlin.Unit;
import net.harawata.appdirs.AppDirsFactory;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import javax.swing.text.html.Option;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Config implements Chip8Initializer {

    private static ElementPath USE_VARIANT_QUIRKS;

    private static ElementPath VF_RESET;
    private static ElementPath I_INCREMENT;
    private static ElementPath DISPLAY_WAIT;
    private static ElementPath CLIPPING;
    private static ElementPath SHIFT_VX_IN_PLACE;
    private static ElementPath JUMP_WITH_VX;

    private static ElementPath VARIANT;
    private static ElementPath COLOR_PALETTE;
    private static ElementPath DISPLAY_ANGLE;
    private static ElementPath INSTRUCTIONS_PER_FRAME;

    private static ElementPath VOLUME;
    private static ElementPath MUTED;
    private static ElementPath KEYBOARD_LAYOUT;
    private static ElementPath SHOW_INFO_BAR;

    private static ElementPath SHOW_DEBUGGER;
    private static ElementPath SHOW_DISASSEMBLER;

    private static final Path APP_DIR = Path.of(AppDirsFactory.getInstance().getUserDataDir("jchip", null, null));
    public static final ConfigManager CONFIG_MANAGER = ConfigManagerBuilderKt.tomlConfigManager("data", APP_DIR.resolve("data.toml"), manager -> {
        manager.section("file", file -> {
            file.<Path, StringType>listSetting("recent_files", List.of(), recentFiles -> {
               recentFiles.setSerializer(list -> new ListType<>(list.stream().map(e -> new StringType(e.toString())).collect(Collectors.toList())));
               recentFiles.setDeserializer(list -> list.getValue().stream().map(e -> Path.of(e.getValue())).collect(Collectors.toList()));
               return Unit.INSTANCE;
            });
            return Unit.INSTANCE;
        });
        manager.section("emulator", emulator -> {
            emulator.section("quirks", quirks -> {
                quirks.setComment("""
                        Valid values: "enabled", "disabled", "unspecified"\s""");

                USE_VARIANT_QUIRKS = quirks.booleanSetting("use_variant_quirks", false, _ -> Unit.INSTANCE);
                VF_RESET = quirks.enumSetting("vf_reset", BooleanValue.unspecified, _ -> Unit.INSTANCE);
                I_INCREMENT = quirks.enumSetting("i_increment", MemoryIncrementValue.unspecified, _ -> Unit.INSTANCE);
                DISPLAY_WAIT = quirks.enumSetting("display_wait", BooleanValue.unspecified, _ -> Unit.INSTANCE);
                CLIPPING = quirks.enumSetting("clipping", BooleanValue.unspecified, _ -> Unit.INSTANCE);
                SHIFT_VX_IN_PLACE = quirks.enumSetting("shift_vx_in_place", BooleanValue.unspecified, _ -> Unit.INSTANCE);
                JUMP_WITH_VX = quirks.enumSetting("jump_with_vx", BooleanValue.unspecified, _ -> Unit.INSTANCE);
                return Unit.INSTANCE;
            });
            VARIANT = emulator.enumSetting("variant", VariantValue.unspecified, _ -> Unit.INSTANCE);
            COLOR_PALETTE = emulator.enumSetting("color_palette", ColorPaletteValue.unspecified, _ -> Unit.INSTANCE);
            DISPLAY_ANGLE = emulator.numberSetting("display_angle", -1, displayAngle -> {
                displayAngle.setMinValue(-1);
                displayAngle.setMinValue(360);
                return Unit.INSTANCE;
            });
            INSTRUCTIONS_PER_FRAME = emulator.numberSetting("instructions_per_frame", -1, ipf -> {
                ipf.setMinValue(-1);
                return Unit.INSTANCE;
            });

            return Unit.INSTANCE;
        });
        manager.section("settings", settings -> {
            VOLUME = settings.numberSetting("volume", 50, volume -> {
                volume.setMinValue(0);
                volume.setMaxValue(100);
               return Unit.INSTANCE;
            });
            MUTED = settings.booleanSetting("muted", false, _ -> Unit.INSTANCE);
            KEYBOARD_LAYOUT = settings.enumSetting("keyboard_layout", KeyboardLayoutValue.qwerty, _ -> Unit.INSTANCE);
            SHOW_INFO_BAR = settings.booleanSetting("show_info_bar", true, _ -> Unit.INSTANCE);
            return Unit.INSTANCE;
        });
        manager.section("debug", debug -> {
            SHOW_DEBUGGER = debug.booleanSetting("show_debugger", false, _ -> Unit.INSTANCE);
            SHOW_DISASSEMBLER = debug.booleanSetting("show_disassembler", false, _ -> Unit.INSTANCE);
            return Unit.INSTANCE;
        });
        return Unit.INSTANCE;
    });


    public Config() {
        /*
        if (!Files.exists(APP_DIR)) {
            try {
                Files.createDirectory(APP_DIR);
            } catch (Exception e) {}
        }
        CONFIG_MANAGER.loadFromFile();
        CONFIG_MANAGER.saveToFile();

         */
    }

    @Override
    public Optional<Integer> getInstructionsPerFrame() {
        return switch (this.getRawNumberSetting(INSTRUCTIONS_PER_FRAME)) {
            case Integer i when i > 0 -> Optional.of(i);
            case null, default -> Optional.empty();
        };
    }

    @Override
    public Optional<ColorPalette> getColorPalette() {
        return switch (this.<ColorPaletteValue>getRawEnumSetting(COLOR_PALETTE)) {
            case unspecified -> Optional.empty();
            case cadmium -> Optional.of(BuiltInColorPalette.CADMIUM);
            case silicon8 -> Optional.of(BuiltInColorPalette.SILICON8);
            case pico8 -> Optional.of(BuiltInColorPalette.PICO8);
            case octoclassic -> Optional.of(BuiltInColorPalette.OCTO_CLASSIC);
            case lcd -> Optional.of(BuiltInColorPalette.LCD);
            case c64 -> Optional.of(BuiltInColorPalette.C64);
            case intellivision -> Optional.of(BuiltInColorPalette.INTELLIVISION);
            case cga -> Optional.of(BuiltInColorPalette.CGA);
            case null -> Optional.empty();
        };
    }

    @Override
    public Optional<DisplayAngle> getDisplayAngle() {
        return switch (this.getRawNumberSetting(DISPLAY_ANGLE)) {
            case Integer i -> switch (i) {
                case 0, 360 -> Optional.of(DisplayAngle.DEG_0);
                case 90 -> Optional.of(DisplayAngle.DEG_90);
                case 180 -> Optional.of(DisplayAngle.DEG_180);
                case 270 -> Optional.of(DisplayAngle.DEG_270);
                default -> Optional.empty();
            };
            case null, default -> Optional.empty();
        };
    }

    @Override
    public Optional<Variant> getVariant() {
        return switch (this.<VariantValue>getRawEnumSetting(VARIANT)) {
            case unspecified -> Optional.empty();
            case chip_8 -> Optional.of(Variant.CHIP_8);
            case strict_chip_8 -> Optional.of(Variant.STRICT_CHIP_8);
            case chip_8x -> Optional.of(Variant.CHIP_8X);
            case chip_48 -> Optional.of(Variant.CHIP_48);
            case schip_10 -> Optional.of(Variant.SUPER_CHIP_10);
            case schip_11 -> Optional.of(Variant.SUPER_CHIP_11);
            case schip_modern -> Optional.of(Variant.SUPER_CHIP_MODERN);
            case xo_chip -> Optional.of(Variant.XO_CHIP);
            case mega_chip -> Optional.of(Variant.MEGA_CHIP);
            case hyperwave_chip_64 -> Optional.of(Variant.HYPERWAVE_CHIP_64);
            case hybrid_chip_8 -> Optional.of(Variant.HYBRID_CHIP_8);
            case hybrid_chip_8x -> Optional.of(Variant.HYBRID_CHIP_8X);
            case cosmac_vip -> Optional.of(Variant.COSMAC_VIP);
            case null -> Optional.empty();
        };
    }

    @Override
    public Optional<Boolean> doVFReset() {
        return this.getBooleanQuirk(VF_RESET);
    }

    @Override
    public Optional<Chip8EmulatorSettings.MemoryIncrementQuirk> getMemoryIncrementQuirk() {
        return switch (this.<MemoryIncrementValue>getRawEnumSetting(I_INCREMENT)) {
            case unspecified -> Optional.empty();
            case none -> Optional.of(Chip8EmulatorSettings.MemoryIncrementQuirk.NONE);
            case increment_x -> Optional.of(Chip8EmulatorSettings.MemoryIncrementQuirk.INCREMENT_X);
            case increment_x_1 -> Optional.of(Chip8EmulatorSettings.MemoryIncrementQuirk.INCREMENT_X_1);
            case null -> Optional.empty();
        };
    }

    @Override
    public Optional<Boolean> doDisplayWait() {
        return this.getBooleanQuirk(DISPLAY_WAIT);
    }

    @Override
    public Optional<Boolean> doClipping() {
        return this.getBooleanQuirk(CLIPPING);
    }

    @Override
    public Optional<Boolean> doShiftVXInPlace() {
        return this.getBooleanQuirk(SHIFT_VX_IN_PLACE);
    }

    @Override
    public Optional<Boolean> doJumpWithVX() {
        return this.getBooleanQuirk(JUMP_WITH_VX);
    }

    private Optional<Boolean> getBooleanQuirk(ElementPath path) {
        return switch (this.<BooleanValue>getRawEnumSetting(path)) {
            case enabled -> Optional.of(true);
            case disabled -> Optional.of(false);
            case null, default -> Optional.empty();
        };
    }

    @Nullable
    private Boolean getRawBooleanSetting(ElementPath path) {
        Setting<Boolean, ?> setting = ConfigManagerUtils.getBooleanSetting(CONFIG_MANAGER, path);
        if (setting == null) {
            return null;
        }
        return setting.getValue().getRaw();
    }

    @Nullable
    private Number getRawNumberSetting(ElementPath path) {
        Setting<Number, ?> setting = ConfigManagerUtils.getNumberSetting(CONFIG_MANAGER, path);
        if (setting == null) {
            return null;
        }
        return setting.getValue().getRaw();
    }

    @Nullable
    private <E extends Enum<E>> E getRawEnumSetting(ElementPath path) {
        Setting<E, ?> setting = ConfigManagerUtils.getEnumSetting(CONFIG_MANAGER, path);
        if (setting == null) {
            return null;
        }
        return setting.getValue().getRaw();
    }

    private enum BooleanValue {
        unspecified,
        enabled,
        disabled,
    }

    private enum MemoryIncrementValue {
        unspecified,
        none,
        increment_x,
        increment_x_1,
    }

    private enum VariantValue {
        unspecified,
        chip_8,
        strict_chip_8,
        chip_8x,
        chip_48,
        schip_10,
        schip_11,
        schip_modern,
        xo_chip,
        mega_chip,
        hyperwave_chip_64,
        hybrid_chip_8,
        hybrid_chip_8x,
        cosmac_vip
    }

    private enum ColorPaletteValue {
        unspecified,
        cadmium,
        silicon8,
        pico8,
        octoclassic,
        lcd,
        c64,
        intellivision,
        cga
    }

    private enum KeyboardLayoutValue {
        qwerty,
        dvorak,
        azerty,
        colemak
    }

}
