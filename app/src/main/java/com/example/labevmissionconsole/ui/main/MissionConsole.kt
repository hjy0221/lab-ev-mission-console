package com.example.labevmissionconsole.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.labevmissionconsole.BuildConfig
import com.example.labevmissionconsole.domain.MissionStage
import com.example.labevmissionconsole.domain.SensorReading
import com.example.labevmissionconsole.domain.SiteSummary
import com.example.labevmissionconsole.theme.CockpitBackground
import com.example.labevmissionconsole.theme.CockpitPanel
import com.example.labevmissionconsole.theme.CockpitPanelRaised
import com.example.labevmissionconsole.theme.DriveAmber
import com.example.labevmissionconsole.theme.ElectricCyan
import com.example.labevmissionconsole.theme.HmiDivider
import com.example.labevmissionconsole.theme.HmiTextPrimary
import com.example.labevmissionconsole.theme.HmiTextSecondary
import com.example.labevmissionconsole.theme.ReadyGreen
import com.example.labevmissionconsole.theme.LABEVMissionConsoleTheme

@Composable
fun MissionConsole(viewModel: MissionViewModel = viewModel()) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  VehicleShell(state) {
    when (state.stage) {
      MissionStage.MISSION -> MissionScreen(state, viewModel::startMission)
      MissionStage.DRIVE -> DriveScreen(state, viewModel::simulateArrival)
      MissionStage.FIELD -> FieldScreen(state, viewModel::toggleMeasurement)
      MissionStage.ANALYSIS -> AnalysisScreen(state, viewModel::runAnalysis, viewModel::generateReport)
      MissionStage.REPORT -> ReportScreen(state, viewModel::reset)
    }
  }
}

