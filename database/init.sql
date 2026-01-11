-- Sports Match Tracker Database Schema
-- SQLite initialization script

PRAGMA foreign_keys = ON;

-- Role table (tworzymy najpierw, bo uzytkownik od niej zależy)
CREATE TABLE IF NOT EXISTS roles (
    name TEXT PRIMARY KEY CHECK(name IN ('user', 'admin'))
);

-- User table with role (Użytkownik ma rolę - związek N:1)
CREATE TABLE IF NOT EXISTS users (
    email TEXT PRIMARY KEY,
    nick TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    roles_name TEXT NOT NULL,
    FOREIGN KEY (roles_name) REFERENCES roles(name) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Address table
CREATE TABLE IF NOT EXISTS adresses (
    street TEXT,
    number INTEGER,
    city TEXT,
    PRIMARY KEY (street, number, city)
);

-- Facility/Venue table
CREATE TABLE IF NOT EXISTS buildings (
    name TEXT PRIMARY KEY,
    street TEXT NOT NULL,
    number INTEGER NOT NULL,
    city TEXT NOT NULL,
    FOREIGN KEY (street, number, city) REFERENCES adresses(street, number, city) ON UPDATE CASCADE
);

-- Sport table
CREATE TABLE IF NOT EXISTS sports (
    name TEXT PRIMARY KEY
);

-- League table (Liga należy do Sport - związek N:1)
CREATE TABLE IF NOT EXISTS leagues (
    name TEXT,
    country TEXT,
    sports_name TEXT NOT NULL,
    PRIMARY KEY (name, country),
    FOREIGN KEY (sports_name) REFERENCES sports(name) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Team table
CREATE TABLE IF NOT EXISTS teams (
    name TEXT PRIMARY KEY,
    city TEXT NOT NULL,
    league_name TEXT NOT NULL,
    league_country TEXT NOT NULL,
    FOREIGN KEY (league_name, league_country) REFERENCES leagues(name, country) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Subskrypcje lig
CREATE TABLE IF NOT EXISTS league_subscriptions (
    subscriber_email TEXT,
    league_name TEXT,
    league_country TEXT,
    PRIMARY KEY (subscriber_email, league_name, league_country),
    FOREIGN KEY (subscriber_email) REFERENCES users(email) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (league_name, league_country) REFERENCES leagues(name, country) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Subskrypcje drużyn
CREATE TABLE IF NOT EXISTS team_subscriptions (
    subscriber_email TEXT,
    team_name TEXT,
    PRIMARY KEY (subscriber_email, team_name),
    FOREIGN KEY (subscriber_email) REFERENCES users(email) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (team_name) REFERENCES teams(name) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Season table
CREATE TABLE IF NOT EXISTS seasons (
    date_start DATE,
    date_end DATE,
    league_name TEXT NOT NULL,
    league_country TEXT NOT NULL,
    PRIMARY KEY (date_start, date_end, league_name, league_country),
    FOREIGN KEY (league_name, league_country) REFERENCES leagues(name, country) ON DELETE CASCADE ON UPDATE CASCADE,
    CHECK(date_start < date_end)
);

-- Match table
CREATE TABLE IF NOT EXISTS matches (
    start_date DATETIME,
    home_score INTEGER NOT NULL,
    away_score INTEGER NOT NULL,
    season_start_date DATE,
    season_end_date DATE,
    season_league_name TEXT,
    season_league_country TEXT,
    building_name TEXT NOT NULL,
    home_team_name TEXT NOT NULL,
    away_team_name TEXT NOT NULL,
    PRIMARY KEY (start_date, season_start_date, season_end_date, season_league_name, season_league_country),
    FOREIGN KEY (season_start_date, season_end_date, season_league_name, season_league_country) 
        REFERENCES seasons(date_start, date_end, league_name, league_country) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (building_name) REFERENCES buildings(name) ON UPDATE CASCADE,
    FOREIGN KEY (home_team_name) REFERENCES teams(name) ON UPDATE CASCADE,
    FOREIGN KEY (away_team_name) REFERENCES teams(name) ON UPDATE CASCADE
);

-- Match Event table
CREATE TABLE IF NOT EXISTS match_events (
    game_time INTEGER,
    event Text,
    match_start_date DATETIME,
    match_season_start_date DATE,
    match_season_end_date DATE,
    match_season_league_name TEXT,
    match_season_league_country TEXT,
    PRIMARY KEY (game_time, match_start_date, match_season_start_date, match_season_end_date, match_season_league_name, match_season_league_country),
    FOREIGN KEY (match_start_date, match_season_start_date, match_season_end_date, match_season_league_name, match_season_league_country) 
        REFERENCES matches(start_date, season_start_date, season_end_date, season_league_name, season_league_country) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Table (League Standing) - POPRAWIONE: dodano identyfikator i poprawiono związki
-- Tabela reprezentuje pozycję drużyny w lidze w danym sezonie
CREATE TABLE IF NOT EXISTS tables (
    season_end_date DATE,
    season_start_date DATE,
    season_league_name TEXT,
    season_league_country TEXT,
    PRIMARY KEY (season_start_date, season_end_date, season_league_name, season_league_country),
    FOREIGN KEY (season_start_date, season_end_date, season_league_name, season_league_country) 
        REFERENCES seasons(date_start, date_end, league_name, league_country) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS positions_in_table (
    table_season_start_date DATE,
    table_season_end_date DATE,
    table_season_league_name TEXT,
    table_season_league_country TEXT,
    team_name TEXT,
    draws INTEGER DEFAULT 0,
    losses INTEGER DEFAULT 0,
    wins INTEGER DEFAULT 0,
    matches_played INTEGER DEFAULT 0,
    points INTEGER DEFAULT 0,
    PRIMARY KEY (table_season_start_date, table_season_end_date, table_season_league_name, table_season_league_country, team_name),
    FOREIGN KEY (table_season_start_date, table_season_end_date, table_season_league_name, table_season_league_country) 
        REFERENCES tables(season_start_date, season_end_date, season_league_name, season_league_country) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (team_name) REFERENCES teams(name) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Sample data for testing

-- Sample roles
INSERT OR IGNORE INTO roles (name) VALUES 
    ('admin'),
    ('user');

INSERT OR IGNORE INTO users (email, nick, password, roles_name) VALUES 
    ('test@example.com', 'testUser', 'test123', 'user'),
    ('admin@example.com', 'testAdmin', 'admin123', 'admin');

INSERT OR IGNORE INTO roles (name) VALUES 
    ('admin'),
    ('user'),
    ('moderator');

-- Sample sports
INSERT OR IGNORE INTO sports (name) VALUES 
    ('Football'),
    ('Basketball'),
    ('Volleyball');

-- Sample addresses
INSERT OR IGNORE INTO adresses (street, number, city) VALUES 
    ('Wembley Way', 1, 'London'),
    ('Connaught Road', 7, 'London'),
    ('Sir Matt Busby Way', 1, 'Manchester'),
    ('Anfield Road', 1, 'Liverpool'),
    ('Ashburton Grove', 1, 'London'),
    ('Stamford Bridge', 1, 'London'),
    ('Madison Square', 4, 'New York'),
    ('Staples Center Drive', 1111, 'Los Angeles'),
    ('TD Garden Place', 100, 'Boston'),
    ('United Center Drive', 1901, 'Chicago'),
    ('Łazienkowska', 3, 'Warsaw'),
    ('Bułgarska', 17, 'Poznań');

-- Sample buildings/venues
INSERT OR IGNORE INTO buildings (name, street, number, city) VALUES 
    ('Wembley Stadium', 'Wembley Way', 1, 'London'),
    ('Emirates Stadium', 'Ashburton Grove', 1, 'London'),
    ('Old Trafford', 'Sir Matt Busby Way', 1, 'Manchester'),
    ('Anfield', 'Anfield Road', 1, 'Liverpool'),
    ('Stamford Bridge', 'Stamford Bridge', 1, 'London'),
    ('Madison Square Garden', 'Madison Square', 4, 'New York'),
    ('Staples Center', 'Staples Center Drive', 1111, 'Los Angeles'),
    ('TD Garden', 'TD Garden Place', 100, 'Boston'),
    ('United Center', 'United Center Drive', 1901, 'Chicago'),
    ('Stadion Narodowy', 'Łazienkowska', 3, 'Warsaw'),
    ('INEA Stadion', 'Bułgarska', 17, 'Poznań');

-- Sample leagues
INSERT OR IGNORE INTO leagues (name, country, sports_name) VALUES 
    -- Football
    ('Premier League', 'England', 'Football'),
    ('La Liga', 'Spain', 'Football'),
    ('Ekstraklasa', 'Poland', 'Football'),
    -- Basketball
    ('NBA', 'USA', 'Basketball'),
    ('EuroLeague', 'Europe', 'Basketball'),
    -- Volleyball
    ('PlusLiga', 'Poland', 'Volleyball'),
    ('Serie A', 'Italy', 'Volleyball');

-- Sample teams - Football
INSERT OR IGNORE INTO teams (name, city, league_name, league_country) VALUES 
    -- Premier League
    ('Arsenal', 'London', 'Premier League', 'England'),
    ('Chelsea', 'London', 'Premier League', 'England'),
    ('Manchester United', 'Manchester', 'Premier League', 'England'),
    ('Liverpool', 'Liverpool', 'Premier League', 'England'),
    ('Manchester City', 'Manchester', 'Premier League', 'England'),
    ('Tottenham', 'London', 'Premier League', 'England'),
    -- La Liga
    ('Real Madrid', 'Madrid', 'La Liga', 'Spain'),
    ('Barcelona', 'Barcelona', 'La Liga', 'Spain'),
    ('Atletico Madrid', 'Madrid', 'La Liga', 'Spain'),
    ('Sevilla', 'Sevilla', 'La Liga', 'Spain'),
    -- Ekstraklasa
    ('Legia Warszawa', 'Warsaw', 'Ekstraklasa', 'Poland'),
    ('Lech Poznań', 'Poznań', 'Ekstraklasa', 'Poland'),
    ('Wisła Kraków', 'Kraków', 'Ekstraklasa', 'Poland'),
    ('Pogoń Szczecin', 'Szczecin', 'Ekstraklasa', 'Poland'),
    -- NBA
    ('Lakers', 'Los Angeles', 'NBA', 'USA'),
    ('Celtics', 'Boston', 'NBA', 'USA'),
    ('Bulls', 'Chicago', 'NBA', 'USA'),
    ('Warriors', 'San Francisco', 'NBA', 'USA'),
    ('Knicks', 'New York', 'NBA', 'USA'),
    -- EuroLeague
    ('Real Madrid Basketball', 'Madrid', 'EuroLeague', 'Europe'),
    ('Barcelona Basketball', 'Barcelona', 'EuroLeague', 'Europe'),
    ('Olympiacos', 'Athens', 'EuroLeague', 'Europe'),
    -- PlusLiga - 
    ('ZAKSA Kędzierzyn-Koźle', 'Kędzierzyn-Koźle', 'PlusLiga', 'Poland'),
    ('Jastrzębski Węgiel', 'Jastrzębie-Zdrój', 'PlusLiga', 'Poland'),
    ('Projekt Warszawa', 'Warsaw', 'PlusLiga', 'Poland'),
    ('Trefl Gdańsk', 'Gdańsk', 'PlusLiga', 'Poland'),
    ('Asseco Resovia', 'Rzeszów', 'PlusLiga', 'Poland'),
    -- Serie A
    ('Perugia Volley', 'Perugia', 'Serie A', 'Italy'),
    ('Lube Civitanova', 'Civitanova', 'Serie A', 'Italy'),
    ('Modena Volley', 'Modena', 'Serie A', 'Italy');

-- Sample seasons (zakończone i trwające)
INSERT OR IGNORE INTO seasons (date_start, date_end, league_name, league_country) VALUES 
    -- Premier League
    ('2024-08-15', '2025-05-25', 'Premier League', 'England'),  -- zakończony
    ('2025-08-16', '2026-05-24', 'Premier League', 'England'),  -- trwający
    -- La Liga
    ('2024-08-20', '2025-05-30', 'La Liga', 'Spain'),
    ('2025-08-18', '2026-05-28', 'La Liga', 'Spain'),
    -- Ekstraklasa
    ('2024-07-20', '2025-05-15', 'Ekstraklasa', 'Poland'),
    ('2025-07-18', '2026-05-20', 'Ekstraklasa', 'Poland'),
    -- NBA
    ('2024-10-22', '2025-04-13', 'NBA', 'USA'),
    ('2025-10-20', '2026-04-15', 'NBA', 'USA'),
    -- EuroLeague
    ('2024-10-03', '2025-05-25', 'EuroLeague', 'Europe'),
    ('2025-10-02', '2026-05-24', 'EuroLeague', 'Europe'),
    -- PlusLiga
    ('2024-09-20', '2025-04-30', 'PlusLiga', 'Poland'),
    ('2025-09-19', '2026-04-29', 'PlusLiga', 'Poland'),
    -- Serie A
    ('2024-10-10', '2025-05-10', 'Serie A', 'Italy'),
    ('2025-10-09', '2026-05-09', 'Serie A', 'Italy');

-- Sample tables (po jednej dla każdego sezonu)
INSERT OR IGNORE INTO tables (season_start_date, season_end_date, season_league_name, season_league_country) VALUES 
    ('2024-08-15', '2025-05-25', 'Premier League', 'England'),
    ('2025-08-16', '2026-05-24', 'Premier League', 'England'),
    ('2024-08-20', '2025-05-30', 'La Liga', 'Spain'),
    ('2025-08-18', '2026-05-28', 'La Liga', 'Spain'),
    ('2024-07-20', '2025-05-15', 'Ekstraklasa', 'Poland'),
    ('2025-07-18', '2026-05-20', 'Ekstraklasa', 'Poland'),
    ('2024-10-22', '2025-04-13', 'NBA', 'USA'),
    ('2025-10-20', '2026-04-15', 'NBA', 'USA'),
    ('2024-10-03', '2025-05-25', 'EuroLeague', 'Europe'),
    ('2025-10-02', '2026-05-24', 'EuroLeague', 'Europe'),
    ('2024-09-20', '2025-04-30', 'PlusLiga', 'Poland'),
    ('2025-09-19', '2026-04-29', 'PlusLiga', 'Poland'),
    ('2024-10-10', '2025-05-10', 'Serie A', 'Italy'),
    ('2025-10-09', '2026-05-09', 'Serie A', 'Italy');

-- Sample matches - Premier League (zakończony sezon 2024/2025)
INSERT OR IGNORE INTO matches (start_date, home_score, away_score, season_start_date, season_end_date, season_league_name, season_league_country, building_name, home_team_name, away_team_name) VALUES 
    ('2024-08-16 15:00:00', 2, 1, '2024-08-15', '2025-05-25', 'Premier League', 'England', 'Emirates Stadium', 'Arsenal', 'Chelsea'),
    ('2024-09-14 17:30:00', 3, 1, '2024-08-15', '2025-05-25', 'Premier League', 'England', 'Old Trafford', 'Manchester United', 'Liverpool'),
    ('2024-10-05 20:00:00', 1, 1, '2024-08-15', '2025-05-25', 'Premier League', 'England', 'Stamford Bridge', 'Chelsea', 'Arsenal'),
    ('2024-11-10 15:00:00', 2, 0, '2024-08-15', '2025-05-25', 'Premier League', 'England', 'Anfield', 'Liverpool', 'Manchester City'),
    ('2024-12-26 12:30:00', 4, 2, '2024-08-15', '2025-05-25', 'Premier League', 'England', 'Emirates Stadium', 'Arsenal', 'Manchester United');
-- Sample matches - Premier League (trwający sezon 2025/2026)
INSERT OR IGNORE INTO matches (start_date, home_score, away_score, season_start_date, season_end_date, season_league_name, season_league_country, building_name, home_team_name, away_team_name) VALUES 
    ('2025-08-17 15:00:00', 3, 0, '2025-08-16', '2026-05-24', 'Premier League', 'England', 'Emirates Stadium', 'Arsenal', 'Liverpool'),
    ('2025-09-21 17:30:00', 2, 2, '2025-08-16', '2026-05-24', 'Premier League', 'England', 'Old Trafford', 'Manchester United', 'Chelsea'),
    ('2025-12-28 15:00:00', 1, 0, '2025-08-16', '2026-05-24', 'Premier League', 'England', 'Anfield', 'Liverpool', 'Arsenal'),
    ('2026-02-15 20:00:00', 0, 0, '2025-08-16', '2026-05-24', 'Premier League', 'England', 'Stamford Bridge', 'Chelsea', 'Manchester City'),
    ('2026-03-22 15:00:00', 0, 0, '2025-08-16', '2026-05-24', 'Premier League', 'England', 'Emirates Stadium', 'Arsenal', 'Tottenham');

-- Sample matches - Ekstraklasa (zakończony sezon)
INSERT OR IGNORE INTO matches (start_date, home_score, away_score, season_start_date, season_end_date, season_league_name, season_league_country, building_name, home_team_name, away_team_name) VALUES 
    ('2024-07-21 18:00:00', 2, 1, '2024-07-20', '2025-05-15', 'Ekstraklasa', 'Poland', 'Stadion Narodowy', 'Legia Warszawa', 'Lech Poznań'),
    ('2024-08-25 20:30:00', 1, 1, '2024-07-20', '2025-05-15', 'Ekstraklasa', 'Poland', 'INEA Stadion', 'Lech Poznań', 'Wisła Kraków'),
    ('2024-10-06 17:00:00', 3, 0, '2024-07-20', '2025-05-15', 'Ekstraklasa', 'Poland', 'Stadion Narodowy', 'Legia Warszawa', 'Pogoń Szczecin'),
    ('2024-11-24 14:30:00', 0, 2, '2024-07-20', '2025-05-15', 'Ekstraklasa', 'Poland', 'INEA Stadion', 'Lech Poznań', 'Legia Warszawa');

-- Sample matches - Ekstraklasa (trwający sezon)
INSERT OR IGNORE INTO matches (start_date, home_score, away_score, season_start_date, season_end_date, season_league_name, season_league_country, building_name, home_team_name, away_team_name) VALUES 
    ('2025-07-19 18:00:00', 1, 0, '2025-07-18', '2026-05-20', 'Ekstraklasa', 'Poland', 'Stadion Narodowy', 'Legia Warszawa', 'Wisła Kraków'),
    ('2025-09-14 20:30:00', 2, 2, '2025-07-18', '2026-05-20', 'Ekstraklasa', 'Poland', 'INEA Stadion', 'Lech Poznań', 'Pogoń Szczecin'),
    ('2026-02-08 17:00:00', 0, 0, '2025-07-18', '2026-05-20', 'Ekstraklasa', 'Poland', 'Stadion Narodowy', 'Legia Warszawa', 'Lech Poznań'),
    ('2026-01-08 22:50:00', 0, 0, '2025-07-18', '2026-05-20', 'Ekstraklasa', 'Poland', 'INEA Stadion', 'Lech Poznań', 'Wisła Kraków');

-- Sample matches - NBA (zakończony sezon)
INSERT OR IGNORE INTO matches (start_date, home_score, away_score, season_start_date, season_end_date, season_league_name, season_league_country, building_name, home_team_name, away_team_name) VALUES 
    ('2024-10-23 19:30:00', 112, 108, '2024-10-22', '2025-04-13', 'NBA', 'USA', 'Staples Center', 'Lakers', 'Celtics'),
    ('2024-11-15 20:00:00', 105, 98, '2024-10-22', '2025-04-13', 'NBA', 'USA', 'TD Garden', 'Celtics', 'Bulls'),
    ('2024-12-25 17:00:00', 118, 115, '2024-10-22', '2025-04-13', 'NBA', 'USA', 'Madison Square Garden', 'Knicks', 'Lakers'),
    ('2025-01-20 19:00:00', 102, 99, '2024-10-22', '2025-04-13', 'NBA', 'USA', 'United Center', 'Bulls', 'Warriors');

-- Sample matches - NBA (trwający sezon)
INSERT OR IGNORE INTO matches (start_date, home_score, away_score, season_start_date, season_end_date, season_league_name, season_league_country, building_name, home_team_name, away_team_name) VALUES 
    ('2025-10-21 19:30:00', 110, 105, '2025-10-20', '2026-04-15', 'NBA', 'USA', 'Staples Center', 'Lakers', 'Warriors'),
    ('2025-11-28 20:00:00', 98, 95, '2025-10-20', '2026-04-15', 'NBA', 'USA', 'TD Garden', 'Celtics', 'Knicks'),
    ('2025-12-25 17:00:00', 112, 108, '2025-10-20', '2026-04-15', 'NBA', 'USA', 'Madison Square Garden', 'Knicks', 'Lakers'),
    ('2026-02-14 19:00:00', 0, 0, '2025-10-20', '2026-04-15', 'NBA', 'USA', 'United Center', 'Bulls', 'Celtics'),
    ('2026-03-10 20:30:00', 0, 0, '2025-10-20', '2026-04-15', 'NBA', 'USA', 'Staples Center', 'Lakers', 'Bulls');
-- Sample matches - PlusLiga (zakończony sezon)
INSERT OR IGNORE INTO matches (start_date, home_score, away_score, season_start_date, season_end_date, season_league_name, season_league_country, building_name, home_team_name, away_team_name) VALUES 
    ('2024-09-21 18:00:00', 3, 1, '2024-09-20', '2025-04-30', 'PlusLiga', 'Poland', 'Stadion Narodowy', 'Projekt Warszawa', 'ZAKSA Kędzierzyn-Koźle'),
    ('2024-10-19 17:30:00', 3, 2, '2024-09-20', '2025-04-30', 'PlusLiga', 'Poland', 'Stadion Narodowy', 'Projekt Warszawa', 'Jastrzębski Węgiel'),
    ('2024-12-14 19:00:00', 2, 3, '2024-09-20', '2025-04-30', 'PlusLiga', 'Poland', 'Stadion Narodowy', 'Projekt Warszawa', 'Trefl Gdańsk');

-- Sample matches - PlusLiga (trwający sezon)
INSERT OR IGNORE INTO matches (start_date, home_score, away_score, season_start_date, season_end_date, season_league_name, season_league_country, building_name, home_team_name, away_team_name) VALUES 
    ('2025-09-20 18:00:00', 3, 0, '2025-09-19', '2026-04-29', 'PlusLiga', 'Poland', 'Stadion Narodowy', 'Projekt Warszawa', 'Asseco Resovia'),
    ('2025-11-23 17:30:00', 3, 2, '2025-09-19', '2026-04-29', 'PlusLiga', 'Poland', 'Stadion Narodowy', 'Projekt Warszawa', 'ZAKSA Kędzierzyn-Koźle'),
    ('2026-01-18 19:00:00', 0, 0, '2025-09-19', '2026-04-29', 'PlusLiga', 'Poland', 'Stadion Narodowy', 'Projekt Warszawa', 'Jastrzębski Węgiel'),
    ('2026-03-07 18:30:00', 0, 0, '2025-09-19', '2026-04-29', 'PlusLiga', 'Poland', 'Stadion Narodowy', 'Projekt Warszawa', 'Trefl Gdańsk');

-- Sample match events (tylko dla niektórych meczów)
-- Arsenal vs Chelsea (2024-08-16) - gole
INSERT OR IGNORE INTO match_events (game_time, event, match_start_date, match_season_start_date, match_season_end_date, match_season_league_name, match_season_league_country) VALUES 
    (23, 'Gol - Arsenal (Saka)', '2024-08-16 15:00:00', '2024-08-15', '2025-05-25', 'Premier League', 'England'),
    (45, 'Gol - Chelsea (Sterling)', '2024-08-16 15:00:00', '2024-08-15', '2025-05-25', 'Premier League', 'England'),
    (78, 'Gol - Arsenal (Martinelli)', '2024-08-16 15:00:00', '2024-08-15', '2025-05-25', 'Premier League', 'England');

-- Manchester United vs Liverpool (2024-09-14) - gole
INSERT OR IGNORE INTO match_events (game_time, event, match_start_date, match_season_start_date, match_season_end_date, match_season_league_name, match_season_league_country) VALUES 
    (12, 'Gol - Manchester United (Rashford)', '2024-09-14 17:30:00', '2024-08-15', '2025-05-25', 'Premier League', 'England'),
    (34, 'Gol - Manchester United (Fernandes)', '2024-09-14 17:30:00', '2024-08-15', '2025-05-25', 'Premier League', 'England'),
    (56, 'Gol - Liverpool (Salah)', '2024-09-14 17:30:00', '2024-08-15', '2025-05-25', 'Premier League', 'England'),
    (89, 'Gol - Manchester United (Hojlund)', '2024-09-14 17:30:00', '2024-08-15', '2025-05-25', 'Premier League', 'England');

-- Lakers vs Celtics (2024-10-23) - kosze
INSERT OR IGNORE INTO match_events (game_time, event, match_start_date, match_season_start_date, match_season_end_date, match_season_league_name, match_season_league_country) VALUES 
    (5, 'Kosz 3pkt - Lakers (LeBron James)', '2024-10-23 19:30:00', '2024-10-22', '2025-04-13', 'NBA', 'USA'),
    (12, 'Kosz 2pkt - Celtics (Jayson Tatum)', '2024-10-23 19:30:00', '2024-10-22', '2025-04-13', 'NBA', 'USA'),
    (28, 'Kosz 3pkt - Lakers (Anthony Davis)', '2024-10-23 19:30:00', '2024-10-22', '2025-04-13', 'NBA', 'USA');

-- Legia vs Lech (2024-07-21) - gole
INSERT OR IGNORE INTO match_events (game_time, event, match_start_date, match_season_start_date, match_season_end_date, match_season_league_name, match_season_league_country) VALUES 
    (15, 'Gol - Legia Warszawa (Josue)', '2024-07-21 18:00:00', '2024-07-20', '2025-05-15', 'Ekstraklasa', 'Poland'),
    (67, 'Gol - Lech Poznań (Ishak)', '2024-07-21 18:00:00', '2024-07-20', '2025-05-15', 'Ekstraklasa', 'Poland'),
    (82, 'Gol - Legia Warszawa (Pekhart)', '2024-07-21 18:00:00', '2024-07-20', '2025-05-15', 'Ekstraklasa', 'Poland');

-- Sample positions in table - Premier League 2024/2025 (zakończony sezon) - POPRAWIONE: usunięto team_city i team_sports_name
INSERT OR IGNORE INTO positions_in_table (table_season_start_date, table_season_end_date, table_season_league_name, table_season_league_country, team_name, draws, losses, wins, matches_played, points) VALUES 
    ('2024-08-15', '2025-05-25', 'Premier League', 'England', 'Arsenal', 8, 4, 26, 38, 86),
    ('2024-08-15', '2025-05-25', 'Premier League', 'England', 'Manchester City', 6, 5, 27, 38, 87),
    ('2024-08-15', '2025-05-25', 'Premier League', 'England', 'Liverpool', 10, 6, 22, 38, 76),
    ('2024-08-15', '2025-05-25', 'Premier League', 'England', 'Manchester United', 7, 12, 19, 38, 64),
    ('2024-08-15', '2025-05-25', 'Premier League', 'England', 'Chelsea', 9, 13, 16, 38, 57),
    ('2024-08-15', '2025-05-25', 'Premier League', 'England', 'Tottenham', 8, 14, 16, 38, 56);

-- Sample positions in table - Premier League 2025/2026 (trwający sezon)
INSERT OR IGNORE INTO positions_in_table (table_season_start_date, table_season_end_date, table_season_league_name, table_season_league_country, team_name, draws, losses, wins, matches_played, points) VALUES 
    ('2025-08-16', '2026-05-24', 'Premier League', 'England', 'Arsenal', 2, 1, 12, 15, 38),
    ('2025-08-16', '2026-05-24', 'Premier League', 'England', 'Manchester City', 3, 2, 10, 15, 33),
    ('2025-08-16', '2026-05-24', 'Premier League', 'England', 'Liverpool', 4, 3, 8, 15, 28),
    ('2025-08-16', '2026-05-24', 'Premier League', 'England', 'Manchester United', 3, 5, 7, 15, 24),
    ('2025-08-16', '2026-05-24', 'Premier League', 'England', 'Chelsea', 5, 4, 6, 15, 23),
    ('2025-08-16', '2026-05-24', 'Premier League', 'England', 'Tottenham', 2, 7, 6, 15, 20);

-- Sample positions in table - Ekstraklasa 2024/2025 (zakończony sezon)
INSERT OR IGNORE INTO positions_in_table (table_season_start_date, table_season_end_date, table_season_league_name, table_season_league_country, team_name, draws, losses, wins, matches_played, points) VALUES 
    ('2024-07-20', '2025-05-15', 'Ekstraklasa', 'Poland', 'Legia Warszawa', 5, 3, 22, 30, 71),
    ('2024-07-20', '2025-05-15', 'Ekstraklasa', 'Poland', 'Lech Poznań', 6, 4, 20, 30, 66),
    ('2024-07-20', '2025-05-15', 'Ekstraklasa', 'Poland', 'Wisła Kraków', 8, 10, 12, 30, 44),
    ('2024-07-20', '2025-05-15', 'Ekstraklasa', 'Poland', 'Pogoń Szczecin', 7, 11, 12, 30, 43);

-- Sample positions in table - Ekstraklasa 2025/2026 (trwający sezon)
INSERT OR IGNORE INTO positions_in_table (table_season_start_date, table_season_end_date, table_season_league_name, table_season_league_country, team_name, draws, losses, wins, matches_played, points) VALUES 
    ('2025-07-18', '2026-05-20', 'Ekstraklasa', 'Poland', 'Legia Warszawa', 2, 1, 9, 12, 29),
    ('2025-07-18', '2026-05-20', 'Ekstraklasa', 'Poland', 'Lech Poznań', 3, 2, 7, 12, 24),
    ('2025-07-18', '2026-05-20', 'Ekstraklasa', 'Poland', 'Wisła Kraków', 4, 4, 4, 12, 16),
    ('2025-07-18', '2026-05-20', 'Ekstraklasa', 'Poland', 'Pogoń Szczecin', 3, 5, 4, 12, 15);

-- Sample positions in table - NBA 2024/2025 (zakończony sezon)
INSERT OR IGNORE INTO positions_in_table (table_season_start_date, table_season_end_date, table_season_league_name, table_season_league_country, team_name, draws, losses, wins, matches_played, points) VALUES 
    ('2024-10-22', '2025-04-13', 'NBA', 'USA', 'Celtics', 0, 18, 64, 82, 128),
    ('2024-10-22', '2025-04-13', 'NBA', 'USA', 'Lakers', 0, 29, 53, 82, 106),
    ('2024-10-22', '2025-04-13', 'NBA', 'USA', 'Warriors', 0, 36, 46, 82, 92),
    ('2024-10-22', '2025-04-13', 'NBA', 'USA', 'Bulls', 0, 43, 39, 82, 78),
    ('2024-10-22', '2025-04-13', 'NBA', 'USA', 'Knicks', 0, 32, 50, 82, 100);

-- Sample positions in table - NBA 2025/2026 (trwający sezon)
INSERT OR IGNORE INTO positions_in_table (table_season_start_date, table_season_end_date, table_season_league_name, table_season_league_country, team_name, draws, losses, wins, matches_played, points) VALUES 
    ('2025-10-20', '2026-04-15', 'NBA', 'USA', 'Celtics', 0, 8, 22, 30, 44),
    ('2025-10-20', '2026-04-15', 'NBA', 'USA', 'Lakers', 0, 10, 20, 30, 40),
    ('2025-10-20', '2026-04-15', 'NBA', 'USA', 'Warriors', 0, 14, 16, 30, 32),
    ('2025-10-20', '2026-04-15', 'NBA', 'USA', 'Bulls', 0, 17, 13, 30, 26),
    ('2025-10-20', '2026-04-15', 'NBA', 'USA', 'Knicks', 0, 12, 18, 30, 36);

-- Sample positions in table - PlusLiga 2024/2025 (zakończony sezon)
INSERT OR IGNORE INTO positions_in_table (table_season_start_date, table_season_end_date, table_season_league_name, table_season_league_country, team_name, draws, losses, wins, matches_played, points) VALUES 
    ('2024-09-20', '2025-04-30', 'PlusLiga', 'Poland', 'ZAKSA Kędzierzyn-Koźle', 0, 6, 24, 30, 72),
    ('2024-09-20', '2025-04-30', 'PlusLiga', 'Poland', 'Jastrzębski Węgiel', 0, 8, 22, 30, 66),
    ('2024-09-20', '2025-04-30', 'PlusLiga', 'Poland', 'Projekt Warszawa', 0, 10, 20, 30, 60),
    ('2024-09-20', '2025-04-30', 'PlusLiga', 'Poland', 'Trefl Gdańsk', 0, 14, 16, 30, 48),
    ('2024-09-20', '2025-04-30', 'PlusLiga', 'Poland', 'Asseco Resovia', 0, 18, 12, 30, 36);

-- Sample positions in table - PlusLiga 2025/2026 (trwający sezon)
INSERT OR IGNORE INTO positions_in_table (table_season_start_date, table_season_end_date, table_season_league_name, table_season_league_country, team_name, draws, losses, wins, matches_played, points) VALUES 
    ('2025-09-19', '2026-04-29', 'PlusLiga', 'Poland', 'ZAKSA Kędzierzyn-Koźle', 0, 2, 10, 12, 30),
    ('2025-09-19', '2026-04-29', 'PlusLiga', 'Poland', 'Jastrzębski Węgiel', 0, 3, 9, 12, 27),
    ('2025-09-19', '2026-04-29', 'PlusLiga', 'Poland', 'Projekt Warszawa', 0, 4, 8, 12, 24),
    ('2025-09-19', '2026-04-29', 'PlusLiga', 'Poland', 'Trefl Gdańsk', 0, 6, 6, 12, 18),
    ('2025-09-19', '2026-04-29', 'PlusLiga', 'Poland', 'Asseco Resovia', 0, 8, 4, 12, 12);

