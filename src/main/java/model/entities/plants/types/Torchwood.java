package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.Modifier;
import model.entities.plants.Plants;

public class Torchwood extends Modifier {

    private boolean blueFlame = false;

    public Torchwood(Vec2 position) {
        super(Plants.TORCHWOOD, position);
    }

    public boolean isBlueFlame() {
        return blueFlame;
    }

    @Override
    public void onPlantFood(Game game) {
        blueFlame = true;
    }
}
