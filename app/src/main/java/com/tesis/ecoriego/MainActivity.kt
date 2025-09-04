package com.tesis.ecoriego

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

/* ========= Datos y enums ========= */

enum class Tab { Dashboard, Reports, Profile, Alerts }
enum class Zone { VillaAlemana, Petorca }
enum class AlertType { Maintenance, Efficiency, Progress, Settings }

data class AlertItem(val type: AlertType, val message: String)

data class ZoneStats(
    val temperatureRanges: Map<String, IntRange>,
    val humidityRanges: Map<String, IntRange>,
    val savings: Triple<Int, Int, Int>, // diario, semanal, mensual
    val consumptionEcoriego: List<Int>,
    val consumptionTrad: List<Int>
)

val climateData: Map<Zone, ZoneStats> = mapOf(
    Zone.VillaAlemana to ZoneStats(
        temperatureRanges = mapOf(
            "morning" to (10..20), "afternoon" to (20..28),
            "evening" to (15..20), "night" to (8..15)
        ),
        humidityRanges = mapOf(
            "morning" to (60..80), "afternoon" to (40..60),
            "evening" to (55..75), "night" to (70..90)
        ),
        savings = Triple(450, 3150, 4800),
        consumptionEcoriego = listOf(340, 360, 320, 380, 350, 330, 370),
        consumptionTrad = listOf(680, 720, 640, 760, 700, 660, 740)
    ),
    Zone.Petorca to ZoneStats(
        temperatureRanges = mapOf(
            "morning" to (12..22), "afternoon" to (25..35),
            "evening" to (18..25), "night" to (10..18)
        ),
        humidityRanges = mapOf(
            "morning" to (40..60), "afternoon" to (20..40),
            "evening" to (35..55), "night" to (50..70)
        ),
        savings = Triple(500, 3500, 14000),
        consumptionEcoriego = listOf(480, 520, 470, 530, 500, 490, 510),
        consumptionTrad = listOf(1200, 1300, 1175, 1325, 1250, 1225, 1275)
    )
)

/* ========= Activity ========= */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                EcoriegoApp()
            }
        }
    }
}

/* ========= App ========= */

