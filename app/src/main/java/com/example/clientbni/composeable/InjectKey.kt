package com.example.clientbni.composeable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.clientbni.MainViewModel

interface InjectKey {
    val mainViewModel: MainViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun InjectMasterKey() {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = mainViewModel.masterKeyController,
            onValueChange = mainViewModel::masterKeyOnchange,
            label = { Text(text = "Master Key") }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun InjectPinKey() {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = mainViewModel.pinKeyController,
            onValueChange = {},
            label = { Text(text = "Pin Key") },
            readOnly = true
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun InjectTdkKey() {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = mainViewModel.tdkKeyController,
            onValueChange = {},
            label = { Text(text = "TDK Key") },
            readOnly = true
        )
    }
}