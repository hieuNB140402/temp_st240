package com.meskiep.vaithat.core.service
import com.meskiep.vaithat.data.model.PathAPI
import retrofit2.Response
import retrofit2.http.GET
interface ApiService {
    @GET("/api/app/ST107_DreamcoreMaker")
    suspend fun getAllData(): Response<Map<String, List<PathAPI>>>
}