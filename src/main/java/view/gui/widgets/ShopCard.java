package view.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import view.gui.Assets;
import view.gui.Theme;
import view.gui.UiKit;

public final class ShopCard extends Table {

    public static final float WIDTH = 178f;
    public static final float HEIGHT = WIDTH * 498f / 343f;

    private static final String FRAME = "IMAGE_UI_STORE_CARD_PROMOTED_BACKGROUND";
    private static final float ART = 88f;
    private static final float PRICE_ICON = 34f;
    private static final float PRICE_TEXT = 1.15f;
    private static final float BAND = 26f;
    private static final float HOVER_LIFT = 1.04f;

    private static final float COUNTDOWN_TEXT = 0.86f;

    private final Table sheen = new Table();
    private Label countdown;

    public ShopCard(UiKit ui, Assets assets, String title, String blurb, String art,
            String currencyIcon, int price, boolean daily, final Runnable onPick) {
        this(ui, assets, title, blurb, art, currencyIcon, price, null, 0, daily, onPick);
    }

    public ShopCard(UiKit ui, Assets assets, String title, String blurb, String art,
            String currencyIcon, int price, String intoIcon, int into, boolean daily,
            final Runnable onPick) {
        this(ui, assets, title, blurb, art, null, currencyIcon, price, intoIcon, into,
                daily, onPick);
    }

    public ShopCard(UiKit ui, Assets assets, String title, String blurb, String art,
            com.badlogic.gdx.scenes.scene2d.Actor face, String currencyIcon, int price,
            String intoIcon, int into, boolean daily, final Runnable onPick) {
        Stack layers = new Stack();
        TextureRegion back = assets == null ? null : assets.region(FRAME);
        if (back != null) {
            Image sheet = new Image(back);
            sheet.setScaling(Scaling.fit);
            layers.add(sheet);
        }
        layers.add(band(ui, daily));
        layers.add(body(ui, assets, title, blurb, art, face, currencyIcon, price,
                intoIcon, into));
        sheen.setBackground(ui.primitives().rounded(18, new Color(1f, 1f, 1f, 0f),
                Theme.SUN, 3));
        sheen.setVisible(false);
        layers.add(sheen);

        add(layers).size(WIDTH, HEIGHT);
        setTouchable(Touchable.enabled);
        setTransform(true);
        setOrigin(Align.center);
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onPick.run();
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer,
                    com.badlogic.gdx.scenes.scene2d.Actor from) {
                sheen.setVisible(true);
                setScale(HOVER_LIFT);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer,
                    com.badlogic.gdx.scenes.scene2d.Actor to) {
                sheen.setVisible(false);
                setScale(1f);
            }
        });
    }

    private Table band(UiKit ui, boolean daily) {
        Table strip = new Table();
        strip.top();
        Table paint = new Table();
        paint.setBackground(ui.primitives().rounded(12,
                daily ? Theme.SUN : new Color(1f, 1f, 1f, 0f), Theme.OUTLINE, daily ? 2 : 0));
        if (daily) {
            countdown = new Label("", ui.skin(), "titleOnDark");
            countdown.setAlignment(Align.center);
            countdown.setFontScale(COUNTDOWN_TEXT);
            tickCountdown();
            paint.add(countdown).grow();
        }
        strip.add(paint).height(BAND).growX().padTop(BAND * 0.42f)
                .padLeft(BAND * 0.7f).padRight(BAND * 0.7f);
        return strip;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        tickCountdown();
    }

    private void tickCountdown() {
        if (countdown != null) {
            countdown.setText("DAILY  " + model.shop.Shop.refreshCountdown());
        }
    }

    private Table body(UiKit ui, Assets assets, String title, String blurb, String art,
            com.badlogic.gdx.scenes.scene2d.Actor face, String currencyIcon, int price,
            String intoIcon, int into) {
        Table body = new Table();
        body.pad(Theme.PAD_SMALL).padTop(BAND * 1.9f).top();
        if (face != null) {
            face.setTouchable(Touchable.disabled);
            body.add(face).size(ART, ART * 0.86f).row();
        }
        TextureRegion icon = face != null || assets == null || art == null
                ? null : assets.region(art);
        if (icon != null) {
            Image flat = new Image(icon);
            flat.setScaling(Scaling.fit);
            body.add(flat).size(ART).row();
        }
        Label name = new Label(title, ui.skin(), "titleOnDark");
        name.setAlignment(Align.center);
        body.add(name).growX().padTop(Theme.PAD_SMALL).row();
        Label note = new Label(blurb, ui.skin(), "smallOnDark");
        note.setWrap(true);
        note.setAlignment(Align.center);
        body.add(note).growX().padTop(2f).row();
        body.add(price(ui, currencyIcon, price, intoIcon, into))
                .expand().bottom().padBottom(Theme.PAD_SMALL);
        return body;
    }

    private Table price(UiKit ui, String currencyIcon, int price, String intoIcon,
            int into) {
        Table tag = new Table();
        if (currencyIcon != null) {
            Image coin = new Image(ui.drawable(currencyIcon));
            coin.setScaling(Scaling.fit);
            tag.add(coin).size(PRICE_ICON).padRight(4f);
        }
        Label amount = new Label(String.valueOf(price), ui.skin(), "titleOnDark");
        amount.setFontScale(PRICE_TEXT);
        tag.add(amount);
        if (intoIcon != null) {
            Label arrow = new Label(" > ", ui.skin(), "titleOnDark");
            arrow.setFontScale(PRICE_TEXT);
            tag.add(arrow);
            Image got = new Image(ui.drawable(intoIcon));
            got.setScaling(Scaling.fit);
            tag.add(got).size(PRICE_ICON).padRight(4f);
            Label sum = new Label(String.valueOf(into), ui.skin(), "titleOnDark");
            sum.setFontScale(PRICE_TEXT);
            tag.add(sum);
        }
        return tag;
    }
}
