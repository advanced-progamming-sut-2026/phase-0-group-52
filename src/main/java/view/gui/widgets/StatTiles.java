package view.gui.widgets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import model.entities.plants.PlantRecord;
import view.gui.Assets;
import view.gui.Theme;
import view.gui.UiKit;

public final class StatTiles {

    private static final float ICON = 40f;
    private static final float BADGE = 44f;
    private static final float PAD = 6f;
    private static final float VALUE_SCALE = 1.25f;

    private final UiKit ui;
    private final Assets assets;

    public StatTiles(UiKit ui, Assets assets) {
        this.ui = ui;
        this.assets = assets;
    }

    private Drawable region(String id) {
        if (id == null || assets == null) {
            return null;
        }
        TextureRegion found = assets.region(id);
        return found == null ? null : new TextureRegionDrawable(found);
    }

    private static String iconFor(String label) {
        String key = label.toUpperCase();
        if (key.startsWith("SUN COST")) {
            return "IMAGE_UI_ALMANAC_ALMANAC_STAT_ICON_SUNCOST";
        }
        if (key.startsWith("SUN PRODUCTION") || key.startsWith("PRODUCTION")) {
            return "IMAGE_UI_ALMANAC_ALMANAC_STAT_ICON_SUNPRODUCTION";
        }
        if (key.startsWith("RECHARGE")) {
            return "IMAGE_UI_ALMANAC_PLANTS_RECHARGE_ICON";
        }
        if (key.startsWith("TOUGHNESS")) {
            return "IMAGE_UI_ALMANAC_PLANTS_TOUGHNESS_ICON";
        }
        if (key.contains("DAMAGE")) {
            return "IMAGE_UI_ALMANAC_PLANTS_DAMAGE_ICON";
        }
        if (key.startsWith("RANGE")) {
            return "IMAGE_UI_ALMANAC_PLANTS_RANGE_ICON";
        }
        if (key.startsWith("AREA")) {
            return "IMAGE_UI_ALMANAC_PLANTS_POWERAREA_ICON";
        }
        if (key.contains("ARMING") || key.startsWith("ATTACK RATE")) {
            return "IMAGE_UI_ALMANAC_ALMANAC_STAT_ICON_ARMINGTIME";
        }
        if (key.contains("PLANT FOOD")) {
            return "IMAGE_UI_ALMANAC_PLANT_FOOD_STAT_ICON";
        }
        if (key.startsWith("SPECIAL")) {
            return "IMAGE_UI_ALMANAC_ALMANAC_STAT_ICON_SPECIAL";
        }
        if (key.startsWith("CATEGORY")) {
            return "IMAGE_UI_ALMANAC_ALMANAC_STAT_ICON_FAMILY";
        }
        return "IMAGE_UI_ALMANAC_PLANTS_VARIABLE_ICON";
    }

    public Table tile(String label, String value, boolean upgraded) {
        Table tile = new Table();
        tile.setBackground(ui.primitives().rounded(10, Theme.PANEL,
                Theme.OUTLINE_SOFT, 2));
        tile.left();
        tile.pad(PAD);

        Drawable icon = region(iconFor(label));
        if (icon != null) {
            Image mark = new Image(icon);
            mark.setScaling(Scaling.fit);
            tile.add(mark).size(ICON).padRight(PAD);
        }

        Table text = new Table();
        text.left();
        Label caption = new Label(label.toUpperCase(), ui.skin(), "statLabel");
        text.add(caption).left().row();
        Label body = new Label(value, ui.skin(), "statValue");
        body.setFontScale(VALUE_SCALE);
        if (upgraded) {
            body.setColor(Theme.GREEN_DARK);
        }
        text.add(body).left().padTop(-1f).row();
        if (upgraded) {
            Table rule = new Table();
            rule.setBackground(ui.primitives().flat(Theme.GREEN));
            text.add(rule).height(2f).growX().padTop(1f);
        }
        tile.add(text).left().growX().minWidth(0f);
        return tile;
    }

    public Table categoryTile(PlantRecord record) {
        Table tile = tile("Category", AlmanacControls.pretty(record.getCategory().name()), false);
        tile.add(badge(record)).size(BADGE).right();
        return tile;
    }

    private Table badge(PlantRecord record) {
        Table holder = new Table();
        Stack stack = new Stack();
        Drawable banner = region("IMAGE_UI_PACKETS_MINTFAM_BANNER");
        if (banner != null) {
            Image disc = new Image(banner);
            disc.setScaling(Scaling.fit);
            disc.setColor(Theme.plantFamily(record.getCategory().name()));
            stack.add(disc);
        }
        Drawable glyph = region(record.getCategoryBadge());
        if (glyph != null) {
            Table pad = new Table();
            Image mark = new Image(glyph);
            mark.setScaling(Scaling.fit);
            pad.add(mark).grow().pad(BADGE * 0.22f);
            stack.add(pad);
        }
        holder.add(stack).grow();
        return holder;
    }
}
