package com.capstone.composeanimationdrills.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.capstone.composeanimationdrills.extension.randomText
import com.example.compose.MyCustomTheme
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@Preview(showBackground = true)
@Composable
private fun Preview() {

    var position: Offset by remember {
        mutableStateOf(Offset.Zero)
    }

    var isDragging by remember {
        mutableStateOf(false)
    }

    val textState by remember {
        mutableStateOf(String.randomText(20))
    }

    val scaleState = remember {
        Animatable(initialValue = 1f)
    }

    LaunchedEffect(isDragging) {
        launch {
            scaleState.animateTo(
                targetValue = 0.9f,
                animationSpec = tween(
                    durationMillis = 50
                )
            )
            scaleState.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessHigh
                )
            )
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    if(isDragging) {
                        scaleX = scaleState.value
                        scaleY = scaleState.value
                    }
                    translationX = position.x
                    translationY = position.y

                }
                .background(
                    color = if (isDragging) Black.copy(
                        alpha = 0.1f
                    ) else Color.Transparent
                )
                .pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onDragEnd = {
                            isDragging = false
                        },
                        onDrag = { change: PointerInputChange, dragAmount: Offset ->
                            change.consume()
                            position += Offset(dragAmount.x, dragAmount.y)
                        }
                    )
                }

        ) {
            Text(
                text = textState
            )
        }

    }
}

@Composable
fun DraggableScreen(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    /*
    Screen 단위로 DragAndFollowInfo() 생김
     */
    val state = remember { DragAndFollowInfo() }

    CompositionLocalProvider(
        LocalDragAndFollowInfo provides state
    ) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            content()

        }
    }
}

val LocalDragAndFollowInfo = compositionLocalOf {
    DragAndFollowInfo()
}

class DragAndFollowInfo {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition: Offset by mutableStateOf(Offset.Zero)
    var dragOffset: Offset by mutableStateOf(Offset.Zero)
    var draggableComposable by mutableStateOf<(@Composable () -> Unit)?>(null)
    var data: Any? by mutableStateOf(null)
}