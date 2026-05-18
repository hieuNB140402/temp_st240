package com.meskiep.vaithat.ui.customize

import android.R.attr.path
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.lifecycle.ViewModel
import com.meskiep.vaithat.core.extension.dLog
import com.meskiep.vaithat.core.extension.eLog
import com.meskiep.vaithat.core.extension.iLog
import com.meskiep.vaithat.core.helper.BitmapHelper
import com.meskiep.vaithat.core.helper.MediaHelper
import com.meskiep.vaithat.core.helper.StringHelper
import com.meskiep.vaithat.core.utils.key.ValueKey
import com.meskiep.vaithat.core.utils.state.SaveState
import com.meskiep.vaithat.data.app.DataRepository
import com.meskiep.vaithat.data.local.data_character.DataCharacter
import com.meskiep.vaithat.data.local.edit.EditCharacter
import com.meskiep.vaithat.data.model.custom.CustomizeModel
import com.meskiep.vaithat.data.model.custom.ItemColorImageModel
import com.meskiep.vaithat.data.model.custom.ItemColorModel
import com.meskiep.vaithat.data.model.custom.ItemNavCustomModel
import com.meskiep.vaithat.data.model.custom.LayerListModel
import com.meskiep.vaithat.data.model.custom.NavigationModel
import com.meskiep.vaithat.data.model.custom.SuggestionModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.collections.forEachIndexed
import kotlin.collections.indexOfFirst
import kotlin.collections.mapIndexed

@HiltViewModel
class CustomizeViewModel @Inject constructor(val dataRepository: DataRepository) : ViewModel() {

    // Flow Declaration
    //==================================================================================================================
    // Trạng thái flip
    private val _isFlip = MutableStateFlow(false)
    val isFlip = _isFlip.asStateFlow()

    private val _isCreated = MutableStateFlow(false)
    val isCreated = _isCreated.asStateFlow()

    // Danh sách Navigation bottom
    private val _bottomNavigationList = MutableStateFlow(listOf<NavigationModel>())
    val bottomNavigationList = _bottomNavigationList.asStateFlow()

    private val _isShowMoreColor = MutableStateFlow(false)
    val isShowMoreColor = _isShowMoreColor.asStateFlow()


    // Normal Declaration
    //==================================================================================================================
    // Data gốc
    var dataCustomize: CustomizeModel? = null

    // Đếm số lần random, chỉ số được chọn
    var countRandom = 0

    var customizeStatusPlay = ValueKey.CREATE

    var positionNavSelected = -1

    var positionCustom = -1

    val itemNavList = ArrayList<ArrayList<ItemNavCustomModel>>()

    // Danh sách màu
    var colorItemNavList = ArrayList<ArrayList<ItemColorModel>>()

    // Trạng thái chọn item/màu
    var positionColorItemList = ArrayList<Int>()

    val isSelectedItemList = ArrayList<Boolean>()

    // Key + Path đã chọn
    var keySelectedItemList = ArrayList<String>()

    var pathSelectedList = ArrayList<String>()

    // Danh sách ImageView trên layout
    val imageViewList = ArrayList<ImageView>()

    val colorListMost = ArrayList<String>()

    var suggestionModel = SuggestionModel()

    var thumbPathEdit = ""

    // Initialization
    //==================================================================================================================
    suspend fun setupDataGetSuccess(dataName: String, customStatusPlay: Int) {
        val dataCharacter = selectDataCharacterByDataName(dataName)
        val customizeModel = MediaHelper.readModelFromFileRealPath<CustomizeModel>(dataCharacter.fileNameInternal)
        updateDataCustomize(customizeModel!!)
        updateCustomizeStatusPlay(customStatusPlay)
        dLog("customizeModel: $customizeModel")
    }

    fun initValueDefault() {
        updatePositionDefault()
        setBottomNavigationListDefault()
    }

    fun updatePositionDefault() {
        updatePositionCustom(dataCustomize!!.layerList.first().positionCustom)
        updatePositionNavSelected(dataCustomize!!.layerList.first().positionNavigation)
    }

