package com.example.sportsmatchtracker.repository.subscriptions

import com.example.sportsmatchtracker.model.subscriptions.LeagueSubscription
import com.example.sportsmatchtracker.repository.Repository

abstract class SubscriptionsRepository: Repository() {
    protected abstract fun parseServerError(message: String): Exception
    abstract suspend fun fetchSubscriptions(userEmail: String)
}