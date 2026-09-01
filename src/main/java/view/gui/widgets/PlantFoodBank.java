package view.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import view.gui.Assets;

public final class PlantFoodBank extends Widget {

    private static final float PREF_WIDTH = 104f;
    private static final float PREF_HEIGHT = 44f;

    private static final float DIAL_CENTRE = 0.194f;
    private static final float DIAL_SIZE = 0.29f;
    private static final float SOCKET_FIRST = 0.4248f;
    private static final float SOCKET_STEP = 0.11655f;
    private static final float SOCKET_SIZE = 0.1214f;
    private static final float SOCKET_ROW = 0.5f;
    private static final float SLOT_FILL = 0.95f;
    private static final float LEAF_INSET = 0.12f;
    private static final float LEAF_SIZE = 0.76f;

    private final Assets assets;
    private final int slots;

    private int filled;

    public PlantFoodBank(Assets assets, int slots) {
        this.assets = assets;
        this.slots = Math.max(1, slots);
    }

    public void setFilled(int value) {
        filled = Math.max(0, Math.min(slots, value));
    }

    @Override
    public float getPrefWidth() {
        return PREF_WIDTH;
    }

    @Override
    public float getPrefHeight() {
        return PREF_HEIGHT;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (assets == null) {
            return;
        }
        batch.setColor(1f, 1f, 1f, getColor().a * parentAlpha);
        TextureRegion bank = assets.region("IMAGE_UI_HUD_INGAME_PLANTFOOD_BANK");
        if (bank != null) {
            batch.draw(bank, getX(), getY(), getWidth(), getHeight());
        }
        TextureRegion full = assets.region("IMAGE_UI_HUD_INGAME_PLANTFOOD_BANK_FILLED_SLOT");
        TextureRegion leaf = assets.region("IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON");
        if (leaf != null) {
            float dial = getWidth() * DIAL_SIZE;
            batch.draw(leaf, getX() + getWidth() * DIAL_CENTRE - dial / 2f,
                    getY() + getHeight() * SOCKET_ROW - dial / 2f, dial, dial);
        }
        float size = getWidth() * SOCKET_SIZE * SLOT_FILL;
        for (int i = 0; i < slots; i++) {
            float centre = getX() + getWidth() * (SOCKET_FIRST + SOCKET_STEP * i);
            float x = centre - size / 2f;
            float y = getY() + getHeight() * SOCKET_ROW - size / 2f;
            if (i >= filled) {
                continue;
            }
            if (full != null) {
                batch.draw(full, x, y, size, size);
            } else if (leaf != null) {
                batch.draw(leaf, x + size * LEAF_INSET, y + size * LEAF_INSET,
                        size * LEAF_SIZE, size * LEAF_SIZE);
            }
        }
        batch.setColor(Color.WHITE);
    }
}
