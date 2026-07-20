package controller;

import database.QuestRepository;
import minigame.MinigameType;
import model.App;
import model.User;
import model.enums.Menu;
import model.quest.QuestDef;
import model.quest.QuestProgress;
import model.quest.QuestState;
import model.quest.RewardService;

import java.util.ArrayList;
import java.util.List;

public class TravelLogMenuController {

    private final QuestRepository repository = new QuestRepository();
    private final RewardService rewardService = new RewardService();
    private String currentPage = "daily";

    private User user() {
        return App.getInstance().getLoggedInUser();
    }

    private boolean noUser() {
        if (user() == null) {
            System.out.println("No logged-in user.");
            return true;
        }
        return false;
    }

    public void showPage(String page) {
        String p = page.trim().toLowerCase();
        if (!p.equals("daily") && !p.equals("main") && !p.equals("epic") && !p.equals("minigame")) {
            System.out.println("Error: Unknown page: " + page + ". Pages: daily, main, epic, minigame.");
            return;
        }
        this.currentPage = p;
        showCurrentPage();
    }

    public void showCurrentPage() {
        if (noUser()) {
            return;
        }
        System.out.println("== Travel Log — page: " + currentPage + " ==");
        if (currentPage.equals("minigame")) {
            showMinigames();
            return;
        }
        QuestState state = repository.load(user().getUsername());
        List<QuestProgress> onPage = questsForPage(state, currentPage);
        if (onPage.isEmpty()) {
            System.out.println("No quests here. (pages: daily, main, epic, minigame)");
            return;
        }
        onPage.sort((a, b) -> Integer.compare(
            b.getDef().getPriority().getPrioritynum(),
            a.getDef().getPriority().getPrioritynum()));
        for (QuestProgress qp : onPage) {
            printQuest(qp);
        }
    }

    private List<QuestProgress> questsForPage(QuestState state, String page) {
        List<QuestProgress> result = new ArrayList<>();
        for (QuestProgress qp : state.getQuests()) {
            if (qp.getDef().getCategory().name().equalsIgnoreCase(page)) {
                result.add(qp);
            }
        }
        return result;
    }

    private void printQuest(QuestProgress qp) {
        QuestDef def = qp.getDef();
        String status = qp.isCompleted() ? (qp.isClaimed() ? "claimed" : "completed") : "in progress";
        System.out.printf("[%-8s] %-24s progress: %d/%d | reward: %d %s | %s%n",
            def.getPriority(), def.getDisplayName(),
            (long) qp.getProgress(), qp.getTarget(),
            rewardService.amountFor(qp), def.getRewardType(), status);
    }

    private void showMinigames() {
        for (MinigameType m : MinigameType.values()) {
            System.out.println("- " + m.name());
        }
    }

    public void showCurrentMenu() {
        System.out.println("You are now in the Travel Log menu. Current page: " + currentPage);
    }

    public void exitMenu() {
        App.getInstance().setCurrentMenu(Menu.MainMenu);
    }
}
