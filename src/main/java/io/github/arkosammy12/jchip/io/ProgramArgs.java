package io.github.arkosammy12.jchip.io;

import picocli.CommandLine;

import java.nio.file.Path;

public class ProgramArgs {

    @CommandLine.Option(names = "--debug")
    private boolean debug = false;

    @CommandLine.Option(names = {"--rom", "--rom-path"})
    private Path romPath;

    @CommandLine.Option(names = "--variant")
    private String consoleVariant = ConsoleVariant.CHIP_8.getIdentifier();

    @CommandLine.Option(names = {"--save-state", "--save-path"})
    private Path saveStatePath;

    public boolean debugEnabled() {
        return this.debug;
    }

    public Path getRomPath() {
        return this.convertToAbsolutePathIfNeeded(this.romPath);
    }
    public Path getSaveStatePath() {
        return this.convertToAbsolutePathIfNeeded(this.saveStatePath);
    }
    public ConsoleVariant getConsoleVariant() {
        return ConsoleVariant.getVariantForIdentifier(this.consoleVariant);
    }
    private Path convertToAbsolutePathIfNeeded(Path path) {
        Path ret = path;
        if (!ret.isAbsolute()) {
            ret = ret.toAbsolutePath();
        }
        return ret;
    }

}
