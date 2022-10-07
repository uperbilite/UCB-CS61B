package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import edu.princeton.cs.introcs.StdDraw;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class Engine {
    /** WIDTH and HEIGHT should be odd number. */
    public static final int WIDTH = 81;
    public static final int HEIGHT = 41;
    /** more addRoomAttempts means denser room. */
    public static final int ADD_ROOM_ATTEMPTS = 3500;
    /** extraRoomSize decide the max size of a room can be. */
    public static final int EXTRA_ROOM_SIZE = 2;
    /** make a door so that the world is not singly-connected,
     *  increase this to get a more loosely connected world
     */
    public static final double EXTRA_DOOR_DEGREE = 0.05;
    /** the lower the bendingDegree is, the straighter the way is. */
    public static final double BENDING_DEGREE = 0;
    /** the max seed number. */
    public static final long MAX_SEED = Long.parseLong("9223372036854775807");
    public static final TERenderer ter = new TERenderer();
    {
        // set the window size
        ter.initialize(WIDTH, HEIGHT + 2);
    }
    private String log = "";

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        mainMenu();
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
        return null;
    }

    private void logInfo(char c) {
        log += c;
    }

    private void mainMenu() {
        Window.displayMainMenu();
        interactInMainMenu();
    }

    private void interactInMainMenu() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                switch (Character.toLowerCase(c)) {
                    case 'n' -> newGame();
                    case 'l' -> loadGame();
                    case 'q' -> quitGame();
                }
            }
        }
    }

    private void newGame() {
        long seed = getInputSeed();
        deleteLog();
        Generator ger = new Generator(new Random(seed));
        TETile[][] world = ger.generateWorld();
        startGame(world, new Avatar(world));
    }

    private void loadGame() {
        log = getLogContent();
        long seed = getSeedFromLog();
        String operation = getOperationFromLog();

        Generator ger = new Generator(new Random(seed));
        TETile[][] world = ger.generateWorld();
        Avatar ava = retrieveAvatar(world, operation);

        startGame(world, ava);
    }

    private Avatar retrieveAvatar(TETile[][] world, String operations) {
        Avatar ava = new Avatar(world);
        for (int i = 0; i < operations.length(); i++) {
            switch (Character.toLowerCase(operations.charAt(i))) {
                case 'w' -> ava.moveUp();
                case 'a' -> ava.moveLeft();
                case 's' -> ava.moveDown();
                case 'd' -> ava.moveRight();
            }
        }
        return ava;
    }

    private long getSeedFromLog() {
        assert Character.toLowerCase(log.charAt(0)) == 'n';
        for (int i = 1; i < log.length(); i++) {
            if (Character.toLowerCase(log.charAt(i)) == 's') {
                return Long.parseLong(log.substring(1, i));
            }
        }
        assert false;
        return 0;
    }

    private String getOperationFromLog() {
        for (int i = 0; i < log.length(); i++) {
            if (Character.toLowerCase(log.charAt(i)) == 's') {
                return log.substring(i);
            }
        }
        assert false;
        return null;
    }

    private void quitGame() {
        System.exit(0);
    }

    private void deleteLog() {
        File f = Paths.get("./log.txt").toFile();
        if (f.exists()) {
            f.delete();
        }
    }

    private long getInputSeed() {
        logInfo('n');
        String seed = "";

        while (true) {
            Window.displayInputSeed(seed);
            if (!StdDraw.hasNextKeyTyped()) {
                continue;
            }

            char c = StdDraw.nextKeyTyped();
            if (Character.toLowerCase(c) == 's') {
                logInfo('s');
                try {
                    return Long.parseLong(seed);
                } catch (NumberFormatException exp) {
                    Window.displayInvalidInputSeed();
                    quitGame();
                }
            }

            seed += c;
            logInfo(c);
        }
    }

    private void startGame(TETile[][] world, Avatar ava) {
        while (true) {
            ter.renderFrame(world);
            Window.displayHUD(world);
            Window.displayShortestMovingPath(world, ava);
            if (!StdDraw.hasNextKeyTyped()) {
                continue;
            }
            char c = StdDraw.nextKeyTyped();
            switch (Character.toLowerCase(c)) {
                case 'w' -> ava.moveUp();
                case 'a' -> ava.moveLeft();
                case 's' -> ava.moveDown();
                case 'd' -> ava.moveRight();
                case ':' -> waitForQuitInput();
            }
            logInfo(c);
        }
    }

    private void waitForQuitInput() {
        while (true) {
            if (!StdDraw.hasNextKeyTyped()) {
                continue;
            }
            char c = StdDraw.nextKeyTyped();
            if (Character.toLowerCase(c) == 'q') {
                saveAndQuitGame();
            }
            // do nothing and return
            return;
        }
    }

    private void saveAndQuitGame() {
        saveGame(log);
        quitGame();
    }

    private String getLogContent() {
        File f = Paths.get("./log.txt").toFile();
        if (!f.exists()) {
            Window.displayLogNotExist();
            quitGame();
        }
        try {
            return Files.readString(f.toPath());
        } catch (IOException exp) {
            throw new IllegalArgumentException(exp.getMessage());
        }
    }

    private void saveGame(String log) {
        File f = Paths.get("./log.txt").toFile();
        try {
            BufferedOutputStream str = new BufferedOutputStream(Files.newOutputStream(f.toPath()));
            str.write(log.getBytes(StandardCharsets.UTF_8));
            str.close();
        } catch (IOException exp) {
            throw new IllegalArgumentException(exp.getMessage());
        }
    }
}
