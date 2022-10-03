package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

class Room {
    private Random rand;
    private Position pos;
    private int width;
    private int height;

    Room(Random rand, int extraRoomSize) {
        this.rand = rand;
        this.pos = new Position();
        this.width = RandomUtils.uniform(rand, 3, 4 + extraRoomSize);
        this.height = RandomUtils.uniform(rand, 3, 4 + extraRoomSize);
    }

    public boolean isValidPosition(TETile[][] world) {
        return pos.x % 2 == 1 && pos.y % 2 == 1
                && pos.x > 0 && pos.x + width - 1 < world.length - 1
                && pos.y > 0 && pos.y + height - 1 < world[0].length - 1;
    }

    public boolean isValidSize() {
        return width % 2 == 1 && height % 2 == 1;
    }

    public boolean isNotOverlapOtherRoom(TETile[][] world) {
        for (int i = -1; i <= width; i++) {
            for (int j = -1; j <= height; j++) {
                if (world[pos.x + i][pos.y + j] != Tileset.WALL) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isValidRoom(TETile[][] world) {
        return isValidPosition(world) && isValidSize() && isNotOverlapOtherRoom(world);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Position getPos() {
        return this.pos;
    }

    class Position {
        int x;
        int y;

        Position() {
            this.x = RandomUtils.uniform(rand, Engine.WIDTH);
            this.y = RandomUtils.uniform(rand, Engine.HEIGHT);
        }

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        int getX() {
            return this.x;
        }

        int getY() {
            return this.y;
        }
    }
}
