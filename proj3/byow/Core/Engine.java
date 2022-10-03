package byow.Core;

import byow.InputDemo.InputSource;
import byow.InputDemo.StringInputDevice;
import byow.TileEngine.TETile;

import java.util.*;

public class Engine {
    /** WIDTH and HEIGHT should be odd number. */
    public static final int WIDTH = 81;
    public static final int HEIGHT = 41;
    /** more addRoomAttempts means denser room. */
    public static final int addRoomAttempts = 2000;
    /** extraRoomSize decide the max size of a room can be. */
    public static final int extraRoomSize = 2;
    /** the lower the bendingDegree is, the straighter the way is. */
    public static final double bendingDegree = 0;
    /** the max seed number. */
    public static final long MAX_SEED = Long.parseLong("9223372036854775807");

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // TODO: Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.

        TETile[][] world = new TETile[WIDTH][HEIGHT];

        input = input.toLowerCase();
        InputSource inputSource = new StringInputDevice(input);

        while (inputSource.possibleNextInput()) {
            char c = inputSource.getNextKey();
            switch (c) {
                case 'n':
                    long seed = getSeed(inputSource);
                    Random rand = new Random(seed);
                    Generator ger = new Generator(rand);
                    world = ger.generateWorld();
                    break;
                case 'l':
                    break;
                case 'q':
                    break;
                default:
                    // do nothing
            }
        }

        return world;
    }

    private long getSeed(InputSource s) {
        StringBuilder seedString = new StringBuilder();
        while (s.possibleNextInput()) {
            char c = s.getNextKey();
            if (c == 's') {
                break;
            }
            seedString.append(c);
        }
        long seed = Long.parseLong(String.valueOf(seedString));
        assert seed < MAX_SEED;
        return seed;
    }
}
