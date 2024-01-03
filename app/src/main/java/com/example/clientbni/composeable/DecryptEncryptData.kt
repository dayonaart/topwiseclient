package com.example.clientbni.composeable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.clientbni.MainViewModel
import kotlinx.coroutines.launch

interface DecryptEncryptData {
    val mainViewModel: MainViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun EncryptData() {
        val focus = LocalFocusManager.current
        val coroutineScope = rememberCoroutineScope()
        Column(horizontalAlignment = Alignment.End) {
            OutlinedTextField(
                value = mainViewModel.encryptController,
                onValueChange = mainViewModel::encryptOnchange,
                label = { Text(text = "text to encrypt") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focus.clearFocus()
                })

            )
            Spacer(modifier = Modifier.height(height = 10.dp))
            OutlinedButton(onClick = {
                coroutineScope.launch {
                    mainViewModel.encryptData()
                }
            }) {
                Text(text = "Encrypt")
            }
            Spacer(modifier = Modifier.height(height = 10.dp))
            if (mainViewModel.encryptResult != null)
                OutlinedTextField(
                    readOnly = true,
                    value = "${mainViewModel.encryptResult}",
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 8
                )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DecryptData() {
        val focus = LocalFocusManager.current
        val coroutineScope = rememberCoroutineScope()
        Column(horizontalAlignment = Alignment.End) {
            OutlinedTextField(
                value = mainViewModel.decryptController,
                onValueChange = mainViewModel::decryptOnchange,
                label = { Text(text = "text to decrypt") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focus.clearFocus()
                })
            )
            Spacer(modifier = Modifier.height(height = 10.dp))
            OutlinedButton(onClick = {
                coroutineScope.launch {
                    mainViewModel.decryptData()
                }
            }) {
                Text(text = "Decrypt")
            }
            Spacer(modifier = Modifier.height(height = 10.dp))
            if (mainViewModel.decryptResult != null)
                OutlinedTextField(
                    readOnly = true,
                    value = "${mainViewModel.decryptResult}",
                    onValueChange = mainViewModel::decryptOnchange,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 8
                )
        }
    }
}