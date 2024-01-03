package id.co.payment2go.terminalsdkhelper.tarik_tunai

import TarikTunaiScreen
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import id.co.payment2go.terminalsdkhelper.R
import id.co.payment2go.terminalsdkhelper.core_ui.composable.DialogFailure
import id.co.payment2go.terminalsdkhelper.core_ui.composable.LoadingAnimationDialog
import id.co.payment2go.terminalsdkhelper.tarik_tunai.ui.CardEntryScreen
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class TarikTunaiActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val intentNavigation = intent.getStringExtra("NAVIGATION")
            val intentFld3 = intent.getStringExtra("FLD3")
            val intentFld4 = intent.getStringExtra("FLD4")
            val intentFld43 = intent.getStringExtra("FLD43")
            val intentFld48 = intent.getStringExtra("FLD48")
            val intentIdTransaction = intent.getStringExtra("ID_TRANSACTION")
            val intentBiayaLoket = intent.getStringExtra("BIAYA_LOKET")
            val intentTrxTypeId = intent.getStringExtra("TRX_TYPE_ID")
            val intentRefNum = intent.getStringExtra("REF_NUM")
            val intentNarasi = intent.getStringExtra("NARASI")
            val intentMustOffUs = intent.getBooleanExtra("MUST_OFF_US", false)

            val viewModel: TarikTunaiViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            val event = viewModel.eventFlow

            LaunchedEffect(key1 = intentNavigation) {
                if (intentNavigation == "pinEntry") {
                    if (intentFld4 != null) {
                        viewModel.setFld4(intentFld4)
                    }
                    navController.navigate(TarikTunaiScreen.PinEntry.route)
                }
                if (intentNavigation == "cardEntry") {
                    navController.navigate(TarikTunaiScreen.CardEntry.route)
                }

            }

            LaunchedEffect(
                intentFld3,
                intentFld43,
                intentFld48,
                intentMustOffUs,
                intentBiayaLoket,
                intentRefNum,
                intentNarasi,
                intentTrxTypeId,
                intentIdTransaction,
                intentNarasi
            ) {

                if (intentFld43 != null) {
                    viewModel.setFld43(intentFld43)
                }
                if (intentFld48 != null) {
                    viewModel.setFld48(intentFld48)
                }
                if (intentFld3 != null) {
                    viewModel.setFld3(intentFld3)
                }
                if (intentBiayaLoket != null) {
                    viewModel.setBiayaLoket(intentBiayaLoket)
                }
                if (intentRefNum != null) {
                    viewModel.setRefNum(intentRefNum)
                }
                if (intentIdTransaction != null) {
                    viewModel.setTransactionId(intentIdTransaction)
                }
                if (intentNarasi != null) {
                    viewModel.setNarasi(intentNarasi)
                }
                if (intentTrxTypeId != null) {
                    viewModel.setTrxTypeId(intentTrxTypeId)
                }
                if (intentNarasi != null) {
                    viewModel.setNarasi(intentNarasi)
                }
                viewModel.setMustOffUs(intentMustOffUs)
            }

            LaunchedEffect(key1 = true) {
                event.collectLatest {
                    finish()
                }
            }

            NavHost(
                navController = navController,
                startDestination = TarikTunaiScreen.CardEntry.route
            ) {
                composable(TarikTunaiScreen.CardEntry.route) {
                    LaunchedEffect(true) {
                        viewModel.searchCard()
                    }
                    CardEntryScreen(
                        isLoading = state.isLoading,
                        loadingMessage = state.loadingMessage,
                        errorMessage = state.popUpMessage,
                        isPopUpShowing = state.isErrorPopUpShowing,
                        onNavigateUp = viewModel::cancelTransaction,
                    )
                }
                composable(TarikTunaiScreen.PinEntry.route) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                        LaunchedEffect(true) {
                            viewModel.eventFlow.collectLatest {
                                finish()
                            }
                        }

                        LaunchedEffect("showPinpad") {
                            viewModel.showPinpad()
                        }

                        if (state.isErrorPopUpShowing) {
                            DialogFailure(
                                text = state.popUpMessage,
                                onDismissButtonClicked = {
                                    viewModel.dismissPopUp()
                                    viewModel.showPinpad()
                                }
                            )
                        }

                        if (state.isLoading) {
                            LoadingAnimationDialog(
                                message = state.loadingMessage,
                                onDismissRequest = {}
                            )
                        }

                        Text(
                            text = "Masukkan PIN Kartu Debit\nNasabah Anda",
                            textAlign = TextAlign.Center,
                            fontFamily = Font(R.font.montserrat_bold).toFontFamily(),
                            modifier = Modifier.padding(top = 48.dp)
                        )
                    }
                }
            }
        }
    }

    override fun finish() {
        setResult(
            Activity.RESULT_OK,
            Intent().also {
                it.putExtra("code", this::class.java.name)
            })
        super.finish()
    }
}