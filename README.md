# Sports Match Tracker

TCP server z bazą danych SQLite do trackowania meczy.

## Wymagania

- GCC
- SQLite3
- Make

## Instalacja

```bash
# Instalacja zależności (Ubuntu/Debian)
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
│   ├── server.c      # Główny kod serwera
│   └── cJSON.c       # Parser JSON
├── database/
│   ├── init.sql      # Schemat bazy danych
│   └── sports.db     # Plik bazy SQLite (generowany)
├── Makefile          # Build script
└── README.md
```

## API

Wysyłaj JSON przez TCP do portu 1100:

### SELECT Query

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

## Dodawanie zmian do bazy

Wszystkie zmiany w schemacie lub danych dodawaj do `database/init.sql`, potem:

```bash
rm database/sports.db
make init-db
```

## Makefile komendy

- `make` - kompiluje projekt
- `make clean` - usuwa pliki obiektowe
- `make init-db` - tworzy bazę danych
