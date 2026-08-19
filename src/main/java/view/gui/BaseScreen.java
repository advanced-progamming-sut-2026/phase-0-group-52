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

public abstract class BaseScreen implements Screen {
    protected final GameContext context;
    protected final UiKit ui;
    protected final Stage stage;

    protected final Table content = new Table();

    private final Table root = new Table();
    private final TopBar topBar;
    private final Toasts toasts;
    private com.badlogic.gdx.scenes.scene2d.ui.ScrollPane contentScroll;

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
        if (scrollContent()) {
            contentScroll = new com.badlogic.gdx.scenes.scene2d.ui.ScrollPane(content, ui.skin());
            contentScroll.setFadeScrollBars(false);
            contentScroll.setScrollingDisabled(true, false);
            contentScroll.setOverscroll(false, false);
            UiKit.focusOnHover(contentScroll);
            root.add(contentScroll).grow().pad(Theme.PAD_LARGE);
        } else {
            contentScroll = null;
            root.add(content).grow().pad(Theme.PAD_LARGE);
        }
        stage.addActor(root);
    }

    protected TopBar topBar() {
        return topBar;
    }

    protected boolean scrollContent() {
        return true;
    }

    protected abstract void build();

    protected void refresh() {
    }

    @Override
    public void show() {
        content.clear();
        build();

        toasts.setSize(stage.getViewport().getWorldWidth() - Theme.PAD_LARGE * 2, 240f);
        toasts.setPosition(Theme.PAD_LARGE, Theme.PAD_LARGE);
        stage.addActor(toasts);

        Gdx.input.setInputProcessor(stage);
        if (contentScroll != null) {
            stage.setScrollFocus(contentScroll);
        }
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
        toasts.remove();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
