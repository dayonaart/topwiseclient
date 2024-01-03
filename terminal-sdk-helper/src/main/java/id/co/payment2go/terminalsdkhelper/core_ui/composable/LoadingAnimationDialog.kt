package id.co.payment2go.terminalsdkhelper.core_ui.composable

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import id.co.payment2go.terminalsdkhelper.R

@Composable
fun LoadingAnimationDialog(
    modifier: Modifier = Modifier,
    message: String,
    colors: List<Color> = listOf(
        Color(255, 193, 0),
        Color(255, 154, 0),
        Color(255, 116, 0)
    ),
    animationDuration: Int = 1000,
    initialRadius: Dp = 10.dp,
    maxRadius: Dp = 30.dp,
    onDismissRequest: () -> Unit
) {
    val transition = rememberInfiniteTransition()
    val color = remember { colors }
    val animatedRadii = color.indices.map { index ->
        transition.animateFloat(
            initialValue = with(LocalDensity.current) { initialRadius.toPx() },
            targetValue = with(LocalDensity.current) { maxRadius.toPx() },
            animationSpec = infiniteRepeatable(
                tween(animationDuration, delayMillis = index * 300, easing = FastOutSlowInEasing),
                RepeatMode.Reverse
            ),
            label = ""
        )
    }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = modifier
                .wrapContentWidth()
                .height(200.dp)
                .background(color = Color.White, shape = RoundedCornerShape(6.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Canvas(
                    modifier = Modifier.size(maxRadius.times(2))
                ) {
                    for (i in color.indices) {
                        drawCircle(
                            color = color[i],
                            radius = animatedRadii[i].value,
                            center = center
                        )
                    }
                }
                Text(
                    text = message,
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.montserrat_medium))
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingAnimationDialogPreview() {
    LoadingAnimationDialog(message = "") {}
}