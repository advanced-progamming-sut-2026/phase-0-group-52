package pvz.controller.menu;

import pvz.model.App;
import pvz.model.User;
import pvz.model.entities.plants.PlantData;
import pvz.model.entities.plants.Plants;
import pvz.model.greenhouse.Greenhouse;
import pvz.model.shop.Shop;
import pvz.view.MenuType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GreenhouseMenuController {

    private static final Pattern PLANT_POT =
            Pattern.compile("^plant\\s+pot\\s+at\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$");
    private static final Pattern COLLECT =
            Pattern.compile("^collect\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$");
    private static final Pattern GROW =
            Pattern.compile("^grow\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)$");
    private static final Pattern UPGRADE_PLANT =
            Pattern.compile("^upgrade\\s+plant\\s+-t\\s+(.+)$");
    private static final Pattern BUY =
            Pattern.compile("^shop\\s+buy\\s+-i\\s+(\\d+)\\s+-n\\s+(\\d+)(?:\\s+-t\\s+(.+))?$");

    private final App app;

    public GreenhouseMenuController(App app) {
        this.app = app;
    }

    public void handleCommand(String line) {
        String command = line.trim();
        String[] parts = command.split("\\s+");
        if (parts[0].equals("menu")) {
            handleMenu(parts);
            return;
        }
        User user = app.getCurrentuser();
        if (user == null) {
            System.out.println("Error: No user is logged in.");
            return;
        }
        Greenhouse greenhouse = app.getGreenhouse();
        Shop shop = app.getShop();
        Matcher m;
        if (command.equals("show greenhouse")) {
            greenhouse.print();
        } else if ((m = PLANT_POT.matcher(command)).matches()) {
            System.out.println(greenhouse.plantPot(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))));
        } else if ((m = COLLECT.matcher(command)).matches()) {
            System.out.println(greenhouse.collect(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), user));
        } else if ((m = GROW.matcher(command)).matches()) {
            System.out.println(greenhouse.grow(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), user));
        } else if (command.equals("enter shop")) {
            System.out.println("Entered the shop. Coins: " + user.getCoins() + " | Diamonds: " + user.getGems());
            shop.printList();
            shop.printDaily();
        } else if (command.equals("shop list")) {
            shop.printList();
        } else if (command.equals("shop daily")) {
            shop.printDaily();
        } else if ((m = UPGRADE_PLANT.matcher(command)).matches()) {
            Plants type = findPlant(m.group(1));
            if (type == null) {
                System.out.println("Error: Unknown plant: " + m.group(1));
                return;
            }
            System.out.println(PlantData.upgrade(user, type));
        } else if (command.equals("show plant levels")) {
            boolean any = false;
            for (Plants type : Plants.values()) {
                int level = user.getPlantLevel(type);
                if (level > 1) {
                    System.out.println("  " + type.getName() + " | level " + level);
                    any = true;
                }
            }
            if (!any) System.out.println("All plants are at level 1.");
        } else if ((m = BUY.matcher(command)).matches()) {
            Plants type = null;
            if (m.group(3) != null) {
                type = findPlant(m.group(3));
                if (type == null) {
                    System.out.println("Error: Unknown plant: " + m.group(3));
                    return;
                }
            }
            System.out.println(shop.buy(user, greenhouse,
                    Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), type));
        } else {
            System.out.println("invalid command");
        }
    }

    private void handleMenu(String[] parts) {
        if (parts.length >= 3 && parts[1].equals("show") && parts[2].equals("current")) {
            System.out.println("Current menu: " + app.getCurrentmenu());
            return;
        }
        if (parts.length >= 3 && parts[1].equals("enter")) {
            MenuType target = MenuType.fromName(parts[2]);
            if (target == null) {
                System.out.println("Error: Unknown menu: " + parts[2]);
                return;
            }
            app.setCurrentmenu(target);
            if (target.toMenu() != null) app.setCurrentMenu(target.toMenu());
            return;
        }
        System.out.println("Error: Usage: menu show current  |  menu enter <menu_name>");
    }

    private Plants findPlant(String input) {
        String normalized = input.trim().replace(' ', '_').replace('-', '_');
        for (Plants p : Plants.values()) {
            if (p.getName().equalsIgnoreCase(input.trim()) || p.name().equalsIgnoreCase(normalized))
                return p;
        }
        return null;
    }
}
