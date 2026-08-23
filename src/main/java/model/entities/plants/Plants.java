package model.entities.plants;

import java.util.ArrayList;
import model.entities.plants.PlantTag;
import model.entities.plants.PlantsCategory;

import java.util.Arrays;

public enum Plants {

    SUNFLOWER("Sunflower", PlantsCategory.SUN_PRODUCER,
            new ArrayList<>(Arrays.asList(PlantTag.DAY)), 50, 300, 0, 24, 5),
    TWIN_SUNFLOWER("Twin Sunflower", PlantsCategory.SUN_PRODUCER,
            new ArrayList<>(Arrays.asList(PlantTag.DAY)), 125, 300, 0, 24, 15),
    SUN_SHROOM("Sun-shroom", PlantsCategory.SUN_PRODUCER,
            new ArrayList<>(Arrays.asList(PlantTag.SHROOM, PlantTag.RAMP_UP, PlantTag.NIGHT)), 25, 300, 0, 24, 5),
    PRIMAL_SUNFLOWER("Primal Sunflower", PlantsCategory.SUN_PRODUCER,
            new ArrayList<>(), 75, 300, 0, 24, 5),
    GOLD_BLOOM("Gold Bloom", PlantsCategory.SUN_PRODUCER,
            new ArrayList<>(), 0, 0, 0, 0, 75),

    PEASHOOTER("Peashooter", PlantsCategory.SHOOTER,
            new ArrayList<>(Arrays.asList(PlantTag.PEA)), 100, 300, 20, 1.5, 5),
    REPEATER("Repeater", PlantsCategory.SHOOTER,
            new ArrayList<>(Arrays.asList(PlantTag.PEA)), 200, 300, 20, 1.5, 5),
    THREEPEATER("Threepeater", PlantsCategory.SHOOTER,
            new ArrayList<>(Arrays.asList(PlantTag.PEA)), 300, 300, 20, 1.5, 5),
    SNOW_PEA("Snow Pea", PlantsCategory.SHOOTER,
            new ArrayList<>(Arrays.asList(PlantTag.ICE, PlantTag.PEA)), 150, 300, 20, 1.5, 5),
    ROTOBAGA("Rotobaga", PlantsCategory.SHOOTER,
            new ArrayList<>(), 150, 300, 10, 1.5, 5),
    PEA_POD("Pea Pod", PlantsCategory.SHOOTER,
            new ArrayList<>(Arrays.asList(PlantTag.PEA, PlantTag.STACK)), 125, 300, 20, 1.5, 5),
    SPLIT_PEA("Split Pea", PlantsCategory.SHOOTER,
            new ArrayList<>(Arrays.asList(PlantTag.PEA)), 125, 300, 20, 1.5, 5),
    CITRON("Citron", PlantsCategory.SHOOTER,
            new ArrayList<>(Arrays.asList(PlantTag.CHARGE)), 350, 300, 800, 9, 5),
    BOWLING_BULB("Bowling Bulb", PlantsCategory.SHOOTER,
            new ArrayList<>(Arrays.asList(PlantTag.CHARGE)), 200, 300, 40, 2, 5),
    FIRE_PEASHOOTER("Fire Peashooter", PlantsCategory.SHOOTER,
            new ArrayList<>(Arrays.asList(PlantTag.FIRE, PlantTag.PEA)), 175, 300, 40, 1.5, 5),
    STARFRUIT("Starfruit", PlantsCategory.SHOOTER,
            new ArrayList<>(), 150, 300, 20, 1.5, 5),
    GOO_PEASHOOTER("Goo Peashooter", PlantsCategory.SHOOTER,
            new ArrayList<>(Arrays.asList(PlantTag.POISON)), 125, 300, 20, 1.5, 5),
    MEGA_GATLING_PEA("Mega Gatling Pea", PlantsCategory.SHOOTER,
            new ArrayList<>(Arrays.asList(PlantTag.PEA)), 400, 300, 20, 1.5, 5),
    SEA_SHROOM("Sea-shroom", PlantsCategory.SHOOTER,
            new ArrayList<>(Arrays.asList(PlantTag.SHROOM, PlantTag.WATER)), 0, 300, 20, 1.5, 15),
    PUFF_SHROOM("Puff-shroom", PlantsCategory.SHOOTER,
            new ArrayList<>(Arrays.asList(PlantTag.SHROOM)), 0, 300, 20, 1.5, 5),

    CAULIPOWER("Caulipower", PlantsCategory.MAGIC,
            new ArrayList<>(Arrays.asList(PlantTag.MAGIC, PlantTag.CHARGE)), 250, 300, 9999, 12, 15),
    ELECTRIC_BLUEBERRY("Electric Blueberry", PlantsCategory.MAGIC,
            new ArrayList<>(Arrays.asList(PlantTag.CHARGE)), 150, 300, 5000, 12, 15),
    MAGNET_SHROOM("Magnet-shroom", PlantsCategory.MAGIC,
            new ArrayList<>(Arrays.asList(PlantTag.SHROOM, PlantTag.MAGIC)), 100, 300, 0, 10, 15),
    SNAPDRAGON("Snapdragon", PlantsCategory.STRIKE_THROUGH,
            new ArrayList<>(Arrays.asList(PlantTag.FIRE, PlantTag.AOE)), 150, 300, 30, 2, 5),

