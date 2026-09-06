package com.tripping.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun RouteMenuSheet(
    onDismiss: () -> Unit,
    onCreateRoute: () -> Unit,
    onViewRoute: () -> Unit
){

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 10.dp,
                end = 10.dp,
                bottom = 90.dp
            )
            .background(
                Color.White,
                RoundedCornerShape(8.dp)
            )
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
                tint = Color(0xFF0074CE)
            )

            Spacer(
                Modifier.width(12.dp)
            )

            Text(
                "루트 보기",
                modifier = Modifier.weight(1f)
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
                contentDescription = null,
                tint = Color(0xFF0074CE)
            )

            Spacer(
                Modifier.width(12.dp)
            )

            Text(
                "루트 생성",
                modifier = Modifier.weight(1f)
            )

            Icon(
                Icons.Default.KeyboardArrowRight,
                null
            )
        }
    }
}