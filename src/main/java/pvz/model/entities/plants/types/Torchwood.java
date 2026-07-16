package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.Modifier;
import pvz.model.entities.plants.Plants;

public class Torchwood extends Modifier {

    private boolean blueFlame = false;

    public Torchwood(Vec2 position) {
        super(Plants.TORCHWOOD, position);
    }

    public boolean isBlueFlame() { return blueFlame; }

    @Override
    public void onPlantFood(Game game) {
        blueFlame = true;
    }
}
