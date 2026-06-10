package pvz.model.entities;

public class Lawnmower {
    private int line;
    private boolean isactive;

    public void destroyZombies(){

    }

    public Lawnmower(int line, boolean isactive) {
        this.line = line;
        this.isactive = isactive;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public boolean isIsactive() {
        return isactive;
    }

    public void setIsactive(boolean isactive) {
        this.isactive = isactive;
    }
}
