package pvz.model;

public class User {
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String gender;
    private boolean isLogged;
    private int coinBalance;
    private int diamondBalance;
    private int lastChapter;
    private int lastLevel;
    private int minigamesFinished;
    private int dailyQuestCount;
    private int otherQuestCount;
    private int highScore;
    private Collection collection;


    public User(String username, String password, String nickname, String email, String gender, boolean isLogged, int coinBalance,
                int diamondBalance, int lastChapter, int lastLevel, int minigamesFinished,
                int dailyQuestCount, int otherQuestCount, int highScore, Collection collection) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;
        this.isLogged = isLogged;
        this.coinBalance = coinBalance;
        this.diamondBalance = diamondBalance;
        this.lastChapter = lastChapter;
        this.lastLevel = lastLevel;
        this.minigamesFinished = minigamesFinished;
        this.dailyQuestCount = dailyQuestCount;
        this.otherQuestCount = otherQuestCount;
        this.highScore = highScore;
        this.collection = collection;
    }

    public int getHighScore() {
        return highScore;
    }

    public void setHighScore(int highScore) {
        this.highScore = highScore;
    }

    public int getOtherQuestCount() {
        return otherQuestCount;
    }

    public void setOtherQuestCount(int otherQuestCount) {
        this.otherQuestCount = otherQuestCount;
    }

    public int getDailyQuestCount() {
        return dailyQuestCount;
    }

    public void setDailyQuestCount(int dailyQuestCount) {
        this.dailyQuestCount = dailyQuestCount;
    }

    public int getMinigamesFinished() {
        return minigamesFinished;
    }

    public void setMinigamesFinished(int minigamesFinished) {
        this.minigamesFinished = minigamesFinished;
    }

    public int getLastLevel() {
        return lastLevel;
    }

    public void setLastLevel(int lastLevel) {
        this.lastLevel = lastLevel;
    }

    public int getLastChapter() {
        return lastChapter;
    }

    public void setLastChapter(int lastChapter) {
        this.lastChapter = lastChapter;
    }

    public int getDiamondBalance() {
        return diamondBalance;
    }

    public void setDiamondBalance(int diamondBalance) {
        this.diamondBalance = diamondBalance;
    }

    public int getCoinBalance() {
        return coinBalance;
    }

    public void setCoinBalance(int coinBalance) {
        this.coinBalance = coinBalance;
    }

    public boolean isLogged() {
        return isLogged;
    }

    public void setLogged(boolean logged) {
        isLogged = logged;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
