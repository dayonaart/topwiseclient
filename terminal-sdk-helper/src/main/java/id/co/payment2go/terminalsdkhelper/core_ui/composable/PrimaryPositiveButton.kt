package id.co.payment2go.terminalsdkhelper.core_ui.composable

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.co.payment2go.terminalsdkhelper.R

@Composable
fun PrimaryPositiveButton(
    modifier: Modifier = Modifier,
    text: String = "Lanjut",
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0XFFE55300)
        )
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontFamily = Font(R.font.montserrat_bold, FontWeight.Normal).toFontFamily()
        )
    }
}