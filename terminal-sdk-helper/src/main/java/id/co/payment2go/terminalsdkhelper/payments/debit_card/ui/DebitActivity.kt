package id.co.payment2go.terminalsdkhelper.payments.debit_card.ui

import PaymentScreen
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import id.co.payment2go.terminalsdkhelper.payments.debit_card.ui.card_entry.CardEntryScreen
import id.co.payment2go.terminalsdkhelper.payments.debit_card.ui.confirmation_debit.ConfirmationDebitScreen
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class DebitActivity : ComponentActivity() {

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val totalAmountIntent = intent?.getStringExtra("TOTAL_AMOUNT")
        val transactionIdIntent = intent?.getStringExtra("TRANSACTION_ID")
        val descriptionIntent = intent?.getStringExtra("DESCRIPTION")
        val refNumIntent = intent?.getStringExtra("REF_NUM")
        setContent {
            val navController = rememberNavController()
            val systemUiController = rememberSystemUiController()

            var totalAmount by remember {
                mutableStateOf("")
            }
            var transactionId by remember {
                mutableStateOf("")
            }
            var description by remember {
                mutableStateOf("")
            }
            var refNum by remember {
                mutableStateOf("")
            }
            val viewModel: CardViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            val eventFlow = viewModel.eventFlow

            LaunchedEffect(true) {
                systemUiController.setSystemBarsColor(color = Color.White, darkIcons = true)
                eventFlow.collectLatest { eventUi ->
                    when (eventUi) {
                        is EventUi.Finish -> finish()
                        is EventUi.RetryPinEntry -> {}
                    }
                }
            }


            NavHost(navController, startDestination = PaymentScreen.CardEntry.route) {
                composable(
                    route = PaymentScreen.CardEntry.route
                ) {
                    BackHandler {
                    }
                    LaunchedEffect(true) {
                        if (totalAmountIntent != null && transactionIdIntent != null && descriptionIntent != null && refNumIntent != null) {
                            totalAmount = totalAmountIntent
                            transactionId = transactionIdIntent
                            description = descriptionIntent
                            refNum = refNumIntent
                        }
                        if (transactionId.isNotEmpty()) {
                            viewModel.setTransactionId(transactionId)
                        }
                        if (totalAmount.isNotEmpty()) {
                            viewModel.setAmount(totalAmount.toLongOrNull() ?: -1)
                        }
                        val encodedUrlString = description.replace("%20", " ")
                        viewModel.setDescription(encodedUrlString)
                        if (refNum.isNotEmpty()) {
                            viewModel.setRefNumber(refNum)
                        }
                    }
                    CardEntryScreen(
                        title = "Metode Pembayaran",
                        isCardInserted = state.isCardInserted,
                        onCardInserted = {
                            navController.navigate(PaymentScreen.PaymentConfirmationDebit.route)
                        },
                        isTimeoutConnection = state.isTimeoutConnection,
                        isLoading = state.isLoading,
                        loadingMessage = state.loadingMessage,
                        errorMessage = state.popUpMessage,
                        isPopUpShowing = state.isPopUpShowing,
                        setCardInserted = viewModel::setCardInserted,
                        onNavigateUp = viewModel::cancelTransaction,

                        )
                }
                composable(
                    route = PaymentScreen.PaymentConfirmationDebit.route,
                ) {

                    LaunchedEffect("sendEvent") {
                        viewModel.eventFlow.collect {
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
                    if (state.isPopUpShowing) {
                        DialogFailure(
                            text = state.popUpMessage,
                            isTimeoutConnection = state.isTimeoutConnection,
                            onDismissButtonClicked = {
                                viewModel.dismissPopUp()
                            }
                        )
                    }

                    ConfirmationDebitScreen(
                        totalAmount = state.totalAmount,
                        binName = state.checkedBin.binName,
                        maskedCardNumber = state.maskedCardNumber,
                        onNextButtonClicked = {
                            navController.navigate(PaymentScreen.PinEntry.route)
                        },
                        onCancelButtonClicked = {
                            viewModel.cancelTransaction(true)
                        }
                    )
                }
                composable(route = PaymentScreen.PinEntry.route) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                        LaunchedEffect(true) {
                            viewModel.eventFlow.collectLatest {
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
                                isTimeoutConnection = state.isTimeoutConnection,
                                text = state.popUpMessage,
                                onDismissButtonClicked = {
                                    if (state.isTimeoutConnection) {
                                        viewModel.dismissPopUp()
                                        viewModel.cancelTransaction(true)
                                    } else {
                                        viewModel.dismissPopUp()
                                        viewModel.showPinpad()
                                    }
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