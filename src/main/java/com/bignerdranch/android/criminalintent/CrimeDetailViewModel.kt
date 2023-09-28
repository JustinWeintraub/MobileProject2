package com.bignerdranch.android.criminalintent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import android.util.Log
import androidx.lifecycle.SavedStateHandle

const val CURRENT_IMAGE_INDEX = "CURRENT_IMAGE_INDEX"
private const val TAG = "CrimeDetailViewModel"

class CrimeDetailViewModel(crimeId: UUID, private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val crimeRepository = CrimeRepository.get()
    private val _crime: MutableStateFlow<Crime?> = MutableStateFlow(null)
    var photoFileNameIndex: Int
        get() = savedStateHandle.get(CURRENT_IMAGE_INDEX) ?: 0
        set(value) = savedStateHandle.set(CURRENT_IMAGE_INDEX, value)
    val crime: StateFlow<Crime?> = _crime.asStateFlow()

    init {
        viewModelScope.launch {
            _crime.value = crimeRepository.getCrime(crimeId)
        }
    }
    fun incrementImgIdx() {
        photoFileNameIndex = (photoFileNameIndex + 1) % 4
    }
    fun updateCrime(onUpdate: (Crime) -> Crime) {
        _crime.update { oldCrime ->
            oldCrime?.let { onUpdate(it) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        crime.value?.let { crimeRepository.updateCrime(it) }
    }
}

class CrimeDetailViewModelFactory(
    private val crimeId: UUID,
    private val savedStateHandle: SavedStateHandle
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CrimeDetailViewModel(crimeId, savedStateHandle) as T
    }
}
