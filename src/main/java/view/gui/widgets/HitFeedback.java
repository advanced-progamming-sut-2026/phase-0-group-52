package view.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class HitFeedback {

    private static final float FLASH_TIME = 0.22f;
    private static final float FLASH_STRENGTH = 0.22f;

    private final Map<Actor, Float> hurt = new HashMap<Actor, Float>();

    public void hit(Actor actor) {
        if (actor != null) {
            hurt.put(actor, FLASH_TIME);
        }
    }

    public void bite(Actor plant) {
        hit(plant);
    }

    public boolean isFlashing(Actor actor) {
        return hurt.containsKey(actor);
    }

    public void act(float delta) {
        Iterator<Map.Entry<Actor, Float>> fading = hurt.entrySet().iterator();
        while (fading.hasNext()) {
            Map.Entry<Actor, Float> entry = fading.next();
            float left = entry.getValue() - delta;
            Actor actor = entry.getKey();
            if (left <= 0f || actor.getStage() == null) {
                paint(actor, 1f, 1f, 1f);
                fading.remove();
                continue;
            }
            entry.setValue(left);
            float pulse = (float) Math.abs(Math.sin(left / FLASH_TIME * Math.PI));
            float shade = 1f - pulse * FLASH_STRENGTH;
            paint(actor, shade, shade, shade);
        }
    }

    private static void paint(Actor actor, float r, float g, float b) {
        if (actor instanceof PamActor) {
            ((PamActor) actor).setTint(r, g, b, 1f);
        } else {
            actor.setColor(r, g, b, Color.WHITE.a);
        }
    }
}
