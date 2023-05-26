package com.capstone.composeanimationdrills

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Paint.Align
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.capstone.composeanimationdrills.utils.FileUtils
import com.example.compose.MyCustomTheme

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
                    IntentTestScreen()
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

            textState = try {
                FileUtils.readFileFromUri(context, uri)
            } catch (e: Exception) {
                e.printStackTrace()
                "파일을 디코딩 할 수 없어요 :("
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
                launcher.launch(arrayOf("*/*"))
            }
        ) {
            Text("클릭")
        }

        Text(
            text = textState
        )
    }

}