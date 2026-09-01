package view.gui.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import controller.menu.ChoosePlantMenuController;
import model.ChapterType;
import model.Result;
import model.entities.plants.Plants;
import model.entities.zombies.ZombieRecord;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.Theme;
import view.gui.TopBar;
import view.gui.UiKit;
import view.gui.widgets.AlmanacFilterPopup;
import view.gui.widgets.LawnView;
import view.gui.widgets.LevelIntro;
import view.gui.widgets.PlantPickCard;
import view.gui.widgets.SearchBar;
import view.gui.widgets.SeedPacket;

import java.util.ArrayList;
import java.util.List;

public final class ChoosePlantScreen extends BaseScreen {

    private static final int COLUMNS = 5;
    private static final int SLOT_COLUMNS = 2;
    private static final float PACKET_SCALE = 0.88f;
    private static final float SLOT_SCALE = 0.9f;
    private static final float CARD_HEIGHT = 232f;
    private static final float PANEL_WIDTH = 632f;
    private static final float ROCK_WIDTH = 400f;
    private static final float ROCK_HEIGHT = 104f;
    private static final float ROCK_FONT = 1.45f;
    private static final float REVEAL = 0.4f;

    private final ChoosePlantMenuController controller;
    private final AlmanacFilterPopup.Rules rules = new AlmanacFilterPopup.Rules();

    private Table slotGrid;
    private Table grid;
    private PlantPickCard card;
    private Plants shown;

    private LawnView lawn;
    private Image catcher;
    private LevelIntro intro;
    private ChapterType lawnChapter;

