package view.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import model.quest.QuestCategory;
import model.quest.QuestPriorities;
import model.quest.QuestProgress;
import model.quest.QuestText;
import view.gui.Assets;
import view.gui.Theme;
import view.gui.UiKit;

public final class QuestCard extends Table {

    public interface Actionable {
        boolean isActionable();
    }

    private static final float BAR_HEIGHT = 57f;
    private static final float ICON = 42f;
    private static final float PEPPER_X = -64f;
    private static final float PEPPER_Y = -30.9f;
    private static final float PEPPER_W = 104f;
    private static final float PEPPER_H = 176f;
    private static final float CHILLI_WIDTH = 38f;
    private static final float CHILLI_HEIGHT = 50f;
    private static final float REWARD_ICON = 46f;
    private static final float TRACK_HEIGHT = 14f;
    private static final int COIN_PLURAL_FROM = 200;
    private static final int GEM_PLURAL_FROM = 50;

    private static final Color DIM = new Color(0.55f, 0.55f, 0.58f, 1f);

    private final UiKit ui;
    private final Assets pam;
    private final QuestProgress quest;
    private PamActor meter;
    private boolean flourishing;
    private Actionable actionable;
    private com.badlogic.gdx.scenes.scene2d.Action hoverAction;

    public QuestCard(UiKit ui, Assets pam, QuestProgress quest, Runnable onClaim) {
        this.ui = ui;
        this.pam = pam;
        this.quest = quest;

        setTransform(true);
        Drawable face = ui.ninePatchFile(panelPath(), (int) BAR_HEIGHT, 12, 14, 14);
        if (face != null) {
            setBackground(face);
        } else {
            setBackground(ui.primitives().rounded(Theme.RADIUS, Theme.PANEL,
                    Theme.OUTLINE, Theme.BORDER));
        }

        pad(0f, Theme.PAD_SMALL, Theme.PAD_SMALL, Theme.PAD_SMALL);
        add(headerRow()).growX().height(BAR_HEIGHT).row();
        add(body()).grow().padLeft(Theme.PAD).padRight(Theme.PAD).padTop(2f);

        addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y,
                    int pointer, com.badlogic.gdx.scenes.scene2d.Actor from) {
                if (pointer != -1) {
                    return;
                }
                flourish();
                if (actionable != null && actionable.isActionable()) {
                    hoverTo(1.04f);
                }
            }

