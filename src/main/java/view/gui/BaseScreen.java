package view.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import util.Log;

/**
 * Shared plumbing for every menu screen: a stage, a background, the top bar and
 * the toast layer.
 *
 * <p>Subclasses fill {@link #content} and nothing else. In particular they never
 * decide which screen comes next — they ask a controller to change menus, and
 * {@link PvzGame} notices the model changed and swaps screens. That one-way flow is
 * what keeps navigation rules in the controller layer where the console version
 * already put them.
 */
public abstract class BaseScreen implements Screen {

    protected final GameContext context;
    protected final UiKit ui;
    protected final Stage stage;

    /** The area subclasses populate. */
    protected final Table content = new Table();

    private final Table root = new Table();
    private final TopBar topBar;
    private final Toasts toasts;

    protected BaseScreen(GameContext context, String title) {
        this.context = context;
        this.ui = context.ui();
        this.stage = new Stage(new FitViewport(Theme.WORLD_WIDTH, Theme.WORLD_HEIGHT));
        this.toasts = context.toasts();

        Image backdrop = new Image(new TextureRegion(ui.primitives().verticalGradient(
                64, 256, Theme.BACKDROP_ALT, Theme.BACKDROP)));
        backdrop.setScaling(Scaling.stretch);
        backdrop.setFillParent(true);
        stage.addActor(backdrop);

        topBar = new TopBar(context, title);

        root.setFillParent(true);
        root.top();
        root.add(topBar).growX().row();
        root.add(content).grow().pad(Theme.PAD_LARGE);
        stage.addActor(root);
    }

    /** Builds the screen body. Called once, after construction. */
    protected abstract void build();

    /** Refreshes any labels that mirror model values. Called every frame. */
    protected void refresh() {
    }

    @Override
    public void show() {
        content.clear();
        build();

        // The toast layer is shared across screens, so it is (re)parented here on
        // every show rather than owned by one stage.
        toasts.setSize(stage.getViewport().getWorldWidth() - Theme.PAD_LARGE * 2, 240f);
        toasts.setPosition(Theme.PAD_LARGE, Theme.PAD_LARGE);
        stage.addActor(toasts);

        Gdx.input.setInputProcessor(stage);
        Animations.enter(content);
        Log.debug("view", "Entered " + getClass().getSimpleName());
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(Theme.BACKDROP.r, Theme.BACKDROP.g, Theme.BACKDROP.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        topBar.refresh();
        refresh();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        toasts.setSize(stage.getViewport().getWorldWidth() - Theme.PAD_LARGE * 2, 240f);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        // The toast layer is shared between screens, so hand it back rather than
        // letting the outgoing stage dispose of it.
        toasts.remove();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
