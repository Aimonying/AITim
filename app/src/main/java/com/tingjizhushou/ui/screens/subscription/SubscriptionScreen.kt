package com.tingjizhushou.ui.screens.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tingjizhushou.R
import com.tingjizhushou.data.model.SubscriptionStatus
import com.tingjizhushou.ui.viewmodel.SubscriptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    navController: NavController,
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val subscriptionStatus by viewModel.subscriptionStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val purchaseSuccess by viewModel.purchaseSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_subscription),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CurrentStatusCard(subscriptionStatus = subscriptionStatus)
            }
            
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.choose_plan),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LightPlanCard(
                        isSelected = subscriptionStatus.subscriptionType == SubscriptionStatus.TYPE_LIGHT,
                        isPurchased = subscriptionStatus.subscriptionType == SubscriptionStatus.TYPE_LIGHT && !subscriptionStatus.isExpired(),
                        onSelect = { viewModel.purchaseLightSubscription() },
                        isLoading = isLoading
                    )
                    
                    ProPlanCard(
                        isSelected = subscriptionStatus.subscriptionType == SubscriptionStatus.TYPE_PRO,
                        isPurchased = subscriptionStatus.subscriptionType == SubscriptionStatus.TYPE_PRO && !subscriptionStatus.isExpired(),
                        onSelect = { viewModel.purchaseProSubscription() },
                        isLoading = isLoading
                    )
                    
                    LifetimePlanCard(
                        isSelected = subscriptionStatus.subscriptionType == SubscriptionStatus.TYPE_LIFETIME,
                        isPurchased = subscriptionStatus.subscriptionType == SubscriptionStatus.TYPE_LIFETIME,
                        onSelect = { viewModel.purchaseLifetimeSubscription() },
                        isLoading = isLoading
                    )
                }
            }
            
            item {
                FeaturesComparison()
            }
            
            item {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.terms_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
    
    if (purchaseSuccess) {
        SnackbarHost {
            Snackbar(
                action = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text(stringResource(R.string.ok))
                    }
                }
            ) {
                Text(stringResource(R.string.purchase_success))
            }
        }
    }
    
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(errorMessage ?: "") },
            confirmButton = {
                Button(onClick = { viewModel.clearError() }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

@Composable
private fun CurrentStatusCard(subscriptionStatus: SubscriptionStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Crown,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subscriptionStatus.getStatusText(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subscriptionStatus.getUsageText(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun LightPlanCard(
    isSelected: Boolean,
    isPurchased: Boolean,
    onSelect: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = if (isSelected || isPurchased) {
            CardDefaults.outlinedCardBorder(
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            CardDefaults.outlinedCardBorder()
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.plan_light),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.plan_light_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                if (isPurchased) {
                    Badge(
                        colors = BadgeDefaults.badgeColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(stringResource(R.string.current_plan))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.Baseline
            ) {
                Text(
                    text = "¥",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "9.9",
                    style = MaterialTheme.typography.displayLarge
                )
                Text(
                    text = "/月",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && !isPurchased
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else if (isPurchased) {
                    Text(stringResource(R.string.already_purchased))
                } else {
                    Text(stringResource(R.string.subscribe))
                }
            }
        }
    }
}

@Composable
private fun ProPlanCard(
    isSelected: Boolean,
    isPurchased: Boolean,
    onSelect: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = if (isSelected || isPurchased) {
            CardDefaults.outlinedCardBorder(
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            null
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.plan_pro),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.plan_pro_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                if (isPurchased) {
                    Badge(
                        colors = BadgeDefaults.badgeColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(stringResource(R.string.current_plan))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.Baseline
            ) {
                Text(
                    text = "¥",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "19.9",
                    style = MaterialTheme.typography.displayLarge
                )
                Text(
                    text = "/月",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = !isLoading && !isPurchased
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else if (isPurchased) {
                    Text(stringResource(R.string.already_purchased))
                } else {
                    Text(stringResource(R.string.subscribe))
                }
            }
        }
    }
}

@Composable
private fun LifetimePlanCard(
    isSelected: Boolean,
    isPurchased: Boolean,
    onSelect: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = if (isSelected || isPurchased) {
            CardDefaults.outlinedCardBorder(
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            CardDefaults.outlinedCardBorder()
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.plan_lifetime),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.plan_lifetime_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                if (isPurchased) {
                    Badge(
                        colors = BadgeDefaults.badgeColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(stringResource(R.string.current_plan))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.Baseline
            ) {
                Text(
                    text = "¥",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "399",
                    style = MaterialTheme.typography.displayLarge
                )
                Text(
                    text = stringResource(R.string.one_time_purchase),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && !isPurchased
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else if (isPurchased) {
                    Text(stringResource(R.string.already_purchased))
                } else {
                    Text(stringResource(R.string.buy_now))
                }
            }
        }
    }
}

@Composable
private fun FeaturesComparison() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.features_comparison),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            FeatureRow(
                feature = stringResource(R.string.feature_basic_transcription),
                free = true,
                light = true,
                pro = true,
                lifetime = true
            )
            
            FeatureRow(
                feature = stringResource(R.string.feature_minutes),
                free = false,
                light = true,
                pro = true,
                lifetime = true
            )
            
            FeatureRow(
                feature = stringResource(R.string.feature_advanced_templates),
                free = false,
                light = false,
                pro = true,
                lifetime = true
            )
            
            FeatureRow(
                feature = stringResource(R.string.feature_pdf_export),
                free = false,
                light = false,
                pro = true,
                lifetime = true
            )
            
            FeatureRow(
                feature = stringResource(R.string.feature_cloud_backup),
                free = false,
                light = false,
                pro = true,
                lifetime = true
            )
            
            FeatureRow(
                feature = stringResource(R.string.feature_lifetime),
                free = false,
                light = false,
                pro = false,
                lifetime = true
            )
        }
    }
}

@Composable
private fun FeatureRow(
    feature: String,
    free: Boolean,
    light: Boolean,
    pro: Boolean,
    lifetime: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = feature,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        FeatureIcon(enabled = free)
        FeatureIcon(enabled = light)
        FeatureIcon(enabled = pro)
        FeatureIcon(enabled = lifetime)
    }
}

@Composable
private fun FeatureIcon(enabled: Boolean) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (enabled) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}