    suspend fun initValueData() {
        resetDataList()
        // Khong dong
        addValueToItemNavList()

        setItemColorDefault()
        // Khong dong
        setFocusItemNavDefault()
    }

    fun resetDataList() {
        val quantityLayer = dataCustomize!!.layerList.size
        val positionColorItemList = ArrayList<Int>(quantityLayer)
        val isSelectedItemList = ArrayList<Boolean>(quantityLayer)
        val keySelectedItemList = ArrayList<String>(quantityLayer)
        val pathSelectedList = ArrayList<String>(quantityLayer)

        repeat(quantityLayer) {
            positionColorItemList.add(0)
            isSelectedItemList.add(false)
            keySelectedItemList.add("")
            pathSelectedList.add("")
        }

        updatePositionColorItemList(positionColorItemList)
        updateIsSelectedItemList(isSelectedItemList)
        updateKeySelectedItemList(keySelectedItemList)
        updatePathSelectedList(pathSelectedList)
    }

    suspend fun initDataEdit(fileNameInternal: String) {
        val startTime = System.currentTimeMillis()
        val editModel = selectEditCharacterByFileNameInternal(fileNameInternal)
        updateThumbPathEdit(editModel.thumbPath)

        val suggestionModel = MediaHelper.readModelFromFileRealPath<SuggestionModel>(editModel.fileNameInternal) ?: return
        updatePathSelectedList(suggestionModel.pathSelectedList)
        updateKeySelectedItemList(suggestionModel.keySelectedItemList)

        keySelectedItemList.forEachIndexed { index, keySelected ->
            if (keySelected != "") {
                // Vi tri item duoc chon trong layer rcv
                val positionItemLayerSelected = itemNavList[index].indexOfFirst { it.pathNoColor == keySelected }
                // Item duoc chon trong layer rcv
                val itemLayerSelected = itemNavList[index][positionItemLayerSelected]

                val positionNavigation = itemLayerSelected.positionNavigation
                val positionCustom = itemLayerSelected.positionCustom

                // Dat lai focus
                setIsSelectedItem(positionNavigation)
                setItemNavList(positionNavigation, positionItemLayerSelected)

                if (pathSelectedList[positionCustom] != keySelected) {
                    // Vi tri color duoc chon (hinh anh mau duoc chon)
                    val positionColorSelected =
                        itemLayerSelected.listImageColor.indexOfFirst { it.path == pathSelectedList[positionCustom] }
                    // Dat lai focus mau (thanh mau)
                    setColorItemNav(positionNavigation, positionColorSelected)
                    // Dat lai vi tri mau duoc chon (da duoc chon hay chua)
                    setPositionColorItem(positionNavigation, positionColorSelected)
                }
            }
        }

        iLog("timeInitDataEdit: ${System.currentTimeMillis() - startTime}")
    }


    // Getter / Setter
    //==================================================================================================================
    fun updatePositionNavSelected(position: Int) {
        positionNavSelected = position
    }

    fun updatePositionCustom(position: Int) {
        positionCustom = position
    }

    fun updateDataCustomize(data: CustomizeModel) {
        dataCustomize = data
    }

    fun updateCustomizeStatusPlay(status: Int) {
        customizeStatusPlay = status
    }

    fun setIsFlip() {
        _isFlip.value = !_isFlip.value
    }

    fun setIsCreated(status: Boolean) {
        _isCreated.value = status
    }

    fun updatePositionColorItemList(positionList: ArrayList<Int>) {
        positionColorItemList.clear()
        positionColorItemList.addAll(positionList)
    }

    fun setPositionColorItem(position: Int, newPosition: Int) {
        positionColorItemList =
            positionColorItemList.mapIndexed { index, oldPosition -> if (index == position) newPosition else oldPosition }
                .toCollection(ArrayList())
    }

    fun updateIsSelectedItemList(selectedList: ArrayList<Boolean>) {
        isSelectedItemList.clear()
        isSelectedItemList.addAll(selectedList)
    }

