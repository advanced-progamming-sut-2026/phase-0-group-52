package model;

import model.level.Level;
import model.news.NewsList;

import java.util.HashMap;
import java.util.Map;

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
    private int difficultyLevel;
    private NewsList newsList;
    private  String passwordHash;
    private int securityQuestion;
    private  String securityAnswerHash;
    private boolean stayLoggedIn;


    private int id;
    private int mostMeowPoint;
    private int gamesPlayed;
    private int coins;
    private int gems;
    private Map<Level, Boolean> levels = new HashMap<>();
    private String lastWonGame;
    private int miniGamesPlayed;
    private int maxPoint;
    private int questDailyNum;
    private int questNonDailyNum;
    private int seedPacket;
    private int plantFoodNum;



    /** سازنده‌ی بدون‌آرگومان؛ برای بازسازیِ کاربر از فایلِ ذخیره‌سازی (JSON) با setterها. */
    public User() {
    }

    public User(String username, String passwordHash, String nickname, String email,
                String gender, int securityQuestion, String securityAnswerHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;
        this.securityQuestion = securityQuestion;
        this.securityAnswerHash = securityAnswerHash;
        this.difficultyLevel = 3;
    }

    public int getPlantFoodNum() {
        return plantFoodNum;
    }

    public void setPlantFoodNum(int plantFoodNum) {
        this.plantFoodNum = plantFoodNum;
    }

    public int getSeedPacket() {
        return seedPacket;
    }

    public void setSeedPacket(int seedPacket) {
        this.seedPacket = seedPacket;
    }

    public int getQuestNonDailyNum() {
        return questNonDailyNum;
    }

    public void setQuestNonDailyNum(int questNonDailyNum) {
        this.questNonDailyNum = questNonDailyNum;
    }

    public int getQuestDailyNum() {
        return questDailyNum;
    }

    public void setQuestDailyNum(int questDailyNum) {
        this.questDailyNum = questDailyNum;
    }

    public int getMaxPoint() {
        return maxPoint;
    }

    public void setMaxPoint(int maxPoint) {
        this.maxPoint = maxPoint;
    }

    public int getMiniGamesPlayed() {
        return miniGamesPlayed;
    }

    public void setMiniGamesPlayed(int miniGamesPlayed) {
        this.miniGamesPlayed = miniGamesPlayed;
    }

    public String getLastWonGame() {
        return lastWonGame;
    }

    public void setLastWonGame(String lastWonGame) {
        this.lastWonGame = lastWonGame;
    }

    public Map<Level, Boolean> getLevels() {
        return levels;
    }

    public void setLevels(Map<Level, Boolean> levels) {
        this.levels = levels;
    }

    public int getGems() {
        return gems;
    }

    public void setGems(int gems) {
        this.gems = gems;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getMostMeowPoint() {
        return mostMeowPoint;
    }

    public void setMostMeowPoint(int mostMeowPoint) {
        this.mostMeowPoint = mostMeowPoint;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSecurityAnswerHash() {
        return securityAnswerHash;
    }

    public void setSecurityAnswerHash(String securityAnswerHash) {
        this.securityAnswerHash = securityAnswerHash;
    }

    public int getSecurityQuestion() {
        return securityQuestion;
    }

    public boolean isStayLoggedIn() {
        return stayLoggedIn;
    }

    public void setStayLoggedIn(boolean stayLoggedIn) {
        this.stayLoggedIn = stayLoggedIn;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public NewsList getNewsList() {
        return newsList;
    }

    public void setNewsList(NewsList newsList) {
        this.newsList = newsList;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
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

    public void setSecurityQuestion(int securityQuestion) {
        this.securityQuestion = securityQuestion;
    }


}