    CACTUS("Cactus", PlantsCategory.STRIKE_THROUGH,
            new ArrayList<>(), 175, 300, 30, 1.5, 5),
    FUME_SHROOM("Fume-shroom", PlantsCategory.STRIKE_THROUGH,
            new ArrayList<>(Arrays.asList(PlantTag.SHROOM)), 125, 300, 20, 1.5, 5),

    LIGHTNING_REED("Lightning Reed", PlantsCategory.STRIKE_THROUGH,
            new ArrayList<>(Arrays.asList(PlantTag.AOE)), 125, 300, 10, 1, 5),
    CABBAGE_PULT("Cabbage-pult", PlantsCategory.LOBBER,
            new ArrayList<>(), 100, 300, 40, 2.9, 5),
    KERNEL_PULT("Kernel-pult", PlantsCategory.LOBBER,
            new ArrayList<>(), 100, 300, 20, 2.9, 5),
    MELON_PULT("Melon-pult", PlantsCategory.LOBBER,
            new ArrayList<>(Arrays.asList(PlantTag.AOE)), 325, 300, 80, 2.9, 5),
    WINTER_MELON("Winter Melon", PlantsCategory.LOBBER,
            new ArrayList<>(Arrays.asList(PlantTag.ICE, PlantTag.AOE)), 500, 300, 80, 2.9, 5),
    PEPPER_PULT("Pepper-pult", PlantsCategory.LOBBER,
            new ArrayList<>(Arrays.asList(PlantTag.FIRE, PlantTag.AOE)), 200, 300, 50, 2.9, 5),

    POTATO_MINE("Potato Mine", PlantsCategory.EXPLOSIVE,
            new ArrayList<>(Arrays.asList(PlantTag.TRAP, PlantTag.CHARGE)), 25, 300, 1800, 0, 25),
    PRIMAL_POTATO_MINE("Primal Potato Mine", PlantsCategory.EXPLOSIVE,
            new ArrayList<>(Arrays.asList(PlantTag.TRAP, PlantTag.CHARGE)), 50, 300, 2400, 0, 5),
    CHERRY_BOMB("Cherry Bomb", PlantsCategory.EXPLOSIVE,
            new ArrayList<>(), 150, 0, 1800, 0, 35),
    SQUASH("Squash", PlantsCategory.EXPLOSIVE,
            new ArrayList<>(Arrays.asList(PlantTag.TRAP)), 50, 300, 1800, 0, 20),
    GRAPESHOT("Grapeshot", PlantsCategory.EXPLOSIVE,
            new ArrayList<>(), 150, 0, 1800, 0, 35),
    JALAPENO("Jalapeno", PlantsCategory.EXPLOSIVE,
            new ArrayList<>(Arrays.asList(PlantTag.FIRE)), 125, 0, 1800, 0, 35),
    DOOM_SHROOM("Doom-shroom", PlantsCategory.EXPLOSIVE,
            new ArrayList<>(Arrays.asList(PlantTag.SHROOM)), 125, 0, 1800, 0, 15),
    TANGLE_KELP("Tangle Kelp", PlantsCategory.EXPLOSIVE,
            new ArrayList<>(Arrays.asList(PlantTag.TRAP, PlantTag.WATER)), 25, 300, 9999, 0, 15),
    ICEBERG_LETTUCE("Iceberg Lettuce", PlantsCategory.EXPLOSIVE,
            new ArrayList<>(Arrays.asList(PlantTag.TRAP, PlantTag.ICE)), 0, 300, 0, 0, 20),
    ICE_SHROOM("Ice-shroom", PlantsCategory.EXPLOSIVE,
            new ArrayList<>(Arrays.asList(PlantTag.SHROOM, PlantTag.ICE)), 75, 0, 0, 0, 50),
    HOT_POTATO("Hot Potato", PlantsCategory.EXPLOSIVE,
            new ArrayList<>(Arrays.asList(PlantTag.FIRE)), 0, 0, 0, 0, 5),
    GRAVE_BUSTER("Grave Buster", PlantsCategory.EXPLOSIVE,
            new ArrayList<>(), 0, 0, 9999, 0, 10),

    BONK_CHOY("Bonk Choy", PlantsCategory.MELEE,
            new ArrayList<>(), 150, 300, 15, 0.25, 5),
    PHAT_BEET("Phat Beet", PlantsCategory.MELEE,
            new ArrayList<>(Arrays.asList(PlantTag.AOE)), 150, 300, 15, 2, 5),
    CHOMPER("Chomper", PlantsCategory.MELEE,
            new ArrayList<>(), 150, 300, 9999, 40, 5),
    WASABI_WHIP("Wasabi Whip", PlantsCategory.MELEE,
            new ArrayList<>(Arrays.asList(PlantTag.FIRE)), 150, 300, 40, 2, 5),
    KIWIBEAST("Kiwibeast", PlantsCategory.MELEE,
            new ArrayList<>(Arrays.asList(PlantTag.AOE, PlantTag.RAMP_UP)), 175, 300, 15, 2, 5),

