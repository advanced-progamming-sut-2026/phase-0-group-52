package controller;

import database.UserRepository;
import model.App;
import model.User;

public class SaveService {

    private final UserRepository userRepo = new UserRepository();

    public void persist() {
        persist(App.getInstance().getLoggedInUser());
    }

    public void persist(User user) {
        if (user == null || user.getUsername() == null) {
            return;
        }
        userRepo.updateStats(user);
    }
}
