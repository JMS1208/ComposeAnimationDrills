package com.capstone.dragdrop.ui.screen

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.tooling.preview.Preview
import com.capstone.composeanimationdrills.extension.randomText


@Preview(showBackground = true)
@Composable
private fun Preview() {

    var position: Offset by remember {
        mutableStateOf(Offset.Zero)
    }

    var isDragging by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .onGloballyPositioned {
                    position = it.positionInParent()
                }
                .graphicsLayer {
                    if(isDragging) {
                        translationX = position.x
                        translationY = position.y
                    }
                }
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
                text = String.randomText(20)
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