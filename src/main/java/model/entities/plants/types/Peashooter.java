package model.entities.plants.types;

import model.Vec2;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;

public class Peashooter extends Shooter {

    public Peashooter(Vec2 position) {
        super(Plants.PEASHOOTER, position);
    }
}
