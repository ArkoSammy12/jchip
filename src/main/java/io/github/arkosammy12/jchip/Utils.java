package io.github.arkosammy12.jchip;

public final class Utils {

    private Utils() {
        throw new AssertionError();
    }

    public static int getIntegerForCharacter(char c) {
        return switch (c) {
          case '0' -> 0x0;
          case '1' -> 0x1;
          case '2' -> 0x2;
          case '3' -> 0x3;
          case '4' -> 0x4;
          case '5' -> 0x5;
          case '6' -> 0x6;
          case '7' -> 0x7;
          case '8' -> 0x8;
          case '9' -> 0x9;
          case 'a' -> 0xA;
          case 'b' -> 0xB;
          case 'c' -> 0xC;
          case 'd' -> 0xD;
          case 'e' -> 0xE;
          case 'f' -> 0xF;
          default -> -1;
        };
    }

}