    fun setIsSelectedItem(position: Int) {
        isSelectedItemList[position] = true
    }

    fun updateKeySelectedItemList(keyList: ArrayList<String>) {
        keySelectedItemList.clear()
        keySelectedItemList = keyList
    }

    fun setKeySelected(position: Int, newKey: String) {
        keySelectedItemList[position] = newKey
    }

    fun updatePathSelectedList(pathList: List<String>) {
        pathSelectedList.clear()
        pathSelectedList.addAll(pathList)
    }

    fun setPathSelected(position: Int, newPath: String) {
        pathSelectedList[position] = newPath
    }

    fun setColorListMost(colorList: ArrayList<String>) {
        colorListMost.clear()
        colorListMost.addAll(colorList)
    }

    fun updateSuggestionModel(model: SuggestionModel) {
        suggestionModel = model
    }

    fun updateThumbPathEdit(thumbPath: String) {
        thumbPathEdit = thumbPath
    }


    // Navigation
    //==================================================================================================================
    fun setBottomNavigationListDefault() {
        _bottomNavigationList.value = dataCustomize!!.layerList.mapIndexed { index, layerList ->
            NavigationModel(imageNavigation = layerList.imageNavigation, isSelected = index == 0)
        }
    }

    fun setClickBottomNavigation(position: Int) {
        _bottomNavigationList.value = _bottomNavigationList.value.mapIndexed { index, model ->
            model.copy(isSelected = index == position)
        }
    }

    fun getPositionCustomByPositionNavigation(position: Int): Int {
        return dataCustomize!!.layerList[position].positionCustom
    }


    // Item Nav / Layer
    //==================================================================================================================
    fun addValueToItemNavList() {
        itemNavList.clear()
        dataCustomize!!.layerList.forEachIndexed { index, layer ->
            if (index == 0) {
                itemNavList.add(createListItem(layer, true))
            } else {
                itemNavList.add(createListItem(layer))
            }
        }
    }

    fun setFocusItemNavDefault() {
        for (itemParent in itemNavList) {
            itemParent.forEachIndexed { index, item ->
                item.isSelected = index == 0
            }
        }
        itemNavList.first()[0].isSelected = false
        itemNavList.first()[1].isSelected = true
    }

    fun setItemNavList(positionNavigation: Int, position: Int) {
        itemNavList[positionNavigation] =
            itemNavList[positionNavigation].mapIndexed { index, models -> models.copy(isSelected = index == position) }
                .toCollection(ArrayList())
    }

    fun getCurrentItemNavList(): List<ItemNavCustomModel> {
        return itemNavList[positionNavSelected]
    }

    fun createListItem(layers: LayerListModel, isBody: Boolean = false): ArrayList<ItemNavCustomModel> {
        val listItem = arrayListOf<ItemNavCustomModel>()
        val positionCustom = layers.positionCustom
        val positionNavigation = layers.positionNavigation

        if (isBody) {
            listItem.add(
                ItemNavCustomModel(
                    pathThumb = ValueKey.RANDOM_LAYER,
                    pathNoColor = "",
                    positionCustom = positionCustom,
                    positionNavigation = positionNavigation
                )
            )
        } else {
            listItem.add(
                ItemNavCustomModel(
                    pathThumb = ValueKey.NONE_LAYER,
                    pathNoColor = "",
                    positionCustom = positionCustom,
                    positionNavigation = positionNavigation,
                    isSelected = true
                )
            )
            listItem.add(
                ItemNavCustomModel(
                    pathThumb = ValueKey.RANDOM_LAYER,
                    pathNoColor = "",
                    positionCustom = positionCustom,
                    positionNavigation = positionNavigation,
                )
            )
        }
        for (layer in layers.layer) {
            if (!layer.isMoreColors) {
                listItem.add(
                    ItemNavCustomModel(
                        pathThumb = layer.thumbPath,
                        pathNoColor = layer.imagePath,
                        positionCustom = positionCustom,
                        positionNavigation = positionNavigation
                    )
                )
            } else {
                val listItemColor = ArrayList<ItemColorImageModel>()

                for (colorModel in layer.listColor) {
                    listItemColor.add(
                        ItemColorImageModel(
                            color = colorModel.color, path = colorModel.path
                        )
                    )
                }
                listItem.add(
                    ItemNavCustomModel(
                        pathThumb = layer.thumbPath,
                        pathNoColor = layer.imagePath,
                        positionCustom = positionCustom,
                        positionNavigation = positionNavigation,
                        isSelected = false,
                        listImageColor = listItemColor,
                    )
                )
            }
        }
        return listItem
    }

