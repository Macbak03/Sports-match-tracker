-- Sports Match Tracker Database Schema
-- SQLite initialization script

-- Role table (tworzymy najpierw, bo uzytkownik od niej zależy)
CREATE TABLE IF NOT EXISTS rola (
    nazwa TEXT PRIMARY KEY
);

-- User table with role (Użytkownik ma rolę - związek N:1)
CREATE TABLE IF NOT EXISTS uzytkownik (
    email TEXT PRIMARY KEY,
    nick TEXT NOT NULL,
    haslo TEXT NOT NULL,
    rola_nazwa TEXT NOT NULL,
    FOREIGN KEY (rola_nazwa) REFERENCES rola(nazwa) ON DELETE CASCADE
);

-- Address table
CREATE TABLE IF NOT EXISTS adres (
    ulica TEXT,
    numer INTEGER,
    miasto TEXT,
    PRIMARY KEY (ulica, numer, miasto)
);

-- Facility/Venue table
CREATE TABLE IF NOT EXISTS obiekt (
    nazwa TEXT PRIMARY KEY,
    ulica TEXT NOT NULL,
    numer INTEGER NOT NULL,
    miasto TEXT NOT NULL,
    FOREIGN KEY (ulica, numer, miasto) REFERENCES adres(ulica, numer, miasto)
);

-- Sport table
CREATE TABLE IF NOT EXISTS sport (
    nazwa TEXT PRIMARY KEY
);

-- League table (Liga należy do Sport - związek N:1)
CREATE TABLE IF NOT EXISTS liga (
    nazwa TEXT,
    kraj TEXT,
    sport_nazwa TEXT NOT NULL,
    PRIMARY KEY (nazwa, kraj),
    FOREIGN KEY (sport_nazwa) REFERENCES sport(nazwa) ON DELETE CASCADE
);

-- Team table
CREATE TABLE IF NOT EXISTS druzyna (
    nazwa TEXT,
    miasto TEXT,
    PRIMARY KEY (nazwa, miasto)
);

-- Team participates in League
CREATE TABLE IF NOT EXISTS druzyna_liga (
    druzyna_nazwa TEXT,
    druzyna_miasto TEXT,
    liga_nazwa TEXT,
    liga_kraj TEXT,
    PRIMARY KEY (druzyna_nazwa, druzyna_miasto, liga_nazwa, liga_kraj),
    FOREIGN KEY (druzyna_nazwa, druzyna_miasto) REFERENCES druzyna(nazwa, miasto) ON DELETE CASCADE,
    FOREIGN KEY (liga_nazwa, liga_kraj) REFERENCES liga(nazwa, kraj) ON DELETE CASCADE
);

-- Role table
CREATE TABLE IF NOT EXISTS rola (
    nazwa TEXT PRIMARY KEY
);


-- Subscription table - POPRAWIONE: związki identyfikujące
-- Subskrypcja jest encją słabą, zależną od użytkownika i ligi
CREATE TABLE IF NOT EXISTS subskrypcja (
    email TEXT,
    liga_nazwa TEXT,
    liga_kraj TEXT,
    typ TEXT NOT NULL,
    PRIMARY KEY (email, liga_nazwa, liga_kraj),
    FOREIGN KEY (email) REFERENCES uzytkownik(email) ON DELETE CASCADE,
    FOREIGN KEY (liga_nazwa, liga_kraj) REFERENCES liga(nazwa, kraj) ON DELETE CASCADE
);

-- Season table
CREATE TABLE IF NOT EXISTS sezon (
    data_start DATE,
    data_koniec DATE,
    liga_nazwa TEXT,
    liga_kraj TEXT,
    PRIMARY KEY (data_start, data_koniec, liga_nazwa, liga_kraj),
    FOREIGN KEY (liga_nazwa, liga_kraj) REFERENCES liga(nazwa, kraj) ON DELETE CASCADE
);

