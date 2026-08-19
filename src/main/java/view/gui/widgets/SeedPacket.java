package view.gui.widgets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import model.entities.plants.Plants;
import view.gui.Animations;
import view.gui.Theme;
import view.gui.UiKit;

public final class SeedPacket extends Stack {
    private final UiKit ui;
    private final Plants plant;

    private final Table face = new Table();
    private final Table overlay = new Table();

    private Label costLabel;
    private Label levelLabel;
    private Image lockShade;
    private Table boostRing;
    private Table selectedRing;
    private Table progressTrack;
    private Table progressFill;

    private boolean locked;
    private boolean boosted;
    private boolean selected;

    public SeedPacket(UiKit ui, Plants plant) {
        this.ui = ui;
        this.plant = plant;
        build();
    }

    private void build() {
        face.setBackground(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                new TextureRegion(ui.primitives().packetFace(
                        Theme.PACKET_WIDTH, Theme.PACKET_HEIGHT,
                        Theme.plantFamily(plant.getCategory().name()), false))));
        face.pad(Theme.PAD_SMALL);
        face.top();

        face.add(buildPortrait()).size(44f).padTop(7f).row();

        Label name = new Label(shortName(plant.getName()), ui.skin(), "small");
        name.setAlignment(Align.center);
        name.setWrap(true);
        name.setFontScale(0.82f);
        face.add(name).width(Theme.PACKET_WIDTH - 10f).padTop(3f).expandY().top().row();

        costLabel = new Label(String.valueOf(plant.getCost()), ui.skin(), "small");
        costLabel.setAlignment(Align.center);
        face.add(costLabel).padBottom(2f);

        add(face);

        overlay.setFillParent(true);
        add(overlay);

        setSize(Theme.PACKET_WIDTH, Theme.PACKET_HEIGHT);
        Animations.attachPress(this);
    }

    private Image buildPortrait() {
        return ui.token(42, Theme.plantFamily(plant.getCategory().name()));
    }

    private String shortName(String name) {
        if (name == null) {
            return "";
        }
        return (name.length() <= 15) ? name : name.substring(0, 13) + "..";
    }

    public SeedPacket setLocked(boolean value) {
        this.locked = value;
        if (value && lockShade == null) {
            lockShade = new Image(ui.primitives().flat(Theme.alpha(Theme.LOCKED, 0.55f)));
            lockShade.setFillParent(true);
            overlay.addActor(lockShade);
            Label label = new Label("LOCKED", ui.skin(), "smallOnDark");
            label.setAlignment(Align.center);
            Table holder = new Table();
            holder.setFillParent(true);
            holder.center();
            holder.add(label);
            overlay.addActor(holder);
        } else if (!value && lockShade != null) {
            overlay.clearChildren();
            lockShade = null;
        }
        return this;
    }

    public SeedPacket setBoosted(boolean value) {
        this.boosted = value;
        if (value && boostRing == null) {
            boostRing = new Table();
            boostRing.setFillParent(true);
            boostRing.setBackground(ui.primitives().rounded(8, new com.badlogic.gdx.graphics.Color(0, 0, 0, 0),
                    Theme.BOOSTED, 3));
            overlay.addActor(boostRing);
        } else if (!value && boostRing != null) {
            boostRing.remove();
            boostRing = null;
        }
        return this;
    }

    public SeedPacket setSelected(boolean value) {
        this.selected = value;
        if (value && selectedRing == null) {
            selectedRing = new Table();
            selectedRing.setFillParent(true);
            selectedRing.setBackground(ui.primitives().rounded(8,
                    new com.badlogic.gdx.graphics.Color(0, 0, 0, 0), Theme.SELECTED, 4));
            overlay.addActor(selectedRing);
        } else if (!value && selectedRing != null) {
            selectedRing.remove();
            selectedRing = null;
        }
        return this;
    }

    public SeedPacket setLevel(int level) {
        if (levelLabel == null) {
            levelLabel = new Label("", ui.skin(), "small");
            Table holder = new Table();
            holder.setFillParent(true);
            holder.top().left();
            holder.add(levelLabel).pad(3f);
            overlay.addActor(holder);
        }
        levelLabel.setText("L" + level);
        return this;
    }

    public SeedPacket setProgress(int collected, int needed) {
        float ratio = (needed <= 0) ? 0f : Math.min(1f, collected / (float) needed);
        float width = (Theme.PACKET_WIDTH - 6) * Math.max(0.02f, ratio);
        if (progressFill == null) {
            progressTrack = new Table();
            progressTrack.setFillParent(true);
            progressTrack.bottom().left();

            progressFill = new Table();
            progressFill.setBackground(ui.primitives().flat(Theme.GREEN));
            progressTrack.add(progressFill).height(4f).width(width);
            overlay.addActor(progressTrack);
        } else {
            progressTrack.getCell(progressFill).width(width);
            progressTrack.invalidateHierarchy();
        }
        return this;
    }

    public SeedPacket setCooldown(float secondsRemaining) {
        if (costLabel == null) {
            return this;
        }
        if (secondsRemaining > 0f) {
            costLabel.setText(String.format("%.1fs", secondsRemaining));
            costLabel.setColor(Theme.TEXT_MUTED);
        } else {
            costLabel.setText(String.valueOf(plant.getCost()));
            costLabel.setColor(Theme.TEXT);
        }
        return this;
    }

    public SeedPacket setAffordable(boolean value) {
        face.getColor().a = value ? 1f : 0.55f;
        return this;
    }

    public SeedPacket onClick(Runnable action) {
        UiKit.onClick(this, action);
        return this;
    }

    public Plants getPlant() {
        return plant;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isBoosted() {
        return boosted;
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    public float getPrefWidth() {
        return Theme.PACKET_WIDTH;
    }

    @Override
    public float getPrefHeight() {
        return Theme.PACKET_HEIGHT;
    }
}
