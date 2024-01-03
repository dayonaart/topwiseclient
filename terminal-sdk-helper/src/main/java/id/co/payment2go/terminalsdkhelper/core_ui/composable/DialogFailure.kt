package id.co.payment2go.terminalsdkhelper.core_ui.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import id.co.payment2go.terminalsdkhelper.R

@Composable
@Preview
fun DialogFailure(
    modifier: Modifier = Modifier,
    text: String = "Ini adalah contoh message",
    isTimeoutConnection: Boolean = false,
    onDismissButtonClicked: () -> Unit = {}
) {
    val annotatedText = if (text.contains("chip", true)) {
        buildAnnotatedString {
            append(text.substring(0, text.indexOf("chip"))) // Bagian teks sebelum kata "chip"
            withStyle(
                style = SpanStyle(
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    textDecoration = TextDecoration.None
                )
            ) {
                append("chip")
            }
            // Bagian teks setelah kata "chip"
            append(text.substring(text.indexOf("chip") + "chip".length))
        }
    } else {
        AnnotatedString(text)
    }

    Dialog(
        onDismissRequest = onDismissButtonClicked,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .align(Alignment.Center),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val imageFailure = if (isTimeoutConnection) {
                    painterResource(id = R.drawable.timeout_connection)
                } else {
                    painterResource(id = R.drawable.ic_wrong_pass_or_pin)
                }

                Image(
                    painter = imageFailure,
                    contentDescription = "Wrong Pin",
                    modifier = if (isTimeoutConnection) Modifier
                        .padding(top = 10.dp)
                        .size(200.dp) else Modifier
                        .fillMaxWidth()

                )
                Spacer(modifier = Modifier.height(16.dp))
                if (isTimeoutConnection) {
                    val timeoutMessage = annotatedText.split("=")
                    timeoutMessage.forEachIndexed { i, t ->
                        Text(
                            text = t,
                            fontFamily = Font(R.font.montserrat_medium).toFontFamily(),
                            textAlign = TextAlign.Center,
                            fontSize = if (i == 0) 15.sp else 14.sp,
                            color = Color.Black,
                            fontWeight = if (i == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                } else {
                    Text(
                        text = annotatedText,
                        fontFamily = Font(R.font.montserrat_medium).toFontFamily(),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                PrimaryPositiveButton(
                    text = if (isTimeoutConnection) "Mutasi Rekening" else "OK",
                    onClick = onDismissButtonClicked,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}