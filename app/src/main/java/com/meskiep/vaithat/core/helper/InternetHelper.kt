package com.meskiep.vaithat.core.helper

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.meskiep.vaithat.core.utils.state.HandleState

object InternetHelper {
    fun checkInternetState(context: Context, state : ((HandleState) -> Unit) = {}){
        if (isInternetAvailable(context)){
            state.invoke(HandleState.SUCCESS)
        }else{
            state.invoke(HandleState.FAIL)
        }
    }

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}