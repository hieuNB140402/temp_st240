package com.meskiep.vaithat.data.app

import android.content.Context
import android.util.Log
import com.meskiep.vaithat.core.extension.dLog
import com.meskiep.vaithat.core.helper.MediaHelper
import com.meskiep.vaithat.core.service.RetrofitClient
import com.meskiep.vaithat.core.service.RetrofitPreventive
import com.meskiep.vaithat.core.utils.DataLocal.isFailBaseURL
import com.meskiep.vaithat.core.utils.key.AssetsKey
import com.meskiep.vaithat.core.utils.key.DomainKey
import com.meskiep.vaithat.core.utils.key.ValueKey
import com.meskiep.vaithat.core.utils.state.CallApiState
import com.meskiep.vaithat.core.utils.state.HandleState
import com.meskiep.vaithat.data.local.data_character.DataCharacter
import com.meskiep.vaithat.data.local.data_character.DataCharacterDAO
import com.meskiep.vaithat.data.local.edit.EditCharacter
import com.meskiep.vaithat.data.local.edit.EditCharacterDAO
import com.meskiep.vaithat.data.model.DataAPI
import com.meskiep.vaithat.data.model.PathAPI
import com.meskiep.vaithat.data.model.custom.ColorModel
import com.meskiep.vaithat.data.model.custom.CustomizeModel
import com.meskiep.vaithat.data.model.custom.LayerListModel
import com.meskiep.vaithat.data.model.custom.LayerModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import javax.inject.Inject


