package view.gui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import controller.menu.MinigameMenuController;
import minigame.BeghouledBoard;
import minigame.MinigameType;
import model.entities.plants.PlantData;
import model.entities.plants.PlantRecord;
import model.entities.plants.Plants;
import model.enums.MenuType;
import view.gui.GameContext;
import view.gui.Navigator;
import view.gui.Theme;
import view.gui.UiKit;
import view.gui.widgets.XpBar;

public final class BeghouledScreen implements Screen, Navigator.Hosted {

    private static final float TILE = 84f;
    private static final float GAP = 5f;
    private static final int CORNER = 12;
    private static final float BAR_HEIGHT = 20f;
    private static final float POP_TIME = 0.18f;
    private static final float SWAP_TIME = 0.12f;
    private static final float BOARD_PAD = 14f;

    private final GameContext context;
    private final UiKit ui;
    private final Stage stage;
    private final MinigameMenuController menu;

    private BeghouledBoard board;
    private Table grid;
    private Label sunLabel;
    private Label movesLabel;
    private Label chainLabel;
    private Table barHolder;
    private int pickedColumn = -1;
    private int pickedRow = -1;
    private boolean finished;
    private boolean scored;

    public BeghouledScreen(GameContext context) {
        this.context = context;
        this.ui = context.ui();
        this.stage = context.assets().newStage();
        this.menu = new MinigameMenuController(context.app());
    }

    @Override
    public Stage uiStage() {
        return stage;
    }

    private void build() {
        stage.clear();
        Table root = new Table();
        root.setFillParent(true);

        com.badlogic.gdx.scenes.scene2d.utils.Drawable backdrop =
                ui.imageFile("assets/ui/minigames/beghouled.png");
        if (backdrop != null) {
            Image sheet = new Image(backdrop);
            sheet.setScaling(Scaling.fill);
            sheet.setColor(0.42f, 0.42f, 0.42f, 1f);
            Table veil = new Table();
            veil.setFillParent(true);
            veil.add(sheet).grow();
            stage.addActor(veil);
        } else {
            Table plain = new Table();
            plain.setFillParent(true);
            plain.setBackground(ui.primitives().flat(Theme.BACKDROP));
            stage.addActor(plain);
        }

        root.add(header()).growX().pad(Theme.PAD_SMALL).row();
        root.add(boardPanel()).expand().center().row();
        root.add(footer()).growX().pad(Theme.PAD_SMALL);
        stage.addActor(root);
        stage.addActor(context.toasts());
        rebuildGrid();
    }

    private Table header() {
        Table bar = new Table();
        bar.left();
        Label title = new Label("Beghouled", ui.skin(), "titleOnDark");
        title.setColor(Theme.SUN);
        bar.add(title).left().padRight(Theme.PAD_LARGE);

        sunLabel = new Label("", ui.skin(), "titleOnDark");
        movesLabel = new Label("", ui.skin(), "titleOnDark");
        chainLabel = new Label("", ui.skin(), "onDark");
        chainLabel.setColor(Theme.GREEN_LIGHT);

        bar.add(sunLabel).left().padRight(Theme.PAD_LARGE);
        bar.add(movesLabel).left().padRight(Theme.PAD_LARGE);
        bar.add(chainLabel).left();
        bar.add().expandX();
        bar.add(ui.faceButton("Quit", "secondary", new Runnable() {
            @Override
            public void run() {
                leave();
            }
        })).right();
        return bar;
    }

    private Table footer() {
        barHolder = new Table();
        Table row = new Table();
        row.add(barHolder).growX().height(BAR_HEIGHT);
        return row;
    }

    private Table boardPanel() {
        Table panel = ui.panel();
        grid = new Table();
        panel.add(grid).pad(BOARD_PAD);
        return panel;
    }

    private void rebuildGrid() {
        grid.clearChildren();
        for (int row = 0; row < BeghouledBoard.ROWS; row++) {
            for (int column = 0; column < BeghouledBoard.COLUMNS; column++) {
                grid.add(tile(column, row)).size(TILE).pad(GAP / 2f);
            }
            grid.row();
        }
        refreshLabels();
    }

