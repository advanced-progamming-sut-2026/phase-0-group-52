package controller;

import database.QuestRepository;
import database.UserRepository;
import model.App;
import model.Game;
import model.User;
import model.quest.QuestDef;
import model.quest.QuestManager;
import model.quest.QuestProgress;
import model.quest.QuestState;

public class QuestService {

    private final QuestRepository questRepo = new QuestRepository();
    private final UserRepository userRepo = new UserRepository();
    private final QuestManager manager = new QuestManager();

    public boolean claim(QuestDef def) {
        User user = App.getInstance().getLoggedInUser();
        if (user == null || user.getUsername() == null || def == null) {
            return false;
        }
        QuestState state = questRepo.load(user.getUsername());
        for (QuestProgress qp : state.getQuests()) {
            if (qp.getDef() == def) {
                if (!manager.claim(user, qp)) {
                    return false;
                }
                questRepo.save(state);
                userRepo.updateStats(user);
                return true;
            }
        }
        return false;
    }

    public boolean togglePin(QuestDef def) {
        User user = App.getInstance().getLoggedInUser();
        if (user == null || user.getUsername() == null || def == null) {
            return false;
        }
        QuestState state = questRepo.load(user.getUsername());
        for (QuestProgress qp : state.getQuests()) {
            if (qp.getDef() == def) {
                qp.setPinned(!qp.isPinned());
                questRepo.save(state);
                return true;
            }
        }
        return false;
    }

    public boolean forceComplete(QuestDef def) {
        User user = App.getInstance().getLoggedInUser();
        if (user == null || user.getUsername() == null || def == null) {
            return false;
        }
        QuestState state = questRepo.load(user.getUsername());
        for (QuestProgress qp : state.getQuests()) {
            if (qp.getDef() == def) {
                manager.forceComplete(qp);
                questRepo.save(state);
                return true;
            }
        }
        return false;
    }

    public void onLevelEnd(Game game, boolean won) {
        User user = App.getInstance().getLoggedInUser();
        if (user == null || user.getUsername() == null) {
            return;
        }
        QuestState state = questRepo.load(user.getUsername());
        manager.onLevelEnd(user, state.getQuests(), game, won);
        questRepo.save(state);
        userRepo.updateStats(user);
    }
}
