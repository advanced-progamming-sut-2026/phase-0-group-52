package pvz.controller.menu;

import pvz.model.App;
import pvz.model.ChapterType;
import pvz.model.Plant;
import pvz.model.entities.plants.CollectionPlant;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.zombies.Zombies;
import pvz.view.CollectionMenu;
import pvz.view.MenuType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class CollectionMenuController {

    private final App app;
    private final CollectionMenu view;

    public CollectionMenuController(App app){
        this.app = app;
        this.view = new CollectionMenu();
    }

    public void handleCommand(String[] parts){
        if(parts.length < 2){
            view.showError("Invalid command.");
            return;
        }
        if(parts[1]=="collection"){
            switch(parts[2]){
                case "show-plants":
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
                case "show-all-plants":
                    Plants[] plants = Plants.values();
                    System.out.println("------------");
                    for(int i = 0; i < plants.length; i++){
                        Plants plant = plants[i];
                        System.out.println(i + ". " + plant.getName() + ":");
                        System.out.println("  - Category: " + plant.getCategory().toString());
                        System.out.println("  - Description: " + plant.getDescription());
                        System.out.println("  - Tags: " + plant.getTags());
                        System.out.println("  - Base Sun Cost: " + plant.getBasecost());
                        System.out.println("  - Base HP: " + plant.getBaseHP());
                        System.out.println("  - Base Attack Damage: " + plant.getBasedamage());
                    }
                    System.out.println("------------");
                    break;
                case "show-zombies":
                    ArrayList<Zombies> unlockedzombies = app.getCurrentuser().getCollection().getUnlockedzombies();
                    System.out.println("hanooz zombie implement na");
                    break;
                case "show-all-zombies":
                    Zombies[] zombies = Zombies.values();
                    System.out.printf("hanooz zombie implement na");
                    break;
                case "show-plant":
                    String plantname = parts[4];
                    Optional<Plants> result = Arrays.asList(Plants.values()).stream().filter(i -> i.toString().equalsIgnoreCase(plantname)).findFirst();
                    Plants plant = null;
                    if(result.isPresent()){plant = result.get();}
                    else return;
                    System.out.println(plant.getName() + ":");
                    System.out.println("  - Category: " + plant.getCategory().toString());
                    System.out.println("  - Description: " + plant.getDescription());
                    System.out.println("  - Tags: " + plant.getTags());
                    System.out.println("  - Base Sun Cost: " + plant.getBasecost());
                    System.out.println("  - Base HP: " + plant.getBaseHP());
                    System.out.println("  - Base Attack Damage: " + plant.getBasedamage());
                    break;
                case "show-zombie":
                    System.out.println("baba kiram too zombie");
                    break;
                case "upgrade-plant":
                    System.out.println("not implemenet yet");
                    break;
                case "purchase-plant":
                    System.out.println("not implemented yet");
                    break;
            }

        }
        else{
            switch(parts[1]){
                case "show":
                    if(parts.length >= 3 && parts[2].equals("current")) System.out.println("Current menu: " + app.getCurrentmenu());
                    else view.showError("Usage: menu show current");
                    break;
                case "enter":
                    handleEnter(parts);
                    break;
            }
        }
    }
    private void handleEnter(String[] parts) {
        if (parts.length < 3) {
            view.showError("Usage: menu enter <menu_name>  |  menu enter chapter -c <chapterName>");
            return;
        }
        if (parts[2].equals("chapter")) {
            handleEnterChapter(parts);
            return;
        }
        try {
            MenuType target = MenuType.valueOf(parts[2].toUpperCase());
            app.setCurrentmenu(target);
        } catch (IllegalArgumentException e) {
            view.showError("Unknown menu: " + parts[2]);
        }
    }

    private void handleEnterChapter(String[] parts) {
        if (parts.length < 5 || !parts[3].equals("-c")) {
            view.showError("Usage: menu enter chapter -c <chapterName>");
            return;
        }
        ChapterType chapter;
        try {
            chapter = ChapterType.valueOf(parts[4].toUpperCase());
        } catch (IllegalArgumentException e) {
            view.showError("Invalid chapter: " + parts[4] +
                    ". Options: ANCIENT_EGYPT, FROSTBITE_CAVES, BIG_WAVE_BEACH, DARK_AGES");
            return;
        }
        view.showEnteredChapter(chapter.name());
    }
}
