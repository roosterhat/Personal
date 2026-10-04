package com.example.armcontrol.ui

import android.graphics.drawable.Icon
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SlideUpTabPanel(
    tabs: List<BottomTabItem>,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var displayIndex by remember { mutableStateOf<Int?>(null) }

    // Only update the rendered content when a tab is actually opened —
    // never clear it just because selectedIndex went to null on close,
    // so the exit animation has something to slide away.
    LaunchedEffect(selectedIndex) {
        if (selectedIndex != null) {
            displayIndex = selectedIndex
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        AnimatedVisibility(
            modifier = Modifier.padding(top = 12.dp),
            visible = selectedIndex != null,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(250)
            ) + fadeIn(tween(200)),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(200)
            ) + fadeOut(tween(150))
        ) {
            val tab = displayIndex?.let { tabs[it] }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (tab?.height != null) Modifier.height(tab.height)
                        else Modifier.heightIn(max = 320.dp)
                    ),
                tonalElevation = 6.dp,
                shadowElevation = 12.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .then(if (tab?.scrollable != false) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                ) {
                    tab?.content?.invoke()
                }
            }
        }

        NavigationBar {
            tabs.forEachIndexed { index, tab ->
                NavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = if (selectedIndex == index) null else index },
                    icon = { if( tab.icon != null) Icon(tab.icon, contentDescription = tab.label) },
                    //label = { Text(tab.label) },
                    modifier = Modifier.height(30.dp).padding(0.dp)
                )
            }
        }
    }
}

data class BottomTabItem(
    val label: String,
    val scrollable: Boolean = true,
    val height: Dp? = null,
    val content: @Composable () -> Unit,
    val icon: ImageVector? = null
)