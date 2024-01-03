package id.co.payment2go.terminalsdkhelper.payments.debit_card.ui.card_entry

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import id.co.payment2go.terminalsdkhelper.R
import id.co.payment2go.terminalsdkhelper.core_ui.composable.DialogFailure
import id.co.payment2go.terminalsdkhelper.core_ui.composable.LoadingAnimationDialog
import id.co.payment2go.terminalsdkhelper.core_ui.composable.ToolbarSection

//@Preview(showBackground = true)
@Composable
fun CardEntryScreen(
    title: String,
    isLoading: Boolean,
    loadingMessage: String,
    isCardInserted: Boolean,
    errorMessage: String,
    isPopUpShowing: Boolean,
    isTimeoutConnection: Boolean = false,
    onCardInserted: () -> Unit,
    onNavigateUp: (Boolean) -> Unit,
    setCardInserted: (Boolean) -> Unit,
) {
    if (isLoading) {
        LoadingAnimationDialog(
            message = loadingMessage,
            onDismissRequest = {}
        )
    }
    if (isPopUpShowing) {
        DialogFailure(
            isTimeoutConnection = isTimeoutConnection,
            text = errorMessage,
            onDismissButtonClicked = { onNavigateUp(true) }
        )
    }
    DisposableEffect("") {
        onDispose {
            setCardInserted(false)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 13.dp)
    ) {
        ToolbarSection(
            title = title,
            modifier = Modifier.fillMaxWidth(),
            onNavigateUp = { onNavigateUp(true) }
        )
        LogoSection(Modifier.align(Alignment.Center))

        Subtitle(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 28.dp, end = 28.dp, bottom = 116.dp)
        )

        LaunchedEffect(isCardInserted) {
            if (isCardInserted) {
                onCardInserted()
            }
        }
    }
}

@Composable
fun LogoSection(modifier: Modifier = Modifier) {

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(R.drawable.edc_android)
            .build(),
        imageLoader = ImageLoader.Builder(LocalContext.current)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build(),
        contentDescription = "EDC Android",
        modifier = modifier.size(280.dp)
    )
}

@Composable
fun Subtitle(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = "Gesek kartu debit nasabah Anda pada magnetik reader atau masukkan pada bagian bawah mesin EDC Android",
        fontFamily = Font(R.font.montserrat_medium, FontWeight.Normal).toFontFamily(),
        textAlign = TextAlign.Center
    )
}