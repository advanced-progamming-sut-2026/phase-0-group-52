package model.entities.zombies;

import java.util.Collections;
import java.util.List;

public final class ZombieRecord {
    private final int id;
    private final String alias;
    private final String enumName;
    private final String name;
    private final String chapter;
    private final String description;
    private final String flavor;
    private final Rating toughness;
    private final Rating speed;
    private final double hp;
    private final double eatDps;
    private final double moveSpeed;
    private final int waveCost;
    private final int weight;
    private final String armor;
    private final boolean spawnable;
    private final String packetBackground;
    private final String packetIcon;
    private final float[] backgroundPlacement;
    private final float[] iconPlacement;
    private final String animationName;
    private final String animationPath;
    private final float[] canvas;
    private final List<String> armorParts;
    private final String bodyPath;
    private final String headPath;
    private final List<String> hideParts;
    private final List<String> clips;

    public ZombieRecord(int id, String alias, String enumName, String name, String chapter,
            String description, String flavor, Rating toughness, Rating speed, double hp, double eatDps,
            double moveSpeed, int waveCost, int weight, String armor, boolean spawnable,
            String packetBackground, String packetIcon, float[] backgroundPlacement,
            float[] iconPlacement, String animationName,
            String animationPath, float[] canvas, List<String> armorParts,
            String bodyPath, String headPath, List<String> hideParts,
            List<String> clips) {
        this.id = id;
        this.alias = alias;
        this.enumName = enumName;
        this.name = name;
        this.chapter = chapter;
        this.description = description;
        this.flavor = flavor;
        this.toughness = toughness;
        this.speed = speed;
        this.hp = hp;
        this.eatDps = eatDps;
        this.moveSpeed = moveSpeed;
        this.waveCost = waveCost;
        this.weight = weight;
        this.armor = armor;
        this.spawnable = spawnable;
        this.packetBackground = packetBackground;
        this.packetIcon = packetIcon;
        this.backgroundPlacement = backgroundPlacement;
        this.iconPlacement = iconPlacement;
        this.animationName = animationName;
        this.animationPath = animationPath;
        this.canvas = canvas;
        this.armorParts = armorParts;
        this.bodyPath = bodyPath;
        this.headPath = headPath;
        this.hideParts = hideParts;
        this.clips = clips;
    }

    public static final class Rating {
        private final int index;
        private final String label;
        private final int steps;

        public Rating(int index, String label, int steps) {
            this.index = index;
            this.label = label;
            this.steps = steps;
        }

        public int getIndex() {
            return index;
        }

        public String getLabel() {
            return label;
        }

        public int getSteps() {
            return steps;
        }

        public float ratio() {
            return steps <= 1 ? 0f : (index + 1) / (float) steps;
        }
    }

    public int getId() {
        return id;
    }

    public String getAlias() {
        return alias;
    }

    public String getEnumName() {
        return enumName;
    }

    public String getName() {
        return name;
    }

    public String getChapter() {
        return chapter;
    }

    public String getDescription() {
        return description;
    }

    public String getFlavor() {
        return flavor == null ? "" : flavor;
    }

    public Rating getToughness() {
        return toughness;
    }

    public Rating getSpeed() {
        return speed;
    }

    public double getHp() {
        return hp;
    }

    public double getEatDps() {
        return eatDps;
    }

    public double getMoveSpeed() {
        return moveSpeed;
    }

    public int getWaveCost() {
        return waveCost;
    }

    public int getWeight() {
        return weight;
    }

    public String getArmor() {
        return armor;
    }

    public boolean isSpawnable() {
        return spawnable;
    }

    public String getPacketBackground() {
        return packetBackground;
    }

    public String getPacketIcon() {
        return packetIcon;
    }

    public float[] getBackgroundPlacement() {
        return backgroundPlacement;
    }

    public float[] getIconPlacement() {
        return iconPlacement;
    }

    public String getAnimationName() {
        return animationName;
    }

    public String getAnimationPath() {
        return animationPath;
    }

    public float getCanvasWidth() {
        return canvas == null || canvas.length < 2 || canvas[0] <= 0f ? 0f : canvas[0];
    }

    public float getCanvasHeight() {
        return canvas == null || canvas.length < 2 || canvas[1] <= 0f ? 0f : canvas[1];
    }

    public List<String> getArmorParts() {
        return armorParts == null ? Collections.<String>emptyList() : armorParts;
    }

    public String getBodyPath() {
        return bodyPath;
    }

    public String getHeadPath() {
        return headPath;
    }

    public List<String> getHideParts() {
        return hideParts == null ? Collections.<String>emptyList() : hideParts;
    }

    public boolean isComposite() {
        return bodyPath != null && !bodyPath.isEmpty()
                && headPath != null && !headPath.isEmpty();
    }

    public boolean hasAnimation() {
        return animationPath != null && !animationPath.isEmpty();
    }

    public List<String> getClips() {
        return clips == null ? Collections.<String>emptyList() : clips;
    }
}
