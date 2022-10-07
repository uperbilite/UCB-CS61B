package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

class Room {
    private Coordinate pos;
    private int width;
    private int height;

    Room(Random rand, int extraRoomSize) {
        this.pos = new Coordinate(RandomUtils.uniform(rand, Engine.WIDTH),
                                  RandomUtils.uniform(rand, Engine.HEIGHT));
        this.width = RandomUtils.uniform(rand, 3, 4 + extraRoomSize);
        this.height = RandomUtils.uniform(rand, 3, 4 + extraRoomSize);
    }

    public boolean isValidPosition(TETile[][] world) {
        return pos.getX() % 2 == 1 && pos.getY() % 2 == 1
                && pos.getX() > 0 && pos.getX() + width - 1 < world.length - 1
                && pos.getY() > 0 && pos.getY() + height - 1 < world[0].length - 1;
    }

    public boolean isValidSize() {
        return width % 2 == 1 && height % 2 == 1;
    }

    public boolean isNotOverlapOtherRoom(TETile[][] world) {
        for (int i = -1; i <= width; i++) {
            for (int j = -1; j <= height; j++) {
                if (world[pos.getX() + i][pos.getY() + j] != Tileset.WALL) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isValidRoom(TETile[][] world) {
        return isValidPosition(world) && isValidSize() && isNotOverlapOtherRoom(world);
    }

    public Coordinate getPos() {
        return this.pos;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

}