@Composable
private fun VehicleShell(state: MissionUiState, content: @Composable () -> Unit) {
  Column(Modifier.fillMaxSize().background(CockpitBackground)) {
    Row(Modifier.fillMaxWidth().height(70.dp).background(Color(0xFF070A0E)).padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
      Box(Modifier.size(42.dp).background(ElectricCyan, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
        Text("L", color = CockpitBackground, fontWeight = FontWeight.Black, fontSize = 24.sp)
      }
      Column(Modifier.padding(start = 14.dp).weight(1f)) {
        Text("LAB EV · ${BuildConfig.BACKEND_NAME}", color = HmiTextPrimary, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text(state.mission.title, color = HmiTextSecondary, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
      }
      StatusChip("EV ${state.vehicle.batteryPercent}%", ReadyGreen)
      Spacer(Modifier.width(10.dp))
      StatusChip(if (state.vehicle.isParked) "P · PARKED" else "D · DRIVING", if (state.vehicle.isParked) ReadyGreen else DriveAmber)
    }
    StageStrip(state.stage)
    Box(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 18.dp)) { content() }
  }
}

@Composable
private fun StageStrip(current: MissionStage) {
  Row(Modifier.fillMaxWidth().height(42.dp).background(CockpitPanel).padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
    MissionStage.entries.forEachIndexed { index, stage ->
      val active = stage == current
      val done = stage.ordinal < current.ordinal
      Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(if (active) 24.dp else 18.dp).background(if (active) ElectricCyan else if (done) ReadyGreen else HmiDivider, RoundedCornerShape(50)), contentAlignment = Alignment.Center) {
          Text("${index + 1}", color = if (active) CockpitBackground else HmiTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
        Text(stage.label(), color = if (active) HmiTextPrimary else HmiTextSecondary, fontSize = 11.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.padding(start = 8.dp))
      }
    }
  }
}

private fun MissionStage.label() = when (this) {
  MissionStage.MISSION -> "MISSION"
  MissionStage.DRIVE -> "DRIVE"
  MissionStage.FIELD -> "MEASURE"
  MissionStage.ANALYSIS -> "ANALYZE"
  MissionStage.REPORT -> "REPORT"
}

@Composable
private fun MissionScreen(state: MissionUiState, onStart: () -> Unit) {
  HmiColumns(
    main = {
      SectionLabel("NEXT MISSION")
      Text("READY TO DEPART", color = HmiTextPrimary, fontWeight = FontWeight.Black, fontSize = 40.sp)
      Text("Site B · 물류 차량 진입로", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 21.sp, modifier = Modifier.padding(top = 4.dp))
      Text("8.4 km  ·  예상 18분", color = HmiTextSecondary, fontSize = 16.sp, modifier = Modifier.padding(top = 5.dp))
      Spacer(Modifier.height(24.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ReadyTile("VEHICLE", "${state.vehicle.batteryPercent}%", "Battery", Modifier.weight(1f))
        ReadyTile("EQUIPMENT", "${state.vehicle.connectedSensors}/${state.vehicle.totalSensors}", "Connected", Modifier.weight(1f))
        ReadyTile("POSITION", "GPS", "Ready", Modifier.weight(1f))
      }
    },
    side = {
      ActionPanel("MISSION READY", "차량과 연구 장비가 준비되었습니다.") {
        CheckLine("Vehicle systems", true)
        CheckLine("Sensor package", true)
        CheckLine("Mission storage", true)
        Spacer(Modifier.weight(1f))
        HmiButton("START MISSION", onStart, state.isBusy)
      }
    },
  )
}

@Composable
private fun DriveScreen(state: MissionUiState, onArrive: () -> Unit) {
  HmiColumns(
    main = {
      SectionLabel("ROUTE GUIDANCE")
      Row(verticalAlignment = Alignment.Bottom) {
        Text("${state.route.etaMinutes}", color = HmiTextPrimary, fontWeight = FontWeight.Black, fontSize = 92.sp)
        Text("MIN", color = HmiTextSecondary, fontWeight = FontWeight.Bold, fontSize = 22.sp, modifier = Modifier.padding(start = 8.dp, bottom = 18.dp))
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(bottom = 16.dp)) {
          Text("${"%.1f".format(state.route.distanceKm)} km", color = ElectricCyan, fontWeight = FontWeight.Black, fontSize = 31.sp)
          Text("TO SITE B", color = HmiTextSecondary, fontSize = 13.sp)
        }
      }
      RouteLine()
      Text("다음 작업 · 대기환경 현장 측정", color = HmiTextPrimary, fontWeight = FontWeight.Bold, fontSize = 19.sp, modifier = Modifier.padding(top = 20.dp))
    },
    side = {
      ActionPanel("SITE B", "물류 차량 진입로") {
        KeyValue("ARRIVAL", "14:42")
        KeyValue("MISSION", "PM2.5 · TEMP · RH")
        Spacer(Modifier.weight(1f))
        Text("DEMO CONTROL", color = DriveAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        HmiButton("ARRIVE AT SITE", onArrive, state.isBusy, DriveAmber)
      }
    },
  )
}

@Composable
private fun FieldScreen(state: MissionUiState, onToggle: () -> Unit) {
  val latest = state.readings.lastOrNull()
  HmiColumns(
    main = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
          SectionLabel("SITE B · LIVE SENSORS")
          Text(if (state.isMeasuring) "MEASURING" else "READY TO MEASURE", color = HmiTextPrimary, fontWeight = FontWeight.Black, fontSize = 34.sp)
        }
        StatusChip(if (state.isMeasuring) "● LIVE" else "STANDBY", if (state.isMeasuring) ReadyGreen else HmiTextSecondary)
      }
      Spacer(Modifier.height(14.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SensorTile("PM2.5", latest?.pm25, "μg/m³", ElectricCyan, Modifier.weight(1.2f))
        SensorTile("TEMP", latest?.temperature, "°C", ReadyGreen, Modifier.weight(1f))
        SensorTile("HUMIDITY", latest?.humidity, "%", DriveAmber, Modifier.weight(1f))
      }
      Spacer(Modifier.height(12.dp))
      CompactSparkline(state.readings)
    },
    side = {
      ActionPanel(if (state.isMeasuring) "MEASUREMENT ACTIVE" else "FIELD READY", "Vehicle secured in Park") {
        CheckLine("Parking brake", state.vehicle.isParked)
        CheckLine("Sensor mast", true)
        CheckLine("GPS lock", state.vehicle.gpsReady)
        Text("NOTE", color = HmiTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
        Text("화물차 통행 많음 · 남서풍", color = HmiTextPrimary, fontSize = 15.sp, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.weight(1f))
        HmiButton(if (state.isMeasuring) "STOP & ANALYZE" else "START MEASUREMENT", onToggle, false, if (state.isMeasuring) DriveAmber else ElectricCyan)
      }
    },
  )
}

@Composable
private fun AnalysisScreen(state: MissionUiState, onAnalyze: () -> Unit, onReport: () -> Unit) {
  HmiColumns(
    main = {
      SectionLabel("MISSION ANALYSIS")
      Text("SITE COMPARISON", color = HmiTextPrimary, fontWeight = FontWeight.Black, fontSize = 34.sp)
      Text("PM2.5 average · μg/m³", color = HmiTextSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 3.dp, bottom = 15.dp))
      state.summaries.forEach { HmiSummaryBar(it) }
    },
    side = {
      val analysis = state.analysis
      ActionPanel(if (analysis == null) "AI READY" else "AI OBSERVATION", if (analysis == null) "Review measurements and field note" else "Evidence-limited summary") {
        if (analysis == null) {
          BigCallout("3", "sites measured", ElectricCyan)
          Text("원인을 단정하지 않고 지점 간 차이만 분석합니다.", color = HmiTextSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 12.dp))
          Spacer(Modifier.weight(1f))
          HmiButton("RUN ANALYSIS", onAnalyze, state.isBusy)
        } else {
          Text("SITE B", color = DriveAmber, fontWeight = FontWeight.Black, fontSize = 30.sp)
          Text("다른 지점 평균보다 약 60% 높음", color = HmiTextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.padding(top = 5.dp))
          Text("인과관계는 확인되지 않았습니다.", color = HmiTextSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 10.dp))
          Spacer(Modifier.weight(1f))
          HmiButton("CREATE REPORT", onReport, state.isBusy)
        }
      }
    },
  )
}

