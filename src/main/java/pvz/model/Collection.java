package pvz.model;

import pvz.model.entities.plants.CollectionPlant;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.zombies.Zombies;

import java.util.ArrayList;

public class Collection {
    private ArrayList<CollectionPlant> unlockedplants;
    private ArrayList<CollectionPlant> lockedplants;
    private ArrayList<Zombies> unlockedzombies;
    private ArrayList<Zombies> lockedzombies;

    public ArrayList<CollectionPlant> getUnlockedplants(){
        return unlockedplants;
    }

    public ArrayList<CollectionPlant> getLockedplants(){
        return lockedplants;
    }

    public ArrayList<Zombies> getUnlockedzombies(){
        return unlockedzombies;
    }

    public ArrayList<Zombies> getLockedzombies(){
        return lockedzombies;
    }

    public void unlockPlant(Plants plant){

    }
}
