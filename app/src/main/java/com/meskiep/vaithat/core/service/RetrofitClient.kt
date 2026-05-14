package com.meskiep.vaithat.core.service

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.meskiep.vaithat.core.utils.key.DomainKey
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


object RetrofitClient : BaseRetrofitHelper() {
    val api =
        Retrofit.Builder().baseUrl(DomainKey.DOMAIN)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(okHttpClient!!)
            .build()
            .create(ApiService::class.java)
}
