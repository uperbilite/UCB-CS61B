package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Avatar {
    Coordinate pos;
    TETile[][] world;

    Avatar(TETile[][] world) {
        pos = new Coordinate(1, 1);
        this.world = world;
        refreshAvatarPosition();
    }

    public void refreshAvatarPosition() {
        world[pos.getX()][pos.getY()] = Tileset.AVATAR;
    }

    public void moveUp() {
        if (world[pos.getX()][pos.getY() + 1] == Tileset.WALL) {
            return;
        }
        pos = pos.shift(0, 1);
        refreshAvatarPosition();
    }

    public void moveDown() {
        if (world[pos.getX()][pos.getY() - 1] == Tileset.WALL) {
            return;
        }
        pos = pos.shift(0, -1);
        refreshAvatarPosition();
    }

    public void moveLeft() {
        if (world[pos.getX() - 1][pos.getY()] == Tileset.WALL) {
            return;
        }
        pos = pos.shift(-1, 0);
        refreshAvatarPosition();
    }

    public void moveRight() {
        if (world[pos.getX() + 1][pos.getY()] == Tileset.WALL) {
            return;
        }
        pos = pos.shift(1, 0);
        refreshAvatarPosition();
    }
}
