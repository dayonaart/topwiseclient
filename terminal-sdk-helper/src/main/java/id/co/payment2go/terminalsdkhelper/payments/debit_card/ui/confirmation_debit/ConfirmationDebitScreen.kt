package id.co.payment2go.terminalsdkhelper.payments.debit_card.ui.confirmation_debit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import id.co.payment2go.terminalsdkhelper.R
import id.co.payment2go.terminalsdkhelper.core_ui.composable.PrimaryPositiveButton
import java.text.DecimalFormat

@Composable
@Preview(showBackground = true)
fun ConfirmationDebitScreen(
    totalAmount: Long = 40000,
    binName: String = "BNI Debit",
    maskedCardNumber: String = "**********3462",
    onNextButtonClicked: () -> Unit = {},
    onCancelButtonClicked: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1.0f)) {
                TitleSection()
                AmountSection(totalAmount)
                Divider(thickness = 8.dp, color = Color(0xFFF5F5F5))
                Spacer(modifier = Modifier.height(16.dp))
                CardSection(
                    cardName = "BNI Debit",
                    cardType = binName,
                    maskedCardNumber = maskedCardNumber
                )
                InfoSection()
            }
            BottomBarSection(onNextButtonClicked, onCancelButtonClicked)
        }
    }
}

@Composable
fun BottomBarSection(onNextButtonClicked: () -> Unit, onCancelButtonClicked: () -> Unit) {
    Divider(thickness = 8.dp, color = Color(0xFFE3E3E3))
    Row(modifier = Modifier.padding(16.dp, 12.dp)) {
        Button(
            modifier = Modifier
                .weight(1.0f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF2C7381)
            ),
            onClick = onCancelButtonClicked
        ) {
            Text(text = "Batal")
        }
        PrimaryPositiveButton(modifier = Modifier.weight(1.0f), onClick = onNextButtonClicked)
    }
}

@Composable
fun InfoSection() {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .background(Color(0xFFF3F9F9), RoundedCornerShape(4.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_info),
            contentDescription = "Info",
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Pastikan kembali data transaksi yang dimasukkan sudah benar. Halaman ini adalah bagian terakhir dari konfirmasi transaksi nasabah.",
            fontSize = 12.sp,
            fontFamily = Font(
                R.font.montserrat_regular,
                FontWeight.Normal
            ).toFontFamily(),
            color = Color(0xFF2C7381),
            lineHeight = 16.sp,
            textAlign = TextAlign.Justify,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}

@Composable
fun CardSection(cardName: String, cardType: String, maskedCardNumber: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(12.dp))
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    val cardImage: Painter = painterResource(id = R.drawable.ic_card)
                    Image(painter = cardImage, contentDescription = "card logo")
                    Text(
                        text = "Kartu Debit",
                        fontSize = 14.sp,
                        fontWeight = FontWeight(600),
                        fontFamily = Font(
                            R.font.montserrat_regular,
                            FontWeight.Normal
                        ).toFontFamily(),
                        modifier = Modifier.padding(horizontal = 6.dp),
                    )
                }
                val secureImage: Painter = painterResource(id = R.drawable.ic_secure)
                Image(
                    painter = secureImage,
                    contentDescription = "Secure",
                )
            }
            Divider(thickness = 1.dp, color = Color(0xFFE3E3E3))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val cardThumbImage: Painter = painterResource(id = R.drawable.card_thumb_new)
                Image(
                    painter = cardThumbImage,
                    contentDescription = "Card Thumbnail",
                    modifier = Modifier.height(78.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = cardName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Font(
                            R.font.montserrat_regular,
                            FontWeight.Normal
                        ).toFontFamily(),
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = cardType,
                        fontSize = 12.sp,
                        fontWeight = FontWeight(400),
                        fontFamily = Font(
                            R.font.montserrat_regular,
                            FontWeight.Normal
                        ).toFontFamily(),
                        color = Color(0xFF343838)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = maskedCardNumber,
                        fontSize = 14.sp,
                        fontWeight = FontWeight(600),
                        fontFamily = Font(
                            R.font.montserrat_regular,
                            FontWeight.Normal
                        ).toFontFamily(),
                    )
                }
            }
        }
    }
}

@Composable
fun AmountSection(totalAmount: Long) {
    val amount = formatToRupiah(amount = totalAmount)

    Column(
        modifier = Modifier.padding(16.dp, 16.dp)
    ) {
        Text(
            text = "Nominal Transaksi",
            fontSize = 13.sp,
            fontWeight = FontWeight(600),
            fontFamily = Font(R.font.montserrat_medium, FontWeight.Normal).toFontFamily(),
            color = Color(0xFF9E9E9E),
        )
        Text(
            text = "Rp $amount",
            fontSize = 32.sp,
            fontWeight = FontWeight(600),
            fontFamily = Font(R.font.montserrat_medium, FontWeight.Normal).toFontFamily(),
            color = Color(0xFF424242),
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFFFEFFE6), shape = RoundedCornerShape(4.dp))
                .padding(8.dp)

        ) {
            Row {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.explaination_mark)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.offset(y = 4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Berikut adalah total bayar yang akan didebet dari Rekening Anda untuk pembayaran ke BNI Agen46",
                    fontSize = 12.sp,
                    fontFamily = Font(R.font.montserrat_medium, FontWeight.Normal).toFontFamily(),
                    color = Color(0xFF876800)
                )
            }
        }

    }
}

@Composable
fun TitleSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier
                    .padding(8.dp, 16.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight(500),
                fontFamily = Font(R.font.montserrat_medium).toFontFamily(),
                text = "Konfirmasi Transaksi",
            )
        }
    }
}

fun formatToRupiah(amount: Long): String {
    val number = amount.toString().toLongOrNull() ?: 0
    val formatter = DecimalFormat("#,###")
    val symbols = formatter.decimalFormatSymbols
    symbols.groupingSeparator = ','
    symbols.decimalSeparator = '.'
    formatter.decimalFormatSymbols = symbols
    return formatter.format(number)
}
