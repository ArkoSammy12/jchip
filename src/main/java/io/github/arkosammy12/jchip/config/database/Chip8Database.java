package io.github.arkosammy12.jchip.config.database;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.github.arkosammy12.jchip.config.SettingsProvider;
import io.github.arkosammy12.jchip.exceptions.EmulatorException;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.video.BuiltInColorPalette;
import io.github.arkosammy12.jchip.video.ColorPalette;
import io.github.arkosammy12.jchip.video.CustomColorPalette;
import org.tinylog.Logger;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Chip8Database implements SettingsProvider {

    private final Hashes hashes;
    private final Platforms platforms;
    private final Programs programs;

    private ProgramEntry programEntry;
    private RomEntry romEntry;
    private PlatformEntry platformEntry;

    public Chip8Database() {
        try {
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, Integer>>() {}.getType();
            this.hashes = new Hashes(gson.fromJson(loadJsonFromResources("/database/sha1-hashes.json"), mapType));

            Type programListType = new TypeToken<List<ProgramEntry>>() {}.getType();
            this.programs = new Programs(gson.fromJson(loadJsonFromResources("/database/programs.json"), programListType));

            Type platformListType = new TypeToken<List<PlatformEntry>>() {}.getType();
            this.platforms = new Platforms(gson.fromJson(loadJsonFromResources("/database/platforms.json"), platformListType));
        } catch (Exception e) {
            throw new EmulatorException("Failed to initialize CHIP-8 metadata database ", e);
        }
    }

    public void fetchDataForRom(byte[] rom) {
        this.programEntry = null;
        this.romEntry = null;
        this.platformEntry = null;
        try {
            String sha1 = getSha1Hash(rom);
            Optional<Integer> indexOptional = this.getHashes().flatMap(hashes -> hashes.getIndexForHash(sha1));
            if (indexOptional.isEmpty()) {
                throw new EmulatorException("Unable to obtain program entry index from hashes database!");
            }
            int index = indexOptional.get();

            Optional<ProgramEntry> programEntryOptional = this.getPrograms().flatMap(programs -> programs.getProgramEntryAt(index));
            if (programEntryOptional.isEmpty()) {
                throw new EmulatorException("Unable to obtain program entry from programs database!");
            }

            this.programEntry = programEntryOptional.get();
            this.romEntry = this.programEntry.getRomEntries()
                    .flatMap(romEntries -> Optional.ofNullable(romEntries.get(sha1)))
                    .orElseThrow(() ->  new EmulatorException("Unable to obtain rom entry from program entry!"));

            Optional<List<PlatformEntry>> platformsOptional = this.getPlatforms().flatMap(Platforms::getPlatformEntries);
            if (platformsOptional.isEmpty()) {
                throw new EmulatorException("Unable to read platforms database!");
            }

            List<PlatformEntry> platformEntryList = platformsOptional.get();
            Consumer<String> platformIdConsumer = platformId -> {
                for (PlatformEntry platformElement : platformEntryList) {
                    Optional<String> idOptional = platformElement.getId();
                    if (idOptional.isEmpty()) {
                        continue;
                    }
                    String id = idOptional.get();
                    if (id.equals(platformId)) {
                        this.platformEntry = platformElement;
                        break;
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

    private Optional<Hashes> getHashes() {
        return Optional.ofNullable(this.hashes);
    }

    private Optional<Programs> getPrograms() {
        return Optional.ofNullable(this.programs);
    }

    private Optional<Platforms> getPlatforms() {
        return Optional.ofNullable(this.platforms);
    }

}
