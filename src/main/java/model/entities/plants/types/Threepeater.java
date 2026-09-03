package model.entities.plants.types;

import model.Vec2;
import model.entities.plants.Muzzle;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;

import java.util.Arrays;
import java.util.List;

public class Threepeater extends Shooter {

    public Threepeater(Vec2 position) {
        super(Plants.THREEPEATER, position);
    }

    @Override
    public List<Muzzle> ports() {
        return Arrays.asList(
                new Muzzle("up", -1, 1),
                new Muzzle("mid", 0, 1),
                new Muzzle("down", 1, 1));
    }
}
