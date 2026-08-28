package view.gui.layout;

import com.badlogic.gdx.math.Rectangle;

public enum Handle {
    NONE, MOVE, N, S, E, W, NE, NW, SE, SW;

    public static Handle at(Rectangle box, float x, float y, float grip) {
        if (box == null) {
            return NONE;
        }
        boolean inside = x >= box.x - grip && x <= box.x + box.width + grip
                && y >= box.y - grip && y <= box.y + box.height + grip;
        if (!inside) {
            return NONE;
        }
        boolean left = Math.abs(x - box.x) <= grip;
        boolean right = Math.abs(x - (box.x + box.width)) <= grip;
        boolean bottom = Math.abs(y - box.y) <= grip;
        boolean top = Math.abs(y - (box.y + box.height)) <= grip;
        return corner(left, right, bottom, top);
    }

    private static Handle corner(boolean left, boolean right, boolean bottom, boolean top) {
        if (top && left) {
            return NW;
        }
        if (top && right) {
            return NE;
        }
        if (bottom && left) {
            return SW;
        }
        if (bottom && right) {
            return SE;
        }
        if (top) {
            return N;
        }
        if (bottom) {
            return S;
        }
        if (left) {
            return W;
        }
        if (right) {
            return E;
        }
        return MOVE;
    }

    public boolean resizes() {
        return this != NONE && this != MOVE;
    }

    public boolean pullsLeft() {
        return this == W || this == NW || this == SW;
    }

    public boolean pullsRight() {
        return this == E || this == NE || this == SE;
    }

    public boolean pullsTop() {
        return this == N || this == NE || this == NW;
    }

    public boolean pullsBottom() {
        return this == S || this == SE || this == SW;
    }
}
