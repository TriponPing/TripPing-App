package com.tripping.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun RouteMenuDialog(
    onDismiss: () -> Unit,
    onCreateRoute: () -> Unit,
    onViewRoute: () -> Unit
){

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                onDismiss()
            },
        contentAlignment = Alignment.BottomCenter
    ){

        Column(
            modifier = Modifier
                .width(360.dp)
                .background(
                    Color.White,
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ){

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onViewRoute()
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ){

                Icon(
                    Icons.Default.Map,
                    contentDescription = null,
                    tint = Color(0xFF0066FF)
                )

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Text(
                    text="루트 보기",
                    modifier=Modifier.weight(1f)
                )

                Icon(
                    Icons.Default.KeyboardArrowRight,
                    null
                )
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onCreateRoute()
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ){

                Icon(
                    Icons.Default.AddCircleOutline,
                    contentDescription=null,
                    tint=Color(0xFF0066FF)
                )

                Spacer(
                    modifier=Modifier.width(12.dp)
                )


                Text(
                    text="루트 생성",
                    modifier=Modifier.weight(1f)
                )


                Icon(
                    Icons.Default.KeyboardArrowRight,
                    null
                )
            }
        }
    }
}