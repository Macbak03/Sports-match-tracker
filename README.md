# Sports Match Tracker

## Autorzy
Maciej Bąk 155228
Kacper Mumot 159395

## Opis

Aplikacja mobilna umożliwiająca użytkownikom śledzenie wyników meczów z różnych dyscyplin sportowych, przeglądanie statystyk, tabel ligowych oraz zarządzanie własnymi subskrypcjami ulubionych drużyn, lig i meczów.
Aplikacja łączy się z serwerem TCP, na którym obsługiwane są zapytania do bazy danych SQLite.

Główne funkcjonalności:

1. Zarządzanie użytkownikami - rejestracja i logowanie użytkowników
2. Przeglądanie wydarzeń sportowych - lista bieżących i zaplanowanych meczów z możliwością filtrowania, możliwość wyszukiwania konkretnych meczów, wyświetlanie podstawowych danych meczu
3. Szczegóły meczu - widok szczegółowy wybranego meczu (wynik, status, lokalizacja, zdarzenia meczowe itd.), możliwość śledzenia meczu
4. Tabele ligowe dla poszczególnych sezonów
5. Drużyny  - lista drużyn z możliwością filtrowania po sporcie i kraju, szczegóły drużyny (nazwa, miasto, nadchodzące mecze, ostatnie wyniki)
6. Ulubione - użytkownik może obserwować wybrane drużyny i ligi

## Wymagania

- GCC
- SQLite3
- Make

## Instalacja

```bash
# Instalacja zależności
sudo apt install gcc sqlite3 libsqlite3-dev make

# Inicjalizacja bazy danych
make init-db

# Kompilacja
make
```

## Uruchomienie

```bash
./Server/server.out
```

Serwer nasłuchuje na porcie **1100**.

## Struktura projektu

```
.
├── Server/
│   ├── server.c          # Główny kod serwera
│   └── cJSON.c           # Parser JSON
├── database/
│   ├── init.sql          # Schemat bazy danych
│   └── sports.db         # Plik bazy SQLite (generowany)
|
├──Sports Match Tracker   # Folder z plikami aplikacji
|
├── Makefile
└── README.md
```

### Przykładowe query

```json
{
  "action": "SELECT",
  "table": "users",
  "columns": ["email", "nick"],
  "where": {
    "column": "email",
    "operator": "=",
    "value": "test@example.com"
  }
}
```

### Odpowiedź

```json
{
  "status": "success",
  "data": [
    {
      "email": "test@example.com",
      "nick": "testuser"
    }
  ],
  "count": 1
}
```

## Makefile komendy

- `make` - kompiluje projekt
- `make clean` - usuwa pliki obiektowe
- `make init-db` - tworzy bazę danych



