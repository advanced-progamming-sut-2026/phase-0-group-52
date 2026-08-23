package view.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Scaling;
import pvz.skin.PvzSkin;

public final class UiKit implements Disposable {
    private final Assets assets;
    private final Primitives primitives = new Primitives();
    private final Skin skin = new Skin();
    private Skin artSkin;

    private BitmapFont fontSmall;
    private BitmapFont fontBody;
    private BitmapFont fontTitle;
    private BitmapFont fontHuge;
    private BitmapFont fontButton;
    private BitmapFont fontTitleOutline;
    private BitmapFont fontHand;

    public UiKit(Assets assets) {
        this.assets = assets;
        long start = System.currentTimeMillis();
        artSkin = loadPvzSkin();
        buildFonts();
        long fontsDone = System.currentTimeMillis();
        buildDrawables();
        buildStyles();
        util.Log.debug("gui", "Interface ready in " + (System.currentTimeMillis() - start)
                + " ms (fonts " + (fontsDone - start) + " ms)");
    }

    public Skin skin() {
        return skin;
    }

    public boolean hasArtSkin() {
        return artSkin != null;
    }

    private Skin loadPvzSkin() {
        Skin loaded = PvzSkin.get();
        util.Log.info("gui", "Loaded the PvZ art skin and its fonts");
        return loaded;
    }

    private Drawable art(String name, Drawable fallback) {
        if (artSkin != null) {
            try {
                return artSkin.getDrawable(name);
            } catch (RuntimeException e) {
                util.Log.debug("gui", "No skin drawable named " + name);
            }
        }
        if (skin.has(name, Drawable.class)) {
            return skin.getDrawable(name);
        }
        return fallback;
    }

    private final java.util.Set<String> missingFiles = new java.util.HashSet<String>();

    public TextureRegion regionFile(String path) {
        Drawable art = imageFile(path);
        if (art instanceof com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable) {
            return ((com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable) art).getRegion();
        }
        return null;
    }

    public Drawable ninePatchFile(String path, int top, int bottom, int left, int right) {
        TextureRegion region = regionFile(path);
        if (region == null) {
            return null;
        }
        com.badlogic.gdx.graphics.g2d.NinePatch patch =
                new com.badlogic.gdx.graphics.g2d.NinePatch(region, left, right, top, bottom);
        patch.setPadding(0f, 0f, 0f, 0f);
        return new com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable(patch);
    }

    public Drawable imageFile(String path) {
        String key = "file:" + path;
        if (skin.has(key, Drawable.class)) {
            return skin.getDrawable(key);
        }
        if (missingFiles.contains(path)) {
            return null;
        }
        loadIconFile(key, path);
        if (skin.has(key, Drawable.class)) {
            return skin.getDrawable(key);
        }
        missingFiles.add(path);
        return null;
    }