    public ChoosePlantScreen(GameContext context) {
        super(context, "Choose your plants");
        this.controller = new ChoosePlantMenuController(context.app());
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (intro == null || !intro.isRunning()) {
                    return false;
                }
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.SPACE
                        || keycode == Input.Keys.ENTER) {
                    intro.skip();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected TopBar.Section section() {
        return TopBar.Section.OTHER;
    }

    @Override
    protected boolean scrollContent() {
        return false;
    }

    @Override
    protected float contentPad() {
        return 0f;
    }

    @Override
    protected void build() {
        buildLawn();
        muster();
        content.top().left();
        slotGrid = new Table();
        grid = new Table();
        card = new PlantPickCard(ui, context.assets());

        content.add(leftColumn()).grow().pad(Theme.PAD);
        content.add(rightPanel()).width(PANEL_WIDTH).growY();

        repaint();
    }

    private Table leftColumn() {
        Table left = new Table();
        left.top().left();
        left.add(slotGrid).left().row();
        left.add().expandY().row();
        com.badlogic.gdx.scenes.scene2d.ui.TextButton rock =
                ui.styledButton("Let's Rock!", "epic", new Runnable() {
                    @Override
                    public void run() {
                        rock();
                    }
                });
        rock.getLabel().setFontScale(ROCK_FONT);
        left.add(rock).size(ROCK_WIDTH, ROCK_HEIGHT).center().padBottom(Theme.PAD_LARGE);
        return left;
    }

    private void rock() {
        Result result = new controller.menu.LevelController(context.app()).start();
        if (!result.isSuccess()) {
            context.toasts().error(result.getMessage());
        }
    }

    private Table rightPanel() {
        Table panel = ui.panel();
        panel.top();
        panel.add(searchRow()).growX().right().padBottom(Theme.PAD_SMALL).row();
        panel.add(card).growX().height(CARD_HEIGHT).padBottom(Theme.PAD_SMALL).row();
        ScrollPane pane = new ScrollPane(grid, ui.skin(), "bare");
        pane.setScrollingDisabled(true, false);
        pane.setFadeScrollBars(false);
        pane.setOverscroll(false, false);
        UiKit.focusOnHover(pane);
        panel.add(pane).grow();
        return panel;
    }

    private void buildLawn() {
        ChapterType chapter = controller.chapter();
        if (lawn != null && chapter == lawnChapter) {
            return;
        }
        if (lawn != null) {
            lawn.remove();
            catcher.remove();
        }
        lawnChapter = chapter;
        lawn = new LawnView(context.assets(), chapter);
        catcher = new Image(ui.primitives().flat(Theme.alpha(Theme.BACKDROP, 0f)));
        catcher.setFillParent(true);
        catcher.setTouchable(Touchable.disabled);
        catcher.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                intro.skip();
                return true;
            }
        });
        intro = new LevelIntro(lawn);
        stage.getRoot().addActorAt(1, lawn);
        stage.getRoot().addActorAt(2, catcher);
    }

    private void muster() {
        lawn.clearPerformers();
        List<ZombieRecord> wave = controller.firstWave();
        for (int i = 0; i < wave.size(); i++) {
            lawn.addZombie(wave.get(i), i);
        }
    }

    private void playIntro() {
        lawn.setCamera(0f);
        catcher.setTouchable(Touchable.enabled);
        rootTable().getColor().a = 0f;
        rootTable().setTouchable(Touchable.disabled);
        intro.play(new Runnable() {
            @Override
            public void run() {
                reveal();
            }
        });
    }

    public void skipIntro() {
        if (intro != null) {
            intro.skip();
        }
    }

    public void poseIntro(float fraction) {
        if (lawn == null) {
            return;
        }
        skipIntro();
        rootTable().clearActions();
        rootTable().getColor().a = 0f;
        lawn.setCamera(fraction * lawn.maxCamera());
    }

    public void poseReady() {
        if (lawn == null) {
            return;
        }
        skipIntro();
        rootTable().clearActions();
        content.clearActions();
        rootTable().getColor().a = 1f;
        content.getColor().a = 1f;
        lawn.setCamera(0f);
    }

    private void reveal() {
        catcher.setTouchable(Touchable.disabled);
        rootTable().setTouchable(Touchable.childrenOnly);
        rootTable().addAction(Actions.fadeIn(REVEAL));
    }

    private Table searchRow() {
        return SearchBar.build(ui, rules.getQuery(), filterFace(),
                new SearchBar.Sink() {
                    @Override
                    public void typed(String text, TextField field) {
                        rules.setQuery(text);
                        rebuildGrid();
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        new AlmanacFilterPopup(context, rules, new Runnable() {
                            @Override
                            public void run() {
                                rebuildGrid();
                            }
                        }).showOn(stage);
                    }
                });
    }

    private com.badlogic.gdx.scenes.scene2d.utils.Drawable filterFace() {
        com.badlogic.gdx.graphics.g2d.TextureRegion found = context.assets() == null
                ? null : context.assets().region("IMAGE_UI_ALMANAC_FILTER_BUTTON_UP");
        return found == null ? null
                : new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(found);
    }

    private void repaint() {
        if (shown == null) {
            List<Plants> owned = controller.owned();
            shown = owned.isEmpty() ? null : owned.get(0);
        }
        rebuildSlots();
        rebuildGrid();
        card.show(shown, shown == null ? 1 : controller.levelOf(shown));
    }

    private void rebuildSlots() {
        slotGrid.clearChildren();
        List<Plants> picked = new ArrayList<Plants>(controller.picked());
        for (int i = 0; i < controller.slots(); i++) {
            slotGrid.add(slot(i < picked.size() ? picked.get(i) : null))
                    .size(SeedPacket.ART_W * SLOT_SCALE, SeedPacket.ART_H * SLOT_SCALE)
                    .pad(4f);
            if ((i + 1) % SLOT_COLUMNS == 0) {
                slotGrid.row();
            }
        }
    }

    private Table slot(final Plants plant) {
        Table cell = new Table();
        if (plant == null) {
            cell.setBackground(ui.primitives().rounded(8,
                    Theme.alpha(Theme.darken(Theme.PANEL_SUNKEN, 0.35f), 0.88f),
                    Theme.OUTLINE, 3));
            cell.pad(0f);
            return cell;
        }
        SeedPacket packet = new SeedPacket(ui, context.assets(), plant,
                SeedPacket.Mode.GAME, SLOT_SCALE);
        packet.setLevel(controller.levelOf(plant));
        packet.setBoosted(controller.isBoosted(plant));
        packet.onClick(new Runnable() {
            @Override
            public void run() {
                show(plant);
                report(controller.drop(plant));
            }
        });
        cell.pad(0f);
        cell.add(packet).grow();
        return cell;
    }

    private void rebuildGrid() {
        grid.clearChildren();
        grid.top().left();
        List<Plants> owned = controller.owned();
        int column = 0;
        for (Plants plant : rules.apply()) {
            if (!owned.contains(plant)) {
                continue;
            }
            grid.add(available(plant)).pad(3f);
            if (++column % COLUMNS == 0) {
                grid.row();
            }
        }
        if (column == 0) {
            grid.add(ui.muted("No plants match that search.")).pad(Theme.PAD);
        }
    }

    private SeedPacket available(final Plants plant) {
        SeedPacket packet = new SeedPacket(ui, context.assets(), plant,
                SeedPacket.Mode.GAME, PACKET_SCALE);
        packet.setLevel(controller.levelOf(plant));
        packet.setBoosted(controller.isBoosted(plant));
        packet.setSelected(controller.isPicked(plant));
        packet.onClick(new Runnable() {
            @Override
            public void run() {
                show(plant);
                report(controller.isPicked(plant)
                        ? controller.drop(plant) : controller.pick(plant));
            }
        });
        return packet;
    }

    private void show(Plants plant) {
        shown = plant;
    }

    private void report(Result result) {
        if (!result.isSuccess()) {
            context.toasts().error(result.getMessage());
        }
        repaint();
    }

    @Override
    public void show() {
        super.show();
        playIntro();
    }

    @Override
    public void hide() {
        super.hide();
        if (intro != null) {
            intro.skip();
        }
    }
}
