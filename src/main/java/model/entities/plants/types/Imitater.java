package model.entities.plants.types;

import model.Vec2;
import model.entities.plants.Modifier;
import model.entities.plants.Plants;

public class Imitater extends Modifier {

    private Plants copiedType;

    public Imitater(Vec2 position) {
        super(Plants.IMITATER, position);
    }

    public Plants getCopiedType() { return copiedType; }
    public void setCopiedType(Plants copiedType) { this.copiedType = copiedType; }
}
