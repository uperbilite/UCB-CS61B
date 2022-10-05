package byow.Core;

import byow.InputDemo.InputSource;
import byow.InputDemo.StringInputDevice;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static byow.TileEngine.TERenderer.TILE_SIZE;

public class Engine {
    private TERenderer ter = new TERenderer();
    /*
    {
        ter.initialize(WIDTH, HEIGHT + 2, 0, 0);
        StdDraw.setPenColor(Color.WHITE);
    }*/
    /** WIDTH and HEIGHT should be odd number. */
    public static final int WIDTH = 91;
    public static final int HEIGHT = 45;
    /** more addRoomAttempts means denser room. */
    public static final int addRoomAttempts = 3500;
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
        TETile[][] world;
        displayMainMenuFrame();
        world = interactInMainMenu();
        if (world != null) {
            startGame(world);
        }
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww"). The engine should
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
        Avatar ava = null;

        input = input.toLowerCase();
        if (input.charAt(0) == 'l') {
            String history = getHistoryInput();
            // starting with index 1 to filter the l
            input = history + input.substring(1);
        }
        InputSource inputSource = new StringInputDevice(input);

        while (inputSource.possibleNextInput()) {
            char c = inputSource.getNextKey();
            switch (c) {
                case 'n' -> {
                    long seed = getSeed(inputSource);
                    // TODO: validate seed
                    Generator ger = new Generator(new Random(seed));
                    world = ger.generateWorld();
                    ava = new Avatar(world);
                }
                case 'w' -> ava.moveUp();
                case 'a' -> ava.moveLeft();
                case 's' -> ava.moveDown();
                case 'd' -> ava.moveRight();
                case ':' -> {
                    if (inputSource.possibleNextInput()) {
                        c = inputSource.getNextKey();
                        if (c == 'q') {
                            // remove the last 2 characters :q and save the history input string
                            // return null to quit the game and pass the auto grader
                            saveGame(input.substring(0, input.length() - 2));
                            return world;
                        }
                    }
                }
                default -> {
                }
            }
        }

        return world;
    }

    private long getSeed(InputSource s) {
        StringBuilder seedString = new StringBuilder();
        while (s.possibleNextInput()) {
            char c = s.getNextKey();
            c = Character.toLowerCase(c);
            if (c == 's') {
                break;
            }
            seedString.append(c);
        }
        return Long.parseLong(seedString.toString());
    }

    private void displayMainMenuFrame() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 30));
        StdDraw.text(WIDTH / 2, HEIGHT / 2 + 3, "New Game (N)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "Load Game (L)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 3, "Quit (Q)");
        // TODO: add more options
        StdDraw.show();
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 14));
    }

    private void displayInputSeedFrame(String s) {
        StdDraw.clear(Color.BLACK);
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 30));
        StdDraw.text(WIDTH / 2, HEIGHT / 2 + 2, "Please input the seed " +
                "to generating a world, ending with (S)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 2, s);
        StdDraw.show();
        StdDraw.setFont(new Font("Monaco", Font.BOLD, TILE_SIZE - 2));
    }

    private void displayQuitGameFrame() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setFont(new Font("Monaco", Font.BOLD,  30));
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "End Game");
        StdDraw.show();
        StdDraw.setFont(new Font("Monaco", Font.BOLD,  TILE_SIZE - 2));
    }

    /**
     * Decide How the player can do in main menu, and how the engine will respond to player.
     * @return If create a new game or load a game, return the world. If quit directly return
     * null
     */
    private TETile[][] interactInMainMenu() {
        // TODO: handle invalid input exception
        TETile[][] world;
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                c = Character.toLowerCase(c);
                if (c == 'n') {
                    world = newGame();
                    return world;
                } else if (c == 'l') {
                    world = loadGame();
                    // TODO: if there is no history file, quit game
                    return world;
                } else if (c == 'q') {
                    quitGame();
                    return null;
                }
                // TODO: handle more options
            }
        }
    }

    private TETile[][] newGame() {
        // TODO: delete history file
        StringBuilder s = new StringBuilder();
        while (true) {
            displayInputSeedFrame(s.toString());
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                if (c == 's') {
                    long seed = Long.parseLong(s.toString());
                    // TODO: validate seed
                    Generator ger = new Generator(new Random(seed));
                    return ger.generateWorld();
                }
                s.append(c);
            }
        }
    }

    private TETile[][] loadGame() {
        String s = getHistoryInput();
        return interactWithInputString(s);
    }

    private void quitGame() {
        displayQuitGameFrame();
        // TODO: quit game and close window
    }

    private void startGame(TETile[][] world) {
        // TODO: get loaded game status
        Avatar ava = new Avatar(world);
        ter.renderFrame(world);
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                switch (c) {
                    case 'w' -> {
                        ava.moveUp();
                        ter.renderFrame(world);
                    }
                    case 'a' -> {
                        ava.moveLeft();
                        ter.renderFrame(world);
                    }
                    case 's' -> {
                        ava.moveDown();
                        ter.renderFrame(world);
                    }
                    case 'd' -> {
                        ava.moveRight();
                        ter.renderFrame(world);
                    }
                    case ':' -> {
                        while (true) {
                            if (StdDraw.hasNextKeyTyped()) {
                                c = StdDraw.nextKeyTyped();
                                if (c == 'q') {
                                    // TODO: save and quit
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private String getHistoryInput() {
        File f = Paths.get("./history.txt").toFile();
        try {
            return Files.readString(f.toPath());
        } catch (IOException exp) {
            throw new IllegalArgumentException(exp.getMessage());
        }
    }

    private void saveGame(String history) {
        File f = Paths.get("./history.txt").toFile();
        try {
            BufferedOutputStream str = new BufferedOutputStream(Files.newOutputStream(f.toPath()));
            str.write(history.getBytes(StandardCharsets.UTF_8));
            str.close();
        } catch (IOException exp) {
            throw new IllegalArgumentException(exp.getMessage());
        }
    }

    public static void main(String... args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        Engine eng = new Engine();
        TETile[][] world;
    }
}

// TODO: HUD display, include Text that describes the tile currently under the mouse pointer.