    fun handleNoneLayer(position: Int) {
        setIsSelectedItem(positionCustom)
        setPathSelected(positionCustom, "")
        setKeySelected(positionNavSelected, "")
        setItemNavList(positionNavSelected, position)
    }


    // Color
    //==================================================================================================================
    fun setItemColorDefault() {
        colorItemNavList.clear()
        for (i in 0 until dataCustomize!!.layerList.size) {
            // Lấy đối tượng LayerModel đầu tiên trong danh sách con
            val currentLayer = dataCustomize!!.layerList[i].layer.first()
            var firstIndex = true
            // Kiểm tra isMoreColors để thêm màu hoặc danh sách rỗng
            if (currentLayer.isMoreColors) {
                val colorList = ArrayList<ItemColorModel>()
                for (j in 0 until currentLayer.listColor.size) {
                    val color = currentLayer.listColor[j].color
                    if (firstIndex) {
                        colorList.add(ItemColorModel(color, true))
                    } else {
                        colorList.add(ItemColorModel(color))
                    }
                    firstIndex = false
                }
                colorItemNavList.add(colorList)
            } else {
                colorItemNavList.add(arrayListOf())
            }
        }
        val getAllColor = ArrayList<String>()
        itemNavList.forEachIndexed { index, nav ->
            val position = if (index != 0) 2 else 1
            val itemNav = nav[position]
            itemNav.listImageColor.forEach { colorList ->
                getAllColor.add(colorList.color)
            }
        }
        setColorListMost(
            getAllColor.groupingBy { it }.eachCount()
                .filter { it.value > 3 }.keys.toCollection(ArrayList())
        )
    }

    fun setColorItemNav(positionNavSelected: Int, position: Int) {
        colorItemNavList[positionNavSelected] =
            colorItemNavList[positionNavSelected].mapIndexed { index, models -> models.copy(isSelected = index == position) }
                .toCollection(ArrayList())
    }

    fun setIsShowMoreColor() {
        _isShowMoreColor.value = !_isShowMoreColor.value
    }

    fun getCurrentColorItemNavList(): List<ItemColorModel> {
        return colorItemNavList[positionNavSelected]
    }


    // Feature Actions
    //==================================================================================================================
    suspend fun setClickFillLayer(item: ItemNavCustomModel, position: Int): String {
        val path = item.pathNoColor
        setKeySelected(positionNavSelected, path)

        val pathSelected = runCatching {
            if (item.listImageColor.isEmpty()) {
                path
            } else {
                val colorIndex = positionColorItemList.getOrNull(positionNavSelected) ?: return@runCatching path

                item.listImageColor.getOrNull(colorIndex)?.path ?: path
            }
        }.getOrElse {
//            context.logEvent("setClickFillLayer pathSelected: $it")
            eLog("setClickFillLayer pathSelected: $it")
            ""
        }

        runCatching {
            setIsSelectedItem(positionNavSelected)
            setPathSelected(positionCustom, pathSelected)
            setItemNavList(positionNavSelected, position)
        }.onFailure {
            eLog("setClickFillLayer pathSelected: $it")
//            context.logEvent("setClickFillLayer updateState: $it")
        }

        return pathSelected
    }

