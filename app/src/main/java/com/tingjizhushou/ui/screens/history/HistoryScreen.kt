package com.tingjizhushou.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tingjizhushou.R
import com.tingjizhushou.data.local.db.entity.RecordEntity
import com.tingjizhushou.ui.components.RecordCard
import com.tingjizhushou.ui.components.RecordCard

/**
 * History screen composable with full functionality.
 * Displays list of past recordings with search, filter, and favorite features.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val records by viewModel.records.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    
    var showSearchBar by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_history),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    // Search button
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.search)
                        )
                    }
                    
                    // Filter button
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(
                                imageVector = Icons.Filled.FilterList,
                                contentDescription = stringResource(R.string.filter)
                            )
                        }
                        
                        // Filter dropdown menu
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            // Type filter
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.all_types)) },
                                onClick = {
                                    viewModel.filterByType(null)
                                    showFilterMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.type_realtime)) },
                                onClick = {
                                    viewModel.filterByType(RecordEntity.TYPE_REALTIME)
                                    showFilterMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.type_upload)) },
                                onClick = {
                                    viewModel.filterByType(RecordEntity.TYPE_UPLOAD)
                                    showFilterMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.type_text)) },
                                onClick = {
                                    viewModel.filterByType(RecordEntity.TYPE_TEXT)
                                    showFilterMenu = false
                                }
                            )
                            
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            
                            // Favorite filter toggle
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.favorites_only)) },
                                onClick = {
                                    viewModel.toggleFavoritesFilter()
                                    showFilterMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (showFavoritesOnly) Icons.Filled.Star else Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = if (showFavoritesOnly) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            if (showSearchBar) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.searchRecords(it) },
                    onSearch = { focusManager.clearFocus() },
                    onClear = { viewModel.searchRecords("") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Filter indicator
            if (selectedType != null || showFavoritesOnly) {
                FilterIndicator(
                    selectedType = selectedType,
                    showFavoritesOnly = showFavoritesOnly,
                    onClearFilters = {
                        viewModel.filterByType(null)
                        if (showFavoritesOnly) viewModel.toggleFavoritesFilter()
                    }
                )
            }
            
            // Records list or empty state
            if (uiState.isLoading) {
                LoadingState()
            } else if (records.isEmpty()) {
                EmptyHistoryState()
            } else {
                RecordsList(
                    records = records,
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }
    }
    
    // Error Snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error message
            viewModel.clearError()
        }
    }
}

/**
 * Search bar component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.search_records)) },
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear))
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        modifier = modifier,
        singleLine = true
    )
}

/**
 * Filter indicator showing active filters
 */
@Composable
private fun FilterIndicator(
    selectedType: String?,
    showFavoritesOnly: Boolean,
    onClearFilters: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        selectedType?.let { type ->
            Chip(
                label = {
                    Text(
                        when (type) {
                            RecordEntity.TYPE_REALTIME -> stringResource(R.string.type_realtime)
                            RecordEntity.TYPE_UPLOAD -> stringResource(R.string.type_upload)
                            RecordEntity.TYPE_TEXT -> stringResource(R.string.type_text)
                            else -> type
                        }
                    )
                },
                onCloseIconClick = { /* Handled by clear all */ }
            )
        }
        
        if (showFavoritesOnly) {
            Chip(
                label = { Text(stringResource(R.string.favorites)) },
                icon = { Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp)) },
                onCloseIconClick = { /* Handled by clear all */ }
            )
        }
        
        TextButton(onClick = onClearFilters) {
            Text(stringResource(R.string.clear_filters))
        }
    }
}

/**
 * Loading state
 */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Records list
 */
@Composable
private fun RecordsList(
    records: List<RecordEntity>,
    viewModel: HistoryViewModel,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(records) { record ->
            RecordCard(
                record = record,
                onClick = {
                    // Navigate to result screen with record ID
                    navController.navigate("result/${record.id}")
                },
                onDelete = {
                    // Show confirmation dialog before deletion
                    // This can be implemented with a Dialog composable
                    viewModel.deleteRecord(record)
                },
                onFavoriteToggle = {
                    viewModel.toggleFavorite(record)
                }
            )
        }
    }
}

/**
 * Empty state placeholder
 */
@Composable
private fun EmptyHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.History,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_recordings),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}