class DataRepository @Inject constructor(
    private val dataCharacterDAO: DataCharacterDAO,
    private val editCharacterDAO: EditCharacterDAO
) {

    // Room
    // ===========================================================================================================================

    // Data Character
    // ==================================================

    // Inset
    suspend fun insertDataCharacterList(dataCharacterList: List<DataCharacter>) {
        dataCharacterDAO.insertDataCharacterList(dataCharacterList)
    }

    // Get
    suspend fun getAllDataCharacter(): List<DataCharacter> {
        return dataCharacterDAO.getAllDataCharacter()
    }

    suspend fun selectDataCharacterByDataName(dataName: String): DataCharacter {
        return dataCharacterDAO.selectDataCharacterByDataName(dataName)
    }

    // Delete
    suspend fun deleteAllDataCharacter() {
        dataCharacterDAO.deleteAllDataCharacter()
    }

    // Edit Character
    // ==================================================

    // Insert
    suspend fun insertEditCharacter(editCharacter: EditCharacter) {
        editCharacterDAO.insertEditCharacter(editCharacter)
    }

    // Get
    suspend fun getAllDataCharacterDesc(): List<EditCharacter> {
        return editCharacterDAO.getAllDataCharacterDesc()
    }

    // Get
    suspend fun deleteAllEditCharacter() {
        editCharacterDAO.deleteAllEditCharacter()
    }

    suspend fun deleteEditCharacterByFileNameInternal(fileNameInternal: String){
        editCharacterDAO.deleteEditCharacterByFileNameInternal(fileNameInternal)
    }

    suspend fun deleteEditCharacterByFileNameInternals(fileNameInternals: List<String>){
        editCharacterDAO.deleteEditCharacterByFileNameInternals(fileNameInternals)
    }

    // Other
    // ===========================================================================================================================
    suspend fun getAllParts(context: Context): Flow<CallApiState<DataCharacter>> = flow {
        Log.d("nbhieu", "API Calling...")
        emit(CallApiState.Loading)

        val response = withTimeoutOrNull(5_000) {
            try {
                RetrofitClient.api.getAllData()
            } catch (e: Exception) {
                Log.e("nbhieu", "DOMAIN failed: ${e.message}")
                null
            }
        } ?: withTimeoutOrNull(5_000) {
            try {
                RetrofitPreventive.api.getAllData()
            } catch (e: Exception) {
                Log.e("nbhieu", "DOMAIN_PREVENTIVE failed: ${e.message}")
                null
            }
        }

        if (response != null && response.isSuccessful && response.body() != null) {
            response.body()?.let {
                emit(CallApiState.Success(getDataAPI(context, it)))
            }
        } else {
            val file = File(context.filesDir, ValueKey.DATA_FILE_API_INTERNAL)
            if (file.exists()) file.delete()
            emit(CallApiState.Error("Không connect vào được API"))
        }
    }

    suspend fun getDataAPI(context: Context, response: Map<String, List<PathAPI>>): List<DataCharacter> {
        val dataList = response.map { (key, dataBody) ->
            DataAPI(key, dataBody)
        }

        val allDataAPI: ArrayList<CustomizeModel> = arrayListOf()

        // Character 1, Character 2,...
        dataList.forEachIndexed { indexCharacter, data ->
            ///public/app/ChibiMaker/1/avatar.png
            val baseDomain = if (!isFailBaseURL) DomainKey.DOMAIN else DomainKey.DOMAIN_PREVENTIVE

            val avatarCharacter = "$baseDomain${DomainKey.SUB_DOMAIN}/${data.name}/${DomainKey.AVATAR_CHARACTER_API}"
            val layerList = ArrayList<LayerListModel>(data.parts.size)

            data.parts.forEachIndexed { indexLayer, dataLayer ->
                val layerName = dataLayer.parts.split(AssetsKey.SPLIT_LAYER)
                val positionCustom = layerName.first().toInt() - 1
                val positionNavigation = layerName.last().toInt() - 1
                val imageNavigation =
                    "${baseDomain}${DomainKey.SUB_DOMAIN}/${data.name}/${dataLayer.parts}/${DomainKey.IMAGE_NAVIGATION}"
                val layer = getDataLayer(baseDomain, dataLayer, dataLayer.parts)

                val layerListModel = LayerListModel(
                    positionCustom = positionCustom,
                    positionNavigation = positionNavigation,
                    imageNavigation = imageNavigation,
                    layer = layer
                )
                layerList.add(layerListModel)
            }
            layerList.sortBy { it.positionNavigation }

            val dataApi = CustomizeModel(
                dataName = data.name,
                avatar = avatarCharacter,
                level = data.parts.first().level,
                layerList = layerList
            )
            allDataAPI.add(dataApi)
        }

        allDataAPI.forEach {
            dLog("data: avatar: ${it.avatar}")
        }


        val dataCharacterList = allDataAPI.mapIndexed { index, customModel ->
            DataCharacter(
                dataName = customModel.dataName,
                avatarPath = customModel.avatar,
                level = customModel.level,
                fileNameInternal = MediaHelper.writeModelToFile(
                    context,
                    ValueKey.DATA_CHARACTER_ALBUM,
                    customModel.dataName,
                    customModel
                )
            )
        }

        insertDataCharacterList(dataCharacterList)

        return dataCharacterList
    }

    fun getDataLayer(baseDomain: String, pathData: PathAPI, layer: String): ArrayList<LayerModel> {
        return if (pathData.colorArray != "" || pathData.colorArray.isNotEmpty()) {
            getDataAPIColor(baseDomain, pathData, layer)
        } else {
            getDataAPINoColor(baseDomain, pathData, layer)
        }
    }

    fun getDataAPINoColor(baseDomain: String, part: PathAPI, layer: String): ArrayList<LayerModel> {
        val layerPath = ArrayList<LayerModel>(part.quantity)

        val prefix = "$baseDomain${DomainKey.SUB_DOMAIN}/${part.position}/${layer}/"
        val suffix = DomainKey.LAYER_EXTENSION
        val suffixThumb = DomainKey.IMAGE_THUMB
        for (i in 1..part.quantity) {
            layerPath.add(
                LayerModel(
                    imagePath = "$prefix${i}$suffix",
                    thumbPath = "$prefix${suffixThumb}${i}$suffix",
                    isMoreColors = false,
                    listColor = arrayListOf()
                )
            )
        }
        return layerPath
    }

    fun getDataAPIColor(baseDomain: String, part: PathAPI, layer: String): ArrayList<LayerModel> {
        val layerPath = ArrayList<LayerModel>(part.quantity)
        val getColorCode = part.colorArray.split(",")
        val prefix = "$baseDomain${DomainKey.SUB_DOMAIN}/${part.position}/${layer}/"
        val suffix = DomainKey.LAYER_EXTENSION

        for (i in 1..part.quantity) {
            val listColor = ArrayList<ColorModel>(getColorCode.size)
            for (j in 0 until getColorCode.size) {
                listColor.add(
                    ColorModel(
                        "#${getColorCode[j]}",
                        "$prefix${getColorCode[j]}/${i}$suffix"
                    )
                )
            }
            val suffixThumb = DomainKey.IMAGE_THUMB

            layerPath.add(
                LayerModel(
                    imagePath = listColor.first().path,
                    thumbPath = "$prefix${suffixThumb}${i}$suffix",
                    isMoreColors = true,
                    listColor = listColor
                )
            )
        }
        return layerPath
    }
}