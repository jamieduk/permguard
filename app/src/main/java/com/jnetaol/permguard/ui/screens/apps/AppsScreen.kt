package com.jnetaol.permguard.ui.screens.apps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jnetaol.permguard.ui.components.*
import com.jnetaol.permguard.ui.screens.AppViewModel
import com.jnetaol.permguard.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onAppClick: (String) -> Unit
) {
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()

    var sortOrder by remember { mutableStateOf(SortOrder.SCORE_ASC) }

    val sortedApps = remember(searchResults, sortOrder) {
        when (sortOrder) {
            SortOrder.SCORE_ASC -> searchResults.sortedBy { it.privacyScore }
            SortOrder.SCORE_DESC -> searchResults.sortedByDescending { it.privacyScore }
            SortOrder.NAME_ASC -> searchResults.sortedBy { it.appName.lowercase() }
            SortOrder.RISKY_DESC -> searchResults.sortedByDescending { it.riskyPermissionCount }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "All Apps",
                        fontWeight = FontWeight.Bold,
                        color = DarkPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = DarkOnSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.scanApps() }) {
                        Icon(
                            Icons.Default.Refresh,
                            "Rescan",
                            tint = if (isScanning) DarkPrimary else DarkOnSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CrimsonBackground)
            )
        },
        containerColor = CrimsonBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Search apps or packages...", color = DarkOutline)
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        "Search",
                        tint = DarkOutline
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, "Clear", tint = DarkOutline)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkPrimary,
                    unfocusedBorderColor = DarkOutline,
                    focusedTextColor = DarkOnSurface,
                    unfocusedTextColor = DarkOnSurface,
                    cursorColor = DarkPrimary,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Sort bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${sortedApps.size} apps",
                    fontSize = 12.sp,
                    color = DarkOutline,
                    modifier = Modifier.weight(1f)
                )
                SortOrder.entries.forEach { order ->
                    FilterChip(
                        selected = sortOrder == order,
                        onClick = { sortOrder = order },
                        label = {
                            Text(order.label, fontSize = 11.sp)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkPrimary.copy(alpha = 0.3f),
                            selectedLabelColor = DarkPrimary
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (sortedApps.isEmpty()) {
                EmptyState(
                    title = "No Apps Found",
                    message = if (searchQuery.isNotEmpty())
                        "No apps matching '$searchQuery'"
                    else
                        "Run a scan to discover installed apps",
                    icon = Icons.Default.Apps,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sortedApps) { profile ->
                        AppPermissionCard(
                            profile = profile,
                            onClick = { onAppClick(profile.packageName) }
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

enum class SortOrder(val label: String) {
    SCORE_ASC("Risk\u2191"),
    SCORE_DESC("Risk\u2193"),
    NAME_ASC("A-Z"),
    RISKY_DESC("Perms\u2193")
}