            @Override
            public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y,
                    int pointer, com.badlogic.gdx.scenes.scene2d.Actor to) {
                if (pointer == -1) {
                    hoverTo(1f);
                }
            }
        });

        if (claimable()) {
            attract();
            UiKit.onClick(this, onClaim);
        } else if (quest.isClaimed()) {
            setColor(DIM);
        }
    }

    public void setActionable(Actionable test) {
        this.actionable = test;
    }

    private void hoverTo(float target) {
        setOrigin(Align.center);
        if (hoverAction != null) {
            removeAction(hoverAction);
        }
        hoverAction = Actions.scaleTo(target, target, 0.12f, Interpolation.pow2Out);
        addAction(hoverAction);
    }

    public boolean claimable() {
        return quest.isCompleted() && !quest.isClaimed();
    }

    private String panelPath() {
        return "assets/ui/quest_panel_" + quest.getDef().getCategory().name().toLowerCase() + ".png";
    }

    private void attract() {
        setOrigin(Align.center);
        addAction(Actions.forever(Actions.sequence(
                Actions.color(Theme.SUN, 0.45f, Interpolation.sine),
                Actions.color(Color.WHITE, 0.45f, Interpolation.sine))));
        addAction(Actions.forever(Actions.sequence(
                Actions.rotateBy(1.1f, 0.09f), Actions.rotateBy(-2.2f, 0.18f),
                Actions.rotateBy(1.1f, 0.09f), Actions.delay(1.1f))));
    }

    private Table headerRow() {
        Table row = new Table();
        row.left();
        row.padLeft(Theme.PAD).padRight(Theme.PAD);

        TextureRegion icon = pam == null ? null
                : pam.region("IMAGE_UI_QUESTS_QUESTICONS_" + quest.getDef().getIconName());
        if (icon != null) {
            Image art = new Image(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(icon));
            art.setScaling(Scaling.fit);
            row.add(art).size(ICON).padRight(Theme.PAD_SMALL);
        }

        Label name = new Label(quest.getDef().getDisplayName(), ui.skin(), "titleOnDark");
        name.setEllipsis(true);
        row.add(name).growX().minWidth(0f).left().padTop(UiKit.opticalPad(name));

        row.add(chilli()).size(CHILLI_WIDTH, CHILLI_HEIGHT);
        return row;
    }

    private Table chilli() {
        Table holder = new Table();
        meter = new PamActor(pam, Assets.DIFFICULTY_METER, chilliClip())
                .setFit(true)
                .setExtent(PEPPER_X, PEPPER_Y, PEPPER_W, PEPPER_H)
                .setClipped(true)
                .freeze();
        if (!meter.isReady()) {
            return holder;
        }
        holder.add(meter).grow();
        return holder;
    }

    private String chilliClip() {
        switch (quest.getDef().getPriority()) {
            case CRITICAL: return "animation5";
            case HIGH:     return "animation4";
            case MEDIUM:   return "animation3";
            default:       return "animation";
        }
    }

    public void flourish() {
        if (meter == null || !meter.isReady() || flourishing) {
            return;
        }
        flourishing = true;
        meter.setClipped(false);
        meter.play(chilliClip(), false, new Runnable() {
            @Override
            public void run() {
                flourishing = false;
                meter.setClipped(true);
                meter.freeze();
            }
        });
    }

    private Table body() {
        Table body = new Table();
        body.top().left();
        body.add(headline()).left().padTop(Theme.PAD_SMALL).row();

        Label desc = new Label(QuestText.describe(quest), ui.skin(), "default");
        desc.setWrap(true);
        body.add(desc).growX().minWidth(0f).left().padTop(2f).row();

        body.add(new Table()).grow().row();

        body.add(rewardRow()).left().padTop(Theme.PAD_SMALL).row();

        if (quest.getTarget() > 1) {
            body.add(progressRow()).growX().left().padTop(2f).padBottom(2f);
        }
        return body;
    }

    private Table headline() {
        Table line = new Table();
        line.left();
        line.add(priorityTag()).padRight(5f);
        line.add(tag(QuestText.pretty(quest.getDef().getCategory().name()), categoryColour()))
                .padRight(5f);
        line.add(new Label("Quest", ui.skin(), "default"));
        return line;
    }

    private Table priorityTag() {
        String text = quest.getDef().getPriority().name();
        Table holder = new Table();
        if (quest.getDef().getPriority() != QuestPriorities.CRITICAL) {
            holder.add(tag(text, priorityColour()));
            return holder;
        }
        Stack stack = new Stack();
        Table glowLayer = new Table();
        glowLayer.add(tag(text, Theme.PRIORITY_CRITICAL_GLOW)).padLeft(2f).padTop(2f);
        stack.add(glowLayer);
        Table front = new Table();
        front.add(tag(text, Theme.PRIORITY_CRITICAL));
        stack.add(front);
        holder.add(stack);
        return holder;
    }

    private Label tag(String text, Color colour) {
        Label.LabelStyle base = ui.skin().get("default", Label.LabelStyle.class);
        return new Label(text, new Label.LabelStyle(base.font, colour));
    }

    private Color priorityColour() {
        switch (quest.getDef().getPriority()) {
            case CRITICAL: return Theme.PRIORITY_CRITICAL;
            case HIGH:     return Theme.PRIORITY_HIGH;
            case MEDIUM:   return Theme.PRIORITY_MEDIUM;
            default:       return Theme.PRIORITY_LOW;
        }
    }

    private Color categoryColour() {
        QuestCategory category = quest.getDef().getCategory();
        if (category == QuestCategory.EPIC) {
            return Theme.QUEST_EPIC;
        }
        if (category == QuestCategory.MAIN) {
            return Theme.QUEST_MAIN;
        }
        return Theme.QUEST_DAILY;
    }

    private Table rewardRow() {
        Table row = new Table();
        row.left();
        Label caption = new Label("Reward:", ui.skin(), "muted");
        row.add(caption).padRight(Theme.PAD_SMALL).padTop(UiKit.opticalPad(caption));

        int amount = new model.quest.RewardService().amountFor(quest);
        String region = rewardRegion(amount);
        if (quest.getDef().getRewardType() != model.quest.RewardType.PLANT_UNLOCK) {
            Label value = new Label(String.valueOf(amount), ui.skin(), "rowValue");
            row.add(value).padRight(4f).padTop(UiKit.opticalPad(value));
        }
        if (region == null) {
            Drawable single = ui.drawable(singleIcon());
            if (single != null) {
                Image icon = new Image(single);
                icon.setScaling(Scaling.fit);
                row.add(icon).size(REWARD_ICON);
                return row;
            }
        }
        TextureRegion art = pam == null || region == null ? null : pam.region(region);
        if (art != null) {
            Image icon = new Image(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(art));
            icon.setScaling(Scaling.fit);
            row.add(icon).size(REWARD_ICON);
        } else {
            row.add(new Label(quest.getDef().getRewardType().name().toLowerCase(),
                    ui.skin(), "muted"));
        }
        return row;
    }

    private String rewardRegion(int amount) {
        switch (quest.getDef().getRewardType()) {
            case COIN:
                return amount >= COIN_PLURAL_FROM ? "IMAGE_UI_COINS_STACK_0" : null;
            case GEM:
                return amount >= GEM_PLURAL_FROM ? "IMAGE_UI_GEMS_STACK_1" : null;
            case SEED_PACKET:
            case PLANT_UNLOCK:
                return "IMAGE_UI_STOREMULTI_SEEDPACKETICON";
            default:
                return null;
        }
    }

    private String singleIcon() {
        if (quest.getDef().getRewardType() == model.quest.RewardType.GEM) {
            return "gemIcon";
        }
        return "coinIcon";
    }

    private Table progressRow() {
        int done = (int) Math.min(quest.getProgress(), quest.getTarget());
        float ratio = quest.getTarget() <= 0 ? 0f : (float) done / quest.getTarget();

        Table track = new Table();
        track.setBackground(ui.primitives().rounded((int) (TRACK_HEIGHT / 2f),
                Theme.darken(Theme.PANEL_SUNKEN, 0.45f), Theme.darken(Theme.OUTLINE, 0.2f), 2));
        track.left();
        Table fill = new Table();
        fill.setBackground(ui.primitives().rounded((int) (TRACK_HEIGHT / 2f),
                Theme.GREEN, Theme.GREEN_DARK, 2));
        track.add(fill).growY().width(Math.max(2f, 260f * ratio)).left();

        Table row = new Table();
        row.left();
        row.add(track).width(260f).height(TRACK_HEIGHT);
        Label count = new Label(done + "/" + quest.getTarget(), ui.skin(), "muted");
        row.add(count).padLeft(Theme.PAD_SMALL).padTop(UiKit.opticalPad(count));
        return row;
    }
}