@Composable
fun EcoriegoApp() {
    var loggedIn by remember { mutableStateOf(false) }
    var header by remember { mutableStateOf("Iniciar Sesión") }
    var currentTab by remember { mutableStateOf(Tab.Dashboard) }
    var currentZone by remember { mutableStateOf(Zone.VillaAlemana) }

    MaterialTheme(colorScheme = lightColorScheme()) {
        Scaffold(
            bottomBar = {
                if (loggedIn) {
                    NavigationBar(containerColor = Color(0xFF10B981)) {
                        NavigationBarItem(
                            selected = currentTab == Tab.Dashboard,
                            onClick = { currentTab = Tab.Dashboard },
                            icon = { Icon(Icons.Filled.Home, null, tint = Color.White) },
                            label = { Text("Panel", color = Color.White) }
                        )
                        NavigationBarItem(
                            selected = currentTab == Tab.Reports,
                            onClick = { currentTab = Tab.Reports },
                            icon = { Icon(Icons.Filled.Assessment, null, tint = Color.White) },
                            label = { Text("Reportes", color = Color.White) }
                        )
                        NavigationBarItem(
                            selected = currentTab == Tab.Profile,
                            onClick = { currentTab = Tab.Profile },
                            icon = { Icon(Icons.Filled.AccountCircle, null, tint = Color.White) },
                            label = { Text("Perfil", color = Color.White) }
                        )
                        NavigationBarItem(
                            selected = currentTab == Tab.Alerts,
                            onClick = { currentTab = Tab.Alerts },
                            icon = { Icon(Icons.Filled.Notifications, null, tint = Color.White) },
                            label = { Text("Alertas", color = Color.White) }
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { loggedIn = false },
                            icon = { Icon(Icons.Filled.PowerSettingsNew, null, tint = Color.White) },
                            label = { Text("Salir", color = Color.White) }
                        )
                    }
                }
            }
        ) { padding ->
            Surface(Modifier.fillMaxSize().background(Color(0xFFF0F4F8))) {
                Column(Modifier.fillMaxSize().padding(16.dp).padding(padding)) {

                    // Encabezado
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF10B981))
                            .padding(20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ecoriego", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Text(header, color = Color.White, fontSize = 16.sp)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Contenido
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                    ) {
                        if (!loggedIn) {
                            header = "Iniciar Sesión"
                            LoginScreen(
                                onLogin = { user, pass ->
                                    if (user.isNotBlank() && pass.isNotBlank()) {
                                        loggedIn = true
                                        header = "Panel de Control"
                                        currentTab = Tab.Dashboard
                                    }
                                }
                            )
                        } else {
                            when (currentTab) {
                                Tab.Dashboard -> { header = "Panel de Control"; DashboardScreen(currentZone) }
                                Tab.Reports   -> { header = "Reportes"; ReportsScreen(currentZone) }
                                Tab.Profile   -> { header = "Perfil de Usuario"; ProfileScreen(currentZone) }
                                Tab.Alerts    -> { header = "Alertas"; AlertsScreen() }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ========= Pantallas ========= */

@Composable
fun LoginScreen(onLogin: (String, String) -> Unit) {
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Text("Bienvenido", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF1F2937))
        Text("Por favor, inicia sesión para continuar.", color = Color(0xFF6B7280))
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = user, onValueChange = { user = it },
            label = { Text("Usuario") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = pass, onValueChange = { pass = it },
            label = { Text("Contraseña") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        if (error != null) {
            Text(error!!, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(Modifier.height(20.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (user.isBlank() || pass.isBlank()) error = "Ingresa usuario y contraseña."
                else onLogin(user, pass)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
        ) { Text("Iniciar Sesión") }
    }
}

@Composable
fun DashboardScreen(currentZone: Zone) {
    var zone by remember { mutableStateOf(currentZone) }

    // Selector de zona
    var zoneMenu by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Box(Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { zoneMenu = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Zona de trabajo: " + if (zone == Zone.VillaAlemana) "Villa Alemana" else "Petorca")
            }
            DropdownMenu(expanded = zoneMenu, onDismissRequest = { zoneMenu = false }) {
                DropdownMenuItem(text = { Text("Villa Alemana") }, onClick = { zone = Zone.VillaAlemana; zoneMenu = false })
                DropdownMenuItem(text = { Text("Petorca") }, onClick = { zone = Zone.Petorca; zoneMenu = false })
            }
        }

        // Sensores simulados (cada 5s)
        val now = remember { mutableStateOf(java.time.LocalTime.now()) }
        LaunchedEffect(zone) {
            while (true) {
                now.value = java.time.LocalTime.now()
                kotlinx.coroutines.delay(5_000)
            }
        }
        fun simFor(z: Zone): Pair<Int, Int> {
            val hour = now.value.hour
            val key = when (hour) {
                in 6..11 -> "morning"
                in 12..17 -> "afternoon"
                in 18..21 -> "evening"
                else -> "night"
            }
            val data = climateData[z]!!
            val t = data.temperatureRanges[key]!!.random()
            val h = data.humidityRanges[key]!!.random()
            return t to h
        }
        val (temp, hum) = simFor(zone)

        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "Humedad del Suelo",
                value = "$hum%",
                bg = Color(0xFFDBEAFE),
                fg = Color(0xFF1D4ED8),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Temperatura del Aire",
                value = "${temp}°C",
                bg = Color(0xFFFFEDD5),
                fg = Color(0xFFEA580C),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Estados y acciones
        var isAutomatic by remember { mutableStateOf(true) }
        var statusText by remember { mutableStateOf("Riego automático activo") }
        var toggleText by remember { mutableStateOf("DETENER RIEGO") }
        val alerts = remember { mutableStateListOf<AlertItem>() }

        Button(
            onClick = {
                isAutomatic = !isAutomatic
                if (isAutomatic) {
                    statusText = "Riego automático activo"
                    toggleText = "DETENER RIEGO"
                    alerts.clear()
                } else {
                    statusText = "Riego manual activo"
                    toggleText = "ACTIVAR RIEGO AUTOMÁTICO"
                    alerts += AlertItem(AlertType.Efficiency, "Consumo inusual detectado. Revisa posibles fugas.")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = if (isAutomatic) Color(0xFFA3E635) else Color(0xFFEF4444)),
            modifier = Modifier.fillMaxWidth()
        ) { Text(toggleText) }

        Spacer(Modifier.height(8.dp))
        var durationInput by remember { mutableStateOf("") }
        OutlinedTextField(
            value = durationInput,
            onValueChange = { durationInput = it },
            label = { Text("Duración en minutos (opcional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                val d = durationInput.toIntOrNull()
                if (d != null && d > 0) {
                    alerts += AlertItem(AlertType.Progress, "Riego manual por $d minutos. La programación automática se pausa.")
                    statusText = "Riego manual por $d min."
                } else {
                    alerts += AlertItem(AlertType.Efficiency, "Ingresa una duración válida.")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("INICIAR RIEGO POR TIEMPO") }

        Spacer(Modifier.height(16.dp))
        // Estado actual
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFD1FAE5))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(statusText, color = Color(0xFF065F46), fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(16.dp))

        // Configuración automática
        Text("Configuración de Riego Automático", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        val days = listOf("Lun","Mar","Mié","Jue","Vie","Sáb","Dom")
        val dayChecks = remember { mutableStateListOf(false, false, true, false, true, false, false) }
        FlowDays(days, dayChecks)

        Spacer(Modifier.height(8.dp))
        var startTime by remember { mutableStateOf("06:00") }
        var waterLiters by remember { mutableStateOf("100") }
        OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("Hora de inicio (HH:MM)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = waterLiters, onValueChange = { waterLiters = it },
            label = { Text("Cantidad de agua (litros)") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        fun nextEventText(): String {
            val selected = days.zip(dayChecks).filter { it.second }.map { it.first }
            val ds = if (selected.isEmpty()) "Ninguno" else selected.joinToString(", ")
            val liters = waterLiters.toIntOrNull() ?: 0
            return "Próximo riego programado:\n$ds a las $startTime\nCantidad de agua: $liters litros"
        }
        var nextEvent by remember { mutableStateOf(nextEventText()) }

        Button(
            onClick = {
                val anyDay = dayChecks.any { it }
                val liters = waterLiters.toIntOrNull()
                if (anyDay && startTime.matches(Regex("""^\d{2}:\d{2}$""")) && liters != null && liters > 0) {
                    nextEvent = nextEventText()
                    alerts += AlertItem(AlertType.Settings, "¡Configuración guardada! $nextEvent")
                } else {
                    alerts += AlertItem(AlertType.Efficiency, "Selecciona al menos un día, hora válida y litros > 0.")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Guardar Configuración") }

        Spacer(Modifier.height(12.dp))
        Text("Próximo Evento Programado", fontWeight = FontWeight.Bold)
        Text(nextEvent)

        Spacer(Modifier.height(16.dp))
        Text("Alertas", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        alerts.forEach { AlertCard(it) }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
fun ReportsScreen(currentZone: Zone) {
    val z = climateData[currentZone]!!
    val (daily, weekly, monthly) = z.savings

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFD1FAE5))
                .padding(16.dp)
        ) {
            Column {
                Text("Consumo de Agua", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF065F46))
                Text("Litros ahorrados este mes: $monthly", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF065F46))
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Comparación de Ahorro Semanal", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        val labels = listOf("Lun","Mar","Mié","Jue","Vie","Sáb","Dom")
        val ecoriego = z.consumptionEcoriego
        val tradicional = z.consumptionTrad
        val maxVal = (tradicional + ecoriego).maxOrNull()?.coerceAtLeast(1) ?: 1

        val density = LocalDensity.current
        val barWidthPx = with(density) { 20.dp.toPx() }
        val gapPx = with(density) { 10.dp.toPx() }
        val chartHeightPx = with(density) { 180.dp.toPx() }
        val leftPaddingPx = with(density) { 40.dp.toPx() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(12.dp)
        ) {
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
            ) {
                val usableWidth = size.width - leftPaddingPx - 16f
                val groupWidth = (barWidthPx * 2 + gapPx) // por si quieres usarlo después
                val spacePerGroup = usableWidth / labels.size
                val baseY = size.height - 20f
                val maxH = chartHeightPx

                // Eje X
                drawLine(Color.Gray, start = Offset(leftPaddingPx, baseY), end = Offset(size.width - 8f, baseY), strokeWidth = 2f)

                labels.forEachIndexed { i, _ ->
                    val xGroup = leftPaddingPx + i * spacePerGroup + 10f
                    val eH = (ecoriego[i].toFloat() / maxVal) * maxH
                    val tH = (tradicional[i].toFloat() / maxVal) * maxH

                    drawRect(
                        color = Color(0xFF10B981),
                        topLeft = Offset(xGroup, baseY - eH),
                        size = androidx.compose.ui.geometry.Size(barWidthPx, eH)
                    )
                    drawRect(
                        color = Color(0xFFD1D5DB),
                        topLeft = Offset(xGroup + barWidthPx + 6f, baseY - tH),
                        size = androidx.compose.ui.geometry.Size(barWidthPx, tH)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryCard("Diario", "${daily}L", modifier = Modifier.weight(1f))
            SummaryCard("Semanal", "${weekly}L", modifier = Modifier.weight(1f))
            SummaryCard("Mensual", "${monthly}L", modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
fun ProfileScreen(currentZone: Zone) {
    var monthlyGoal by remember { mutableStateOf(5000) }
    val currentSavings = climateData[currentZone]!!.savings.third

    var displayName by remember { mutableStateOf("Juan Pérez") }
    val initials = remember(displayName) {
        val parts = displayName.trim().split(" ").filter { it.isNotBlank() }
        when (parts.size) {
            0 -> ""
            1 -> parts[0].first().uppercase()
            else -> (parts.first().first().toString() + parts.last().first()).uppercase()
        }
    }

    val progress = (currentSavings.toFloat() / monthlyGoal.toFloat()).coerceAtMost(1f)

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(48.dp))
                .background(Color(0xFFE5E7EB)),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, fontSize = 28.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = displayName, onValueChange = { displayName = it },
            label = { Text("Nombre a mostrar") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        Text("Mi Meta de Ahorro", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        var inputGoal by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        OutlinedTextField(
            value = inputGoal, onValueChange = { inputGoal = it },
            label = { Text("Meta mensual (litros)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                val g = inputGoal.toIntOrNull()
                if (g != null && g > 0) {
                    monthlyGoal = g
                    message = "¡Meta actualizada a $monthlyGoal L!"
                } else {
                    message = "Ingresa un número válido mayor que 0."
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA3E635)),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Guardar Meta") }

        Spacer(Modifier.height(12.dp))
        Text("Progreso Actual", fontWeight = FontWeight.SemiBold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE5E7EB))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(Color(0xFFA3E635))
            )
        }
        Text("${currentSavings}L ahorrados de ${monthlyGoal}L", fontWeight = FontWeight.SemiBold, color = Color(0xFF15803D))
        if (message.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(message)
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
fun AlertsScreen() {
    val alerts = listOf(
        AlertItem(AlertType.Maintenance, "El sensor de humedad de la zona 1 dejó de funcionar."),
        AlertItem(AlertType.Efficiency, "Consumo inusual detectado. Revisa fugas o configuración."),
        AlertItem(AlertType.Progress, "¡Superaste tu meta de ahorro de agua este mes!")
    )
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Alertas", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        alerts.forEach { AlertCard(it) }
        Spacer(Modifier.height(80.dp))
    }
}

/* ========= UI helpers ========= */

@Composable
fun StatCard(
    title: String,
    value: String,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = fg, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(title, color = fg.copy(alpha = 0.8f))
    }
}

@Composable
fun AlertCard(item: AlertItem) {
    val (bg, border, title) = when (item.type) {
        AlertType.Maintenance -> Triple(Color(0xFFFFE4E6), Color(0xFFFCA5A5), "Alerta de Mantenimiento")
        AlertType.Efficiency  -> Triple(Color(0xFFFEF3C7), Color(0xFFFDE68A), "Alerta de Eficiencia")
        AlertType.Progress    -> Triple(Color(0xFFD1FAE5), Color(0xFFA7F3D0), "Logro")
        AlertType.Settings    -> Triple(Color(0xFFF3F4F6), Color(0xFFD1D5DB), "Configuración Guardada")
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(title, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(item.message)
    }
    Spacer(Modifier.height(8.dp))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowDays(days: List<String>, checks: MutableList<Boolean>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEachIndexed { idx, label ->
            FilterChipDay(
                text = label,
                checked = checks[idx],
                onToggle = { checks[idx] = !checks[idx] }
            )
        }
    }
}

@Composable
fun FilterChipDay(text: String, checked: Boolean, onToggle: () -> Unit) {
    val bg = if (checked) Color(0xFF10B981) else Color(0xFFE5E7EB)
    val fg = if (checked) Color.White else Color(0xFF374151)
    TextButton(
        onClick = onToggle,
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 12.dp)
    ) {
        Text(text, color = fg, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SummaryCard(
    period: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF3F4F6))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = Color(0xFF10B981), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(period)
    }
}
