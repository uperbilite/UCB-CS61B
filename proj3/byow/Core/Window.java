package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static byow.Core.Engine.HEIGHT;
import static byow.Core.Engine.WIDTH;
import static byow.TileEngine.TERenderer.TILE_SIZE;

public class Window {
    public static void displayMainMenu() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 30));
        StdDraw.text(WIDTH / 2, HEIGHT / 2 + 3, "New Game (N)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "Load Game (L)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 3, "Quit (Q)");
        StdDraw.show();
        StdDraw.setFont(new Font("Monaco", Font.BOLD, TILE_SIZE - 2));
    }

    public static void displayInputSeed(String seed) {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 30));
        StdDraw.text(WIDTH / 2, HEIGHT / 2 + 2, "Input your seed to generate the " +
                "world, ending with (S)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 2, seed);
        StdDraw.show();
        StdDraw.setFont(new Font("Monaco", Font.BOLD, TILE_SIZE - 2));
    }

    public static void displayInvalidInputSeed() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 30));
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "Invalid input seed");
        StdDraw.show();
        StdDraw.pause(1000);
        StdDraw.setFont(new Font("Monaco", Font.BOLD, TILE_SIZE - 2));
    }

    public static void displayLogNotExist() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 30));
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "Log does not exist");
        StdDraw.show();
        StdDraw.pause(1000);
        StdDraw.setFont(new Font("Monaco", Font.BOLD, TILE_SIZE - 2));
    }

    public static void displayHUD(TETile[][] world) {
        int x = (int) StdDraw.mouseX();
        int y = (int) StdDraw.mouseY();
        TETile tile;
        String tileName = "";

        try {
            tile = world[x][y];
        } catch (IndexOutOfBoundsException exp) {
            return;
        }

        if (Tileset.AVATAR.equals(tile)) {
            tileName = "Avatar";
        } else if (Tileset.FLOOR.equals(tile)) {
            tileName = "Floor";
        } else if (Tileset.WALL.equals(tile)) {
            tileName = "Wall";
        } else if (Tileset.UNLOCKED_DOOR.equals(tile)) {
            tileName = "Unlocked door";
        }

        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(4, HEIGHT + 1, tileName);
        StdDraw.show();
    }

    public static void displayShortestMovingPath(TETile[][] world, Avatar ava) {
        int x = (int) StdDraw.mouseX();
        int y = (int) StdDraw.mouseY();
        TETile tile;

        try {
            tile = world[x][y];
            if (Tileset.WALL.equals(tile)) {
                return;
            }
        } catch (IndexOutOfBoundsException exp) {
            return;
        }

        Coordinate startPoint = ava.getCurrentPos();
        Coordinate endPoint = new Coordinate(x, y);
        AStar astar = new AStar(world, startPoint, endPoint);
        List<Coordinate> shortestPathNodes = astar.getShortestPath();
        Set<Coordinate> doorTiles = new HashSet<>();

        // show the shortest path
        for (var e : shortestPathNodes) {
            if (Tileset.UNLOCKED_DOOR.equals(world[e.getX()][e.getY()])) {
                doorTiles.add(e);
            }
            if (e.equals(ava.getCurrentPos())) {
                continue;
            }
            world[e.getX()][e.getY()] = Tileset.SAND;
        }
        Engine.ter.renderFrame(world);

        // turn back world
        for (var e : shortestPathNodes) {
            if (doorTiles.contains(e)) {
                world[e.getX()][e.getY()] = Tileset.UNLOCKED_DOOR;
            }
            else if (e.equals(ava.getCurrentPos())) {
                world[e.getX()][e.getY()] = Tileset.AVATAR;
            } else {
                world[e.getX()][e.getY()] = Tileset.FLOOR;
            }
        }
        Engine.ter.renderFrame(world);
    }
}
