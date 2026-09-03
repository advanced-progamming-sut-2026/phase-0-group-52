package model.entities.plants.types;

import model.Vec2;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;

public class GooPeashooter extends Shooter {

    public GooPeashooter(Vec2 position) {
        super(Plants.GOO_PEASHOOTER, position);
    }
}
