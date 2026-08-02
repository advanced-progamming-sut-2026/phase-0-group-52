package controller.menu;

import controller.Navigation;
import model.App;
import model.User;
import model.entities.plants.PlantData;
import model.entities.plants.Plants;
import model.entities.zombies.Zombies;
import view.MenuType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CollectionMenuController {

    private static final Pattern UPGRADE =
            Pattern.compile("^upgrade\\s+plant\\s+-t\\s+(.+)$");
    private static final Pattern BUY =
            Pattern.compile("^buy\\s+plant\\s+-t\\s+(.+)$");
    private static final Pattern DOC_UPGRADE =
            Pattern.compile("^menu\\s+collection\\s+upgrade-plant\\s+-p\\s+(.+)$");
    private static final Pattern DOC_BUY =
            Pattern.compile("^menu\\s+collection\\s+purchase-plant\\s+-p\\s+(.+)$");
    private static final Pattern DOC_SHOW_PLANT =
            Pattern.compile("^menu\\s+collection\\s+show-plant\\s+-p\\s+(.+)$");
    private static final Pattern DOC_SHOW_ZOMBIE =
            Pattern.compile("^menu\\s+collection\\s+show-zombie\\s+-z\\s+(.+)$");

    private static final int PLANT_PURCHASE_PRICE = 2000;

    private final App app;

    public CollectionMenuController(App app) {
        this.app = app;
    }

    public void handleCommand(String line) {
        String command = line.trim();
        Matcher dm;
        if (command.equals("menu collection show-plants") || command.equals("menu collection show-all-plants")) {
            showPlants(); return;
        }
        if (command.equals("menu collection show-zombies") || command.equals("menu collection show-all-zombies")) {
            showZombies(); return;
        }
        if ((dm = DOC_UPGRADE.matcher(command)).matches()) { doUpgrade(dm.group(1)); return; }
        if ((dm = DOC_BUY.matcher(command)).matches()) { buyPlant(dm.group(1)); return; }
        if ((dm = DOC_SHOW_PLANT.matcher(command)).matches()) { showOnePlant(dm.group(1)); return; }
        if ((dm = DOC_SHOW_ZOMBIE.matcher(command)).matches()) { showOneZombie(dm.group(1)); return; }
        String[] parts = command.split("\\s+");
        if (parts[0].equals("menu")) {
            handleMenu(parts);
            return;
        }
        Matcher m;
        if (command.equals("show plants")) {
            showPlants();
        } else if (command.equals("show zombies")) {
            showZombies();
        } else if ((m = UPGRADE.matcher(command)).matches()) {
            doUpgrade(m.group(1));
        } else if ((m = BUY.matcher(command)).matches()) {
            buyPlant(m.group(1));
        } else {
            System.out.println("invalid command");
        }
    }

    private void doUpgrade(String name) {
        User user = app.getCurrentuser();
        if (user == null) { System.out.println("Error: No user is logged in."); return; }
        Plants type = findPlant(name);
        if (type == null) { System.out.println("Error: Unknown plant: " + name); return; }
        System.out.println(PlantData.upgrade(user, type));
    }

    private void showOnePlant(String name) {
        Plants p = findPlant(name);
        if (p == null) { System.out.println("Error: Unknown plant: " + name); return; }
        int level = app.getCurrentuser() != null ? app.getCurrentuser().getPlantLevel(p) : 1;
        System.out.printf("%s | %s | cost: %d | HP: %d | damage: %d | level: %d%n",
                p.getName(), p.getCategory(), p.getCost(), p.getBaseHP(), p.getDamage(), level);
    }

    private void showOneZombie(String name) {
        for (model.entities.zombies.Zombies z : model.entities.zombies.Zombies.values()) {
            if (z.getName().equalsIgnoreCase(name.trim())
                    || z.name().equalsIgnoreCase(name.trim().replace(' ', '_').replace('-', '_'))) {
                System.out.printf("%s | HP: %.0f | speed: %.3f | eatDPS: %.0f | armor: %s%n",
                        z.getName(), z.getHp(), z.getSpeed(), z.getEatDPS(), z.getArmor());
                return;
            }
        }
        System.out.println("Error: Unknown zombie: " + name);
    }

    private void showPlants() {
        User user = app.getCurrentuser();
        System.out.println("Plants collection:");
        System.out.printf("%-20s %-14s %-6s %-6s %-7s %s%n",
                "Name", "Category", "Cost", "HP", "Damage", "Level");
        for (Plants p : Plants.values()) {
            int level = user != null ? user.getPlantLevel(p) : 1;
            System.out.printf("%-20s %-14s %-6d %-6d %-7d %d%n",
                    p.getName(), p.getCategory(), p.getCost(), p.getBaseHP(), p.getDamage(), level);
        }
    }

    private void showZombies() {
        System.out.println("Zombies collection:");
        System.out.printf("%-26s %-8s %-8s %-8s %s%n", "Name", "HP", "Speed", "EatDPS", "Armor");
        for (Zombies z : Zombies.values()) {
            System.out.printf("%-26s %-8.0f %-8.3f %-8.0f %s%n",
                    z.getName(), z.getHp(), z.getSpeed(), z.getEatDPS(), z.getArmor());
        }
    }

    private void buyPlant(String name) {
        User user = app.getCurrentuser();
        if (user == null) { System.out.println("Error: No user is logged in."); return; }
        Plants type = findPlant(name);
        if (type == null) { System.out.println("Error: Unknown plant: " + name); return; }
        int cost = PLANT_PURCHASE_PRICE;
        if (user.getCoins() < cost) {
            System.out.println("Error: Not enough coins. " + type.getName() + " costs " + cost + " coins.");
            return;
        }
        user.setCoins(user.getCoins() - cost);
        user.setSeedPacket(user.getSeedPacket() + 1);
        user.getNewsList().addNews("New plant unlocked: " + type.getName() + "! Add it to your deck.");
        System.out.println("Bought " + type.getName() + " for " + cost + " coins. It is now unlocked.");
    }

    private Plants findPlant(String input) {
        String normalized = input.trim().replace(' ', '_').replace('-', '_');
        for (Plants p : Plants.values())
            if (p.getName().equalsIgnoreCase(input.trim()) || p.name().equalsIgnoreCase(normalized))
                return p;
        return null;
    }

    private void handleMenu(String[] parts) {
        if (parts.length >= 3 && parts[1].equals("show") && parts[2].equals("current")) {
            System.out.println("Current menu: " + app.getCurrentmenu());
            return;
        }
        if (parts.length >= 3 && parts[1].equals("enter")) {
            String navError = Navigation.enter(app, parts[2]);
            if (navError != null) System.out.println("Error: " + navError);
            return;
        }
        System.out.println("Error: Usage: menu show current  |  menu enter <menu_name>");
    }
}
