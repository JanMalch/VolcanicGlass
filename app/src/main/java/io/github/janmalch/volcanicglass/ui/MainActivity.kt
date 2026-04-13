package io.github.janmalch.volcanicglass.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import io.github.janmalch.shed.Shed
import io.github.janmalch.volcanicglass.ui.screens.file.FileScreen
import io.github.janmalch.volcanicglass.ui.screens.onboarding.OnboardingScreen
import io.github.janmalch.volcanicglass.ui.theme.VolcanicGlassTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mViewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        splashScreen.setKeepOnScreenCondition { mViewModel.state.value != MainActivityViewState.Loading }
        setContent {
            val state by mViewModel.state.collectAsStateWithLifecycle()
            // This also causes the stack to reset when vault is set up
            when (val s = state) {
                MainActivityViewState.Loading -> {}
                MainActivityViewState.Failure -> VolcanicGlassTheme {
                    Scaffold { paddingValues ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(paddingValues),
                        ) {
                            Text("Fatal error during app start.")
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = {
                                Shed.startActivity(this@MainActivity)
                            }) {
                                Text("Check logs")
                            }
                        }
                    }
                }

                is MainActivityViewState.Ready ->
                    if (s.hasVault) VolcanicGlassApp(initialScreen = FileScreen(s.mostRecent))
                    else VolcanicGlassApp(initialScreen = OnboardingScreen)
            }
        }
    }
}

@Composable
fun VolcanicGlassApp(initialScreen: NavKey) {
    val backStack = remember { mutableStateListOf(initialScreen) }

    VolcanicGlassTheme {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },

            entryDecorators = listOf(
                // Add the default decorators for managing scenes and saving state
                rememberSaveableStateHolderNavEntryDecorator(),
                // Then add the view model store decorator
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = { key ->
                when (key) {
                    is FileScreen -> NavEntry(key) {
                        FileScreen(
                            key = key,
                            onFileClick = { backStack.add(FileScreen(it.uri)) },
                        )
                    }

                    is OnboardingScreen -> NavEntry(key) {
                        OnboardingScreen()
                    }

                    else -> NavEntry(UnknownNavKey) { Text("?") }
                }
            },
            transitionSpec = {
                // Slide in from right when navigating forward
                slideInHorizontally(initialOffsetX = { it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { -it })
            },
            popTransitionSpec = {
                // Slide in from left when navigating back
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { it })
            },
            predictivePopTransitionSpec = {
                // Slide in from left when navigating back
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { it })
            },
        )
    }
}

private data object UnknownNavKey : NavKey
