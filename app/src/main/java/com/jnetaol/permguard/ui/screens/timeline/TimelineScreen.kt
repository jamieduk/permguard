package com.jnetaol.permguard.ui.screens.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.jnetaol.permguard.data.model.PermissionCategory
import com.jnetaol.permguard.ui.components.*
import com.jnetaol.permguard.ui.screens.AppViewModel
import com.jnetaol.permguard.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onAppClick: (String) -> Unit
) {
    val filteredLogs by viewModel.filteredLogs.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedPermissionCategory.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Permission Timeline",
                        fontWeight = FontWeight.Bold,
                        color = DarkPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = DarkOnSurface)
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

            PermissionFilterBar(
                selected = selectedCategory,
                onCategorySelected = { viewModel.setSelectedPermissionCategory(it) }
            )

            Spacer(Modifier.height(8.dp))

            if (filteredLogs.isEmpty()) {
                EmptyState(
                    title = "No Events Found",
                    message = "No permission events recorded yet. Start monitoring to see activity.",
                    icon = Icons.Default.Timeline,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            "${filteredLogs.size} events",
                            fontSize = 12.sp,
                            color = DarkOutline,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(filteredLogs) { log ->
                        Card(
                            onClick = { onAppClick(log.packageName) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val cat = PermissionCategory.classify(log.permission)
                                val color = when (cat) {
                                    PermissionCategory.MICROPHONE -> androidx.compose.ui.graphics.Color(0xFFFF1744)
                                    PermissionCategory.CAMERA -> androidx.compose.ui.graphics.Color(0xFFD500F9)
                                    PermissionCategory.LOCATION -> androidx.compose.ui.graphics.Color(0xFF00E5FF)
                                    PermissionCategory.CONTACTS -> androidx.compose.ui.graphics.Color(0xFFFF9100)
                                    PermissionCategory.STORAGE -> androidx.compose.ui.graphics.Color(0xFF76FF03)
                                    PermissionCategory.PHONE -> androidx.compose.ui.graphics.Color(0xFF448AFF)
                                    PermissionCategory.SENSORS -> androidx.compose.ui.graphics.Color(0xFF69F0AE)
                                    else -> DarkOutline
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (cat) {
                                            PermissionCategory.MICROPHONE -> Icons.Default.Mic
                                            PermissionCategory.CAMERA -> Icons.Default.CameraAlt
                                            PermissionCategory.LOCATION -> Icons.Default.LocationOn
                                            PermissionCategory.CONTACTS -> Icons.Default.Contacts
                                            PermissionCategory.STORAGE -> Icons.Default.Storage
                                            PermissionCategory.PHONE -> Icons.Default.Phone
                                            PermissionCategory.SENSORS -> Icons.Default.Sensors
                                            else -> Icons.Default.Security
                                        },
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = log.appName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DarkOnSurface
                                    )
                                    Text(
                                        text = cat.label + " \u2022 " + log.accessType,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DarkOnSurfaceVariant
                                    )
                                }

                                Text(
                                    text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.US)
                                        .format(java.util.Date(log.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DarkOutline
                                )
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}
