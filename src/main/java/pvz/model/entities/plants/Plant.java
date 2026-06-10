package pvz.model.entities.plants;


import pvz.model.Vec2;

public abstract class Plant implements PlantInterface {
    private Plants type;
    private double hp;
    private int price;
    private Vec2 position;
    private double attackdamage;

    public Plant(Plants type, double hp, int price, Vec2 position, double attackdamage) {
        this.type = type;
        this.hp = hp;
        this.price = price;
        this.position = position;
        this.attackdamage = attackdamage;
    }

    public void boost(){

    }

    public Plants getType() {
        return type;
    }

    public void setType(Plants type) {
        this.type = type;
    }

    public double getHp() {
        return hp;
    }

    public void setHp(double hp) {
        this.hp = hp;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Vec2 getPosition() {
        return position;
    }

    public void setPosition(Vec2 position) {
        this.position = position;
    }

    public double getAttackdamage() {
        return attackdamage;
    }

    public void setAttackdamage(double attackdamage) {
        this.attackdamage = attackdamage;
    }
}
