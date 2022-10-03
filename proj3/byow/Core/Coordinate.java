package byow.Core;

import java.util.Objects;

public class Coordinate {
    private int x;
    private int y;

    Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
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
        Coordinate other = (Coordinate) o;
        return this.x == other.x && this.y == other.y;
    }

    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }
}
