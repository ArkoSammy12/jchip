package io.github.arkosammy12.jchip.config.database;

import com.google.gson.*;
import io.github.arkosammy12.jchip.config.SettingsProvider;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.video.BuiltInColorPalette;
import io.github.arkosammy12.jchip.video.ColorPalette;
import io.github.arkosammy12.jchip.video.CustomColorPalette;
import org.tinylog.Logger;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Chip8Database implements SettingsProvider {

    private ProgramEntry programEntry;
    private RomEntry romEntry;
    private PlatformEntry platformEntry;

    public Chip8Database(byte[] rom) {
        try {
            JsonElement hashesElement = loadJsonFromResources("/database/sha1-hashes.json");
            if (hashesElement == null || !hashesElement.isJsonObject()) {
                throw new IllegalArgumentException("Unable to read hashes database!");
            }

            String sha1 = getSha1Hash(rom);
            JsonObject hashes = hashesElement.getAsJsonObject();
            JsonElement indexElement = hashes.get(sha1);
            if (indexElement == null || !indexElement.isJsonPrimitive()) {
                throw new IllegalArgumentException("Unable to obtain program entry index from hashes database!");
            }
            int index = indexElement.getAsInt();

            JsonElement programsElement = loadJsonFromResources("/database/programs.json");
            if (programsElement == null || !programsElement.isJsonArray()) {
                throw new IllegalArgumentException("Unable to read programs database!");
            }
            JsonArray programsArray = programsElement.getAsJsonArray();

            JsonElement programElement = programsArray.get(index);
            if (programElement == null || !programElement.isJsonObject()) {
                throw new IllegalArgumentException("Unable to obtain program entry from programs database!");
            }
            Gson gson = new Gson();
            this.programEntry = gson.fromJson(programElement, ProgramEntry.class);
            this.romEntry = this.programEntry.getRomEntries()
                    .flatMap(romEntries -> Optional.ofNullable(romEntries.get(sha1)))
                    .orElseThrow(() ->  new IllegalArgumentException("Unable to obtain rom entry from program entry!"));

            JsonElement platformsElement = loadJsonFromResources("/database/platforms.json");
            if (platformsElement == null || !platformsElement.isJsonArray()) {
                throw new IllegalArgumentException("Unable to read platforms database!");
            }

            JsonArray platforms = platformsElement.getAsJsonArray();
            Consumer<String> platformIdConsumer = platformId -> {
                for (int i = 0; i < platforms.size(); i++) {
                    JsonElement platformElement = platforms.get(i);
                    if (platformElement == null || !platformElement.isJsonObject()) {
                        continue;
                    }
                    JsonObject platform = platformElement.getAsJsonObject();
                    if (platform.has("id")) {
                        JsonElement idElement = platform.get("id");
                        if (idElement == null || !idElement.isJsonPrimitive()) {
                            continue;
                        }
                        String id = idElement.getAsString();
                        if (id.equals(platformId)) {
                            this.platformEntry = gson.fromJson(platform, PlatformEntry.class);
                            break;
                        }
                    }
                }
            };

            Optional<Map.Entry<String, Quirks>> quirkyPlatformsId = Optional.ofNullable(this.romEntry)
                    .flatMap(RomEntry::getQuirkyPlatforms)
                    .orElse(new HashMap<>())
                    .entrySet()
                    .stream()
                    .findFirst();
            if (quirkyPlatformsId.isPresent()) {
                platformIdConsumer.accept(quirkyPlatformsId.get().getKey());
            } else {
                Optional.ofNullable(this.romEntry)
                        .flatMap(RomEntry::getPlatforms)
                        .map(List::getFirst)
                        .ifPresent(platformIdConsumer);
            }
        } catch (Exception e) {
            Logger.error("Failed loading ROM metadata from database: {}", e);
        }
    }

    public Optional<String> getProgramTitle() {
        return Optional.ofNullable(this.programEntry).flatMap(ProgramEntry::getTitle);
    }

    @Override
    public Optional<Integer> getInstructionsPerFrame() {
        return Optional.ofNullable(this.romEntry).flatMap(RomEntry::getTickrate);
    }

    @Override
    public Optional<ColorPalette> getColorPalette() {
        Optional<List<String>> colorsOptional = Optional.ofNullable(this.romEntry)
                .flatMap(RomEntry::getColors)
                .flatMap(RomEntry.Colors::getPixels);
        if (colorsOptional.isPresent()) {
            List<String> pixels = colorsOptional.get();
            if (!pixels.isEmpty()) {
                int[][] customPixelColors = new int[pixels.size()][3];
                for (int i = 0; i < pixels.size(); i++) {
                    String hex = pixels.get(i);
                    customPixelColors[i][0] = Integer.parseInt(hex.substring(1, 3), 16);
                    customPixelColors[i][1] = Integer.parseInt(hex.substring(3, 5), 16);
                    customPixelColors[i][2] = Integer.parseInt(hex.substring(5, 7), 16);
                }
                return Optional.of(new CustomColorPalette(BuiltInColorPalette.CADMIUM, customPixelColors));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<DisplayAngle> getDisplayAngle() {
        return Optional.ofNullable(this.romEntry)
                .flatMap(RomEntry::getScreenRotation)
                .map(DisplayAngle::getDisplayAngleForIntValue);
    }

    @Override
    public Optional<Chip8Variant> getChip8Variant() {
        return Optional.ofNullable(this.platformEntry)
                .flatMap(PlatformEntry::getId)
                .map(Chip8Variant::getVariantForPlatformId);
    }

    @Override
    public Optional<Boolean> doVFReset() {
        return getQuirk(Quirks::getLogic);
    }

    @Override
    public Optional<Boolean> doIncrementIndex() {
        return getQuirk(Quirks::getMemoryLeaveIUnchanged).map(v -> !v);
    }

    @Override
    public Optional<Boolean> doDisplayWait() {
        return getQuirk(Quirks::getVBlank);
    }

    @Override
    public Optional<Boolean> doClipping() {
        return getQuirk(Quirks::getWrap).map(v -> !v);
    }

    @Override
    public Optional<Boolean> doShiftVXInPlace() {
        return getQuirk(Quirks::getShift);
    }

    @Override
    public Optional<Boolean> doJumpWithVX() {
        return getQuirk(Quirks::getJump);
    }

    private <T> Optional<T> getQuirk(Function<Quirks, Optional<T>> getter) {
        return Optional.ofNullable(this.romEntry)
                .flatMap(RomEntry::getQuirkyPlatforms)
                .flatMap(quirkyPlatforms ->
                        Optional.ofNullable(this.platformEntry)
                                .flatMap(PlatformEntry::getId)
                                .flatMap(platformId -> Optional.ofNullable(quirkyPlatforms.get(platformId)))
                )
                .flatMap(getter)
                .or(() -> Optional.ofNullable(this.platformEntry)
                        .flatMap(PlatformEntry::getQuirks)
                        .flatMap(getter)
                );
    }

    private JsonElement loadJsonFromResources(String resourcePath) {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException(resourcePath + " not found in resources");
            }
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return JsonParser.parseString(json);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Unable to load resource %s.", resourcePath), e);
        }
    }

    private static String getSha1Hash(byte[] data) throws Exception {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = sha1.digest(data);
        return HexFormat.of().formatHex(hashBytes);
    }

}
