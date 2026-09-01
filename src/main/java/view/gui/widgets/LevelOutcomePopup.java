package view.gui.widgets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import controller.menu.LevelController;
import view.gui.ChapterArt;
import view.gui.GameContext;
import view.gui.Popup;
import view.gui.Theme;

public final class LevelOutcomePopup extends Popup {

    private static final float ART = 190f;
    private static final float HEADLINE = 1.6f;
    private static final String LOSE_PAM =
            "768/FULL/EFFECTS/ZOMBIE_BIGHEAD_SHOCK/ZOMBIE_BIGHEAD_SHOCK.PAM";

    private final GameContext context;
    private final LevelController controller;

    public LevelOutcomePopup(GameContext context, LevelController controller,
            boolean won, String message) {
        super(context.ui(), won ? "Level Complete!" : "The Zombies Ate Your Brains!", 760f, 620f);
        this.context = context;
        this.controller = controller;

        body().add(banner(won)).size(ART, ART).center().padBottom(Theme.PAD).row();

        Label headline = new Label(won
                ? "Ancient Egypt bows to your garden."
                : "They got through. Try a different line-up.",
                ui.skin(), won ? "titleOnDark" : "onDark");
        headline.setAlignment(Align.center);
        headline.setFontScale(HEADLINE);
        headline.setColor(won ? Theme.SUN : Theme.RED_LIGHT);
        body().add(headline).growX().center().padBottom(Theme.PAD_SMALL).row();

        Label text = new Label(message == null ? "" : message, ui.skin(), "muted");
        text.setWrap(true);
        text.setAlignment(Align.center);
        body().add(text).growX().center().pad(Theme.PAD).row();

        Table row = new Table();
        row.add(ui.faceButton("Exit to the map", "primary", new Runnable() {
            @Override
            public void run() {
                LevelOutcomePopup.this.controller.leave();
                close();
            }
        })).pad(Theme.PAD_SMALL);
        footer().add(row).center();
        sealClose();
    }

    private void sealClose() {
        for (com.badlogic.gdx.scenes.scene2d.Actor actor : getChildren()) {
            hideCloseButton(actor);
        }
    }

    private void hideCloseButton(com.badlogic.gdx.scenes.scene2d.Actor actor) {
        if (actor instanceof Table) {
            for (com.badlogic.gdx.scenes.scene2d.Actor child
                    : ((Table) actor).getChildren()) {
                if (Theme.RED.equals(child.getColor())) {
                    child.setVisible(false);
                    child.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
                }
                hideCloseButton(child);
            }
        }
    }

    private Table banner(boolean won) {
        Table holder = new Table();
        if (context.assets() == null) {
            return holder;
        }
        if (won) {
            TextureRegion trophy = context.assets().region("IMAGE_ENDLEVEL_"
                    + ChapterArt.world(controller.chapter()) + "_TROPHY");
            if (trophy == null) {
                trophy = context.assets().region("IMAGE_ENDLEVEL_EGYPT_TROPHY");
            }
            if (trophy != null) {
                Image art = new Image(new TextureRegionDrawable(trophy));
                art.setScaling(Scaling.fit);
                holder.add(art).grow();
            }
            return holder;
        }
        PamActor shock = PlantStage.anchored(context.assets(), LOSE_PAM, "animation", 390f, 390f);
        if (shock.isReady()) {
            shock.play("animation", true, null);
            holder.add(shock).grow();
        }
        return holder;
    }
}
