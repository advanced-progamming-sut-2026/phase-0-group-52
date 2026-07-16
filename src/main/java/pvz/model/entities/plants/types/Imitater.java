package pvz.model.entities.plants.types;

import pvz.model.Vec2;
import pvz.model.entities.plants.Modifier;
import pvz.model.entities.plants.Plants;

public class Imitater extends Modifier {

    private Plants copiedType;

    public Imitater(Vec2 position) {
        super(Plants.IMITATER, position);
    }

    public Plants getCopiedType() { return copiedType; }
    public void setCopiedType(Plants copiedType) { this.copiedType = copiedType; }
}
