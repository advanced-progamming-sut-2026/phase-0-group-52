package model.entities.zombies;

public enum Zombies {
    //              name                  hp     eatDPS  speed  waveCost weight  armor
    // ---------- Shared / Ancient Egypt ----------
    ZOMBIE_DEFAULT     ("ZombieDefault",     190,  100,   0.185,  100,   1000, ArmorType.DEFAULT),
    ZOMBIE_ARMOR1      ("ZombieArmor1",      190,  100,   0.185,  200,   3000, ArmorType.CONEHEAD),   // Conehead (armor 370)
    ZOMBIE_ARMOR2      ("ZombieArmor2",      190,  100,   0.185,  400,   4000, ArmorType.BUCKETHEAD), // Buckethead (armor 1100)
    ZOMBIE_ARMOR4      ("ZombieArmor4",      190,  100,   0.185,  700,   3000, ArmorType.BRICKHEAD),  // Brickhead (armor 2200)
    ZOMBIE_GARGANTUAR  ("ZombieGargantuar", 3600,    0,   0.24,  1500,   3000, ArmorType.DEFAULT),    // SmashDamage 1500
    ZOMBIE_IMP         ("ZombieImp",         190,  100,   0.22,   100,   1000, ArmorType.DEFAULT),
    ZOMBIE_RA          ("ZombieRa",          190,  100,   0.2,    100,    700, ArmorType.DEFAULT),    // MaxClaimedSunCurrency 250
    ZOMBIE_EXPLORER    ("ZombieExplorer",    250,  100,   0.25,   250,   3000, ArmorType.DEFAULT),    // MaxTorchReach 37
    ZOMBIE_TOMB_RAISER ("ZombieTombRaiser",  380,  100,   0.185,  300,   2000, ArmorType.DEFAULT),

    // ---------- Frostbite Caves ----------
    ZOMBIE_ICE_AGE_DODO      ("ZombieIceAgeDodo",      490, 100, 0.3,  600, 3500, ArmorType.DEFAULT),
    ZOMBIE_ICE_AGE_HUNTER    ("ZombieIceAgeHunter",    700, 100, 0.12, 500, 3500, ArmorType.DEFAULT),
    ZOMBIE_ICE_AGE_TROGLOBITE("ZombieIceAgeTroglobite",470, 100, 0.185,600, 3500, ArmorType.DEFAULT),

    // ---------- Big Wave Beach ----------
    ZOMBIE_BEACH_FISHERMAN("ZombieBeachFisherman",1000, 100, 0.185, 700, 2500, ArmorType.DEFAULT),
    ZOMBIE_BEACH_OCTOPUS  ("ZombieBeachOctopus",   910, 100, 0.12,  900, 3500, ArmorType.DEFAULT),
    ZOMBIE_BEACH_SNORKEL  ("ZombieBeachSnorkel",   350, 100, 0.185, 200, 3000, ArmorType.DEFAULT),

    // ---------- Dark Ages ----------
    ZOMBIE_DARK_ARMOR3   ("ZombieDarkArmor3",  190, 100, 0.185, 550, 4500, ArmorType.KNIGHT), // Shoulder+Crown (1600)
    ZOMBIE_DARK_JUGGLER  ("ZombieDarkJuggler", 420, 100, 0.2,   450, 3500, ArmorType.DEFAULT),
    ZOMBIE_WIZARD        ("ZombieWizard",      490, 100, 0.12,  800, 3500, ArmorType.DEFAULT),
    ZOMBIE_DARK_KING     ("ZombieDarkKing",   1000, 100, 0.185, 750, 2000, ArmorType.DEFAULT),
    ZOMBIE_DARK_IMP_DRAGON("ZombieDarkImpDragon",190,100,0.185, 150, 2000, ArmorType.DEFAULT),

    // ---------- Other worlds (data file includes these) ----------
    ZOMBIE_MODERN_ALL_STAR("ZombieModernAllStar",1100, 100, 0.16,  1000, 3500, ArmorType.DEFAULT),
    ZOMBIE_LOST_CITY_JANE ("ZombieLostCityJane",  350, 100, 0.25,   200, 3000, ArmorType.DEFAULT),
    ZOMBIE_CRYSTAL_SKULL  ("ZombieCrystalSkull",  250, 100, 0.185,  500, 3000, ArmorType.DEFAULT),
    ZOMBIE_PROSPECTOR     ("ZombieProspector",    190, 100, 0.16,   200, 3000, ArmorType.DEFAULT),
    ZOMBIE_PIANO          ("ZombiePiano",         840,4000, 0.12,   450, 2000, ArmorType.DEFAULT),
    ZOMBIE_NEWSPAPER      ("ZombieNewspaper",     460, 200, 0.22,   700, 4000, ArmorType.DEFAULT), // Newspaper armor (800)
    ZOMBIE_ARCADE         ("ZombieArcade",        490, 100, 0.19,   600, 1000, ArmorType.DEFAULT);

    private final String name;
    private final double hp;
    private final double eatDPS;
    private final double speed;
    private final int waveCost;
    private final int weight;
    private final ArmorType armor;

    Zombies(String name, double hp, double eatDPS, double speed, int waveCost, int weight, ArmorType armor) {
        this.name = name;
        this.hp = hp;
        this.eatDPS = eatDPS;
        this.speed = speed;
        this.waveCost = waveCost;
        this.weight = weight;
        this.armor = armor;
    }

    public String getName() { return name; }
    public double getHp() { return hp; }
    public double getEatDPS() { return eatDPS; }
    public double getSpeed() { return speed; }
    public int getWaveCost() { return waveCost; }
    public int getWeight() { return weight; }
    public ArmorType getArmor() { return armor; }
}
