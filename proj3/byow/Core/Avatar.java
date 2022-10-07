package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Avatar {
    private TETile[][] world;
    /** current position. */
    private Coordinate currentPos;
    /** previous position. */
    private Coordinate previousPos;
    /** previous position Tile kind. */
    private TETile previousPosTile;

    Avatar(TETile[][] world) {
        this.world = world;
        this.currentPos = new Coordinate(1, 1);
        this.previousPos = new Coordinate(1, 1);
        this.previousPosTile = Tileset.FLOOR;
        refreshAvatarPosition();
    }

    public Coordinate getCurrentPos() {
        return this.currentPos;
    }

    public void refreshAvatarPosition() {
        world[previousPos.getX()][previousPos.getY()] = previousPosTile;
        previousPos = currentPos;
        previousPosTile = world[previousPos.getX()][previousPos.getY()];
        world[currentPos.getX()][currentPos.getY()] = Tileset.AVATAR;
    }

    public void moveUp() {
        if (world[currentPos.getX()][currentPos.getY() + 1] == Tileset.WALL) {
            return;
        }
        currentPos = currentPos.shift(0, 1);
        refreshAvatarPosition();
    }

    public void moveDown() {
        if (world[currentPos.getX()][currentPos.getY() - 1] == Tileset.WALL) {
            return;
        }
        currentPos = currentPos.shift(0, -1);
        refreshAvatarPosition();
    }

    public void moveLeft() {
        if (world[currentPos.getX() - 1][currentPos.getY()] == Tileset.WALL) {
            return;
        }
        currentPos = currentPos.shift(-1, 0);
        refreshAvatarPosition();
    }

    public void moveRight() {
        if (world[currentPos.getX() + 1][currentPos.getY()] == Tileset.WALL) {
            return;
        }
        currentPos = currentPos.shift(1, 0);
        refreshAvatarPosition();
    }
}
