package io.helpdesk.core.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun ViewModel.ioScope(block: suspend () -> Unit) = viewModelScope.launch(Dispatchers.IO) { block() }

fun ViewModel.uiScope(block: suspend () -> Unit) =
    viewModelScope.launch(Dispatchers.Main) { block() }