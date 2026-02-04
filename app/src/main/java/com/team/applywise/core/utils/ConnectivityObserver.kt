package com.team.applywise.core.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * ConnectivityObserver - Watches internet connection status in real-time
 * This helps us show the orange "No Internet" banner when user goes offline
 */
class ConnectivityObserver(context: Context) {
    // Get Android's network manager to check internet status
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * observe() - Continuously monitors internet connection
     * Returns: Flow<Boolean> - true when online, false when offline
     */
    fun observe(): Flow<Boolean> = callbackFlow {
        // Create a callback that Android will use to tell us about network changes
        val callback = object : ConnectivityManager.NetworkCallback() {
            // Called when internet becomes available
            override fun onAvailable(network: Network) {
                trySend(true) // Tell our app: "Internet is back!"
            }

            // Called when internet is lost
            override fun onLost(network: Network) {
                trySend(false) // Tell our app: "Internet is gone!"
            }

            // Called when network capabilities change (like switching from WiFi to mobile data)
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                // Check if we have real internet (not just connected to WiFi without internet)
                val isConnected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                trySend(isConnected)
            }
        }

        // Tell Android we want to monitor internet connectivity
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        // Register our callback so Android will notify us of changes
        connectivityManager.registerNetworkCallback(request, callback)

        // Send the current internet status immediately (don't wait for a change)
        val isConnected = connectivityManager.activeNetwork?.let { network ->
            connectivityManager.getNetworkCapabilities(network)?.let { capabilities ->
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }
        } ?: false // If we can't check, assume offline
        trySend(isConnected)

        // When this observer is no longer needed, clean up and stop monitoring
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}