package id.co.payment2go.terminalsdkhelper.check_balance

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import id.co.payment2go.terminalsdkhelper.check_balance.ui.CardEntryScreen
import id.co.payment2go.terminalsdkhelper.check_balance.ui.CheckBalanceScreen
import id.co.payment2go.terminalsdkhelper.check_balance.ui.CheckBalanceViewModel
import id.co.payment2go.terminalsdkhelper.check_balance.ui.SelectEntryScreen
import id.co.payment2go.terminalsdkhelper.core_ui.EventUi
import id.co.payment2go.terminalsdkhelper.core_ui.composable.DialogFailure
import id.co.payment2go.terminalsdkhelper.core_ui.composable.LoadingAnimationDialog
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class CheckBalanceActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BackHandler {

            }
            val navController = rememberNavController()
            val systemUiController = rememberSystemUiController()

            val transactionIdIntent = intent.getStringExtra("ID_TRANSACTION")
            val refNumberIntent = intent.getStringExtra("REF_NUM")
            val narasiIntent = intent.getStringExtra("NARASI")
            val mustOffUsIntent = intent.getBooleanExtra("MUST_OFF_US", false)

            val viewModel: CheckBalanceViewModel = hiltViewModel()
            val eventFlow = viewModel.eventFlow
            val state by viewModel.state.collectAsState()

            LaunchedEffect(true) {
                systemUiController.setSystemBarsColor(color = Color.White, darkIcons = true)
                viewModel.setTransactionId(transactionIdIntent ?: "")
                viewModel.setRefNum(refNumberIntent ?: "")
                viewModel.setDescription(narasiIntent ?: "")
                viewModel.setMustOffUs(mustOffUsIntent)
            }

            NavHost(navController, startDestination = CheckBalanceScreen.CardEntry.route) {
                composable(
                    route = CheckBalanceScreen.CardEntry.route
                ) {

                    LaunchedEffect(true) {
                        eventFlow.collectLatest {
                            when (it) {
                                is EventUi.RetryPinEntry -> {
                                    viewModel.showPinpad()
                                }

                                is EventUi.Finish -> {
                                    finish()
                                }
                            }
                        }
                    }
                    CardEntryScreen(
                        isCardInserted = state.isCardInserted,
                        onCardInserted = {
                            navController.navigate(CheckBalanceScreen.SelectEntry.route)
                        },
                        isLoading = state.isLoading,
                        loadingMessage = state.loadingMessage,
                        errorMessage = state.popUpMessage,
                        isPopUpShowing = state.isPopUpShowing,
                        setCardInserted = viewModel::setCardInserted,
                        onNavigateUp = viewModel::cancelTransaction,
                    )
                }
                composable(route = CheckBalanceScreen.SelectEntry.route) {
                    SelectEntryScreen(onSelected = { fld3 ->
                        viewModel.setFld3(fld3)
                        navController.navigate(CheckBalanceScreen.PinEntry.route)
                    })
                }
                composable(route = CheckBalanceScreen.PinEntry.route) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                        LaunchedEffect(true) {
                            eventFlow.collectLatest {
                                when (it) {
                                    is EventUi.RetryPinEntry -> {
                                        viewModel.showPinpad()
                                    }

                                    is EventUi.Finish -> {
                                        finish()
                                    }
                                }
                            }
                        }

                        LaunchedEffect("showPinpad") {
                            viewModel.showPinpad()
                        }

                        if (state.isLoading) {
                            LoadingAnimationDialog(
                                message = state.loadingMessage,
                                onDismissRequest = {}
                            )
                        }

                        if (state.isPopUpShowing) {
                            DialogFailure(
                                text = state.popUpMessage,
                                onDismissButtonClicked = {
                                    viewModel.dismissPopUp()
                                    viewModel.showPinpad()
                                }
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