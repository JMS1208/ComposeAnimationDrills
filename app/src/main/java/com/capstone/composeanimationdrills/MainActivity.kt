package com.capstone.composeanimationdrills

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.capstone.composeanimationdrills.extension.randomText
import com.capstone.composeanimationdrills.ui.theme.BalloonScreen
import com.capstone.composeanimationdrills.utils.AssSubtitle
import com.capstone.composeanimationdrills.utils.FileDecoder
import com.capstone.composeanimationdrills.utils.SrtSubtitle
import com.example.compose.MyCustomTheme
import kotlin.math.absoluteValue
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyCustomTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {//
//                    DragFollowPreview()
//                    IntentTestScreen()
                    BalloonScreen()
                }
            }
        }
    }
}




@Composable
private fun IntentTestScreen() {

    val context = LocalContext.current

    var textState by remember {
        mutableStateOf("")
    }

    val coroutine = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri ?: return@rememberLauncherForActivityResult

            try {
//                FileUtils.readFileFromUri(context, uri)
                val result = FileDecoder(context).uriToSubTitle(uri)

                textState = when (result) {
                    is AssSubtitle -> {
                        when (result.scriptInfo.blockState) {
                            AssSubtitle.Block.BlockState.BlockNoName -> "이름이 지정되지 않았어요 ${result.scriptInfo}"
                            is AssSubtitle.Block.BlockState.InputDataEmpty -> "${(result.scriptInfo.blockState as AssSubtitle.Block.BlockState.InputDataEmpty).message} ${result.scriptInfo}"
                            AssSubtitle.Block.BlockState.Normal -> "정상 $result"
                        }

                    }

                    is SrtSubtitle -> {
                        "${result.textLines}"
                    }

                    else -> {
                        "디코딩에 실패하였어요 :("
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                textState = "파일을 디코딩 할 수 없어요\n${e.message}"
            }

        })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                launcher.launch(arrayOf("*/*", "application/x-subrip"))
            }
        ) {
            Text("클릭")
        }

        Text(
            text = textState
        )
    }

}