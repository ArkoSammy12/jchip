package io.github.arkosammy12.jchip.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
    private Optional<Chip8Variant> cliConsoleVariant;

    @CommandLine.Option(names = {"--instructions-per-frame", "-i"}, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Integer> cliInstructionsPerFrame;

    @CommandLine.Option(names = {"--color-palette", "-c"}, converter = ColorPalette.Converter.class,fallbackValue = CommandLine.Option.NULL_VALUE)
    private ColorPalette colorPalette;

    @CommandLine.Option(names = {"--keyboard-layout", "-k"}, defaultValue = "qwerty", converter = KeyboardLayout.Converter.class)
    private KeyboardLayout keyboardLayout;

    @CommandLine.Option(names = {"-a", "--angle"}, defaultValue = "0", converter = DisplayAngle.Converter.class)
    private DisplayAngle displayAngle;

    @CommandLine.Option(names = "--vf-reset", negatable = true, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Boolean> cliDoVFReset;

    @CommandLine.Option(names = "--increment-i", negatable = true, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Boolean> cliDoIncrementIndex;

    @CommandLine.Option(names = "--display-wait", negatable = true, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Boolean> cliDoDisplayWait;

    @CommandLine.Option(names = "--clipping", negatable = true, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Boolean> cliDoClipping;

    @CommandLine.Option(names = "--shift-vx-in-place", negatable = true, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Boolean> cliDoShiftVXInPlace;

    @CommandLine.Option(names = "--jump-with-vx", negatable = true, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Boolean> cliDoJumpWithVX;

    private final int[] rom;

    private JsonObject programObject;
    private JsonObject platformObject;
    private JsonObject romObject;

    private final Chip8Variant chip8Variant;
    private final int instructionsPerFrame;
    private final boolean doVfReset;
    private final boolean doIncrementIndex;
    private final boolean doDisplayWait;
    private final boolean doClipping;
    private final boolean doShiftVXInPlace;
    private final boolean doJumpWithVX;

    public  EmulatorConfig(String[] args) throws Exception {
        CommandLine.populateCommand(this, args);
        Path romPath = this.getRomPath();
        byte[] rawRom = Files.readAllBytes(romPath);
        this.rom = new int[rawRom.length];
        for (int i = 0; i < rom.length; i++) {
            this.rom[i] = rawRom[i] & 0xFF;
        }

        Chip8Variant chip8Variant = null;
        Integer instructionsPerFrame = null;
        Boolean doVfReset = null;
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
            if (this.colorPalette == null && colorsElement instanceof JsonObject colorsObject && colorsObject.has("pixels")) {
                JsonArray pixels = colorsObject.get("pixels").getAsJsonArray();
                if (!pixels.isEmpty()) {
                    int[][] customPixelColors = new int[pixels.size()][3];
                    for (int i = 0; i < pixels.size(); i++) {
                        String hex = pixels.get(i).getAsString();
                        customPixelColors[i][0] = Integer.parseInt(hex.substring(1, 3), 16);
                        customPixelColors[i][1] = Integer.parseInt(hex.substring(3, 5), 16);
                        customPixelColors[i][2] = Integer.parseInt(hex.substring(5, 7), 16);
                    }
                    this.colorPalette = new ColorPalette("cadmium", customPixelColors);
                }
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
            if (this.cliConsoleVariant.isEmpty()) {
                if (this.platformObject != null) {
                    chip8Variant = Chip8Variant.getVariantForDatabaseId(this.platformObject.get("id").getAsString());
                }
                if (this.romObject != null && this.romObject.has("tickrate")) {
                    instructionsPerFrame = this.romObject.get("tickrate").getAsInt();
                }
                if (quirksObject != null && quirksObject.has("logic")) {
                    doVfReset = quirksObject.get("logic").getAsBoolean();
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
            System.err.println("Error loading values from database. Emulator will use default or cli provided values: " + e);
        }

        if (this.colorPalette == null) {
            this.colorPalette = new ColorPalette("cadmium");
        }

        // CLI provided settings take priority over database ones.
        // If neither CLI args were provided and values weren't found from the database,
        // use hardcoded default values.
        if (this.cliConsoleVariant.isPresent()) {
            this.chip8Variant = this.cliConsoleVariant.get();
        } else {
            this.chip8Variant = Objects.requireNonNullElse(chip8Variant, Chip8Variant.CHIP_8);
        }
        if (this.cliDoVFReset.isPresent()) {
            this.doVfReset = this.cliDoVFReset.get();
        } else {
            this.doVfReset = Objects.requireNonNullElse(doVfReset, this.chip8Variant == Chip8Variant.CHIP_8);
        }
        if (this.cliDoIncrementIndex.isPresent()) {
            this.doIncrementIndex = this.cliDoIncrementIndex.get();
        } else {
            this.doIncrementIndex = Objects.requireNonNullElse(doIncrementIndex, this.chip8Variant == Chip8Variant.CHIP_8 || this.chip8Variant == Chip8Variant.XO_CHIP);
        }
        if (this.cliDoDisplayWait.isPresent()) {
            this.doDisplayWait = this.cliDoDisplayWait.get();
        } else {
            this.doDisplayWait = Objects.requireNonNullElse(doDisplayWait, this.chip8Variant == Chip8Variant.CHIP_8 || this.chip8Variant == Chip8Variant.SUPER_CHIP_LEGACY);
        }
        if (this.cliDoClipping.isPresent()) {
            this.doClipping = this.cliDoClipping.get();
        } else {
            this.doClipping = Objects.requireNonNullElse(doClipping, this.chip8Variant != Chip8Variant.XO_CHIP);
        }
        if (this.cliDoShiftVXInPlace.isPresent()) {
            this.doShiftVXInPlace = this.cliDoShiftVXInPlace.get();
        } else {
            this.doShiftVXInPlace = Objects.requireNonNullElse(doShiftVXInPlace,this.chip8Variant == Chip8Variant.SUPER_CHIP_LEGACY || this.chip8Variant == Chip8Variant.SUPER_CHIP_MODERN);
        }
        if (this.cliDoJumpWithVX.isPresent()) {
            this.doJumpWithVX = this.cliDoJumpWithVX.get();
        } else {
            this.doJumpWithVX = Objects.requireNonNullElse(doJumpWithVX,this.chip8Variant == Chip8Variant.SUPER_CHIP_LEGACY || this.chip8Variant == Chip8Variant.SUPER_CHIP_MODERN);
        }
        if (this.cliInstructionsPerFrame.isPresent()) {
            this.instructionsPerFrame = this.cliInstructionsPerFrame.get();
        } else {
            this.instructionsPerFrame = Objects.requireNonNullElse(instructionsPerFrame, this.chip8Variant.getDefaultInstructionsPerFrame(this.doDisplayWait()));
        }
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
        return this.doVfReset;
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


    private JsonElement loadJsonFromResources(String resourcePath) throws Exception {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) throw new IllegalStateException(resourcePath + " not found in resources");
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return JsonParser.parseString(json);
        }
    }

    private Path convertToAbsolutePathIfNeeded(Path path) {
        if (path == null) {
            return null;
        }
        return path.isAbsolute() ? path : path.toAbsolutePath();
    }

    private static String getSha1Hash(byte[] data) throws Exception {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = sha1.digest(data);
        return HexFormat.of().formatHex(hashBytes);
    }
}
