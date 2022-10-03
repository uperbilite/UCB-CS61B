package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.*;
import java.util.stream.Collectors;

public class Generator {
    private TETile[][] world;
    /** Random Object given by seed, seed is from command line or keyboard input. */
    private Random rand;
    private int width;
    private int height;
    private int addRoomAttempts;
    private int extraRoomSize;
    private double bendingDegree;
    /** The count of all regions. A region is either a room or an independent hallway. */
    private int regionNum;
    /** A mapping from a coordinate to the region id that it belongs to. */
    private HashMap<Coordinate, Integer> regionIdByPos;

    public Generator(Random rand) {
        this.rand = rand;
        this.width = Engine.WIDTH;
        this.height = Engine.HEIGHT;
        this.addRoomAttempts = Engine.addRoomAttempts;
        this.extraRoomSize = Engine.extraRoomSize;
        this.bendingDegree = Engine.bendingDegree;
        this.regionNum = 0;
        this.regionIdByPos = new HashMap<>();
        this.world = new TETile[width][height];
    }

    public TETile[][] generateWorld() {
        fillWithWallTiles();
        addRandomRooms();
        addHallWays();
        connectRegion();
        return world;
    }

    private void fillWithWallTiles() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                world[x][y] = Tileset.WALL;
            }
        }
    }

    private void addRandomRooms() {
        for (int i = 0; i < addRoomAttempts; i++) {
            Room r = new Room(rand, extraRoomSize);
            if (r.isValidRoom(world)) {
                addRoom(r);
            }
        }
    }

    private void addRoom(Room room) {
        int w = room.getWidth();
        int h = room.getHeight();
        int x = room.getPos().getX();
        int y = room.getPos().getY();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                world[x + i][y + j] = Tileset.FLOOR;
                regionIdByPos.put(new Coordinate(x + i, y + j), regionNum);
            }
        }
        regionNum++;
    }

    private void addHallWays() {
        for (int y = height - 2; y > 0; y -= 2) {
            for (int x = 1; x < width; x += 2) {
                growHallWayFromPos(x, y);
            }
        }
    }

    private void growHallWayFromPos(int x, int y) {
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};
        for (int i = 0; i < 8; i++) {
            if (world[x + dx[i]][y + dy[i]] != Tileset.WALL) {
                return;
            }
        }

        Stack<Coordinate> grownWay = new Stack<>();
        List<Coordinate> directions = getDirections();
        Coordinate lastDirection = null;
        grownWay.push(new Coordinate(x, y));

        while (!grownWay.isEmpty()) {
            Coordinate c = grownWay.peek();
            world[c.getX()][c.getY()] = Tileset.FLOOR;
            regionIdByPos.put(new Coordinate(c.getX(), c.getY()), regionNum);
            List<Coordinate> possibleDirections = new ArrayList<>();

            for (var d : directions) {
                if (isPossibleDirection(c, d)) {
                    possibleDirections.add(d);
                }
            }

            if (!possibleDirections.isEmpty()) {
                Coordinate d;
                if (possibleDirections.contains(lastDirection)
                        && RandomUtils.uniform(rand) > bendingDegree) {
                    d = lastDirection;
                } else {
                    int randomIdx = RandomUtils.uniform(rand, possibleDirections.size());
                    d = possibleDirections.get(randomIdx);
                }
                Coordinate next = new Coordinate(c.getX() + d.getX(), c.getY() + d.getY());
                lastDirection = d;
                grownWay.push(next);
            } else {
                lastDirection = null;
                grownWay.pop();
            }
        }
        regionNum++;
    }

    /**
     * Use Disjoint Sets to make connection, each time when there is a merge between
     * two regions, it will change the mergedRegion array. Then it will remove the
     * extraneous connector that connects the regions that just merged. When there is
     * only one region left, the merge is done.
     *
     * The process can be described below:
     *      1. Pick a random room to be the main region.
     *      2. Pick a random connector that touches the main region and open it up.
     *      3. The connected region is now part of the main one. Merge it.
     *      4. Remove any extraneous connectors.
     *      5. If there are still connectors left, go to step 2.
     */
    private void connectRegion() {
        var regionIdsByConnectPoint = getConnectPoints();
        var connectPoints = new ArrayList<>(regionIdsByConnectPoint.keySet());
        int[] mergedRegion = new int[regionNum];
        Set<Integer> seperatedRegionNum = new HashSet<>();
        for (int i = 0; i < regionNum; i++) {
            mergedRegion[i] = i;
            seperatedRegionNum.add(i);
        }

        while (seperatedRegionNum.size() != 1) {
            int idx = RandomUtils.uniform(rand, connectPoints.size());
            Coordinate c = connectPoints.get(idx);
            addDoor(c);

            var regionIds = regionIdsByConnectPoint.get(c);
            var mergedRegionIdsArr =
                    regionIds
                            .stream()
                            .map(i -> mergedRegion[i])
                            .collect(Collectors.toList());
            assert mergedRegionIdsArr.size() == 2;
            int father = mergedRegionIdsArr.get(0);
            int son = mergedRegionIdsArr.get(1);

            for (int i = 0; i < regionNum; i++) {
                if (mergedRegion[i] == son) {
                    mergedRegion[i] = father;
                }
            }

            seperatedRegionNum.remove(son);

            connectPoints.removeIf((Coordinate key) -> {
                int connectRegionsNum =
                        regionIdsByConnectPoint
                                .get(key)
                                .stream()
                                .map(i -> mergedRegion[i])
                                .collect(Collectors.toSet())
                                .size();
                return connectRegionsNum <= 1;
            });
        }
    }

    /**
     * Connect point is a coordinate that locates between two or more different regions.
     * @return A hashmap which mapping from connect point to a set of region id that divided
     * by this connect point
     */
    private HashMap<Coordinate, Set<Integer>> getConnectPoints() {
        HashMap<Coordinate, Set<Integer>> regionIdsByPoint = new HashMap<>();
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                if (world[i][j] != Tileset.WALL) {
                    continue;
                }
                Set<Integer> regionIds = new HashSet<>();
                for (int k = 0; k < 4; k++) {
                    int nx = i + dx[k];
                    int ny = j + dy[k];
                    Coordinate c = new Coordinate(nx, ny);
                    if (regionIdByPos.containsKey(c)) {
                        regionIds.add(regionIdByPos.get(c));
                    }
                }
                if (regionIds.size() < 2) {
                    continue;
                }
                regionIdsByPoint.put(new Coordinate(i, j), regionIds);
            }
        }
        return regionIdsByPoint;
    }

    private void addDoor(Coordinate c) {
        world[c.getX()][c.getY()] = Tileset.SAND;
    }

    private boolean isPossibleDirection(Coordinate c, Coordinate d) {
        // TODO: use enum
        int nx = c.getX() + d.getX();
        int ny = c.getY() + d.getY();
        int[] dx = null;
        int[] dy = null;

        if (!(nx > 0 && nx < width - 1 && ny > 0 && ny < height - 1)
                || (world[nx][ny] != Tileset.WALL)) {
            return false;
        }

        if (d.getX() == -1 && d.getY() == 0) { // left
            dx = new int[]{-1, -1, -1, 0, 0};
            dy = new int[]{-1, 0, 1, -1, 1};
        } else if (d.getX() == 1 && d.getY() == 0) { // right
            dx = new int[]{0, 0, 1, 1, 1};
            dy = new int[]{-1, 1, -1, 0, 1};
        } else if (d.getX() == 0 && d.getY() == -1) { // down
            dx = new int[]{-1, -1, 0, 1, 1};
            dy = new int[]{0, -1, -1, 0, -1};
        } else if (d.getX() == 0 && d.getY() == 1) { // up
            dx = new int[]{-1, -1, 0, 1, 1};
            dy = new int[]{0, 1, 1, 0, 1};
        } else {
            assert false;
        }

        for (int i = 0; i < 5; i++) {
            if (world[nx + dx[i]][ny + dy[i]] != Tileset.WALL) {
                return false;
            }
        }
        return true;
    }

    private List<Coordinate> getDirections() {
        List<Coordinate> directions = new ArrayList<>();
        directions.add(new Coordinate(-1, 0));
        directions.add(new Coordinate(1, 0));
        directions.add(new Coordinate(0, -1));
        directions.add(new Coordinate(0, 1));
        return directions;
    }
}
