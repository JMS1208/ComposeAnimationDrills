package com.capstone.composeanimationdrills.ui.theme

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalloonScreen() {

    var isShowing by remember {
        mutableStateOf(false)
    }

    var buttonPosition by remember {
        mutableStateOf(Offset.Zero)
    }

    LaunchedEffect(buttonPosition) {
        Log.e("TAG", "BalloonScreen: $buttonPosition")
    }

    var balloonPosition by remember {
        mutableStateOf(Offset.Zero)
    }

    var balloonSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Title", modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            Text(
                "Stripe", modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            Text(
                "Title", modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            Text(
                "Stripe", modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start)
            ) {
                Text("Charge Processing Fee to Payers")

                Icon(
                    imageVector = Icons.Default.Info,
                    null,
                    modifier = Modifier
                        .onGloballyPositioned {
                            buttonPosition = it.boundsInRoot().bottomCenter
                        }
                        .clickable {
                            isShowing = !isShowing
                        }
                )
            }
        }

        AnimatedVisibility(visible = isShowing) {
            Popup(
                offset = IntOffset(buttonPosition.x.toInt(), buttonPosition.y.toInt())
            ) {

                Box(
                    modifier = Modifier
                        .onGloballyPositioned {
                            balloonPosition = it.positionInRoot()
                        }
                        .graphicsLayer {

                            val leftWidth = buttonPosition.x - balloonPosition.x

                            val tailSize = 32f

                            shadowElevation = 5f

                            shape = GenericShape { size, _ ->

                                moveTo(0f, tailSize/2* sqrt(3f))
                                lineTo(0f, size.height+tailSize/2* sqrt(3f))
                                lineTo(size.width, size.height+tailSize/2* sqrt(3f))
                                lineTo(size.width, tailSize/2* sqrt(3f))
                                lineTo(leftWidth+tailSize/2, tailSize/2* sqrt(3f))
                                lineTo(leftWidth, 0f)
                                lineTo(leftWidth-tailSize/2, tailSize/2* sqrt(3f))

                            }


                        }
                        .padding(top = 32.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
                ) {
                    Text("Band doesn't charge any fees.\nHowever, Stripe charges a processing fee per transaction")
                }
//            }
            }
        }

    }
}