-- Match table
CREATE TABLE IF NOT EXISTS mecz (
    data_rozpoczecia DATETIME PRIMARY KEY,
    wynik_gospodarz INTEGER,
    wynik_gosc INTEGER,
    sezon_data_start DATE NOT NULL,
    sezon_data_koniec DATE NOT NULL,
    sezon_liga_nazwa TEXT NOT NULL,
    sezon_liga_kraj TEXT NOT NULL,
    obiekt_nazwa TEXT NOT NULL,
    FOREIGN KEY (sezon_data_start, sezon_data_koniec, sezon_liga_nazwa, sezon_liga_kraj) 
        REFERENCES sezon(data_start, data_koniec, liga_nazwa, liga_kraj) ON DELETE CASCADE,
    FOREIGN KEY (obiekt_nazwa) REFERENCES obiekt(nazwa) ON DELETE CASCADE
);

-- Match-Team relationships (host and guest)
CREATE TABLE IF NOT EXISTS mecz_druzyna (
    mecz_data_rozpoczecia DATETIME,
    druzyna_nazwa TEXT,
    druzyna_miasto TEXT,
    typ TEXT NOT NULL CHECK(typ IN ('gospodarz', 'gosc')),
    PRIMARY KEY (mecz_data_rozpoczecia, druzyna_nazwa, druzyna_miasto),
    FOREIGN KEY (mecz_data_rozpoczecia) REFERENCES mecz(data_rozpoczecia) ON DELETE CASCADE,
    FOREIGN KEY (druzyna_nazwa, druzyna_miasto) REFERENCES druzyna(nazwa, miasto) ON DELETE CASCADE
);

-- Match Event table
CREATE TABLE IF NOT EXISTS zdarzenie_meczowe (
    czas_gry INTEGER,
    mecz_data_rozpoczecia DATETIME,
    typ_nazwa TEXT NOT NULL,
    PRIMARY KEY (czas_gry, mecz_data_rozpoczecia),
    FOREIGN KEY (mecz_data_rozpoczecia) REFERENCES mecz(data_rozpoczecia) ON DELETE CASCADE,
    FOREIGN KEY (typ_nazwa) REFERENCES typ(nazwa) ON DELETE CASCADE
);

-- Table (League Standing) - POPRAWIONE: dodano identyfikator i poprawiono związki
-- Tabela reprezentuje pozycję drużyny w lidze w danym sezonie
CREATE TABLE IF NOT EXISTS tabela (
    druzyna_nazwa TEXT,
    druzyna_miasto TEXT,
    sezon_data_start DATE,
    sezon_data_koniec DATE,
    sezon_liga_nazwa TEXT,
    sezon_liga_kraj TEXT,
    remisy INTEGER DEFAULT 0,
    przegrane INTEGER DEFAULT 0,
    wygrane INTEGER DEFAULT 0,
    rozegrane_spotkania INTEGER DEFAULT 0,
    punkty INTEGER DEFAULT 0,
    PRIMARY KEY (druzyna_nazwa, druzyna_miasto, sezon_data_start, sezon_data_koniec, sezon_liga_nazwa, sezon_liga_kraj),
    FOREIGN KEY (druzyna_nazwa, druzyna_miasto) REFERENCES druzyna(nazwa, miasto) ON DELETE CASCADE,
    FOREIGN KEY (sezon_data_start, sezon_data_koniec, sezon_liga_nazwa, sezon_liga_kraj) 
        REFERENCES sezon(data_start, data_koniec, liga_nazwa, liga_kraj) ON DELETE CASCADE
);

-- Sample data for testing

-- Sample roles
INSERT OR IGNORE INTO rola (nazwa) VALUES 
    ('admin'),
    ('user'),
    ('moderator');

INSERT OR IGNORE INTO uzytkownik (email, nick, haslo, rola_nazwa) VALUES 
    ('test@example.com', 'macbak', 'password123', 'user'),
    ('admin@example.com', 'admin', 'admin123', 'admin');

-- Sample sports
INSERT OR IGNORE INTO sport (nazwa) VALUES 
    ('Piłka nożna'),
    ('Koszykówka'),
    ('Siatkówka');

-- Sample event types
INSERT OR IGNORE INTO typ (nazwa) VALUES 
    ('Gol'),
    ('Żółta kartka'),
    ('Czerwona kartka'),
    ('Rzut karny'),
    ('Zmiana');
