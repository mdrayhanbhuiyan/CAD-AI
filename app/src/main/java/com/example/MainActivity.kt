package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CadViewModel
import com.example.ui.screens.MainDashboard
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: CadViewModel = viewModel()
                val selectedTab by viewModel.selectedTab.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = SophisticatedBackground,
                    bottomBar = {
                        NavigationBar(
                            containerColor = SophisticatedBackground,
                            contentColor = SophisticatedTextSecondary,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { viewModel.setTab(0) },
                                label = { Text("Workspace", color = if (selectedTab == 0) SophisticatedPrimaryLight else SophisticatedTextMuted) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Workspace",
                                        tint = if (selectedTab == 0) SophisticatedPrimaryLight else SophisticatedTextDim,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = SophisticatedSurface
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { viewModel.setTab(1) },
                                label = { Text("Settings", color = if (selectedTab == 1) SophisticatedPrimaryLight else SophisticatedTextMuted) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = if (selectedTab == 1) SophisticatedPrimaryLight else SophisticatedTextDim,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = SophisticatedSurface
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    when (selectedTab) {
                        0 -> MainDashboard(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        1 -> SettingsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