    private Actor tile(final int column, final int row) {
        final Plants type = board.at(column, row);
        Table cell = new Table();
        boolean picked = column == pickedColumn && row == pickedRow;
        cell.setBackground(ui.primitives().rounded(CORNER,
                Theme.alpha(picked ? Theme.GREEN_LIGHT : Theme.PANEL_SUNKEN, 0.88f),
                picked ? Theme.SUN : Theme.OUTLINE_SOFT, picked ? 4 : 2));

        Stack layers = new Stack();
        Table art = new Table();
        art.add(pieceImage(type)).size(TILE * 0.78f);
        layers.add(art);
        cell.add(layers).grow();
        cell.setTouchable(Touchable.enabled);
        cell.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                pick(column, row);
            }
        });
        return cell;
    }

    private Actor pieceImage(Plants type) {
        PlantRecord record = type == null ? null : PlantData.record(type);
        com.badlogic.gdx.graphics.g2d.TextureRegion art =
                record == null || context.assets() == null
                        ? null : context.assets().packetIcon(type);
        if (art == null) {
            Table blank = new Table();
            blank.setBackground(ui.primitives().rounded(CORNER - 4,
                    type == null ? Theme.PANEL : Theme.plantFamily(
                            type.getCategory().name()),
                    Theme.OUTLINE_SOFT, 2));
            return blank;
        }
        Image icon = new Image(art);
        icon.setScaling(Scaling.fit);
        icon.setTouchable(Touchable.disabled);
        return icon;
    }

    private void pick(int column, int row) {
        if (finished) {
            return;
        }
        if (pickedColumn < 0) {
            pickedColumn = column;
            pickedRow = row;
            rebuildGrid();
            return;
        }
        if (pickedColumn == column && pickedRow == row) {
            clearPick();
            return;
        }
        if (!BeghouledBoard.areNeighbours(pickedColumn, pickedRow, column, row)) {
            pickedColumn = column;
            pickedRow = row;
            rebuildGrid();
            return;
        }
        int fromColumn = pickedColumn;
        int fromRow = pickedRow;
        clearPickQuietly();
        if (!board.swap(fromColumn, fromRow, column, row)) {
            context.toasts().error("That swap makes no match.");
            rebuildGrid();
            return;
        }
        rebuildGrid();
        celebrate();
        checkOver();
    }

    private void celebrate() {
        for (Actor actor : grid.getChildren()) {
            actor.getColor().a = 1f;
        }
        grid.addAction(Actions.sequence(
                Actions.alpha(0.72f, SWAP_TIME),
                Actions.alpha(1f, POP_TIME)));
    }

    private void clearPick() {
        clearPickQuietly();
        rebuildGrid();
    }

    private void clearPickQuietly() {
        pickedColumn = -1;
        pickedRow = -1;
    }

    private void refreshLabels() {
        sunLabel.setText("Sun  " + board.getSun() + " / " + board.getTarget());
        movesLabel.setText("Moves  " + board.getMovesLeft());
        chainLabel.setText(board.getChain() > 1 ? "Chain x" + board.getChain() : "");
        barHolder.clearChildren();
        barHolder.add(new XpBar(
                ui.primitives().rounded((int) (BAR_HEIGHT / 2f),
                        Theme.darken(Theme.PANEL_SUNKEN, 0.4f), Theme.OUTLINE, 2),
                ui.primitives().rounded((int) (BAR_HEIGHT / 2f) - 2,
                        Theme.GREEN, Theme.darken(Theme.GREEN, 0.35f), 1),
                board.progress(), 3f)).growX().height(BAR_HEIGHT);
    }

    private void checkOver() {
        if (finished) {
            return;
        }
        if (board.isWon()) {
            finished = true;
            bank();
            new view.gui.widgets.MinigameOutcomePopup(context, true,
                    "You grew " + board.getSun() + " sun.",
                    board.getSun(), menu.bestScore(MinigameType.BEGHOULED),
                    new Runnable() {
                        @Override
                        public void run() {
                            replay();
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            leave();
                        }
                    }).showOn(stage);
            return;
        }
        if (board.isLost()) {
            finished = true;
            bank();
            new view.gui.widgets.MinigameOutcomePopup(context, false,
                    "Out of moves at " + board.getSun() + " sun.",
                    board.getSun(), menu.bestScore(MinigameType.BEGHOULED),
                    new Runnable() {
                        @Override
                        public void run() {
                            replay();
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            leave();
                        }
                    }).showOn(stage);
        }
    }

    private void bank() {
        if (scored) {
            return;
        }
        scored = true;
        menu.recordScore(MinigameType.BEGHOULED, board.getSun());
    }

    private void replay() {
        board = new BeghouledBoard(level());
        finished = false;
        scored = false;
        clearPickQuietly();
        build();
    }

    private void leave() {
        context.app().setPendingMinigame(null);
        context.app().setCurrentmenu(MenuType.MAIN_MENU);
    }

    private int level() {
        return Math.max(1, context.app().getSelectedLevel());
    }

    @Override
    public void show() {
        board = new BeghouledBoard(level());
        finished = false;
        scored = false;
        clearPickQuietly();
        build();
        Gdx.input.setInputProcessor(stage);
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    clearPick();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        context.toasts().remove();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    public int score() {
        return board == null ? 0 : board.getSun();
    }

    public void poseForTour() {
        if (grid != null) {
            grid.setColor(1f, 1f, 1f, 1f);
        }
    }
}
