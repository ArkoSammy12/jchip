package io.github.arkosammy12.jchip.io;

import picocli.CommandLine;

import java.nio.file.Path;

public class ProgramArgs {

    @CommandLine.Option(names = "--debug")
    private boolean debug = false;

    @CommandLine.Option(names = {"--rom", "--rom-path"})
    private Path romPath;

    @CommandLine.Option(names = "--display-wait", negatable = true, defaultValue = "true", fallbackValue = "true")
    private boolean displayWaitEnabled;

    @CommandLine.Option(names = "--variant")
    private String consoleVariant = ConsoleVariant.CHIP_8.getIdentifier();

    @CommandLine.Option(names = {"--save-state", "--save-path"})
    private Path saveStatePath;

    @CommandLine.Option(names = {"--ipf", "--instructions-per-frame"}, defaultValue = "0")
    private int instructionsPerFrame;

    public boolean debugEnabled() {
        return this.debug;
    }

    public boolean isDisplayWaitEnabled() {
        return this.displayWaitEnabled;
    }

    public int getInstructionsPerFrame() {
        return this.instructionsPerFrame;
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
