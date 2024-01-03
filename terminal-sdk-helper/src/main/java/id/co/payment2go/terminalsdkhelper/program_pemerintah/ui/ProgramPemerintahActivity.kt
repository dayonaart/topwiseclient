package id.co.payment2go.terminalsdkhelper.program_pemerintah.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import id.co.payment2go.terminalsdkhelper.R
import id.co.payment2go.terminalsdkhelper.core_ui.EventUi
import id.co.payment2go.terminalsdkhelper.core_ui.composable.DialogFailure
import id.co.payment2go.terminalsdkhelper.core_ui.composable.LoadingAnimationDialog
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class ProgramPemerintahActivity : ComponentActivity() {

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val fld3Intent = intent.getStringExtra("FLD3")
                ?: throw IllegalArgumentException("FLD3 can't be null")
            val fld48Intent = intent.getStringExtra("FLD48")
                ?: throw IllegalArgumentException("FLD48 can't be null")
            val transactionIdIntent = intent.getStringExtra("ID_TRANSACTION")
            val descriptionIntent = intent.getStringExtra("Narasi")
            val refNumberIntent = intent.getStringExtra("REFNO")

            val navController = rememberNavController()
            val systemUiController = rememberSystemUiController()

            LaunchedEffect(true) {
                systemUiController.setSystemBarsColor(color = Color.White, darkIcons = true)
            }

            val viewModel: ProgramPemerintahViewModel = hiltViewModel()

            NavHost(
                navController = navController,
                startDestination = ProgramPemerintahScreen.CardEntry.route
            ) {

                composable(ProgramPemerintahScreen.CardEntry.route) {
                    LaunchedEffect("sendEvent") {
                        viewModel.eventFlow.collectLatest { eventUi ->
                            when (eventUi) {
                                is EventUi.Finish -> finish()
                                is EventUi.RetryPinEntry -> {}
                            }
                        }
                    }

                    LaunchedEffect(true) {
                        viewModel.setFld3(fld3Intent)
                        viewModel.setFld48(fld48Intent)
                        viewModel.setTransactionId(transactionIdIntent ?: "")
                        viewModel.setRefNum(refNumberIntent ?: "")
                        viewModel.setDescription(descriptionIntent ?: "")
                    }

                    val state by viewModel.state.collectAsState()
                    CardEntryScreen(
                        isLoading = state.isLoading,
                        loadingMessage = state.loadingMessage,
                        isCardInserted = state.isCardInserted,
                        errorMessage = state.popUpMessage,
                        isPopUpShowing = state.isErrorPopUpShowing,
                        setCardInserted = viewModel::setCardInserted,
                        onNavigateUp = viewModel::cancelTransaction,
                        onCardInserted = { navController.navigate(ProgramPemerintahScreen.PinEntry.route) },
                    )
                }
                composable(ProgramPemerintahScreen.PinEntry.route) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                        val state by viewModel.state.collectAsState()
                        LaunchedEffect(true) {
                            viewModel.eventFlow.collectLatest {
                                when (it) {
                                    is EventUi.Finish -> finish()
                                    is EventUi.RetryPinEntry -> viewModel.showPinpad()
                                }
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