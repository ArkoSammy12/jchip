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
import java.util.Optional;

public class EmulatorConfig {

    @CommandLine.Option(names = {"--rom", "-r"})
    private Path romPath;

    @CommandLine.Option(names = {"--variant", "-v"}, converter = ConsoleVariant.Converter.class, defaultValue = CommandLine.Option.NULL_VALUE)
    private Optional<ConsoleVariant> consoleVariant;

    @CommandLine.Option(names = {"--instructions-per-frame", "-i"}, defaultValue = "0")
    private int instructionsPerFrame;

    @CommandLine.Option(names = {"--color-palette", "-c"}, defaultValue = "cadmium")
    private String colorPalette;

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

    private final int[] rom;

    private JsonObject programObject;
    private JsonObject platformObject;
    private JsonObject romObject;

    public EmulatorConfig(String[] args) throws Exception {
        CommandLine.populateCommand(this, args);
        Path romPath = this.getRomPath();
        byte[] rawRom = Files.readAllBytes(romPath);
        this.rom = new int[rawRom.length];
        for (int i = 0; i < rom.length; i++) {
            this.rom[i] = rawRom[i] & 0xFF;
        }
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

            // Get the supported platforms for this rom
            JsonArray romPlatforms = this.romObject.get("platforms").getAsJsonArray();
            String romPlatform = romPlatforms.get(0).getAsString();
            for (int i = 0; i < platformDatabase.size(); i++) {
                JsonElement value = platformDatabase.get(i);
                JsonObject obj = value.getAsJsonObject();
                if (!obj.get("id").getAsString().equals(romPlatform)) {
                    continue;
                }
                this.platformObject = obj;
                break;
            }

            // Get the quirkset from the program object if it exists
            JsonObject quirkSet = null;
            if (this.programObject.has("quirkset")) {
                quirkSet = this.programObject.get("quirkset").getAsJsonObject();
            }

            // Populate emulator configs with values from the database if corresponding cli args weren't provided
            if (this.consoleVariant.isEmpty()) {
                ConsoleVariant variant = ConsoleVariant.getVariantForDatabaseId(romPlatform);
                this.consoleVariant = Optional.of(variant);
            }
            if (this.instructionsPerFrame <= 0) {
                if (this.romObject.has("tickrate")) {
                    this.instructionsPerFrame = this.romObject.get("tickrate").getAsInt();
                } else {
                    this.instructionsPerFrame = this.getConsoleVariant().getDefaultInstructionsPerFrame(this.doDisplayWait());
                }
            }
            if (this.doVFReset.isEmpty()) {
                Boolean quirk = getQuirk("logic");
                if (quirk != null) {
                    this.doVFReset = Optional.of(quirk);
                }
                if (quirkSet != null && quirkSet.has("logic")) {
                    boolean override = quirkSet.get("logic").getAsBoolean();
                    this.doVFReset = Optional.of(override);
                }
            }
            if (this.doIncrementIndex.isEmpty()) {
                Boolean quirk = getQuirk("memoryLeaveIUnchanged");
                if (quirk != null) {
                    quirk = !quirk;
                    this.doIncrementIndex = Optional.of(quirk);
                }
                if (quirkSet != null && quirkSet.has("memoryLeaveIUnchanged")) {
                    boolean override = !quirkSet.get("memoryLeaveIUnchanged").getAsBoolean();
                    this.doIncrementIndex = Optional.of(override);
                }
            }
            if (this.doDisplayWait.isEmpty()) {
                Boolean quirk = getQuirk("vblank");
                if (quirk != null) {
                    this.doDisplayWait = Optional.of(quirk);
                }
                if (quirkSet != null && quirkSet.has("vblank")) {
                    boolean override = quirkSet.get("vblank").getAsBoolean();
                    this.doDisplayWait = Optional.of(override);
                }
            }
            if (this.doClipping.isEmpty()) {
                Boolean quirk = getQuirk("wrap");
                if (quirk != null) {
                    quirk = !quirk;
                    this.doClipping = Optional.of(quirk);
                }
                if (quirkSet != null && quirkSet.has("wrap")) {
                    boolean override = !quirkSet.get("wrap").getAsBoolean();
                    this.doClipping = Optional.of(override);
                }
            }
            if (this.doShiftVXInPlace.isEmpty()) {
                Boolean quirk = getQuirk("shift");
                if (quirk != null) {
                    this.doShiftVXInPlace = Optional.of(quirk);
                }
                if (quirkSet != null && quirkSet.has("shift")) {
                    boolean override = quirkSet.get("shift").getAsBoolean();
                    this.doShiftVXInPlace = Optional.of(override);
                }
            }
            if (this.doJumpWithVX.isEmpty()) {
                Boolean quirk = getQuirk("jump");
                if (quirk != null) {
                    this.doJumpWithVX = Optional.of(quirk);
                }
                if (quirkSet != null && quirkSet.has("jump")) {
                    boolean override = quirkSet.get("jump").getAsBoolean();
                    this.doJumpWithVX = Optional.of(override);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error loading values from database. Emulator will use default or cli provided values", e);
        }
    }

    public int[] getRom() {
        return this.rom;
    }

    public String getProgramTitle() {
        String name = null;
        if (this.programObject.has("title")) {
            name = this.programObject.get("title").getAsString();
        }
        return name;
    }

    public boolean doVFReset() {
        ConsoleVariant consoleVariant = this.getConsoleVariant();
        return doVFReset.orElse(consoleVariant == ConsoleVariant.CHIP_8);
    }

    public boolean doIncrementIndex() {
        ConsoleVariant consoleVariant = this.getConsoleVariant();
        return doIncrementIndex.orElse(consoleVariant == ConsoleVariant.CHIP_8 || consoleVariant == ConsoleVariant.XO_CHIP);
    }

    public boolean doDisplayWait() {
        ConsoleVariant consoleVariant = this.getConsoleVariant();
        return doDisplayWait.orElse(consoleVariant == ConsoleVariant.CHIP_8 || consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY);
    }

    public boolean doClipping() {
        ConsoleVariant consoleVariant = this.getConsoleVariant();
        return doClipping.orElse(consoleVariant != ConsoleVariant.XO_CHIP);
    }

    public boolean doShiftVXInPlace() {
        ConsoleVariant consoleVariant = this.getConsoleVariant();
        return doShiftVXInPlace.orElse(consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY || consoleVariant == ConsoleVariant.SUPER_CHIP_MODERN);
    }

    public boolean doJumpWithVX() {
        ConsoleVariant consoleVariant = this.getConsoleVariant();
        return doJumpWithVX.orElse(consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY || consoleVariant == ConsoleVariant.SUPER_CHIP_MODERN);
    }

    public int getInstructionsPerFrame() {
        return this.instructionsPerFrame;
    }

    public Path getRomPath() {
        return convertToAbsolutePathIfNeeded(romPath);
    }

    public ConsoleVariant getConsoleVariant() {
        return this.consoleVariant.orElse(ConsoleVariant.CHIP_8);
    }

    public ColorPalette getColorPalette() {
        return new ColorPalette(colorPalette);
    }

    private JsonElement loadJsonFromResources(String resourcePath) throws Exception {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) throw new IllegalStateException(resourcePath + " not found in resources");
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return JsonParser.parseString(json);
        }
    }


    private Boolean getQuirk(String key) {
        if (platformObject != null && platformObject.has("quirks")) {
            JsonObject quirks = platformObject.getAsJsonObject("quirks");
            if (quirks.has(key)) {
                return quirks.get(key).getAsBoolean();
            }
        }
        return null;
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
