package ai.tnj.haui.feature.home.ui

import ai.tnj.haui.core.ui.retroTerminalBackground
import ai.tnj.haui.feature.home.ui.agent.AgentTab
import ai.tnj.haui.feature.home.ui.chat.ChatTab
import ai.tnj.haui.feature.home.ui.settings.SettingsTab
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class TabItem(val label: String, val icon: ImageVector)

@Composable
fun HomeScreen() {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    
    val tabs = remember {
        listOf(
            TabItem("AGENT", Icons.Filled.SmartToy),
            TabItem("CHAT", Icons.Filled.Forum),
            TabItem("SETTINGS", Icons.Filled.Settings),
        )
    }

    Scaffold(
        bottomBar = {
            HAUIBottomBar(
                tabs = tabs,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .retroTerminalBackground(gridColor = MaterialTheme.colorScheme.primary)
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            when (selectedTab) {
                0 -> AgentTab(modifier = Modifier.fillMaxSize())
                1 -> ChatTab(modifier = Modifier.fillMaxSize())
                2 -> SettingsTab(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun HAUIBottomBar(
    tabs: List<TabItem>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.95f))
            .navigationBarsPadding()
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            thickness = 1.dp,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            tabs.forEachIndexed { index, tab ->
                HAUIBottomNavItem(
                    modifier = Modifier.weight(1f),
                    tab = tab,
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) }
                )
            }
        }
    }
}

@Composable
private fun HAUIBottomNavItem(
    modifier: Modifier = Modifier,
    tab: TabItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                if (selected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                else Color.Transparent
            )
            .clickable(onClick = onClick),
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.TopCenter)
            )
        }
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = null, // Label text below provides context
                tint = contentColor,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = tab.label,
                color = contentColor,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 10.sp,
                ),
            )
        }
    }
}
