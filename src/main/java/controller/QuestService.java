package controller;

import database.QuestRepository;
import database.UserRepository;
import model.App;
import model.Game;
import model.User;
import model.quest.QuestManager;
import model.quest.QuestState;

public class QuestService {

    private final QuestRepository questRepo = new QuestRepository();
    private final UserRepository userRepo = new UserRepository();
    private final QuestManager manager = new QuestManager();

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
