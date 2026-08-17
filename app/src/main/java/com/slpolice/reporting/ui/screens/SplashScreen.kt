package com.slpolice.reporting.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.slpolice.reporting.ui.components.Eyebrow
import com.slpolice.reporting.ui.theme.BraidGold
import com.slpolice.reporting.ui.theme.Navy

@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.Shield,
            contentDescription = null,
            tint = BraidGold,
            modifier = Modifier.size(52.dp)
        )
        Spacer(Modifier.height(18.dp))
        Eyebrow("Sri Lanka Police", color = BraidGold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Digital Evidence Platform",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
        Spacer(Modifier.height(28.dp))
        CircularProgressIndicator(color = BraidGold, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
    }
}