    WALL_NUT("Wall-nut", PlantsCategory.WALL_NUT,
            new ArrayList<>(), 50, 4000, 0, 0, 20),
    TALL_NUT("Tall-nut", PlantsCategory.WALL_NUT,
            new ArrayList<>(), 125, 8000, 0, 0, 20),
    ENDURIAN("Endurian", PlantsCategory.WALL_NUT,
            new ArrayList<>(), 100, 3000, 20, 0, 15),
    GARLIC("Garlic", PlantsCategory.WALL_NUT,
            new ArrayList<>(Arrays.asList(PlantTag.MOVE_ZOMBIES)), 50, 300, 0, 0, 20),
    SWEET_POTATO("Sweet Potato", PlantsCategory.WALL_NUT,
            new ArrayList<>(Arrays.asList(PlantTag.MOVE_ZOMBIES)), 150, 3000, 0, 0, 20),
    EXPLODE_O_NUT("Explode-o-nut", PlantsCategory.WALL_NUT,
            new ArrayList<>(Arrays.asList(PlantTag.EXPLOSIVE)), 50, 4000, 1800, 0, 20),
    PUMPKIN("Pumpkin", PlantsCategory.WALL_NUT,
            new ArrayList<>(Arrays.asList(PlantTag.STACK)), 150, 4000, 0, 0, 20),
    SUN_BEAN("Sun Bean", PlantsCategory.WALL_NUT,
            new ArrayList<>(Arrays.asList(PlantTag.SUN)), 50, 1000, 0, 0, 20),

    TORCHWOOD("Torchwood", PlantsCategory.MAGIC,
            new ArrayList<>(Arrays.asList(PlantTag.FIRE)), 175, 300, 0, 0, 5),
    HYPNO_SHROOM("Hypno-shroom", PlantsCategory.MAGIC,
            new ArrayList<>(Arrays.asList(PlantTag.SHROOM, PlantTag.MAGIC)), 125, 300, 0, 0, 20),
    IMITATER("Imitater", PlantsCategory.MAGIC,
            new ArrayList<>(), 0, 0, 0, 0, 0),
    LILY_PAD("Lily Pad", PlantsCategory.MAGIC,
            new ArrayList<>(Arrays.asList(PlantTag.WATER, PlantTag.STACK)), 25, 300, 0, 0, 5),

    ENLIGHTEN_MINT("Enlighten-mint", PlantsCategory.SUN_PRODUCER, new ArrayList<>(), 0, 0, 0, 0, 85),
    APPEASE_MINT("Appease-mint", PlantsCategory.SHOOTER, new ArrayList<>(), 0, 0, 0, 0, 85),
    ARMA_MINT("Arma-mint", PlantsCategory.LOBBER, new ArrayList<>(), 0, 0, 0, 0, 85),
    BOMBARD_MINT("Bombard-mint", PlantsCategory.EXPLOSIVE, new ArrayList<>(), 0, 0, 0, 0, 85),
    ENFORCE_MINT("Enforce-mint", PlantsCategory.MELEE, new ArrayList<>(), 0, 0, 0, 0, 85),
    REINFORCE_MINT("Reinforce-mint", PlantsCategory.WALL_NUT, new ArrayList<>(), 0, 0, 0, 0, 85),
    ENCHANT_MINT("Enchant-mint", PlantsCategory.MAGIC, new ArrayList<>(), 0, 0, 0, 0, 85),
    PIERCE_MINT("Pierce-mint", PlantsCategory.STRIKE_THROUGH, new ArrayList<>(), 0, 0, 0, 0, 85);

    private final String name;
    private final PlantsCategory category;
    private final ArrayList<PlantTag> tags;
    private final int cost;
    private final int baseHP;
    private final int damage;
    private final double actionInterval;
    private final int recharge;

    Plants(String name, PlantsCategory category, ArrayList<PlantTag> tags,
           int cost, int baseHP, int damage, double actionInterval, int recharge) {
        this.name = name;
        this.category = category;
        this.tags = tags;
        this.cost = cost;
        this.baseHP = baseHP;
        this.damage = damage;
        this.actionInterval = actionInterval;
        this.recharge = recharge;
    }

    public String getName() { return name; }
    public PlantsCategory getCategory() { return category; }
    public ArrayList<PlantTag> getTags() { return tags; }
    public int getCost() { return cost; }
    public int getBaseHP() { return baseHP; }
    public int getDamage() { return damage; }
    public double getActionInterval() { return actionInterval; }
    public int getRecharge() { return recharge; }
}
