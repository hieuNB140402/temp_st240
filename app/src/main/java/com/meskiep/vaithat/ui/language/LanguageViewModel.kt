package com.meskiep.vaithat.ui.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meskiep.vaithat.core.utils.DataLocal
import com.meskiep.vaithat.data.model.LanguageModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.apply
import kotlin.collections.indexOfFirst
import kotlin.collections.map
import kotlin.collections.toMutableList

class LanguageViewModel : ViewModel() {
    // Flow Declaration
    //==================================================================================================================
    private val _languageList = MutableStateFlow<List<LanguageModel>>(emptyList())
    val languageList: StateFlow<List<LanguageModel>> = _languageList.asStateFlow()

    private val _isFirstLanguage = MutableStateFlow(false)
    val isFirstLanguage: StateFlow<Boolean> = _isFirstLanguage.asStateFlow()

    // Normal Declaration
    //==================================================================================================================
    var codeLang = ""

    // Getter Setter
    //==================================================================================================================
    fun setFirstLanguage(isFirst: Boolean){
        _isFirstLanguage.value = isFirst
    }

    // Function feature
    //==================================================================================================================

    fun loadLanguages(currentLang: String) {
        viewModelScope.launch {
            val list = DataLocal.getLanguageList().toMutableList()

            val index = list.indexOfFirst { it.code == currentLang }
            if (index != -1) {
                val selected = list.removeAt(index)
                list.add(0, selected.apply { if (!isFirstLanguage.value) activate = true })
            }
            codeLang = currentLang
            _languageList.value = list
        }
    }

    fun selectLanguage(code: String) {
        codeLang = code
        val updatedList = _languageList.value.map {
            it.copy(activate = it.code == code)
        }
        _languageList.value = updatedList
    }


}
