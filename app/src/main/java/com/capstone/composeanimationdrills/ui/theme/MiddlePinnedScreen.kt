package com.capstone.composeanimationdrills.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import kotlinx.coroutines.launch
import kotlin.math.abs

@Preview(showBackground = true)
@Composable
private fun Preview() {
    MiddlePinnedScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiddlePinnedScreen() {
    fun getItems(): List<String> {

        return listOf("드라마", "영화", "멜로", "코믹", "액션", "스릴", "가족", "재난", "감동")
    }

    val titles = remember {
        getItems()
    }


    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(
                    Color.Black
                ),
            contentAlignment = Alignment.Center
        ) {

            val lazyState = rememberLazyListState(Int.MAX_VALUE / 2)

            LazyColumn(
                state = lazyState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(32.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(count = Int.MAX_VALUE) {
                    val idx = it % titles.size
                    TitleItem(titles[idx], lazyState)
                }
            }
        }

    }
}

@Composable
private fun TitleItem(
    title: String,
    state: LazyListState
) {


    val layoutInfo by remember { derivedStateOf { state.layoutInfo } }

    var isCenterPosition by remember {
        mutableStateOf(false)
    }

    var alphaState by remember {
        mutableStateOf(0.8f)
    }

    val textStyle = TextStyle(
        color = Color.White,
        fontSize = MaterialTheme.typography.titleLarge.fontSize,
        fontWeight = if (isCenterPosition) FontWeight.Bold else FontWeight.Normal
    )
    val scaleState = remember {
        Animatable(initialValue = 1f)
    }

    LaunchedEffect(isCenterPosition) {

        launch {
            if (isCenterPosition) {
                scaleState.animateTo(
                    targetValue = 1.5f,
                    animationSpec = tween(
                        durationMillis = 50
                    )
                )
            } else {
                scaleState.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 50
                    )
                )
            }
        }

    }

    CompositionLocalProvider(
        LocalTextStyle provides textStyle
    ) {

        Column(
            modifier = Modifier
                .onGloballyPositioned {
                    it
                        .boundsInParent()
                        .let { rect ->
                            isCenterPosition =
                                rect.contains(layoutInfo.viewportSize.center.toOffset())
                            alphaState = 1f - abs(
                                rect.center.y - layoutInfo.viewportSize.center.y
                            ) / (layoutInfo.viewportSize.height / 2)
                        }
                }
                .graphicsLayer {
                    alpha = if (isCenterPosition) 1f else alphaState
//                    scaleX = if (isCenterPosition) 1.5f else 1f
//                    scaleY = if (isCenterPosition) 1.5f else 1f
                    scaleX = scaleState.value
                    scaleY = scaleState.value
                }
                .fillMaxWidth(0.8f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title)
        }

    }

}