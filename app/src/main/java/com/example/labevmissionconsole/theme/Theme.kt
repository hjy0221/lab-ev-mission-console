package com.example.labevmissionconsole.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CockpitColorScheme = darkColorScheme(
  primary = ElectricCyan,
  secondary = ReadyGreen,
  tertiary = DriveAmber,
  background = CockpitBackground,
  surface = CockpitPanel,
  onPrimary = CockpitBackground,
  onBackground = HmiTextPrimary,
  onSurface = HmiTextPrimary,
)

@Composable
fun LABEVMissionConsoleTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = CockpitColorScheme, typography = Typography, content = content)
}
