package model.entities.plants;

import model.ChapterType;

import java.util.Collections;
import java.util.List;

public final class PlantRecord {

    public enum UnlockKind { STARTER, CHAPTER, PREMIUM, MINT }

    public static final class Stat {
        private final String label;
        private final String value;

        public Stat(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }
    }

    private final int id;
    private final String name;
    private final String codeName;
    private final PlantsCategory category;
    private final String mintFamily;
    private final List<PlantTag> tags;
    private final String damageText;
    private final String ability;
    private final String plantFoodEffect;
    private final UnlockKind unlockKind;
    private final ChapterType chapter;
    private final int chapterOrder;
    private final int seedPacketPrice;
    private final String originWorld;
    private final String packetIcon;
    private final String packetBackground;
    private final String description;
    private final String plantFoodDescription;
    private final String flavorText;
    private final List<Stat> stats;
    private final List<Stat> details;
    private final List<PlantUpgrade> upgrades;
    private final PlantAnimations animations;

    PlantRecord(PlantRecordBuilder b) {
        this.id = b.id;
        this.name = b.name;
        this.codeName = b.codeName;
        this.category = b.category;
        this.mintFamily = b.mintFamily;
        this.tags = Collections.unmodifiableList(b.tags);
        this.damageText = b.damageText;
        this.ability = b.ability;
        this.plantFoodEffect = b.plantFoodEffect;
        this.unlockKind = b.unlockKind;
        this.chapter = b.chapter;
        this.chapterOrder = b.chapterOrder;
        this.seedPacketPrice = b.seedPacketPrice;
        this.originWorld = b.originWorld;
        this.packetIcon = b.packetIcon;
        this.packetBackground = b.packetBackground;
        this.description = b.description;
        this.plantFoodDescription = b.plantFoodDescription;
        this.flavorText = b.flavorText;
        this.stats = Collections.unmodifiableList(b.stats);
        this.details = Collections.unmodifiableList(b.details);
        this.upgrades = Collections.unmodifiableList(b.upgrades);
        this.animations = b.animations;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCodeName() {
        return codeName;
    }

    public PlantsCategory getCategory() {
        return category;
    }

    public String getMintFamily() {
        return mintFamily;
    }

    public List<PlantTag> getTags() {
        return tags;
    }

    public boolean hasTag(PlantTag tag) {
        return tags.contains(tag);
    }

    public String getDamageText() {
        return damageText;
    }

    public String getAbility() {
        return ability;
    }

    public String getPlantFoodEffect() {
        return plantFoodEffect;
    }

    public UnlockKind getUnlockKind() {
        return unlockKind;
    }

    public ChapterType getChapter() {
        return chapter;
    }

    public int getChapterOrder() {
        return chapterOrder;
    }

    public int getSeedPacketPrice() {
        return seedPacketPrice;
    }

    public String getOriginWorld() {
        return originWorld;
    }

    public String getPacketIcon() {
        return packetIcon;
    }

    public String getPacketBackground() {
        return packetBackground;
    }

    public String getDescription() {
        return description;
    }

    public String getPlantFoodDescription() {
        return plantFoodDescription;
    }

    public String getFlavorText() {
        return flavorText;
    }

    public List<Stat> getStats() {
        return stats;
    }

    public List<Stat> getDetails() {
        return details;
    }

    public List<PlantUpgrade> getUpgrades() {
        return upgrades;
    }

    public PlantAnimations getAnimations() {
        return animations;
    }
}
