# Sports Match Tracker

TCP server z bazą danych SQLite do trackowania meczy.

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

