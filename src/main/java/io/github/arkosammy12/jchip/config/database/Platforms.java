package io.github.arkosammy12.jchip.config.database;

import java.util.List;
import java.util.Optional;

class Platforms {

    private List<PlatformEntry> platformEntries;

    public Platforms(List<PlatformEntry> platformEntries) {
        this.platformEntries = platformEntries;
    }

    public Optional<List<PlatformEntry>> getPlatformEntries() {
        return Optional.ofNullable(this.platformEntries);
    }

}
