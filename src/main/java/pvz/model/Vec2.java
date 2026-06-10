package pvz.model;

public class Vec2 {
    public double x;
    public double y;

    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2 addVec(Vec2 a, Vec2 b){
        return new Vec2(a.x+b.x, a.y+b.y);

    }


}
