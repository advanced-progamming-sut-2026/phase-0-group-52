package view.gui.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import controller.menu.NewsMenuController;
import model.User;
import model.news.News;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.Theme;

import java.util.List;

public final class NewsScreen extends BaseScreen {
    private final NewsMenuController controller;
    private boolean showAll = true;

    public NewsScreen(GameContext context) {
        super(context, "News");
        this.controller = new NewsMenuController(context.app());
    }

    @Override
    protected void build() {
        Table panel = ui.panel();
        panel.top();

        Table header = new Table();
        header.add(new Label("Announcements", ui.skin(), "title")).left().expandX();
        header.add(ui.secondaryButton(showAll ? "Show unread only" : "Show all", new Runnable() {
            @Override
            public void run() {
                showAll = !showAll;
                content.clear();
                build();
            }
        })).right();
        panel.add(header).growX().padBottom(Theme.PAD).row();

        ScrollPane scroll = new ScrollPane(buildList(), ui.skin());
        scroll.setFadeScrollBars(false);
        view.gui.UiKit.focusOnHover(scroll);
        panel.add(scroll).grow().row();

        Table actions = new Table();
        actions.add(ui.button("Mark all as read", new Runnable() {
            @Override
            public void run() {
                markAllRead();
            }
        })).padRight(Theme.PAD);
        actions.add(ui.secondaryButton("Back to main menu", new Runnable() {
            @Override
            public void run() {
                controller.handleCommand(new String[]{"menu", "enter", "main"});
            }
        }));
        panel.add(actions).padTop(Theme.PAD).right();

        content.add(panel).width(760f).height(460f).center();
    }

    private Table buildList() {
        Table list = new Table();
        list.top();
        list.defaults().growX().pad(Theme.PAD_SMALL);

        User user = context.user();
        List<News> items;
        if (user == null || user.getNewsList() == null) {
            items = java.util.Collections.<News>emptyList();
        } else {
            items = showAll ? user.getNewsList().getAll() : user.getNewsList().getUnread();
        }

        if (items.isEmpty()) {
            list.add(new Label(showAll ? "No announcements yet."
                    : "Nothing unread.", ui.skin(), "muted")).left().row();
        } else {
            for (News item : items) {
                list.add(card(item)).row();
            }
        }
        return list;
    }

    private Table card(News item) {
        Table card = ui.sunken();
        card.left();

        Table marker = new Table();
        marker.setBackground(ui.primitives().flat(
                item.isIsread() ? Theme.alpha(Theme.OUTLINE, 0.25f) : Theme.RED));
        card.add(marker).width(5f).growY().padRight(Theme.PAD);

        Label text = new Label(item.getNews(), ui.skin(), "default");
        text.setWrap(true);
        text.setAlignment(Align.left);
        card.add(text).growX();

        if (!item.isIsread()) {
            Label badge = new Label("NEW", ui.skin(), "small");
            badge.setColor(Theme.RED);
            card.add(badge).right().padLeft(Theme.PAD);
        }
        return card;
    }

    private void markAllRead() {
        controller.handleCommand(new String[]{"menu", "news", "show-unread"});
        context.toasts().success("All announcements marked as read.");
        content.clear();
        build();
    }
}
