package com.example.clientbni

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clientbni.composeable.DecryptEncryptData
import com.example.clientbni.composeable.InjectKey
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import id.co.payment2go.terminalsdkhelper.core.TermLog
import id.co.payment2go.terminalsdkhelper.zcs.utils.ZcsUtility.setDisableNavigation
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), InjectKey, DecryptEncryptData {
    private val TAG = "MainActivity"
    override val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface {
                MainView()
            }
        }

        this.setDisableNavigation(true)

    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: success")
        this.setDisableNavigation(false)
        super.onDestroy()
    }

    override fun onPause() {
        Log.d(TAG, "onpause: success")
        this.setDisableNavigation(false)
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "onStop: success")
        this.setDisableNavigation(false)
        super.onStop()
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    @Composable
    private fun MainView() {
        val coroutineScope = rememberCoroutineScope()
        Scaffold(topBar = {
            Box(
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .background(color = Color.LightGray)
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Text(text = "SDK HELPER", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }) { paddingValues ->
            Box(contentAlignment = Alignment.Center) {
                if (mainViewModel.loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(100.dp),
                            color = Color.Blue,
                            strokeWidth = 5.dp
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp),
                ) {
                    PostLogon()
                    InjectKey()
                    Spacer(modifier = Modifier.height(height = 10.dp))
                    EncryptDecrypt()
                    Spacer(modifier = Modifier.height(height = 20.dp))
                    ShowPinpad()
                    Spacer(modifier = Modifier.height(height = 20.dp))
                    Print()
                    Spacer(modifier = Modifier.height(height = 20.dp))
                    ReadCard()
                    Spacer(modifier = Modifier.height(height = 20.dp))
                    DebitScreen()
                    Spacer(modifier = Modifier.height(height = 20.dp))
                    Test()
                    Spacer(modifier = Modifier.height(height = 20.dp))
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun PostLogon() {
        val coroutineScope = rememberCoroutineScope()
        Column(
            horizontalAlignment = Alignment.End,
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                label = { Text(text = "Response Here") },
                value = mainViewModel.postLogonResponse ?: "", onValueChange = {})
            Spacer(modifier = Modifier.height(height = 20.dp))
            OutlinedButton(onClick = {
                coroutineScope.launch {
                    mainViewModel.postingLogon()
                }
            }) {
                Text(text = "Post Logon")
            }
            Spacer(modifier = Modifier.height(height = 20.dp))
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun InjectKey() {
        val coroutineScope = rememberCoroutineScope()
        Box(
            modifier = Modifier
                .border(width = 1.dp, color = Color.Gray)
                .padding(vertical = 10.dp, horizontal = 10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                InjectMasterKey()
                InjectTdkKey()
                InjectPinKey()
                Spacer(modifier = Modifier.height(height = 10.dp))
                FlowRow(maxItemsInEachRow = 2) {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        mainViewModel.injectResultMessage.split("*").forEach {
                            Text(
                                text = it,
                                color = if (it.contains("success")) Color.Green else Color.Red
                            )
                        }
                    }

                    OutlinedButton(onClick = {
                        coroutineScope.launch {
                            mainViewModel.postLogon()
                        }
                    }) {
                        Text(text = "Inject All Key")
                    }
                }

            }
        }
    }

    @Composable
    private fun EncryptDecrypt() {
        Box(
            modifier = Modifier
                .border(width = 1.dp, color = Color.Gray)
                .padding(vertical = 10.dp, horizontal = 10.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                EncryptData()
                Spacer(modifier = Modifier.height(height = 10.dp))
                DecryptData()
                Spacer(modifier = Modifier.height(height = 10.dp))
            }
        }
    }

    @Composable
    private fun DebitScreen() {
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                mainViewModel.openDebitScreen(this@MainActivity)
            }) {
            Text(text = "Debit Screen")
        }

    }

    @Composable
    private fun ShowPinpad() {
        OutlinedIconButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { mainViewModel.openPinpad() }) {
            Text(text = "Show pinpad")
        }
    }

    @Composable
    private fun Print() {
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                mainViewModel.print()
            }) {
            Text(text = "Print")
        }
    }

    @Composable
    private fun ReadCard() {
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = mainViewModel::readCard
        ) {
            Text(text = "READ CARD")
        }
    }

    @Composable
    private fun Test() {
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = {
            val dataJson = JsonObject().apply {
                addProperty("accountNum", "accountNum")
                addProperty("browser_agent", "browser_agent")
                addProperty("ip_address", "ip_address")
                addProperty("id_api", "id_api")
                addProperty("ip_server", "ip_server")
                addProperty("req_id", "req_id")
                addProperty("session", "session")
                addProperty("kode_loket", "kode_loket")
                addProperty("kode_mitra", "kode_mitra")
                addProperty("kode_cabang", "kode_cabang")
            }.toString()
            TermLog.d(TAG, dataJson)
        }) {
            Text(text = "TEST")
        }
    }
}