@Composable
private fun ReportScreen(state: MissionUiState, onReset: () -> Unit) {
  val report = state.report ?: return
  HmiColumns(
    main = {
      SectionLabel(report.reportId)
      Text("MISSION COMPLETE", color = ReadyGreen, fontWeight = FontWeight.Black, fontSize = 38.sp)
      Text(state.mission.title, color = HmiTextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(top = 4.dp))
      Spacer(Modifier.height(22.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ResultTile("SITES", "3 / 3", Modifier.weight(1f))
        ResultTile("SAMPLES", "${state.summaries.sumOf { it.sampleCount }}", Modifier.weight(1f))
        ResultTile("REPORT", "READY", Modifier.weight(1f))
      }
      Text("NEXT ACTION", color = HmiTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp))
      Text("Site B 동일 시간대 반복 측정", color = HmiTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp))
    },
    side = {
      ActionPanel("REPORT READY", report.date) {
        CheckLine("Measurements saved", true)
        CheckLine("AI summary saved", true)
        CheckLine("Field note attached", true)
        Spacer(Modifier.weight(1f))
        HmiButton("NEW MISSION", onReset)
      }
    },
  )
}

@Composable
private fun HmiColumns(main: @Composable ColumnScope.() -> Unit, side: @Composable ColumnScope.() -> Unit) {
  BoxWithConstraints(Modifier.fillMaxSize()) {
    if (maxWidth >= 820.dp) {
      Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(Modifier.weight(1.65f).fillMaxHeight(), content = main)
        Column(Modifier.width(350.dp).fillMaxHeight(), content = side)
      }
    } else Column(Modifier.fillMaxSize(), content = main)
  }
}