    private void loadIconFile(String name, String path) {
        com.badlogic.gdx.graphics.Texture texture = assets.texture(path);
        if (texture == null) {
            return;
        }
        skin.add(name, texture);
        skin.add(name, new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                new TextureRegion(texture)), Drawable.class);
    }

    public Primitives primitives() {
        return primitives;
    }

    public Actor iconButton(Icons.Icon icon, String fallbackText, Color fallbackFace,
            Runnable action) {
        Drawable normal = art(icon.normal(), null);
        if (normal == null) {
            return labelledButton(fallbackText, fallbackFace, action);
        }
        Drawable selected = art(icon.selected(), normal);

        com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle style =
                new com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle();
        style.imageUp = normal;
        style.imageOver = selected;
        style.imageDown = selected;

        com.badlogic.gdx.scenes.scene2d.ui.ImageButton button =
                new com.badlogic.gdx.scenes.scene2d.ui.ImageButton(style);
        button.getImage().setScaling(Scaling.fit);
        button.getImageCell().grow();
        if (action != null) {
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    action.run();
                }
            });
        }
        Animations.attachPress(button);
        return button;
    }

    public Table labelledButton(String text, Color face, Runnable action) {
        Table button = new Table();
        button.setBackground(buttonFace("brown", face));
        Label label = new Label(text, skin, "smallOnDark");
        label.setAlignment(com.badlogic.gdx.utils.Align.center);
        button.add(label).grow().pad(2f).padBottom(opticalPad(label));
        Animations.attachPress(button);
        if (action != null) {
            onClick(button, action);
        }
        return button;
    }

    public com.badlogic.gdx.scenes.scene2d.ui.ProgressBar skinProgressBar(
            float max, float value, String styleName) {
        if (artSkin == null) {
            return null;
        }
        try {
            com.badlogic.gdx.scenes.scene2d.ui.ProgressBar bar =
                    new com.badlogic.gdx.scenes.scene2d.ui.ProgressBar(
                            0f, Math.max(1f, max), 1f, false, artSkin, styleName);
            bar.setValue(value);
            bar.setAnimateDuration(0.35f);
            return bar;
        } catch (RuntimeException e) {
            util.Log.debug("gui", "No progress bar style named " + styleName);
            return null;
        }
    }

    public Drawable buttonFace(String styleName, Color fallbackFace) {
        if (artSkin != null) {
            try {
                TextButton.TextButtonStyle source =
                        artSkin.get(styleName, TextButton.TextButtonStyle.class);
                if (source.up != null) {
                    return source.up;
                }
            } catch (RuntimeException e) {
                util.Log.debug("gui", "No art button named " + styleName);
            }
        }
        return primitives.rounded(Theme.RADIUS, fallbackFace,
                Theme.darken(fallbackFace, 0.4f), Theme.BORDER);
    }

    private void buildFonts() {
        fontSmall = artSkin.getFont("BRIANNETOD");
        fontBody = artSkin.getFont("FBUSV8C6EI_3");
        fontTitle = artSkin.getFont("FBUSV8C5EI_2");
        fontHuge = artSkin.getFont("FBUSV8C5EI_1_outline");
        fontButton = artSkin.getFont("HOUSE_OF_TERROR");
        fontTitleOutline = artSkin.getFont("FBUSV8C5EI_2_outline");
        fontHand = artSkin.getFont("BRIANNETOD");

        skin.add("small", fontSmall);
        skin.add("body", fontBody);
        skin.add("title", fontTitle);
        skin.add("huge", fontHuge);
    }


    private void buildDrawables() {
        skin.add("white", primitives.pixel());

        skin.add("panel", framedPanel(), Drawable.class);
        skin.add("sunken", art("image_ui_dialog_asset_inner_bkgd_10",
                primitives.sunken()), Drawable.class);
        skin.add("card", art("image_ui_cards_almanac_plant_card_10",
                primitives.panel()), Drawable.class);
        skin.add("questPanel", art("image_ui_quests_panel_edge_to_edge_ten",
                primitives.sunken()), Drawable.class);
        skin.add("listRow", stretched("image_ui_quests_travel_log_panel_default", 16, 16, 16, 16,
                primitives.rounded(6, Theme.PANEL, Theme.OUTLINE_SOFT, 2)), Drawable.class);
        skin.add("listRowSelected", stretched("image_ui_quests_travel_log_panel_epic",
                16, 16, 16, 16, primitives.rounded(6, Theme.PANEL_SUNKEN,
                        Theme.OUTLINE_SOFT, 2)), Drawable.class);
        skin.add("chilliOn", art("image_ui_generic_jalapeno_difficulty_icon",
                primitives.flat(Theme.RED)), Drawable.class);
        skin.add("chilliOff", art("image_ui_generic_jalapeno_difficulty_icon_bg",
                primitives.flat(Theme.LOCKED)), Drawable.class);
        skin.add("inset", art("image_ui_mainmenu_inset_bkgd",
                primitives.sunken()), Drawable.class);
        skin.add("nameField", art("image_ui_mainmenu_name_field_10",
                primitives.rounded(Theme.RADIUS + 6, Theme.lighten(Theme.PANEL, 0.35f),
                        Theme.OUTLINE, Theme.BORDER)), Drawable.class);
        loadIconFile("leaderboardIcon", "assets/ui/leaderboard.png");
        loadIconFile("plusIcon", "assets/ui/currency_plus.png");
        loadIconFile("rankFirst", "assets/ui/rank_standstill.png");
        loadIconFile("rankSecond", "assets/ui/rank_promoted.png");
        loadIconFile("rankThird", "assets/ui/rank_demoted.png");
        skin.add("sortAscending", art("image_ui_generic_arrow_up_green",
                primitives.flat(Theme.GREEN)), Drawable.class);
        skin.add("sortDescending", art("image_ui_generic_arrow_down_orange",
                primitives.flat(Theme.SUN_DEEP)), Drawable.class);
        loadIconFile("leaderboardIconSelected", "assets/ui/leaderboard_selected.png");
        skin.add("topBar", art("image_ui_quests_panel_edge_to_edge_ten",
                primitives.flat(Theme.darken(Theme.OUTLINE, 0.25f))), Drawable.class);
        skin.add("counter", stretched("image_ui_generic_counter_bg", 16, 16, 14, 14,
                primitives.rounded(Theme.RADIUS, Theme.darken(Theme.OUTLINE, 0.45f),
                        Theme.OUTLINE_SOFT, 2)), Drawable.class);
        skin.add("coinIcon", art("image_ui_generic_coin_icon_small",
                primitives.flat(Theme.COIN)), Drawable.class);
        skin.add("gemIcon", art("image_ui_generic_gem_icon_small",
                primitives.flat(Theme.GEM)), Drawable.class);
        skin.add("scrim", primitives.flat(Theme.SCRIM), Drawable.class);
        skin.add("transparent", primitives.flat(new Color(0f, 0f, 0f, 0f)), Drawable.class);
    }

    private Drawable stretched(String region, int left, int right, int top, int bottom,
            Drawable fallback) {
        if (artSkin == null) {
            return fallback;
        }
        TextureRegion source = artSkin.getRegion(region);
        if (source == null) {
            return fallback;
        }
        return new com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable(
                new com.badlogic.gdx.graphics.g2d.NinePatch(source, left, right, top, bottom));
    }

    private Drawable framedPanel() {
        Drawable fallback = primitives.panel();
        if (artSkin == null) {
            return fallback;
        }
        Drawable frame = art("image_ui_dialog_asset_dialogborder_10", null);
        Drawable fill = art("image_ui_dialog_asset_inner_bkgd_10", null);
        if (frame == null || fill == null) {
            return fallback;
        }
        return new Layered(fill, frame, 10f);
    }

    private void buildStyles() {
        buildLabelStyles();
        buildButtonStyles();
        buildFieldStyles();
        buildScrollStyles();
        buildToggleStyles();
    }

    private void buildLabelStyles() {
        skin.add("default", new Label.LabelStyle(fontBody, Theme.TEXT));
        skin.add("small", new Label.LabelStyle(fontSmall, Theme.TEXT));
        skin.add("muted", new Label.LabelStyle(fontSmall, Theme.TEXT_MUTED));
        skin.add("title", new Label.LabelStyle(fontTitle, Theme.TEXT));
        skin.add("huge", new Label.LabelStyle(fontHuge, Theme.TEXT));
        skin.add("onDark", new Label.LabelStyle(fontBody, Theme.TEXT_ON_DARK));
        skin.add("titleOnDark", new Label.LabelStyle(
                fontTitleOutline != null ? fontTitleOutline : fontTitle, Theme.TEXT_ON_DARK));
        skin.add("hugeOnDark", new Label.LabelStyle(fontHuge, Theme.TEXT_ON_DARK));
        skin.add("smallOnDark", new Label.LabelStyle(fontSmall, Theme.TEXT_ON_DARK));
        skin.add("error", new Label.LabelStyle(fontSmall, Theme.RED));
        skin.add("value", new Label.LabelStyle(fontBody, Theme.OUTLINE));
        skin.add("story", new Label.LabelStyle(
                fontHand != null ? fontHand : fontBody, Theme.INK));
        skin.add("statLabel", new Label.LabelStyle(fontSmall, Theme.INK));
        skin.add("statValue", new Label.LabelStyle(fontBody, Theme.darken(Theme.SUN, 0.28f)));
        skin.add("special", new Label.LabelStyle(fontBody, Theme.RED));
        skin.add("packetCost", new Label.LabelStyle(
                fontTitleOutline != null ? fontTitleOutline : fontTitle, Color.WHITE));
        skin.add("rowName", new Label.LabelStyle(fontTitle, Theme.INK));
        skin.add("rowNameSelected", new Label.LabelStyle(fontTitleOutline, Theme.INK_SELECTED));
        skin.add("rowNameMe", new Label.LabelStyle(fontTitle, Theme.INK_SELECTED));
        skin.add("rowNameBig", new Label.LabelStyle(fontTitleOutline, Theme.INK));
        skin.add("rankNumber", new Label.LabelStyle(fontTitle, Theme.INK));
        skin.add("rowHeader", new Label.LabelStyle(fontTitle, Theme.INK));
        skin.add("rowSub", new Label.LabelStyle(fontBody, Theme.INK));
        skin.add("rowValue", new Label.LabelStyle(fontTitleOutline, Theme.INK_VALUE));
    }

    private void buildButtonStyles() {
        skin.add("default", artButton("green", Theme.GREEN, Theme.GREEN_DARK));
        skin.add("primary", artButton("green", Theme.GREEN, Theme.GREEN_DARK));
        skin.add("secondary", artButton("brown", Theme.PANEL_SUNKEN, Theme.OUTLINE));
        skin.add("danger", artButton("purple", Theme.RED, Theme.darken(Theme.RED, 0.3f)));
        skin.add("info", blueButton());

        TextButton.TextButtonStyle tab = textButton(Theme.PANEL_SUNKEN, Theme.OUTLINE_SOFT, Theme.TEXT_MUTED);
        tab.checked = primitives.rounded(Theme.RADIUS, Theme.PANEL, Theme.OUTLINE, Theme.BORDER);
        tab.checkedFontColor = Theme.TEXT;
        skin.add("tab", tab);

        TextButton.TextButtonStyle bare = new TextButton.TextButtonStyle();
        bare.font = fontSmall;
        bare.fontColor = Theme.TEXT;
        bare.up = skin.getDrawable("transparent");
        skin.add("bare", bare);

        Button.ButtonStyle plain = new Button.ButtonStyle();
        plain.up = skin.getDrawable("card");
        plain.down = primitives.rounded(Theme.RADIUS, Theme.darken(Theme.PANEL, 0.12f),
                Theme.OUTLINE, Theme.BORDER);
        plain.over = primitives.rounded(Theme.RADIUS, Theme.lighten(Theme.PANEL, 0.15f),
                Theme.OUTLINE, Theme.BORDER);
        skin.add("cardButton", plain);
    }

    private TextButton.TextButtonStyle blueButton() {
        TextButton.TextButtonStyle style =
                textButton(Theme.BLUE, Theme.darken(Theme.BLUE, 0.3f), Theme.TEXT_ON_DARK);
        Drawable up = art("image_ui_generic_bluebutton_10", null);
        Drawable down = art("image_ui_generic_bluebutton_down_10", null);
        if (up != null) {
            style.up = up;
            style.over = up;
            style.down = (down == null) ? up : down;
            style.disabled = up;
        }
        return style;
    }

    private TextButton.TextButtonStyle artButton(String artName, Color face, Color border) {
        TextButton.TextButtonStyle style = textButton(face, border, Theme.TEXT_ON_DARK);
        if (artSkin == null) {
            return style;
        }
        try {
            TextButton.TextButtonStyle source =
                    artSkin.get(artName, TextButton.TextButtonStyle.class);
            style.up = source.up;
            style.down = source.down;
            style.over = source.over == null ? source.up : source.over;
            style.disabled = source.disabled == null ? source.up : source.disabled;
        } catch (RuntimeException e) {
            util.Log.debug("gui", "No art button named " + artName);
        }
        return style;
    }

    private TextButton.TextButtonStyle textButton(Color face, Color border, Color text) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = (fontButton != null) ? fontButton : fontBody;
        style.fontColor = text;
        style.downFontColor = text;
        style.overFontColor = text;
        style.disabledFontColor = Theme.TEXT_DISABLED;
        style.up = primitives.rounded(Theme.RADIUS, face, border, Theme.BORDER);
        style.down = primitives.rounded(Theme.RADIUS, Theme.darken(face, 0.18f), border, Theme.BORDER);
        style.over = primitives.rounded(Theme.RADIUS, Theme.lighten(face, 0.14f), border, Theme.BORDER);
        style.disabled = primitives.rounded(Theme.RADIUS, Theme.LOCKED,
                Theme.darken(Theme.LOCKED, 0.3f), Theme.BORDER);
        return style;
    }

    private void buildFieldStyles() {
        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.font = fontBody;
        style.fontColor = Theme.TEXT;
        style.messageFont = fontBody;
        style.messageFontColor = Theme.TEXT_DISABLED;
        style.background = art("image_ui_mainmenu_text_entry_field_10",
                primitives.rounded(6, Theme.lighten(Theme.PANEL, 0.4f), Theme.OUTLINE_SOFT, 2));
        style.focusedBackground = art("image_ui_mainmenu_name_field_hover_10",
                primitives.rounded(6, Theme.lighten(Theme.PANEL, 0.55f), Theme.GREEN_DARK, 2));
        style.cursor = primitives.flat(Theme.TEXT);
        style.selection = primitives.flat(Theme.alpha(Theme.GREEN, 0.4f));
        skin.add("default", style);
    }

    private void buildScrollStyles() {
        ScrollPane.ScrollPaneStyle style = new ScrollPane.ScrollPaneStyle();
        style.background = null;
        style.vScroll = art("image_ui_almanac_general_scrollbar_bkgd_10",
                primitives.flat(Theme.alpha(Theme.OUTLINE, 0.18f)));
        style.vScrollKnob = art("image_ui_almanac_general_scrollbar_10",
                primitives.rounded(4, Theme.OUTLINE_SOFT, null, 0));
        style.hScroll = primitives.flat(Theme.alpha(Theme.OUTLINE, 0.18f));
        style.hScrollKnob = primitives.rounded(4, Theme.OUTLINE_SOFT, null, 0);
        skin.add("default", style);

        ScrollPane.ScrollPaneStyle bare = new ScrollPane.ScrollPaneStyle();
        skin.add("bare", bare);
    }

    private void buildToggleStyles() {
        CheckBox.CheckBoxStyle checkbox = new CheckBox.CheckBoxStyle();
        checkbox.font = fontBody;
        checkbox.fontColor = Theme.INK;
        checkbox.overFontColor = Theme.INK;
        checkbox.downFontColor = Theme.INK;
        checkbox.checkboxOff = art("image_ui_mainmenu_checkbox_disabled",
                primitives.rounded(4, Theme.lighten(Theme.PANEL, 0.35f), Theme.OUTLINE, 2));
        checkbox.checkboxOn = art("image_ui_mainmenu_checkbox_enabled",
                primitives.rounded(4, Theme.GREEN, Theme.GREEN_DARK, 2));
        skin.add("default", checkbox);

        SelectBox.SelectBoxStyle select = new SelectBox.SelectBoxStyle();
        select.font = fontBody;
        select.fontColor = Theme.TEXT;
        select.background = primitives.rounded(6, Theme.lighten(Theme.PANEL, 0.3f), Theme.OUTLINE_SOFT, 2);
        select.scrollStyle = skin.get(ScrollPane.ScrollPaneStyle.class);
        select.listStyle = new com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle(
                fontBody, Theme.TEXT_ON_DARK, Theme.TEXT,
                primitives.flat(Theme.GREEN));
        select.listStyle.background = primitives.rounded(6, Theme.PANEL, Theme.OUTLINE, 2);
        skin.add("default", select);
    }

    public Label title(String text) {
        return new Label(text, skin, "title");
    }

    public Label body(String text) {
        return new Label(text, skin, "default");
    }

    public Label muted(String text) {
        return new Label(text, skin, "muted");
    }

    public TextButton button(String text, Runnable onClick) {
        return styledButton(text, "primary", onClick);
    }

    public TextButton secondaryButton(String text, Runnable onClick) {
        return styledButton(text, "secondary", onClick);
    }

    public TextButton dangerButton(String text, Runnable onClick) {
        return styledButton(text, "danger", onClick);
    }

    public TextButton styledButton(String text, String styleName, final Runnable onClick) {
        TextButton button = new TextButton(text, skin, styleName);
        button.padLeft(Theme.PAD_LARGE).padRight(Theme.PAD_LARGE);
        button.getLabelCell().padBottom(opticalPad(button.getLabel()));
        if (onClick != null) {
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    onClick.run();
                }
            });
        }
        Animations.attachPress(button);
        return button;
    }

    public static float opticalPad(Label label) {
        return Math.round(-label.getStyle().font.getData().descent / 2f);
    }

    public CheckBox checkBox(String text) {
        CheckBox box = (artSkin != null && artSkin.has("default", CheckBox.CheckBoxStyle.class))
                ? new CheckBox(text, artSkin) : new CheckBox(text, skin);
        box.getLabel().setColor(Theme.TEXT);
        box.getLabelCell().padLeft(Theme.PAD_SMALL).padBottom(opticalPad(box.getLabel()));
        box.getImageCell().size(30f);
        return box;
    }

    private static float faceMarginTop(String styleName) {
        return 0f;
    }

    private static float faceMarginBottom(String styleName) {
        return "info".equals(styleName) ? 3f : 0f;
    }

    public Table faceButton(String text, String styleName, Runnable onClick) {
        Table holder = new Table();
        holder.add(styledButton(text, styleName, onClick)).grow()
                .padTop(-faceMarginTop(styleName))
                .padBottom(-faceMarginBottom(styleName));
        return holder;
    }

    public Table panel() {
        if (artSkin != null) {
            Table framed = new pvz.skin.BorderedTable();
            framed.pad(30f);
            return framed;
        }
        Table table = new Table();
        table.setBackground(skin.getDrawable("panel"));
        table.pad(Theme.PAD_LARGE);
        return table;
    }

    public Table sunken() {
        Table table = new Table();
        table.setBackground(skin.getDrawable("sunken"));
        table.pad(artSkin == null ? Theme.PAD : Theme.PAD + 4f);
        return table;
    }

    public Image token(int diameter, Color body) {
        Image image = new Image(new TextureRegion(
                primitives.entityToken(diameter, body, Theme.darken(body, 0.35f))));
        image.setScaling(Scaling.fit);
        return image;
    }

    public Actor divider() {
        Image image = new Image(skin.getDrawable("white"));
        image.setColor(Theme.alpha(Theme.OUTLINE, 0.35f));
        return image;
    }

    public static void focusOnHover(final Actor actor) {
        actor.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                    float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && actor.getStage() != null) {
                    actor.getStage().setScrollFocus(actor);
                }
            }

            @Override
            public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                    float x, float y, int pointer, Actor toActor) {
                if (pointer == -1 && actor.getStage() != null
                        && actor.getStage().getScrollFocus() == actor) {
                    actor.getStage().setScrollFocus(null);
                }
            }
        });
    }

    public static void focusOnHover(final Actor hoverArea, final Actor target) {
        hoverArea.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                    float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && target.getStage() != null) {
                    target.getStage().setScrollFocus(target);
                }
            }
        });
    }

    public static void onClick(Actor actor, final Runnable action) {
        actor.setTouchable(Touchable.enabled);
        actor.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                action.run();
            }
        });
    }

    public Drawable drawable(String name) {
        return skin.getDrawable(name);
    }

    @Override
    public void dispose() {
        skin.dispose();
        primitives.dispose();
        if (artSkin != null) {
            artSkin.dispose();
        }
    }
}
