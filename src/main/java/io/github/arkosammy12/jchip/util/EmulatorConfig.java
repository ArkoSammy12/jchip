package io.github.arkosammy12.jchip.util;

import com.google.gson.*;
import org.tinylog.Logger;
import picocli.CommandLine;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;

public class EmulatorConfig {

    @CommandLine.Option(names = {"--rom", "-r"})
    private Path romPath;

    @CommandLine.Option(names = {"--variant", "-v"}, converter = Chip8Variant.Converter.class, defaultValue = CommandLine.Option.NULL_VALUE)
    private Optional<Chip8Variant> consoleVariantArg;

    @CommandLine.Option(names = {"--instructions-per-frame", "-i"}, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Integer> instructionsPerFrameArg;

    @CommandLine.Option(names = {"--color-palette", "-c"}, converter = ColorPalette.Converter.class,fallbackValue = CommandLine.Option.NULL_VALUE)
    private ColorPalette colorPaletteArg;

    @CommandLine.Option(names = {"--keyboard-layout", "-k"}, defaultValue = "qwerty", converter = KeyboardLayout.Converter.class)
    private KeyboardLayout keyboardLayoutArg;

    @CommandLine.Option(names = {"-a", "--angle"}, fallbackValue = CommandLine.Option.NULL_VALUE, converter = DisplayAngle.Converter.class)
    private DisplayAngle displayAngleArg;

    @CommandLine.Option(names = "--vf-reset", negatable = true, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Boolean> doVFResetArg;

    @CommandLine.Option(names = "--increment-i", negatable = true, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Boolean> doIncrementIndexArg;

    @CommandLine.Option(names = "--display-wait", negatable = true, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Boolean> doDisplayWaitArg;

    @CommandLine.Option(names = "--clipping", negatable = true, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Boolean> doClippingArg;

    @CommandLine.Option(names = "--shift-vx-in-place", negatable = true, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Boolean> doShiftVXInPlaceArg;

    @CommandLine.Option(names = "--jump-with-vx", negatable = true, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Boolean> jumpWithVXArg;

    private final int[] rom;

    private JsonObject programObject;
    private JsonObject platformObject;
    private JsonObject romObject;

    private final Chip8Variant chip8Variant;
    private final int instructionsPerFrame;
    private final boolean doVFReset;
    private final boolean doIncrementIndex;
    private final boolean doDisplayWait;
    private final boolean doClipping;
    private final boolean doShiftVXInPlace;
    private final boolean doJumpWithVX;

    public EmulatorConfig(String[] args) throws Exception {
        CommandLine.populateCommand(this, args);
        Path romPath = this.getRomPath();
        byte[] rawRom = Files.readAllBytes(romPath);
        this.rom = new int[rawRom.length];
        for (int i = 0; i < rom.length; i++) {
            this.rom[i] = rawRom[i] & 0xFF;
        }

        Chip8Variant chip8Variant = null;
        Integer instructionsPerFrame = null;
        Boolean doVFReset = null;
        Boolean doIncrementIndex = null;
        Boolean doDisplayWait = null;
        Boolean doClipping = null;
        Boolean doShiftVXInPlace = null;
        Boolean doJumpWithVX = null;

        try {
            JsonObject hashesDatabase = loadJsonFromResources("/database/sha1-hashes.json").getAsJsonObject();
            JsonArray programDatabase = loadJsonFromResources("/database/programs.json").getAsJsonArray();
            JsonArray platformDatabase = loadJsonFromResources("/database/platforms.json").getAsJsonArray();

            // Get our program object using the index in the hashes database
            String sha1 = getSha1Hash(rawRom);
            JsonElement indexElement = hashesDatabase.get(sha1);
            if (indexElement == null) {
                throw new IllegalStateException("Index for loaded ROM not found in database!");
            }
            int programIndex = indexElement.getAsInt();

            JsonElement programElement = programDatabase.get(programIndex);
            if (programElement == null) {
                throw new IllegalStateException("Program entry for loaded ROM not found in database!");
            }
            this.programObject = programElement.getAsJsonObject();

            // Get the specific rom object corresponding to the rom hash within the program object
            JsonElement programRomsElement = this.programObject.get("roms");
            if (programRomsElement == null) {
                throw new IllegalStateException("\"roms\" object for loaded ROM not found within program object in database!");
            }
            JsonObject programRoms = programRomsElement.getAsJsonObject();

            JsonElement romElement = programRoms.get(sha1);
            if (romElement == null) {
                throw new IllegalStateException("ROM entry for loaded ROM not found within program object in database!");
            }
            this.romObject = programRoms.get(sha1).getAsJsonObject();

            JsonElement colorsElement = this.romObject.get("colors");
            if (this.colorPaletteArg == null && colorsElement instanceof JsonObject colorsObject && colorsObject.has("pixels")) {
                JsonArray pixels = colorsObject.get("pixels").getAsJsonArray();
                if (!pixels.isEmpty()) {
                    int[][] customPixelColors = new int[pixels.size()][3];
                    for (int i = 0; i < pixels.size(); i++) {
                        String hex = pixels.get(i).getAsString();
                        customPixelColors[i][0] = Integer.parseInt(hex.substring(1, 3), 16);
                        customPixelColors[i][1] = Integer.parseInt(hex.substring(3, 5), 16);
                        customPixelColors[i][2] = Integer.parseInt(hex.substring(5, 7), 16);
                    }
                    this.colorPaletteArg = new ColorPalette("cadmium", customPixelColors);
                }
            }

            JsonElement screenRotationElement = this.romObject.get("screenRotation");
            if (this.displayAngleArg == null && (screenRotationElement instanceof JsonPrimitive screenRotationPrimitive)) {
                this.displayAngleArg = switch (screenRotationPrimitive.getAsInt()) {
                    case 90 -> DisplayAngle.DEG_90;
                    case 180 -> DisplayAngle.DEG_180;
                    case 270 -> DisplayAngle.DEG_270;
                    default -> DisplayAngle.DEG_0;
                };
            }

            // First, use the quirks established by the platform obtained via the "platforms" array in the rom object
            JsonObject quirksObject = null;
            if (this.romObject != null && this.romObject.has("platforms")) {
                JsonArray romPlatforms = this.romObject.get("platforms").getAsJsonArray();
                if (!romPlatforms.isEmpty()) {
                    String romPlatform = romPlatforms.get(0).getAsString();
                    for (int i = 0; i < platformDatabase.size(); i++) {
                        JsonElement value = platformDatabase.get(i);
                        JsonObject obj = value.getAsJsonObject();
                        if (!obj.get("id").getAsString().equals(romPlatform)) {
                            continue;
                        }
                        this.platformObject = obj;
                        if (obj.has("quirks")) {
                            quirksObject = obj.get("quirks").getAsJsonObject();
                        }
                        break;
                    }
                }
            }

            // Then, check if this program object contains a "quirkset" object, and use its defined quirks if so
            if (this.programObject.has("quirkset")) {
                quirksObject = this.programObject.get("quirkset").getAsJsonObject();
            }

            // Finally, check if this rom object contains a "quirkyPlatforms" object, and merge the platform's quirks with the
            // overrides defined by the value of the platform field within this quirkyPlatform object.
            // The quirks established by quirkPlatforms take priority over the ones in "quirkset", and it takes priority over the quirks in the platform obtained via the "platforms" array.
            if (this.romObject != null && this.romObject.has("quirkyPlatforms")) {
                JsonObject quirkyPlatformsObject = this.romObject.get("quirkyPlatforms").getAsJsonObject();
                Map.Entry<String, JsonElement> entry = quirkyPlatformsObject.entrySet().stream().toList().getFirst();
                String romPlatform = entry.getKey();
                JsonObject quirkOverridesObject = entry.getValue().getAsJsonObject();
                JsonObject normalPlatformQuirks = null;
                for (int i = 0; i < platformDatabase.size(); i++) {
                    JsonElement value = platformDatabase.get(i);
                    JsonObject obj = value.getAsJsonObject();
                    if (!obj.get("id").getAsString().equals(romPlatform)) {
                        continue;
                    }
                    this.platformObject = obj;
                    if (this.platformObject.has("quirks")) {
                        normalPlatformQuirks = this.platformObject.getAsJsonObject("quirks");
                    }
                    break;
                }
                if (normalPlatformQuirks != null) {
                    quirksObject = normalPlatformQuirks.deepCopy();
                    for (Map.Entry<String, JsonElement> override : quirkOverridesObject.entrySet()) {
                        String key = override.getKey();
                        JsonElement value = override.getValue();
                        quirksObject.addProperty(key, value.getAsBoolean());
                    }
                }
            }
            // Populate emulator settings with values from the database if the console variant wasn't specified in the cli args or if the corresponding settings weren't provided frokm the cli
            if (this.consoleVariantArg.isEmpty()) {
                if (this.platformObject != null) {
                    chip8Variant = Chip8Variant.getVariantForPlatformId(this.platformObject.get("id").getAsString());
                }
                if (this.romObject != null && this.romObject.has("tickrate")) {
                    instructionsPerFrame = this.romObject.get("tickrate").getAsInt();
                }
                if (quirksObject != null && quirksObject.has("logic")) {
                    doVFReset = quirksObject.get("logic").getAsBoolean();
                }
                if (quirksObject != null && quirksObject.has("memoryLeaveIUnchanged")) {
                    doIncrementIndex = !quirksObject.get("memoryLeaveIUnchanged").getAsBoolean();
                }
                if (quirksObject != null && quirksObject.has("vblank")) {
                    doDisplayWait = quirksObject.get("vblank").getAsBoolean();
                }
                if (quirksObject != null && quirksObject.has("wrap")) {
                    doClipping = !quirksObject.get("wrap").getAsBoolean();
                }
                if (quirksObject != null && quirksObject.has("shift")) {
                    doShiftVXInPlace = quirksObject.get("shift").getAsBoolean();
                }
                if (quirksObject != null && quirksObject.has("jump")) {
                    doJumpWithVX = quirksObject.get("jump").getAsBoolean();
                }
            }
        } catch (Exception e) {
            Logger.warn("Error loading values from database. Emulator will use default or cli provided values: ", e);
        }

        if (this.colorPaletteArg == null) {
            this.colorPaletteArg = new ColorPalette("cadmium");
        }

        if (this.displayAngleArg == null) {
            this.displayAngleArg = DisplayAngle.DEG_0;
        }

        // CLI provided settings take priority over database ones.
        // If neither CLI args were provided and values weren't found from the database,
        // use hardcoded default values.
        this.chip8Variant = this.consoleVariantArg.orElse(Objects.requireNonNullElse(chip8Variant, Chip8Variant.CHIP_8));
        this.doVFReset = this.doVFResetArg.orElse(Objects.requireNonNullElse(doVFReset, this.chip8Variant.getDefaultQuirkset().doVFReset()));
        this.doIncrementIndex = this.doIncrementIndexArg.orElse(Objects.requireNonNullElse(doIncrementIndex, this.chip8Variant.getDefaultQuirkset().doIncrementIndex()));
        this.doDisplayWait = this.doDisplayWaitArg.orElse(Objects.requireNonNullElse(doDisplayWait, this.chip8Variant.getDefaultQuirkset().doDisplayWait()));
        this.doClipping = this.doClippingArg.orElse(Objects.requireNonNullElse(doClipping, this.chip8Variant.getDefaultQuirkset().doClipping()));
        this.doShiftVXInPlace = this.doShiftVXInPlaceArg.orElse(Objects.requireNonNullElse(doShiftVXInPlace,this.chip8Variant.getDefaultQuirkset().doShiftVXInPlace()));
        this.doJumpWithVX = this.jumpWithVXArg.orElse(Objects.requireNonNullElse(doJumpWithVX,this.chip8Variant.getDefaultQuirkset().doJumpWithVX()));
        this.instructionsPerFrame = this.instructionsPerFrameArg.orElse(Objects.requireNonNullElse(instructionsPerFrame, this.chip8Variant.getDefaultQuirkset().instructionsPerFrame().applyAsInt(this.doDisplayWait)));
    }

    public int[] getRom() {
        return Arrays.copyOf(this.rom, this.rom.length);
    }

    public String getProgramTitle() {
        String name = null;
        if (this.programObject != null && this.programObject.has("title")) {
            name = this.programObject.get("title").getAsString();
        }
        return name;
    }

    public int getInstructionsPerFrame() {
        return this.instructionsPerFrame;
    }

    public ColorPalette getColorPalette() {
        return this.colorPaletteArg;
    }

    public KeyboardLayout getKeyboardLayout() {
        return this.keyboardLayoutArg;
    }

    public DisplayAngle getDisplayAngle() {
        return this.displayAngleArg;
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

    private Path getRomPath() {
        return convertToAbsolutePathIfNeeded(romPath);
    }

    private static Path convertToAbsolutePathIfNeeded(Path path) {
        if (path == null) {
            return null;
        }
        return path.isAbsolute() ? path : path.toAbsolutePath();
    }

    private JsonElement loadJsonFromResources(String resourcePath) throws Exception {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) throw new IllegalStateException(resourcePath + " not found in resources");
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return JsonParser.parseString(json);
        }
    }

    private static String getSha1Hash(byte[] data) throws Exception {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = sha1.digest(data);
        return HexFormat.of().formatHex(hashBytes);
    }

}
