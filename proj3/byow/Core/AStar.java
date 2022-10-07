package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.*;

public class AStar {
    private final TETile[][] world;
    private final Coordinate startPoint;
    private final Coordinate endPoint;
    /** The constance number used for heuristic function. Must be lower than 1. */
    private static final double D = 1.0;

    public AStar(TETile[][] world, Coordinate startPoint, Coordinate endPoint) {
        this.world = world;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    public List<Coordinate> getShortestPath() {
        List<Coordinate> shortestPathNodes = new ArrayList<>();
        PriorityQueue<Node> open = new PriorityQueue<>();
        Set<Node> close = new HashSet<>();

        Node startNode = new Node(startPoint, null, 0, 0);
        open.add(startNode);

        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        while (!open.isEmpty()) {
            Node n = open.peek();
            if (isReachEndPoint(n)) {
                Node t = n;
                while (t != null) {
                    shortestPathNodes.add(t.getPos());
                    t = t.getParent();
                }
                break;
            }

            close.add(open.poll());

            for (int i = 0; i < 4; i++) {
                int nx = n.getPos().getX() + dx[i];
                int ny = n.getPos().getY() + dy[i];
                Coordinate c = new Coordinate(nx, ny);
                Node m = new Node(c, null, n.getDistance() + 1, 0);

                if (Tileset.WALL.equals(world[c.getX()][c.getY()]) || close.contains(m)) {
                    continue;
                }

                if (!open.contains(m)) {
                    m.setParent(n);
                    m.setPriority(calculatePriority(m));
                    open.add(m);
                }

            }
        }
        return shortestPathNodes;
    }

    private boolean isReachEndPoint(Node n) {
        return n.getPos().getX() == endPoint.getX() && n.getPos().getY() == endPoint.getY();
    }

    private double calculatePriority(Node n) {
        return n.getDistance() + manhattanHeuristic(n);
    }

    /** compute heuristic by Mandattan method. */
    private double manhattanHeuristic(Node n) {
        int dx = Math.abs(n.getPos().getX() - endPoint.getX());
        int dy = Math.abs(n.getPos().getY() - endPoint.getY());
        return D * (dx + dy);
    }

    private class Node implements Comparable<Node> {
        Coordinate pos;
        Node parent;
        int distance;
        /** Represent the actual path length from Avatar position to this Node's position,
         *  plus the estimate path length from this Node to the Mouse's position.
         *  Lower value has higher priority
         */
        double priority;

        Node(Coordinate pos, Node parent, int distance, double priority) {
            this.pos = pos;
            this.parent = parent;
            this.distance = distance;
            this.priority = priority;
        }

        Coordinate getPos() {
            return this.pos;
        }

        Node getParent() {
            return this.parent;
        }

        int getDistance() {
            return this.distance;
        }

        void setParent(Node parent) {
            this.parent = parent;
        }

        void setPriority(double priority) {
            this.priority = priority;
        }

        @Override
        public int compareTo(Node o) {
            return Double.compare(this.priority, o.priority);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null) {
                return false;
            }
            if (this.getClass() != o.getClass()) {
                return false;
            }
            Node other = (Node) o;
            return this.getPos().equals(other.getPos());
        }

        public int hashCode() {
            return Objects.hash(this.pos);
        }
    }
}
