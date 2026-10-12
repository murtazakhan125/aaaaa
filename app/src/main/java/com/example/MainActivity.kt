package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.AiPrimary
import com.example.ui.theme.AiTertiary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                OmniAiApp()
            }
        }
    }
}

@Composable
fun OmniAiApp(
    viewModel: ChatViewModel = viewModel()
) {
    val activeScreen by viewModel.activeScreen.collectAsState()

    // Handle back button: if not on chat screen, back returns to chat
    BackHandler(enabled = activeScreen != "chat") {
        viewModel.setScreen("chat")
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = activeScreen == "chat",
                    onClick = { viewModel.setScreen("chat") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Chat",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Chat", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AiTertiary,
                        selectedTextColor = AiTertiary,
                        indicatorColor = AiPrimary.copy(alpha = 0.2f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_item_chat")
                )

                NavigationBarItem(
                    selected = activeScreen == "tools",
                    onClick = { viewModel.setScreen("tools") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "AI Tools",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Studio Tools", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AiTertiary,
                        selectedTextColor = AiTertiary,
                        indicatorColor = AiPrimary.copy(alpha = 0.2f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_item_tools")
                )

                NavigationBarItem(
                    selected = activeScreen == "history",
                    onClick = { viewModel.setScreen("history") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("History", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AiTertiary,
                        selectedTextColor = AiTertiary,
                        indicatorColor = AiPrimary.copy(alpha = 0.2f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_item_history")
                )

                NavigationBarItem(
                    selected = activeScreen == "settings",
                    onClick = { viewModel.setScreen("settings") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Settings", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AiTertiary,
                        selectedTextColor = AiTertiary,
                        indicatorColor = AiPrimary.copy(alpha = 0.2f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_item_settings")
                )
            }
        }
    ) { innerPadding ->
        when (activeScreen) {
            "chat" -> ChatScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            "tools" -> ToolsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            "history" -> HistoryScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            "settings" -> SettingsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            else -> ChatScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
