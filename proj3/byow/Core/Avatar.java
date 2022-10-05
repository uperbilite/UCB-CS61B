package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Avatar {
    private TETile[][] world;
    /** currennt position. */
    private Coordinate cpos;
    /** previous position. */
    private Coordinate ppos;
    /** previous position Tile kind. */
    private TETile pposTile;

    Avatar(TETile[][] world) {
        this.world = world;
        this.cpos = new Coordinate(1, 1);
        this.ppos = new Coordinate(1, 1);
        // TODO: not only floor
        this.pposTile = Tileset.FLOOR;
        refreshAvatarPosition();
    }

    public void refreshAvatarPosition() {
        world[ppos.getX()][ppos.getY()] = pposTile;
        ppos = cpos;
        pposTile = world[ppos.getX()][ppos.getY()];
        world[cpos.getX()][cpos.getY()] = Tileset.AVATAR;
    }

    public void moveUp() {
        if (world[cpos.getX()][cpos.getY() + 1] == Tileset.WALL) {
            return;
        }
        cpos = cpos.shift(0, 1);
        refreshAvatarPosition();
    }

    public void moveDown() {
        if (world[cpos.getX()][cpos.getY() - 1] == Tileset.WALL) {
            return;
        }
        cpos = cpos.shift(0, -1);
        refreshAvatarPosition();
    }

    public void moveLeft() {
        if (world[cpos.getX() - 1][cpos.getY()] == Tileset.WALL) {
            return;
        }
        cpos = cpos.shift(-1, 0);
        refreshAvatarPosition();
    }

    public void moveRight() {
        if (world[cpos.getX() + 1][cpos.getY()] == Tileset.WALL) {
            return;
        }
        cpos = cpos.shift(1, 0);
        refreshAvatarPosition();
    }
}
