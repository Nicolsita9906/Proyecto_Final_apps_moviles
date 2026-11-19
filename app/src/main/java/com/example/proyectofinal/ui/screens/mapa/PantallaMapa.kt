package com.example.proyectofinal.ui.screens.mapa

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyectofinal.R
import com.example.proyectofinal.data.Beige
import com.example.proyectofinal.data.Crema
import com.example.proyectofinal.data.Dorado
import com.example.proyectofinal.data.Gris
import com.example.proyectofinal.data.Oscuro
import com.example.proyectofinal.data.model.BarberiaEntity
import com.example.proyectofinal.data.model.Rol
import com.example.proyectofinal.navigation.Pantallas
import com.example.proyectofinal.ui.screens.AmbarPendiente
import com.example.proyectofinal.ui.screens.BarraInferior
import com.example.proyectofinal.ui.screens.BarraSuperior
import com.example.proyectofinal.ui.screens.BotonPrimario
import com.example.proyectofinal.ui.screens.CampoTexto
import com.example.proyectofinal.ui.screens.Insignia
import com.example.proyectofinal.ui.screens.Rojo
import com.example.proyectofinal.ui.screens.TarjetaBlanca
import com.example.proyectofinal.viewmodel.MapaViewModel
import com.example.proyectofinal.viewmodel.UiState
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.util.Locale

/** Centro de Armenia (Plaza de Bolívar) y el área a la que se limita el mapa. */
private val CENTRO_ARMENIA = GeoPoint(4.5339, -75.6727)
private val LIMITES_ARMENIA = BoundingBox(4.6000, -75.6100, 4.4700, -75.7500)   // norte, este, sur, oeste

/**
 * Mapa de barberías de Armenia.
 *  - Todos ven un marcador por cada barbería con ubicación; al tocarlo aparece su tarjeta.
 *  - Barbero y administrador: al tocar un punto vacío se abre una ventana para registrar
 *    la barbería ahí. La dirección se llena sola con la API de Nominatim (MapaViewModel).
 */
@Composable
fun PantallaMapa(nav: NavController, vm: MapaViewModel = viewModel()) {
    val barberias by vm.barberias.collectAsState()
    var seleccionada by remember { mutableStateOf<BarberiaEntity?>(null) }
    val esCliente = !vm.puedeRegistrar

    Scaffold(
        containerColor = Crema,
        topBar = { BarraSuperior("Mapa de barberías", nav) },
        bottomBar = { if (esCliente) BarraInferior(nav, Pantallas.Mapa.ruta) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            MapaOsm(
                barberias = barberias,
                punto = vm.punto,
                onTocarMapa = { lat, lon ->
                    seleccionada = null
                    vm.seleccionarPunto(lat, lon)      // no hace nada si es cliente
                },
                onTocarMarcador = { seleccionada = it },
                modifier = Modifier.fillMaxSize()
            )

            // Ayuda arriba del mapa
            Text(
                if (vm.puedeRegistrar) "Toca el mapa donde está la barbería para registrarla"
                else "Toca un marcador para ver la barbería",
                color = Oscuro, fontSize = 13.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(12.dp)
                    .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(50))
                    .border(1.dp, Beige, RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )

            if (barberias.isEmpty()) {
                Text(
                    "Aún no hay barberías ubicadas en el mapa",
                    color = Gris, fontSize = 13.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                )
            }

            seleccionada?.let { b ->
                TarjetaBarberiaMapa(
                    barberia = b,
                    rol = vm.rol,
                    esPropia = vm.esPropia(b),
                    nav = nav,
                    onCerrar = { seleccionada = null },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                )
            }
        }
    }

    if (vm.punto != null) VentanaRegistro(vm)
}

// ----------------------------------------------------------------------------------------
//  Mapa (osmdroid es una vista clásica de Android: se mete en Compose con AndroidView)
// ----------------------------------------------------------------------------------------

