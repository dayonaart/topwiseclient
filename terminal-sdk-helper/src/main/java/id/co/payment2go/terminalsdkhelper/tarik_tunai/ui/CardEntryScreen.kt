package id.co.payment2go.terminalsdkhelper.tarik_tunai.ui

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    isLoading: Boolean,
    loadingMessage: String,
    errorMessage: String,
    isPopUpShowing: Boolean,
    onNavigateUp: (Boolean) -> Unit,
) {
    if (isLoading) {
        LoadingAnimationDialog(
            message = loadingMessage,
            onDismissRequest = {}
        )
    }
    if (isPopUpShowing) {
        DialogFailure(
            text = errorMessage,
            onDismissButtonClicked = { onNavigateUp(true) }
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 13.dp)
    ) {
        ToolbarSection(
            "",
            modifier = Modifier.fillMaxWidth(),
            onNavigateUp = { onNavigateUp(true) }
        )
        LogoSection(Modifier.align(Alignment.Center))

        Subtitle(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 28.dp, end = 28.dp, bottom = 116.dp)
        )
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
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        letterSpacing = 0.20.sp
    )
}