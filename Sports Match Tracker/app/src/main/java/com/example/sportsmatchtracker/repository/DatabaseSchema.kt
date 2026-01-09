package com.example.sportsmatchtracker.repository

object DatabaseSchema {
    
    object Roles {
        const val TABLE_NAME = "roles"
        const val NAME = "name"
    }
    
    object Users {
        const val TABLE_NAME = "users"
        const val EMAIL = "email"
        const val NICK = "nick"
        const val PASSWORD = "password"
        const val ROLES_NAME = "roles_name"
    }
    
    object Addresses {
        const val TABLE_NAME = "adresses"
        const val STREET = "street"
        const val NUMBER = "number"
        const val CITY = "city"
    }
    
    object Buildings {
        const val TABLE_NAME = "buildings"
        const val NAME = "name"
        const val STREET = "street"
        const val NUMBER = "number"
        const val CITY = "city"
    }
    
    object Sports {
        const val TABLE_NAME = "sports"
        const val NAME = "name"
    }
    
    object Leagues {
        const val TABLE_NAME = "leagues"
        const val NAME = "name"
        const val COUNTRY = "country"
        const val SPORTS_NAME = "sports_name"
    }
    
    object Teams {
        const val TABLE_NAME = "teams"
        const val NAME = "name"
        const val CITY = "city"
        const val LEAGUE_NAME = "league_name"
        const val LEAGUE_COUNTRY = "league_country"
    }
    
    object LeagueSubscriptions {
        const val TABLE_NAME = "league_subscriptions"
        const val SUBSCRIBER_EMAIL = "subscriber_email"
        const val LEAGUE_NAME = "league_name"
        const val LEAGUE_COUNTRY = "league_country"
    }

    object TeamSubscriptions {
        const val TABLE_NAME = "team_subscriptions"
        const val SUBSCRIBER_EMAIL = "subscriber_email"
        const val TEAM_NAME = "team_name"
    }
    
    object Seasons {
        const val TABLE_NAME = "seasons"
        const val DATE_START = "date_start"
        const val DATE_END = "date_end"
        const val LEAGUE_NAME = "league_name"
        const val LEAGUE_COUNTRY = "league_country"
    }
    
    object Matches {
        const val TABLE_NAME = "matches"
        const val START_DATE = "start_date"
        const val HOME_SCORE = "home_score"
        const val AWAY_SCORE = "away_score"
        const val SEASON_START_DATE = "season_start_date"
        const val SEASON_END_DATE = "season_end_date"
        const val SEASON_LEAGUE_NAME = "season_league_name"
        const val SEASON_LEAGUE_COUNTRY = "season_league_country"
        const val BUILDING_NAME = "building_name"
        const val HOME_TEAM_NAME = "home_team_name"
        const val AWAY_TEAM_NAME = "away_team_name"
    }
    
    object MatchEvents {
        const val TABLE_NAME = "match_events"
        const val GAME_TIME = "game_time"
        const val EVENT = "event"
        const val MATCH_START_DATE = "match_start_date"
        const val MATCH_SEASON_START_DATE = "match_season_start_date"
        const val MATCH_SEASON_END_DATE = "match_season_end_date"
        const val MATCH_SEASON_LEAGUE_NAME = "match_season_league_name"
        const val MATCH_SEASON_LEAGUE_COUNTRY = "match_season_league_country"
    }
    
    object Tables {
        const val TABLE_NAME = "tables"
        const val SEASON_END_DATE = "season_end_date"
        const val SEASON_START_DATE = "season_start_date"
        const val SEASON_LEAGUE_NAME = "season_league_name"
        const val SEASON_LEAGUE_COUNTRY = "season_league_country"
    }
    
    object PositionsInTable {
        const val TABLE_NAME = "positions_in_table"
        const val TABLE_SEASON_START_DATE = "table_season_start_date"
        const val TABLE_SEASON_END_DATE = "table_season_end_date"
        const val TABLE_SEASON_LEAGUE_NAME = "table_season_league_name"
        const val TABLE_SEASON_LEAGUE_COUNTRY = "table_season_league_country"
        const val TEAM_NAME = "team_name"
        const val DRAWS = "draws"
        const val LOSSES = "losses"
        const val WINS = "wins"
        const val MATCHES_PLAYED = "matches_played"
        const val POINTS = "points"
    }
}
