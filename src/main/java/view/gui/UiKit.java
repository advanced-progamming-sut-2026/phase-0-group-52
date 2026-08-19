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

public final class UiKit implements Disposable {
    private final Primitives primitives = new Primitives();
    private final Skin skin = new Skin();

    private BitmapFont fontSmall;
    private BitmapFont fontBody;
    private BitmapFont fontTitle;
    private BitmapFont fontHuge;

    public UiKit() {
        long start = System.currentTimeMillis();
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

    public Primitives primitives() {
        return primitives;
    }

    private void buildFonts() {
        fontSmall = sized(15, 0.85f);
        fontBody = sized(18, 1.05f);
        fontTitle = sized(26, 1.6f);
        fontHuge = sized(40, 2.4f);

        skin.add("small", fontSmall);
        skin.add("body", fontBody);
        skin.add("title", fontTitle);
        skin.add("huge", fontHuge);
    }

    private BitmapFont sized(int pixels, float fallbackScale) {
        BitmapFont font = FontFactory.create(pixels);
        if (font != null) {
            return font;
        }
        font = new BitmapFont();
        font.getRegion().getTexture().setFilter(
                com.badlogic.gdx.graphics.Texture.TextureFilter.Linear,
                com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
        font.getData().setScale(fallbackScale);
        font.setUseIntegerPositions(false);
        return font;
    }

    private void buildDrawables() {
        skin.add("white", primitives.pixel());

        skin.add("panel", primitives.panel(), Drawable.class);
        skin.add("sunken", primitives.sunken(), Drawable.class);
        skin.add("scrim", primitives.flat(Theme.SCRIM), Drawable.class);
        skin.add("transparent", primitives.flat(new Color(0f, 0f, 0f, 0f)), Drawable.class);
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
        skin.add("titleOnDark", new Label.LabelStyle(fontTitle, Theme.TEXT_ON_DARK));
        skin.add("hugeOnDark", new Label.LabelStyle(fontHuge, Theme.TEXT_ON_DARK));
        skin.add("smallOnDark", new Label.LabelStyle(fontSmall, Theme.TEXT_ON_DARK));
        skin.add("error", new Label.LabelStyle(fontSmall, Theme.RED));
        skin.add("value", new Label.LabelStyle(fontBody, Theme.OUTLINE));
    }

    private void buildButtonStyles() {
        skin.add("default", textButton(Theme.GREEN, Theme.GREEN_DARK, Theme.TEXT_ON_DARK));
        skin.add("primary", textButton(Theme.GREEN, Theme.GREEN_DARK, Theme.TEXT_ON_DARK));
        skin.add("secondary", textButton(Theme.PANEL_SUNKEN, Theme.OUTLINE, Theme.TEXT));
        skin.add("danger", textButton(Theme.RED, Theme.darken(Theme.RED, 0.3f), Theme.TEXT_ON_DARK));
        skin.add("info", textButton(Theme.BLUE, Theme.darken(Theme.BLUE, 0.3f), Theme.TEXT_ON_DARK));

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
        plain.up = primitives.rounded(Theme.RADIUS, Theme.PANEL, Theme.OUTLINE, Theme.BORDER);
        plain.down = primitives.rounded(Theme.RADIUS, Theme.darken(Theme.PANEL, 0.12f),
                Theme.OUTLINE, Theme.BORDER);
        plain.over = primitives.rounded(Theme.RADIUS, Theme.lighten(Theme.PANEL, 0.15f),
                Theme.OUTLINE, Theme.BORDER);
        skin.add("card", plain);
    }

    private TextButton.TextButtonStyle textButton(Color face, Color border, Color text) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = fontBody;
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
        style.background = primitives.rounded(6, Theme.lighten(Theme.PANEL, 0.4f), Theme.OUTLINE_SOFT, 2);
        style.focusedBackground = primitives.rounded(6, Theme.lighten(Theme.PANEL, 0.55f), Theme.GREEN_DARK, 2);
        style.cursor = primitives.flat(Theme.TEXT);
        style.selection = primitives.flat(Theme.alpha(Theme.GREEN, 0.4f));
        skin.add("default", style);
    }

    private void buildScrollStyles() {
        ScrollPane.ScrollPaneStyle style = new ScrollPane.ScrollPaneStyle();
        style.background = null;
        style.vScroll = primitives.flat(Theme.alpha(Theme.OUTLINE, 0.18f));
        style.vScrollKnob = primitives.rounded(4, Theme.OUTLINE_SOFT, null, 0);
        style.hScroll = primitives.flat(Theme.alpha(Theme.OUTLINE, 0.18f));
        style.hScrollKnob = primitives.rounded(4, Theme.OUTLINE_SOFT, null, 0);
        skin.add("default", style);
    }

    private void buildToggleStyles() {
        CheckBox.CheckBoxStyle checkbox = new CheckBox.CheckBoxStyle();
        checkbox.font = fontBody;
        checkbox.fontColor = Theme.TEXT;
        checkbox.checkboxOff = primitives.rounded(4, Theme.lighten(Theme.PANEL, 0.35f),
                Theme.OUTLINE, 2);
        checkbox.checkboxOn = primitives.rounded(4, Theme.GREEN, Theme.GREEN_DARK, 2);
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
        button.pad(Theme.PAD_SMALL, Theme.PAD_LARGE, Theme.PAD_SMALL, Theme.PAD_LARGE);
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

    public Table panel() {
        Table table = new Table();
        table.setBackground(skin.getDrawable("panel"));
        table.pad(Theme.PAD_LARGE);
        return table;
    }

    public Table sunken() {
        Table table = new Table();
        table.setBackground(skin.getDrawable("sunken"));
        table.pad(Theme.PAD);
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
        fontSmall.dispose();
        fontBody.dispose();
        fontTitle.dispose();
        fontHuge.dispose();
    }
}
