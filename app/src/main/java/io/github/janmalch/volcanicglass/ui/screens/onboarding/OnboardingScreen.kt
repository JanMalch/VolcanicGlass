package io.github.janmalch.volcanicglass.ui.screens.onboarding

import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey

data object OnboardingScreen : NavKey

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            val pickDirectoryLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocumentTree(),
                onResult = { viewModel.setDirectory(it ?: return@rememberLauncherForActivityResult) }
            )

            Text("Pick your vault directory.")
            Button(onClick = {
                pickDirectoryLauncher.launch(Environment.getExternalStorageDirectory().toUri())
            }) {
                Text("Pick")
            }
        }
    }
}