@Composable
private fun ActionPanel(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
  Column(Modifier.fillMaxSize().background(CockpitPanelRaised, RoundedCornerShape(18.dp)).border(1.dp, HmiDivider, RoundedCornerShape(18.dp)).padding(20.dp)) {
    Text(title, color = HmiTextPrimary, fontWeight = FontWeight.Black, fontSize = 21.sp)
    Text(subtitle, color = HmiTextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
    content()
  }
}

@Composable
private fun HmiButton(text: String, onClick: () -> Unit, busy: Boolean = false, color: Color = ElectricCyan) {
  Button(onClick = onClick, enabled = !busy, modifier = Modifier.fillMaxWidth().height(64.dp).padding(top = 8.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = CockpitBackground), contentPadding = PaddingValues(horizontal = 20.dp)) {
    if (busy) CircularProgressIndicator(Modifier.size(24.dp), color = CockpitBackground, strokeWidth = 3.dp)
    else Text(text, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = .5.sp)
  }
}

@Composable
private fun StatusChip(text: String, color: Color) {
  Row(Modifier.background(color.copy(alpha = .13f), RoundedCornerShape(50)).border(1.dp, color.copy(alpha = .5f), RoundedCornerShape(50)).padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.size(7.dp).background(color, RoundedCornerShape(50)))
    Text(text, color = HmiTextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
  }
}

@Composable private fun SectionLabel(text: String) { Text(text, color = ElectricCyan, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.1.sp, modifier = Modifier.padding(bottom = 6.dp)) }

@Composable
private fun ReadyTile(label: String, value: String, detail: String, modifier: Modifier) {
  Column(modifier.height(112.dp).background(CockpitPanel, RoundedCornerShape(14.dp)).border(1.dp, HmiDivider, RoundedCornerShape(14.dp)).padding(16.dp)) {
    Text(label, color = HmiTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    Text(value, color = HmiTextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 4.dp))
    Text(detail, color = ReadyGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
  }
}

@Composable
private fun SensorTile(label: String, value: Double?, unit: String, color: Color, modifier: Modifier) {
  Column(modifier.height(118.dp).background(CockpitPanel, RoundedCornerShape(14.dp)).border(1.dp, HmiDivider, RoundedCornerShape(14.dp)).padding(15.dp)) {
    Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Black)
    Row(verticalAlignment = Alignment.Bottom) {
      Text(value?.let { "%.1f".format(it) } ?: "--", color = HmiTextPrimary, fontSize = 38.sp, fontWeight = FontWeight.Black)
      Text(unit, color = HmiTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(start = 6.dp, bottom = 7.dp))
    }
  }
}

@Composable
private fun CompactSparkline(readings: List<SensorReading>) {
  val values = readings.map { it.pm25 }
  Column(Modifier.fillMaxWidth().height(112.dp).background(CockpitPanel, RoundedCornerShape(14.dp)).border(1.dp, HmiDivider, RoundedCornerShape(14.dp)).padding(13.dp)) {
    Row { Text("PM2.5 TREND", color = HmiTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); Text("${values.size} SAMPLES", color = HmiTextSecondary, fontSize = 10.sp) }
    Canvas(Modifier.fillMaxSize().padding(top = 8.dp)) {
      if (values.size > 1) {
        val min = values.min()
        val range = (values.max() - min).coerceAtLeast(1.0)
        val path = Path()
        values.forEachIndexed { index, value ->
          val x = index * size.width / (values.size - 1)
          val y = size.height - ((value - min) / range * size.height).toFloat()
          if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, ElectricCyan, style = Stroke(width = 5f, cap = StrokeCap.Round))
      }
    }
  }
}

@Composable
private fun CheckLine(label: String, ok: Boolean) {
  Row(Modifier.fillMaxWidth().height(34.dp), verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.size(9.dp).background(if (ok) ReadyGreen else DriveAmber, RoundedCornerShape(50)))
    Text(label, color = HmiTextPrimary, fontSize = 14.sp, modifier = Modifier.padding(start = 10.dp).weight(1f))
    Text(if (ok) "READY" else "CHECK", color = if (ok) ReadyGreen else DriveAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
  }
}

@Composable private fun KeyValue(label: String, value: String) { Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) { Text(label, color = HmiTextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f)); Text(value, color = HmiTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp) } }

@Composable
private fun RouteLine() {
  Canvas(Modifier.fillMaxWidth().height(55.dp)) {
    val y = size.height / 2
    drawLine(HmiDivider, Offset(14f, y), Offset(size.width - 14f, y), 12f, StrokeCap.Round)
    drawLine(ElectricCyan, Offset(14f, y), Offset(size.width * .68f, y), 12f, StrokeCap.Round)
    drawCircle(ElectricCyan, 15f, Offset(14f, y))
    drawCircle(DriveAmber, 18f, Offset(size.width - 14f, y))
  }
}

@Composable
private fun HmiSummaryBar(summary: SiteSummary) {
  val fraction = (summary.averagePm25 / 32.0).toFloat().coerceIn(0f, 1f)
  Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
    Row { Text(summary.siteName, color = HmiTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f)); Text("${"%.1f".format(summary.averagePm25)}", color = if (summary.highlighted) DriveAmber else HmiTextPrimary, fontWeight = FontWeight.Black, fontSize = 17.sp) }
    Box(Modifier.fillMaxWidth().height(13.dp).background(HmiDivider, RoundedCornerShape(50))) { Box(Modifier.fillMaxWidth(fraction).height(13.dp).background(if (summary.highlighted) DriveAmber else ElectricCyan, RoundedCornerShape(50))) }
  }
}

@Composable private fun BigCallout(value: String, label: String, color: Color) { Row(verticalAlignment = Alignment.Bottom) { Text(value, color = color, fontSize = 62.sp, fontWeight = FontWeight.Black); Text(label, color = HmiTextSecondary, fontSize = 14.sp, modifier = Modifier.padding(start = 9.dp, bottom = 11.dp)) } }

@Composable private fun ResultTile(label: String, value: String, modifier: Modifier) { Column(modifier.height(92.dp).background(CockpitPanel, RoundedCornerShape(14.dp)).border(1.dp, HmiDivider, RoundedCornerShape(14.dp)).padding(15.dp)) { Text(label, color = HmiTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold); Text(value, color = HmiTextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 7.dp)) } }

@Preview(showBackground = true, widthDp = 1080, heightDp = 600)
@Composable private fun CockpitPreview() { LABEVMissionConsoleTheme { MissionConsole() } }
