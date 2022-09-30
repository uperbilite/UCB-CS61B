package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {

    private static final int WIDTH = 50;

    private static final int HEIGHT = 50;


    private static final long SEED = 2873123;

    private static final Random RANDOM = new Random(SEED);

    public static void addHexagon(TETile[][] tiles, Position p, int size) {
        if (size < 2) {
            return;
        }
        TETile t = randomTile();
        int b = size - 1;
        int l = size;
        addHexagonHelper(tiles, t, p, b, l);
    }

    private static void addHexagonHelper(TETile[][] tiles, TETile t, Position p, int b, int l) {
        Position startHexagonRow = p.shift(b, 0);
        drawRow(tiles, t, startHexagonRow, l);

        if (b > 0) {
            addHexagonHelper(tiles, t, p.shift(0, -1), b - 1, l + 2);
        }

        Position reflectHexagonRow = p.shift(b, -(2 * b + 1));
        drawRow(tiles, t, reflectHexagonRow, l);
    }

    private static void drawRow(TETile[][] tiles, TETile t, Position p, int length) {
        for (int i = 0; i < length; i++) {
            tiles[p.x + i][p.y] = t;
        }
    }

    public static void drawWorld(TETile[][] tiles, Position p, int hexagonSize, int worldSize) {
        drawWorldLine(tiles, p, hexagonSize, worldSize);
        for (int i = 1; i < worldSize; i++) {
            p = getUpRightPosition(p, hexagonSize);
            drawWorldLine(tiles, p, hexagonSize, worldSize + i);
        }
        for (int i = worldSize - 2; i >= 0; i--) {
            p = getDownRightPosition(p, hexagonSize);
            drawWorldLine(tiles, p, hexagonSize, worldSize + i);
        }
    }

    private static Position getNextLinePosition(Position p, int hexagonSize) {
        return new Position(p.x, p.y - 2 * hexagonSize);
    }

    private static Position getUpRightPosition(Position p, int hexagonSize) {
        return new Position(p.x + (2 * hexagonSize - 1), p.y + hexagonSize);
    }

    private static Position getDownRightPosition(Position p, int hexagonSize) {
        return new Position(p.x + (2 * hexagonSize - 1), p.y - hexagonSize);
    }

    private static void drawWorldLine(TETile[][] tiles, Position p, int hexagonSize, int length) {
        for (int i = 0; i < length; i++) {
            addHexagon(tiles, p, hexagonSize);
            p = getNextLinePosition(p, hexagonSize);
        }
    }

    public static void fillWithNothingTiles(TETile[][] tiles) {
        int height = tiles[0].length;
        int width = tiles.length;
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(5);
        switch (tileNum) {
            case 0: return Tileset.WALL;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.GRASS;
            case 3: return Tileset.MOUNTAIN;
            case 4: return Tileset.TREE;
            default: return Tileset.NOTHING;
        }
    }

    private static class Position {
        int x;
        int y;

        Position(int x, int y) {
            this.x = x;
            this.y  =y;
        }

        Position shift(int dx, int dy) {
            return new Position(this.x + dx, this.y + dy);
        }
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] hexagonTiles = new TETile[WIDTH][HEIGHT];
        fillWithNothingTiles(hexagonTiles);
        Position p = new Position(2, 25);
        drawWorld(hexagonTiles, p, 3, 3);

        ter.renderFrame(hexagonTiles);
    }
}
