package pvz.model.shop;

public class Item {
    private int price;
    private boolean isdaily;
    private int id;
    private int count;
    private PlantType type;

    public Item(int price, boolean isdaily, int id, int count, PlantType type) {
        this.price = price;
        this.isdaily = isdaily;
        this.id = id;
        this.count = count;
        this.type = type;
    }

    public PlantType getType() {
        return type;
    }

    public void setType(PlantType type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isIsdaily() {
        return isdaily;
    }

    public void setIsdaily(boolean isdaily) {
        this.isdaily = isdaily;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
