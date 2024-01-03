package id.co.payment2go.terminalsdkhelper.check_balance.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import id.co.payment2go.terminalsdkhelper.R

data class Item(
    @DrawableRes
    val icon: Int,
    val name: String,
    val fld3: String
)

@Composable
fun SelectEntryScreen(modifier: Modifier = Modifier, onSelected: (String) -> Unit) {
    val items = remember {
        mutableStateListOf(
            Item(icon = R.drawable.tabungan, name = "Tabungan", fld3 = "311000"),
            Item(icon = R.drawable.giro, name = "Giro", fld3 = "312000")
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()

    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "Pilih Jenis Tabungan",
                fontSize = 20.sp,
                fontFamily = Font(R.font.montserrat_medium).toFontFamily(),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items.forEach { item ->
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(start = 16.dp)
                    .clickable { onSelected(item.fld3) }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.icon)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(if (item.icon == R.drawable.giro) 40.dp else 48.dp)
                        .offset(y = (-8).dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // the list
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.name,
                            fontSize = 16.sp,
                            fontFamily = Font(R.font.montserrat_medium).toFontFamily()
                        )
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(R.drawable.greater_than)
                                .size(1024)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .offset(x = (-8).dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp
                    )
                }
            }
        }

    }
}