    suspend fun setClickRandomLayer(): Pair<String, Boolean> {
        val positionStartLayer = if (positionNavSelected == 0) 1 else 2
        val randomLayer = if (positionNavSelected == 0) {
            if (itemNavList[positionNavSelected].size == 1) {
                1
            } else {
                (positionStartLayer..<itemNavList[positionNavSelected].size).random()
            }
        } else {
            (positionStartLayer..<itemNavList[positionNavSelected].size).random()
        }

        var randomColor: Int? = null

        var isMoreColors = false

        if (itemNavList[positionNavSelected][positionStartLayer].listImageColor.isNotEmpty()) {
            isMoreColors = true
            randomColor = (0..<(itemNavList[positionNavSelected][positionStartLayer].listImageColor.size)).random()
        }
        var pathRandom = itemNavList[positionNavSelected][randomLayer].pathNoColor
        setKeySelected(positionNavSelected, pathRandom)

        if (!isMoreColors) {
            setPositionColorItem(positionCustom, 0)
        } else {
            pathRandom = itemNavList[positionNavSelected][randomLayer].listImageColor[randomColor!!].path
            setPositionColorItem(positionCustom, randomColor)
        }
        setPathSelected(positionCustom, pathRandom)
        setItemNavList(positionNavSelected, randomLayer)
        if (isMoreColors) {
            setColorItemNav(positionNavSelected, randomColor!!)
        }
        return pathRandom to isMoreColors
    }

    suspend fun setClickRandomFullLayer(): Boolean {
        countRandom++
        val isOutTurn = countRandom == 3

        val colorCode = if (colorListMost.isNotEmpty()) colorListMost[(0..<colorListMost.size).random()] else "#123456"
        for (i in 0 until _bottomNavigationList.value.size) {
            val minSize = if (i == 0) 1 else 2
            if (itemNavList[i].size <= minSize) {
                continue
            }
            val randomLayer = (minSize..<itemNavList[i].size).random()

            var randomColor: Int = 0

            val isMoreColors = if (itemNavList[i][minSize].listImageColor.isNotEmpty()) {
                randomColor = itemNavList[i][randomLayer].listImageColor.indexOfFirst { it.color == colorCode }
                if (randomColor == -1) {
                    randomColor = (0..<itemNavList[i][minSize].listImageColor.size).random()
                }
                true
            } else {
                false
            }
            keySelectedItemList[i] = itemNavList[i][randomLayer].pathNoColor

            val pathItem = if (!isMoreColors) {
                positionColorItemList[i] = 0
                itemNavList[i][randomLayer].pathNoColor
            } else {
                positionColorItemList[i] = randomColor
                itemNavList[i][randomLayer].listImageColor[randomColor].path
            }
            pathSelectedList[dataCustomize!!.layerList[i].positionCustom] = pathItem
            setItemNavList(i, randomLayer)
            if (isMoreColors) {
                setColorItemNav(i, randomColor)
            }
        }
        return isOutTurn
    }

    suspend fun setClickChangeColor(position: Int): String {
        var pathColor = ""
        positionColorItemList[positionNavSelected] = position
        // Đã chọn hình ảnh chưa
        if (keySelectedItemList[positionNavSelected] != "") {
            // Duyệt qua từng item trong bộ phận
            for (item in dataCustomize!!.layerList[positionNavSelected].layer) {
                if (item.imagePath == keySelectedItemList[positionNavSelected]) {
                    pathColor = item.listColor[position].path
                    pathSelectedList[positionCustom] = pathColor
                }
            }
        }
        setColorItemNav(positionNavSelected, position)
        return pathColor
    }

    suspend fun setClickReset(): String {
        resetDataList()

        _bottomNavigationList.value.forEachIndexed { index, model ->
            val positionSelected = if (index == 0) 1 else 0
            setItemNavList(index, positionSelected)
            setColorItemNav(index, 0)
        }

        val pathDefault = dataCustomize!!.layerList.first().layer.first().imagePath

        pathSelectedList[dataCustomize!!.layerList.first().positionCustom] = pathDefault
        keySelectedItemList[dataCustomize!!.layerList.first().positionNavigation] = pathDefault
        isSelectedItemList[dataCustomize!!.layerList.first().positionNavigation] = true

        return pathDefault
    }

