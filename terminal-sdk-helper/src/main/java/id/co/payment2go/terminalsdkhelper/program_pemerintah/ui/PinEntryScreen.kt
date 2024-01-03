package id.co.payment2go.terminalsdkhelper.program_pemerintah.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.co.payment2go.terminalsdkhelper.R
import id.co.payment2go.terminalsdkhelper.core_ui.composable.DialogFailure
import id.co.payment2go.terminalsdkhelper.core_ui.composable.LoadingAnimationDialog
import id.co.payment2go.terminalsdkhelper.core_ui.composable.ToolbarSection

@Composable
//@Preview(showBackground = true)
fun PinEntryScreen(
    isLoading: Boolean,
    pins: List<Char>,
    isPopUpShowing: Boolean,
    errorMessage: String,
    loadingMessage: String,
    onPinClicked: (Char) -> Unit,
    onDeletePin: () -> Unit,
    onNavigateUp: () -> Unit,
    onDialogFailure: () -> Unit,
) {
    if (isLoading) {
        LoadingAnimationDialog(message = loadingMessage, onDismissRequest = {})
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f)) {
            ToolbarSection(modifier = Modifier.fillMaxWidth(), onNavigateUp = onNavigateUp)
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Masukkan PIN Kartu Debit Nasabah Anda",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontFamily = Font(R.font.montserrat_bold).toFontFamily(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
            PinFieldSection(pins)
        }
        Column(
            Modifier.weight(1f)
        ) {
            PinInputSection(onPinClicked, onDeletePin)
        }

        if (isPopUpShowing) {
            DialogFailure(
                text = errorMessage,
                onDismissButtonClicked = onDialogFailure
            )
        }
    }
}

@Composable
fun PinField(text: Char = ' ', modifier: Modifier) {
    Box(
        modifier = modifier
            .background(color = Color(0xFFF5FBFC), shape = RoundedCornerShape(6.dp))
            .border(width = 1.dp, color = Color(0xFFB9D0D5), shape = RoundedCornerShape(6.dp))
            .padding(12.dp), contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.toString(),
            fontFamily = Font(R.font.montserrat_medium).toFontFamily(),
            fontSize = 20.sp,
        )
    }
}

@Composable
fun PinFieldSection(pins: List<Char>) {
    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
        repeat(6) { index ->
            Spacer(modifier = Modifier.width(8.dp))

            val pinSize = pins.size
            val pinValue = if (index < pinSize && pinSize > 0) '*' else ' '

            PinField(
                text = pinValue, modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun NumberButton(text: Char, modifier: Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF3B3B3B),
        ),
        modifier = modifier,
        onClick = onClick,
        enabled = enabled
    ) {
        Text(
            text = text.toString(),
            fontSize = 20.sp,
            fontFamily = Font(R.font.montserrat_medium).toFontFamily(),
        )
    }
}

@Composable
fun VerticalDivider(color: Color = Color(0xFFE3E3E3)) {
    Divider(
        Modifier
            .fillMaxHeight()
            .width(1.dp), color = color
    )
}

@Composable
fun HorizontalDivider(color: Color = Color(0xFFE3E3E3)) {
    Divider(
        Modifier
            .fillMaxWidth()
            .height(1.dp), color = color
    )
}


@Composable
fun PinInputSection(
    onPinClicked: (Char) -> Unit, onDeletePin: () -> Unit
) {
    Column(Modifier.padding(16.dp, 24.dp)) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            NumberButton(text = '1', modifier = Modifier
                .weight(1f)
                .fillMaxHeight(), onClick = {
                onPinClicked('1')
            })
            VerticalDivider()
            NumberButton(text = '2', modifier = Modifier
                .weight(1f)
                .fillMaxHeight(), onClick = {
                onPinClicked('2')
            })
            VerticalDivider()
            NumberButton(text = '3', modifier = Modifier
                .weight(1f)
                .fillMaxHeight(), onClick = {
                onPinClicked('3')
            })
        }
        HorizontalDivider()
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            NumberButton(text = '4', modifier = Modifier
                .weight(1f)
                .fillMaxHeight(), onClick = {
                onPinClicked('4')
            })
            VerticalDivider()
            NumberButton(text = '5', modifier = Modifier
                .weight(1f)
                .fillMaxHeight(), onClick = {
                onPinClicked('5')
            })
            VerticalDivider()
            NumberButton(text = '6', modifier = Modifier
                .weight(1f)
                .fillMaxHeight(), onClick = {
                onPinClicked('6')
            })
        }
        HorizontalDivider()
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            NumberButton(text = '7', modifier = Modifier
                .weight(1f)
                .fillMaxHeight(), onClick = {
                onPinClicked('7')
            })
            VerticalDivider()
            NumberButton(text = '8', modifier = Modifier
                .weight(1f)
                .fillMaxHeight(), onClick = {
                onPinClicked('8')
            })
            VerticalDivider()
            NumberButton(text = '9', modifier = Modifier
                .weight(1f)
                .fillMaxHeight(), onClick = {
                onPinClicked('9')
            })
        }
        HorizontalDivider()
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            val deleteImage = painterResource(id = R.drawable.ic_delete)

            NumberButton(text = ' ',
                modifier = Modifier
                    .weight(1f)
                    .alpha(0f),
                enabled = false,
                onClick = {})
            VerticalDivider()
            NumberButton(text = '0', modifier = Modifier
                .weight(1f)
                .fillMaxHeight(), onClick = {
                onPinClicked('0')
            })
            VerticalDivider()
            Button(colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = {
                    onDeletePin()
                }) {
                Image(
                    painter = deleteImage, contentDescription = "description"
                )
            }
        }
    }
}