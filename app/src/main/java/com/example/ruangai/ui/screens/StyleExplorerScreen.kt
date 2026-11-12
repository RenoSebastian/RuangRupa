package com.example.ruangai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ruangai.domain.model.DesignStyle
import com.example.ruangai.ui.components.AppHeader
import com.example.ruangai.ui.components.DesignStyleCard
import com.example.ui.theme.*

@Composable
fun StyleExplorerScreen(
    onSelectStyle: (DesignStyle) -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val allStyles = DesignStyle.values()

    Scaffold(
        topBar = {
            AppHeader(
                title = "Katalog Gaya Interior",
                subtitle = "10 Gaya Terpilih dengan Karakter & Palet Warna",
                onBackClick = onBack
            )
        },
        containerColor = WarmLinen,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 80.dp,
                start = 20.dp,
                end = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    text = "Pilih gaya interior favorit untuk diterapkan pada ruangan Anda:",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                )
            }

            items(allStyles) { style ->
                DesignStyleCard(
                    style = style,
                    onClick = onSelectStyle
                )
            }
        }
    }
}
