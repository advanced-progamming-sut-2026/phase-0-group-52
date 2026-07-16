CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    email TEXT,
    password_hash TEXT,
    gender TEXT,
    nickname TEXT,
    security_question INTEGER,
    answer TEXT,
    coins INTEGER NOT NULL DEFAULT 0,
    gems INTEGER NOT NULL DEFAULT 0,
    seed_packet INTEGER NOT NULL DEFAULT 0,
    plant_food_num INTEGER NOT NULL DEFAULT 0,
    most_meow_point INTEGER NOT NULL DEFAULT 0,
    max_point INTEGER NOT NULL DEFAULT 0,
    games_played INTEGER NOT NULL DEFAULT 0,
    mini_games_played INTEGER NOT NULL DEFAULT 0,
    last_won_game TEXT,
    difficulty_level INTEGER NOT NULL DEFAULT 3,
    stay_logged_in INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS user_progress (
    user_id INTEGER NOT NULL,
    chapter_index INTEGER NOT NULL DEFAULT 1,
    level_index INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
