package io.github.arkosammy12.jchip.config.database;

import java.util.List;
import java.util.Optional;

record Platforms(List<PlatformEntry> platformEntries) {

    public Optional<List<PlatformEntry>> getPlatformEntries() {
        return Optional.ofNullable(this.platformEntries);
    }

}
