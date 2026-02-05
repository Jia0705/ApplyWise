package com.team.applywise.ui.screens.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team.applywise.ui.components.Avatar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.team.applywise.ui.components.DiscardChangesDialog
import com.team.applywise.ui.components.NetworkStatusBanner
import com.team.applywise.core.utils.ConnectivityObserver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.SnackbarHost
import androidx.compose.ui.unit.sp

/**
 * EditProfileScreen - Edit user's name and avatar color
 * 
 * User can:
 * - Change display name
 * - Choose avatar color (Red, Blue, Green, Orange, Magenta)
 * 
 * Changes are saved to:
 * 1. Firestore (always)
 * 2. Firebase Auth display name (if possible)
 * 3. Local memory for avatar color
 * 
 * Shows "Discard changes?" dialog if user backs out with unsaved changes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit, // Called after successful save -> navigate back
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val showColorPicker = remember { mutableStateOf(false) } // Show color picker dialog
    val context = LocalContext.current
    val connectivityObserver = remember { ConnectivityObserver(context) }
    val isOnline by connectivityObserver.observe().collectAsStateWithLifecycle(initialValue = true)
    // Available colors for avatar
    val colorOptions = listOf(
        "red" to Color.Red,
        "magenta" to Color.Magenta,
        "orange" to Color(1f, 0.6f, 0f, 1f),
        "green" to Color.Green,
        "blue" to Color.Blue
    )
    var showDiscardDialog by remember { mutableStateOf(false) } // "Discard changes?" dialog
    var initialState by remember { mutableStateOf<EditProfileUiState?>(null) } // Track if user made changes

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && initialState == null) {
            initialState = uiState
        }
    }

    LaunchedEffect(Unit) {
        viewModel.clearSaveState()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaveSuccess()
        }
    }

    val hasChanges = initialState?.let {
        uiState.nameInput != it.nameInput || uiState.avatarColor != it.avatarColor
    } ?: false

    BackHandler {
        if (hasChanges) {
            showDiscardDialog = true
        } else {
            navController.popBackStack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Custom header with back button and title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (hasChanges) {
                        showDiscardDialog = true
                    } else {
                        navController.popBackStack()
                    }
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                
                Text(
                    text = "Edit Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            NetworkStatusBanner(isOffline = !isOnline)
            Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Avatar(
                        name = uiState.nameInput.ifBlank { "User" },
                        modifier = Modifier.size(72.dp),
                        colorName = uiState.avatarColor.ifBlank { null }
                    )
                    Text(
                        text = uiState.nameInput.ifBlank { "User" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = uiState.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    TextButton(onClick = { showColorPicker.value = true }) {
                        Text("Edit Avatar Color")
                    }
                }
            }

            OutlinedTextField(
                value = uiState.nameInput,
                onValueChange = viewModel::onNameChange,
                label = { Text("Username") },
                singleLine = true,
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )

            OutlinedTextField(
                value = uiState.email,
                onValueChange = {},
                label = { Text("Email") },
                singleLine = true,
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )

            Button(
                onClick = viewModel::saveProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.isSaving
            ) {
                Text("Save Changes")
            }
        } }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showColorPicker.value) {
        AlertDialog(
            onDismissRequest = { showColorPicker.value = false },
            title = { Text("Choose Avatar Color") },
            text = {
               Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        colorOptions.forEach { (name, color) ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(color)
                                    .clickable {
                                        viewModel.updateAvatarColor(name)
                                        showColorPicker.value = false
                                    }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorPicker.value = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showDiscardDialog) {
        DiscardChangesDialog(
            onDiscard = {
                showDiscardDialog = false
                navController.popBackStack()
            },
            onDismiss = { showDiscardDialog = false }
        )
    }
}