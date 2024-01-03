package com.example.paymentservice.core_ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import id.co.payment2go.terminalsdkhelper.R
import id.co.payment2go.terminalsdkhelper.core_ui.composable.PrimaryNegativeButton
import id.co.payment2go.terminalsdkhelper.core_ui.composable.PrimaryPositiveButton

@Preview(showBackground = true)
@Composable
fun DialogPopUp(
    modifier: Modifier = Modifier,
    text: String = "Apakah Anda yakin untuk melakukan pembayaran sebesar Rp328.000,-?",
    onPositiveButtonClicked: () -> Unit = {},
    onNegativeButtonClicked: () -> Unit = {},
    onDismissRequest: () -> Unit = {},
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside
        )
    ) {
        Box(
            modifier = modifier
                .width(300.dp)
                .height(168.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .align(Alignment.Center),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    text = text,
                    fontFamily = Font(R.font.montserrat_medium).toFontFamily(),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PrimaryNegativeButton(
                        text = "Batal",
                        onClick = onNegativeButtonClicked,
                        modifier = Modifier.width(124.dp)
                    )
                    PrimaryPositiveButton(
                        text = "Lanjut",
                        onClick = onPositiveButtonClicked,
                        modifier = Modifier.width(124.dp)
                    )
                }

            }

        }
    }
}