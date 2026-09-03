package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.Projectile;
import model.entities.plants.Muzzle;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;

import java.util.ArrayList;
import java.util.List;

public class PeaPod extends Shooter {

    public static final int MAX_HEADS = 5;
    public static final String HEADS = "heads";

    private static final double SPACING = 0.16d;
    private static final double[] FOOD_FRAMES = {0.10d, 0.30d, 0.50d, 0.70d, 0.92d};

    private int heads = 1;

    public PeaPod(Vec2 position) {
        super(Plants.PEA_POD, position);
    }

    public int getHeads() {
        return heads;
    }

    public boolean addHead() {
        if (heads >= MAX_HEADS) {
            return false;
        }
        heads++;
        return true;
    }

    @Override
    public List<Muzzle> ports() {
        List<Muzzle> all = new ArrayList<Muzzle>();
        for (int head = 1; head <= MAX_HEADS; head++) {
            all.add(new Muzzle("head" + head, 0, 1, (head - 1) * SPACING));
        }
        return all;
    }

    @Override
    public List<Muzzle> portsFor(String state) {
        List<Muzzle> all = ports();
        if (Projectile.FED.equals(state)) {
            List<Muzzle> burst = new ArrayList<Muzzle>();
            for (double frame : FOOD_FRAMES) {
                burst.add(new Muzzle("head1", 0, 1, frame));
            }
            return burst;
        }
        int grown = state != null && state.startsWith(HEADS)
                ? parse(state.substring(HEADS.length())) : heads;
        return all.subList(0, Math.max(1, Math.min(MAX_HEADS, grown)));
    }

    @Override
    protected String shotVariant() {
        return isBursting() ? Projectile.FED : HEADS + heads;
    }

    private static int parse(String digits) {
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