@Composable
private fun MapaOsm(
    barberias: List<BarberiaEntity>,
    punto: Pair<Double, Double>?,
    onTocarMapa: (Double, Double) -> Unit,
    onTocarMarcador: (BarberiaEntity) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    // Siempre la versión más reciente de las funciones (el mapa se crea una sola vez)
    val tocarMapa by rememberUpdatedState(onTocarMapa)
    val tocarMarcador by rememberUpdatedState(onTocarMarcador)

    val mapa = remember {
        configurarOsmdroid(context)
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            minZoomLevel = 12.0
            maxZoomLevel = 19.0
            setScrollableAreaLimitDouble(LIMITES_ARMENIA)
            controller.setZoom(15.0)
            controller.setCenter(CENTRO_ARMENIA)
            // Capa que escucha los toques en zonas vacías del mapa (va de primera, debajo de los marcadores)
            overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    tocarMapa(p.latitude, p.longitude)
                    return true
                }

                override fun longPressHelper(p: GeoPoint): Boolean = false
            }))
        }
    }

    // El mapa necesita saber cuándo la pantalla se pausa o se cierra
    DisposableEffect(lifecycle) {
        val observador = LifecycleEventObserver { _, evento ->
            when (evento) {
                Lifecycle.Event.ON_RESUME -> mapa.onResume()
                Lifecycle.Event.ON_PAUSE -> mapa.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observador)
        onDispose {
            lifecycle.removeObserver(observador)
            mapa.onDetach()
        }
    }

    AndroidView(
        factory = { mapa },
        modifier = modifier,
        update = { m ->
            // Se vuelven a dibujar los marcadores cada vez que cambian las barberías o el punto tocado
            m.overlays.removeAll { it is Marker }
            barberias.forEach { b ->
                m.overlays.add(Marker(m).apply {
                    position = GeoPoint(b.latitud!!, b.longitud!!)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = b.nombre
                    icon = ContextCompat.getDrawable(
                        context,
                        if (b.estadoAprobacion == "APROBADA") R.drawable.marcador_barberia else R.drawable.marcador_pendiente
                    )
                    setOnMarkerClickListener { _, _ ->
                        tocarMarcador(b)
                        true
                    }
                })
            }
            punto?.let { (lat, lon) ->
                m.overlays.add(Marker(m).apply {
                    position = GeoPoint(lat, lon)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = ContextCompat.getDrawable(context, R.drawable.marcador_nuevo)
                    setOnMarkerClickListener { _, _ -> true }
                })
            }
            m.invalidate()
        }
    )
}

/**
 * osmdroid pide identificarse (User-Agent) y una carpeta para guardar las imágenes del mapa.
 * OJO: los servidores de OpenStreetMap bloquean (403) a las apps que se identifican como
 * "com.example...", por eso no se usa context.packageName sino un nombre propio.
 */
private fun configurarOsmdroid(context: Context) {
    Configuration.getInstance().apply {
        userAgentValue = "BarberGo-Android/1.0 (proyecto academico)"
        osmdroidBasePath = File(context.cacheDir, "osmdroid")
        osmdroidTileCache = File(osmdroidBasePath, "tiles")
    }
}

// ----------------------------------------------------------------------------------------
//  Tarjeta de la barbería tocada
// ----------------------------------------------------------------------------------------

@Composable
private fun TarjetaBarberiaMapa(
    barberia: BarberiaEntity,
    rol: String,
    esPropia: Boolean,
    nav: NavController,
    onCerrar: () -> Unit,
    modifier: Modifier
) {
    TarjetaBlanca(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                barberia.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Oscuro,
                modifier = Modifier.weight(1f)
            )
            Text("✕", color = Gris, fontSize = 18.sp, modifier = Modifier.clickable { onCerrar() }.padding(4.dp))
        }
        Text(barberia.direccion, color = Gris, fontSize = 13.sp)
        barberia.telefono?.let { Text("Tel. $it", color = Gris, fontSize = 13.sp) }
        if (barberia.estadoAprobacion != "APROBADA") {
            Spacer(Modifier.height(6.dp))
            Insignia(
                if (barberia.estadoAprobacion == "PENDIENTE") "Pendiente de aprobación" else "Rechazada",
                if (barberia.estadoAprobacion == "PENDIENTE") AmbarPendiente else Rojo
            )
        }

        // Botón según quién mira
        val accion: Pair<String, String>? = when {
            rol == Rol.ADMINISTRADOR -> "Gestionar barberías" to Pantallas.AdminBarberias.ruta
            rol == Rol.BARBERO && esPropia -> "Ir a mi barbería" to Pantallas.BarberoBarberia.ruta
            rol == Rol.BARBERO -> null
            else -> "Ver barbería" to Pantallas.Detalle.crear(barberia.idBarberia)
        }
        accion?.let { (texto, ruta) ->
            Spacer(Modifier.height(10.dp))
            BotonPrimario(texto) { nav.navigate(ruta) }
        }
    }
}

// ----------------------------------------------------------------------------------------
//  Ventana para registrar (o mover) una barbería en el punto tocado — solo barbero y admin
// ----------------------------------------------------------------------------------------

