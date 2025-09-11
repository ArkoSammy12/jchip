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
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

public class EmulatorConfig {

    @CommandLine.Option(names = {"--rom", "-r"})
    private Path romPath;

    @CommandLine.Option(names = {"--variant", "-v"}, converter = ConsoleVariant.Converter.class, defaultValue = CommandLine.Option.NULL_VALUE)
    private Optional<ConsoleVariant> cliConsoleVariant;

    @CommandLine.Option(names = {"--instructions-per-frame", "-i"}, fallbackValue = CommandLine.Option.NULL_VALUE)
    private Optional<Integer> cliInstructionsPerFrame;

    @CommandLine.Option(names = {"--color-palette", "-c"}, defaultValue = "cadmium")
    private String colorPalette;

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

    private final ConsoleVariant consoleVariant;
    private final int instructionsPerFrame;
    private final boolean doVfReset;
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

        ConsoleVariant consoleVariant = null;
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
            int programIndex = hashesDatabase.get(sha1).getAsInt();
            this.programObject = programDatabase.get(programIndex).getAsJsonObject();

            // Get the specific rom object corresponding to the rom hash within the program object
            JsonObject programRoms = this.programObject.get("roms").getAsJsonObject();
            this.romObject = programRoms.get(sha1).getAsJsonObject();


            JsonObject quirksObject = null;

            // First, use the quirks established by the platform obtained via the "platforms" array in the rom object
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

            // Populate emulator configs with values from the database if corresponding cli args weren't provided
            if (this.platformObject != null) {
                consoleVariant = ConsoleVariant.getVariantForDatabaseId(this.platformObject.get("id").getAsString());
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
        } catch (Exception e) {
            System.err.println("Error loading values from database. Emulator will use default or cli provided values: " + e);
        }

        // CLI provided settings take priority over database ones.
        // If neither CLI args were provided and values weren't found from the database,
        // use hardcoded default values.
        if (this.cliConsoleVariant.isPresent()) {
            this.consoleVariant = this.cliConsoleVariant.get();
        } else if (consoleVariant == null) {
            this.consoleVariant = ConsoleVariant.CHIP_8;
        } else {
            this.consoleVariant = consoleVariant;
        }

        if (this.cliDoVFReset.isPresent()) {
            this.doVfReset = this.cliDoVFReset.get();
        } else if (doVfReset == null) {
            this.doVfReset = this.consoleVariant == ConsoleVariant.CHIP_8;
        } else {
            this.doVfReset = doVfReset;
        }

        if (this.cliDoIncrementIndex.isPresent()) {
            this.doIncrementIndex = this.cliDoIncrementIndex.get();
        } else if (doIncrementIndex == null) {
            this.doIncrementIndex = this.consoleVariant == ConsoleVariant.CHIP_8 || this.consoleVariant == ConsoleVariant.XO_CHIP;
        } else {
            this.doIncrementIndex = doIncrementIndex;
        }

        if (this.cliDoDisplayWait.isPresent()) {
            this.doDisplayWait = this.cliDoDisplayWait.get();
        } else if (doDisplayWait == null) {
            this.doDisplayWait = this.consoleVariant == ConsoleVariant.CHIP_8 || this.consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY;
        } else {
            this.doDisplayWait = doDisplayWait;
        }

        if (this.cliDoClipping.isPresent()) {
            this.doClipping = this.cliDoClipping.get();
        } else if (doClipping == null) {
            this.doClipping = this.consoleVariant != ConsoleVariant.XO_CHIP;
        } else {
            this.doClipping = doClipping;
        }

        if (this.cliDoShiftVXInPlace.isPresent()) {
            this.doShiftVXInPlace = this.cliDoShiftVXInPlace.get();
        } else if (doShiftVXInPlace == null) {
            this.doShiftVXInPlace = this.consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY || this.consoleVariant == ConsoleVariant.SUPER_CHIP_MODERN;
        } else {
            this.doShiftVXInPlace = doShiftVXInPlace;
        }

        if (this.cliDoJumpWithVX.isPresent()) {
            this.doJumpWithVX = this.cliDoJumpWithVX.get();
        } else if (doJumpWithVX == null) {
            this.doJumpWithVX = this.consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY || this.consoleVariant == ConsoleVariant.SUPER_CHIP_MODERN;
        } else {
            this.doJumpWithVX = doJumpWithVX;
        }

        if (this.cliInstructionsPerFrame.isPresent()) {
            this.instructionsPerFrame = this.cliInstructionsPerFrame.get();
        } else if (instructionsPerFrame == null) {
            this.instructionsPerFrame = this.consoleVariant.getDefaultInstructionsPerFrame(this.doDisplayWait());
        } else {
            this.instructionsPerFrame = instructionsPerFrame;
        }

    }

    public int[] getRom() {
        return this.rom;
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
        return new ColorPalette(colorPalette);
    }

    public ConsoleVariant getConsoleVariant() {
        return this.consoleVariant;
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
        if (path == null) return null;
        return path.isAbsolute() ? path : path.toAbsolutePath();
    }

    private static String getSha1Hash(byte[] data) throws Exception {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = sha1.digest(data);
        return HexFormat.of().formatHex(hashBytes);
    }
}
