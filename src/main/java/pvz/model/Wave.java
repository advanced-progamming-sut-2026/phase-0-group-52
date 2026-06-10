package pvz.model;

import pvz.model.entities.zombies.Zombie;

import java.util.ArrayList;

public class Wave {
    private ArrayList<Zombie> zombies;
    private int wavenumber;
    private int delay;

    public Wave(ArrayList<Zombie> zombies, int wavenumber, int delay) {
        this.zombies = zombies;
        this.wavenumber = wavenumber;
        this.delay = delay;
    }

    public ArrayList<Zombie> getZombies() {
        return zombies;
    }

    public void setZombies(ArrayList<Zombie> zombies) {
        this.zombies = zombies;
    }

    public int getWavenumber() {
        return wavenumber;
    }

    public void setWavenumber(int wavenumber) {
        this.wavenumber = wavenumber;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}