    suspend fun getDefaultPath(): String {
        val pathImageDefault = dataCustomize!!.layerList.first().layer.first().imagePath
        setIsSelectedItem(positionCustom)
        setPathSelected(positionCustom, pathImageDefault)
        setKeySelected(positionNavSelected, pathImageDefault)
        return pathImageDefault
    }

    fun saveImageFromView(context: Context, view: View): Flow<SaveState> = flow {
        emit(SaveState.Loading)
        val bitmap = BitmapHelper.createBimapFromView(view)
        MediaHelper.saveBitmapToInternalStorageZip(context, ValueKey.CHARACTER_CUSTOMIZE_ALBUM, bitmap).collect { state ->
            emit(state)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun saveEditCharacter(context: Context, thumbPath: String) {

        val editModel = createEditCharacter(context, thumbPath)

        insertDataCharacter(editModel)
    }

    suspend fun createEditCharacter(context: Context, thumbPath: String) : EditCharacter{
        return EditCharacter(
            dataName = dataCustomize!!.dataName,
            thumbPath = thumbPath,
            fileNameInternal = MediaHelper.writeModelToFile<SuggestionModel>(
                context = context,
                folder = ValueKey.EDIT_CHARACTER_ALBUM,
                fileName = StringHelper.generateRandomString(5),
                model = SuggestionModel(
                    pathSelectedList = pathSelectedList,
                    keySelectedItemList = keySelectedItemList
                )
            )
        )
    }


    // Suggestion / Edit
    //==================================================================================================================
    suspend fun updateEditCharacter(context: Context, newThumbPath: String) {
        val oldEditCharacter = selectEditCharacterThumbPathInternal(thumbPathEdit)
        updateThumbPathEdit(newThumbPath)

        var newEditCharacter = createEditCharacter(context, newThumbPath)
        newEditCharacter = newEditCharacter.copy(id = oldEditCharacter.id)

        MediaHelper.deleteFileByPathNotFlow(listOf(oldEditCharacter.thumbPath, oldEditCharacter.fileNameInternal))

        updateEditCharacter(newEditCharacter)
    }

    // View / Layout Helpers
    //==================================================================================================================
    fun setImageViewList(frameLayout: FrameLayout) {
        imageViewList.clear()
        imageViewList.addAll(addImageViewToLayout(dataCustomize!!.layerList.size, frameLayout))
    }

    fun addImageViewToLayout(quantityLayer: Int, frameLayout: FrameLayout): ArrayList<ImageView> {
        val imageViewList = ArrayList<ImageView>()
        repeat(quantityLayer) {
            val imageView = ImageView(frameLayout.context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            frameLayout.addView(imageView)
            imageViewList.add(imageView)
        }
        return imageViewList
    }

    fun getFirstImageView(): ImageView {
        return imageViewList[dataCustomize!!.layerList.first().positionCustom]
    }

    fun getCurrentImageView(): ImageView {
        return imageViewList[positionCustom]
    }


    // Room / Database
    //==================================================================================================================
    suspend fun selectDataCharacterByDataName(dataName: String): DataCharacter {
        return dataRepository.selectDataCharacterByDataName(dataName)
    }

    suspend fun insertDataCharacter(editCharacter: EditCharacter) {
        dataRepository.insertEditCharacter(editCharacter)
    }

    // Edit
    suspend fun selectEditCharacterByThumbPath(thumbPath: String): EditCharacter {
        return dataRepository.selectEditCharacterByThumbPath(thumbPath)
    }

    suspend fun selectEditCharacterByFileNameInternal(fileNameInternal: String): EditCharacter {
        return dataRepository.selectEditCharacterByFileNameInternal(fileNameInternal)
    }

    suspend fun selectEditCharacterThumbPathInternal(thumbPath: String): EditCharacter {
        return dataRepository.selectEditCharacterByThumbPathInternal(thumbPath)
    }

    suspend fun updateEditCharacter(editCharacter: EditCharacter) {
        dataRepository.updateEditCharacter(editCharacter)
    }

}
