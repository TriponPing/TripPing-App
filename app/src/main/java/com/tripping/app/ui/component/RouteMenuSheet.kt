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
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.font.FontWeight


@Composable
fun RouteMenuSheet(
    onDismiss: () -> Unit,
    onCreateRoute: () -> Unit,
    onViewRoute: () -> Unit,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .padding(
                start = 70.dp,
                end = 70.dp,
                bottom = 120.dp
            )
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .background(
                Color.White,
                RoundedCornerShape(16.dp)
            )
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
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
                Modifier.width(15.dp)
            )

            Text(
                "루트 보기",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Icon(
                Icons.Default.KeyboardArrowRight,
                null
            )
        }

        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)

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
                Modifier.width(15.dp)
            )

            Text(
                "루트 생성",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Icon(
                Icons.Default.KeyboardArrowRight,
                null
            )
        }
    }
}