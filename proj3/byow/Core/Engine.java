package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;

import java.math.BigInteger;
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
    public static final BigInteger MAX_SEED = new BigInteger("9223372036854775807");

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

        int seed = getSeed(input);
        Random rand = new Random(seed);
        Generator ger = new Generator(rand);

        return ger.generateWorld();
    }

    private int getSeed(String input) {
        String seed = input.substring(1, input.length() - 1);
        return Integer.parseInt(seed);
    }

    public static boolean validateInput(String input) {
        // TODO
        return false;
    }

    public static void main(String[] args) {
        assert args.length == 1;
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        Engine eng = new Engine();
        TETile[][] world = eng.interactWithInputString(args[0]);
        ter.renderFrame(world);
    }
}
