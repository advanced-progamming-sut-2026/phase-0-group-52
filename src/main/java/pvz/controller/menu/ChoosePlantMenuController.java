package pvz.controller.menu;

import pvz.model.App;
import pvz.model.entities.plants.CollectionPlant;
import pvz.model.entities.plants.Plants;
import pvz.model.level.Level;
import pvz.view.ChoosePlantMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChoosePlantMenuController{

    private final App app;
    private final ChoosePlantMenu view = new ChoosePlantMenu();
    private final Level level;

    public ChoosePlantMenuController(App app, Level level){
        this.app = app;
        this.level = level;
    }

    public void handleCommand(String[] parts){
        if(parts.length < 2){
            view.showError("Invalid command.");
            return;
        }
        if(parts.length < 2){
            view.showError("Invalid command.");
            return;
        }
        switch(parts[1]){
            case "show":
                if(parts.length >= 3 && parts[2].equals("current")) System.out.println("Current menu: " + app.getCurrentmenu());
                else view.showError("Usage: menu show current");
                break;
            case "enter":
                handleEnter(parts);
                break;
            case "all":
                ArrayList<CollectionPlant> unlockedplants = app.getCurrentuser().getCollection().getUnlockedplants();
                System.out.println("------------");
                for(int i = 0; i < unlockedplants.size(); i++){
                    CollectionPlant plant = unlockedplants.get(i);
                    System.out.println(i + ". " + plant.getType().getName() + ":");
                    System.out.println("  - Category: " + plant.getType().getCategory().toString());
                    System.out.println("  - Description: " + plant.getType().getDescription());
                    System.out.println("  - Tags: " + plant.getType().getTags());
                    System.out.println("  - Sun Cost: " + plant.getCost());
                    System.out.println("  - Level: " + plant.getLevel());
                    System.out.println("  - Max HP: " + plant.getMaxhp());
                    System.out.println("  - Attack Damage: " + plant.getAttackdamage());
                }
                System.out.println("------------");
                break;
            case "available":
                ArrayList<Plants> allowedplants = level.getAllowedplants();
                List<CollectionPlant> availableplants = app.getCurrentuser().getCollection().getUnlockedplants().stream().filter(plant -> allowedplants.contains(plant.getType())).collect(Collectors.toList());
                for(int i = 0; i < availableplants.size(); i++){
                    CollectionPlant plant = availableplants.get(i);
                    System.out.println(i + ". " + plant.getType().getName() + ":");
                    System.out.println("  - Category: " + plant.getType().getCategory().toString());
                    System.out.println("  - Description: " + plant.getType().getDescription());
                    System.out.println("  - Tags: " + plant.getType().getTags());
                    System.out.println("  - Sun Cost: " + plant.getCost());
                    System.out.println("  - Level: " + plant.getLevel());
                    System.out.println("  - Max HP: " + plant.getMaxhp());
                    System.out.println("  - Attack Damage: " + plant.getAttackdamage());
                }
                System.out.println("------------");
                break;
        }
        switch(parts[0]){
            case "add":
                System.out.println("game ke khafan shod mizanamesh");
                break;
            case "remove":
                System.out.println("game ke khafan shod mizanamesh");
                break;
            case "boost":
                System.out.println("game rideh felan nabayad bezanamesh");
                break;
            case "start":
                System.out.println("ha kholasse");
        }

    }
}
