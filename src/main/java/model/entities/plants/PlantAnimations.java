package model.entities.plants;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class PlantAnimations {

    private final String plant;
    private final int canvasWidth;
    private final int canvasHeight;
    private final Map<String, Double> clips;
    private final Map<String, String> effects;

    public PlantAnimations(String plant, int canvasWidth, int canvasHeight,
            Map<String, Double> clips, Map<String, String> effects) {
        this.plant = plant;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.clips = Collections.unmodifiableMap(clips);
        this.effects = Collections.unmodifiableMap(effects);
    }

    public String getPlant() {
        return plant;
    }

    public boolean hasPlant() {
        return plant != null && !plant.isEmpty();
    }

    public int getCanvasWidth() {
        return canvasWidth;
    }

    public int getCanvasHeight() {
        return canvasHeight;
    }

    public Map<String, Double> getClips() {
        return clips;
    }

    public boolean hasClip(String clip) {
        return clips.containsKey(clip);
    }

    public double clipDuration(String clip) {
        Double value = clips.get(clip);
        return value == null ? 0.0 : value;
    }

    public Map<String, String> getEffects() {
        return effects;
    }

    public String effect(String key) {
        return effects.get(key);
    }

    public List<String> allPaths() {
        java.util.ArrayList<String> paths = new java.util.ArrayList<String>();
        if (hasPlant()) {
            paths.add(plant);
        }
        paths.addAll(effects.values());
        return paths;
    }
}
