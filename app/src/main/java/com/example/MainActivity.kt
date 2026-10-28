package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.components.BabyVitalsHeader
import com.example.ui.screens.DrugsScreen
import com.example.ui.screens.FluidsScreen
import com.example.ui.screens.JaundiceScreen
import com.example.ui.screens.RespiratoryScreen
import com.example.ui.screens.SavedPatientsDialog
import com.example.ui.screens.SummaryShareDialog
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        val isArabic by viewModel.isArabic.collectAsState()
        val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
          NeoCareProApp(viewModel = viewModel)
        }
      }
    }
  }
}

data class NavTabItem(
  val titleAr: String,
  val titleEn: String,
  val icon: ImageVector,
  val testTag: String
)

@Composable
fun NeoCareProApp(viewModel: MainViewModel) {
  val isArabic by viewModel.isArabic.collectAsState()
  val selectedTab by viewModel.selectedTab.collectAsState()
  val weightKg by viewModel.weightKg.collectAsState()
  val birthWeightG by viewModel.birthWeightG.collectAsState()
  val gaWeeks by viewModel.gestationalAgeWeeks.collectAsState()
  val ageHours by viewModel.ageHours.collectAsState()
  val isUnderWarmerOrPhoto by viewModel.isUnderWarmerOrPhoto.collectAsState()
  val patientLabel by viewModel.patientLabel.collectAsState()

  var showSavedPatientsDialog by remember { mutableStateOf(false) }
  var showSummaryShareDialog by remember { mutableStateOf(false) }

  // Handle system back button
  BackHandler(enabled = selectedTab != 0 || showSavedPatientsDialog || showSummaryShareDialog) {
    when {
      showSavedPatientsDialog -> showSavedPatientsDialog = false
      showSummaryShareDialog -> showSummaryShareDialog = false
      selectedTab != 0 -> viewModel.setTab(0)
    }
  }

  val navItems = listOf(
    NavTabItem("الصفراء", "Jaundice", Icons.Default.WbSunny, "nav_jaundice"),
    NavTabItem("التنفس", "Respiratory", Icons.Default.Air, "nav_respiratory"),
    NavTabItem("السوائل", "Fluids", Icons.Default.WaterDrop, "nav_fluids"),
    NavTabItem("الجرعات", "Drugs & NRP", Icons.Default.Medication, "nav_drugs")
  )

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.statusBars),
    bottomBar = {
      NavigationBar(
        modifier = Modifier
          .fillMaxWidth()
          .windowInsetsPadding(WindowInsets.navigationBars)
          .testTag("bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
      ) {
        navItems.forEachIndexed { index, item ->
          val isSelected = selectedTab == index
          NavigationBarItem(
            selected = isSelected,
            onClick = { viewModel.setTab(index) },
            icon = {
              Icon(
                imageVector = item.icon,
                contentDescription = if (isArabic) item.titleAr else item.titleEn
              )
            },
            label = {
              Text(
                text = if (isArabic) item.titleAr else item.titleEn,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MaterialTheme.colorScheme.primary,
              selectedTextColor = MaterialTheme.colorScheme.primary,
              indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag(item.testTag)
          )
        }
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Sticky Top Patient Vitals & Controls
      BabyVitalsHeader(
        weightKg = weightKg,
        birthWeightG = birthWeightG,
        gaWeeks = gaWeeks,
        ageHours = ageHours,
        isUnderWarmerOrPhoto = isUnderWarmerOrPhoto,
        patientLabel = patientLabel,
        isArabic = isArabic,
        onWeightChange = { viewModel.updateWeight(it) },
        onGaChange = { viewModel.updateGestationalAge(it) },
        onAgeChange = { viewModel.updateAgeHours(it) },
        onWarmerPhotoToggle = { viewModel.toggleWarmerOrPhoto(it) },
        onPresetSelected = { viewModel.applyPreset(it) },
        onOpenSavedPatients = { showSavedPatientsDialog = true },
        onShareSummary = { showSummaryShareDialog = true },
        onToggleLanguage = { viewModel.toggleLanguage() }
      )

      // Tab Content
      AnimatedContent(
        targetState = selectedTab,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "TabContentTransition",
        modifier = Modifier.weight(1f)
      ) { targetTab ->
        when (targetTab) {
          0 -> JaundiceScreen(viewModel = viewModel)
          1 -> RespiratoryScreen(viewModel = viewModel)
          2 -> FluidsScreen(viewModel = viewModel)
          3 -> DrugsScreen(viewModel = viewModel)
        }
      }
    }

    // Dialogs
    if (showSavedPatientsDialog) {
      SavedPatientsDialog(
        viewModel = viewModel,
        onDismiss = { showSavedPatientsDialog = false }
      )
    }

    if (showSummaryShareDialog) {
      SummaryShareDialog(
        summaryText = viewModel.generateClinicalSummary(),
        isArabic = isArabic,
        onDismiss = { showSummaryShareDialog = false }
      )
    }
  }
}