@Composable
private fun VentanaRegistro(vm: MapaViewModel) {
    val context = LocalContext.current
    val ubicables by vm.barberiasUbicables.collectAsState()
    val barberos by vm.barberos.collectAsState()
    val estado = vm.uiState
    val esAdmin = vm.rol == Rol.ADMINISTRADOR

    var idExistente by remember { mutableStateOf<Int?>(null) }     // null = barbería nueva
    var idDueno by remember { mutableStateOf<Int?>(null) }
    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var direccionEditada by remember { mutableStateOf(false) }
    var telefono by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }
    val nueva = idExistente == null

    // Cuando la API responde, la dirección se llena sola (si el usuario no la ha escrito)
    LaunchedEffect(estado) {
        if (estado is UiState.Success && !direccionEditada) direccion = estado.data.textoCorto()
    }

    AlertDialog(
        onDismissRequest = { if (!guardando) vm.cerrarVentana() },
        containerColor = Crema,
        title = { Text(if (nueva) "Registrar barbería aquí" else "Mover barbería aquí", fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                vm.punto?.let { (lat, lon) ->
                    Text(String.format(Locale.US, "Ubicación: %.5f, %.5f", lat, lon), color = Gris, fontSize = 12.sp)
                }
                // Estado de la consulta a la API
                when (estado) {
                    is UiState.Idle -> Unit
                    is UiState.Loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(16.dp), color = Dorado, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Buscando la dirección…", color = Gris, fontSize = 13.sp)
                    }
                    is UiState.Success -> estado.data.barrio()?.let {
                        Text("Barrio: $it", color = Gris, fontSize = 13.sp)
                    }
                    is UiState.Error -> Text(estado.message, color = Rojo, fontSize = 13.sp)
                }
                Spacer(Modifier.height(12.dp))

                if (ubicables.isNotEmpty()) {
                    Selector(
                        etiqueta = "¿Qué quieres ubicar aquí?",
                        opciones = listOf<Pair<Int?, String>>(null to "Una barbería nueva") +
                            ubicables.map { it.idBarberia to it.nombre },
                        seleccion = idExistente,
                        vacio = "Una barbería nueva"
                    ) { idExistente = it }
                }

                if (nueva) {
                    if (esAdmin) {
                        Selector(
                            etiqueta = "Barbero dueño",
                            opciones = barberos.map { it.idUsuario to "${it.nombre} (${it.correo})" },
                            seleccion = idDueno,
                            vacio = if (barberos.isEmpty()) "No hay barberos registrados" else "Elige un barbero"
                        ) { idDueno = it }
                    }
                    CampoTexto("Nombre", nombre, { nombre = it }, "Ej. Barbería el corte")
                }
                CampoTexto(
                    "Dirección", direccion,
                    { direccion = it; direccionEditada = true },
                    if (estado is UiState.Loading) "Buscando…" else "Ej. Cra 14 #10-25"
                )
                if (nueva) {
                    CampoTexto("Teléfono (opcional)", telefono, { telefono = it }, "Ej. 3001234567", KeyboardType.Phone)
                    CampoTexto("Descripción (opcional)", descripcion, { descripcion = it }, "Cuéntales a tus clientes")
                    Text(
                        if (esAdmin) "Quedará aprobada y visible para los clientes."
                        else "Quedará pendiente hasta que el administrador la apruebe.",
                        color = Gris, fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !guardando,
                onClick = {
                    guardando = true
                    vm.guardar(idExistente, nombre, direccion, telefono, descripcion, idDueno) { error ->
                        guardando = false
                        avisar(context, error ?: if (nueva) "Barbería registrada" else "Ubicación actualizada")
                    }
                }
            ) { Text("Guardar", color = Dorado, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(enabled = !guardando, onClick = { vm.cerrarVentana() }) { Text("Cancelar", color = Gris) }
        }
    )
}

/** Lista desplegable sencilla (texto que al tocarlo muestra las opciones). */
@Composable
private fun <T> Selector(
    etiqueta: String,
    opciones: List<Pair<T, String>>,
    seleccion: T?,
    vacio: String,
    onSel: (T) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    val texto = opciones.firstOrNull { it.first == seleccion }?.second ?: vacio

    Text(etiqueta, color = Gris, fontSize = 14.sp)
    Spacer(Modifier.height(4.dp))
    Box {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, Beige, RoundedCornerShape(16.dp))
                .clickable(enabled = opciones.isNotEmpty()) { abierto = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(texto, color = Oscuro, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Text("▾", color = Gris)
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones.forEach { (valor, nombre) ->
                DropdownMenuItem(text = { Text(nombre) }, onClick = { onSel(valor); abierto = false })
            }
        }
    }
    Spacer(Modifier.height(12.dp))
}

private fun avisar(context: Context, texto: String) =
    Toast.makeText(context, texto, Toast.LENGTH_LONG).show()
