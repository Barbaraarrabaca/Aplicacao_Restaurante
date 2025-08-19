package com.example.comidasaborosa.fragmentos

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.comidasaborosa.R
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.net.URL

// Define um Fragmento para mostrar localização e rotas
class FragmentoLocalizacao : Fragment(R.layout.fragmento_localizacao) {
    // Código de requisição para permissão de localização e cliente de localização
    private val REQUEST_LOCATION = 1001
    private val fused by lazy { LocationServices.getFusedLocationProviderClient(requireActivity()) }

    private var map: MapView? = null

    // Variáveis para salvar o estado do mapa
    private var savedMapCenter: GeoPoint? = null
    private var savedMapZoom: Double = 15.0
    private var isMapInitialized = false

    // Destino fixo definido como Tomar, Portugal (coordenadas)
    private val destinoTomar = GeoPoint(39.6031, -8.4098)

    // Configuração inicial do OSMDroid (biblioteca de mapas)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar configuração do osmdroid apenas uma vez
        val ctx = requireContext().applicationContext
        Configuration.getInstance().load(
            ctx,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(ctx)
        )
        Configuration.getInstance().userAgentValue = ctx.packageName
    }
    // Chamado quando a view do fragmento é criada
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Associa a view do mapa
        map = view.findViewById(R.id.map)

        // Configurações iniciais do mapa
        map?.apply {
            // Usa tiles do estilo MAPNIK (OpenStreetMap)
            setTileSource(TileSourceFactory.MAPNIK)
            // Ativa controles multitouch
            setMultiTouchControls(true)

            // Restaurar estado se existir
            savedMapCenter?.let { center ->
                controller.setCenter(center)
                controller.setZoom(savedMapZoom)
            }
        }

        // Só inicializar localização uma vez
        if (!isMapInitialized) {
            obterLocalEPlotar()
            isMapInitialized = true
        }
    }


    // Obter localização e colocar marcadores
    private fun obterLocalEPlotar() {
        // Verifica permissão de localização
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
            return
        }
        // Obtém última localização conhecida
        fused.lastLocation.addOnSuccessListener { loc: Location? ->
            loc?.let { location ->
                // Converte Location para GeoPoint (OSMDroid)
                val origem = GeoPoint(location.latitude, location.longitude)

                map?.apply {
                    // Usar a localização atual
                    if (savedMapCenter == null) {
                        controller.setZoom(savedMapZoom)
                        controller.setCenter(origem)
                        savedMapCenter = origem
                    }

                    // Limpar overlays anteriores antes de adicionar novos
                    overlays.clear()

                    // marcador do utilizador
                    overlays.add(Marker(this).apply {
                        position = origem
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Você"
                    })

                    // marcador destino (Tomar)
                    overlays.add(Marker(this).apply {
                        position = destinoTomar
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Tomar"
                    })

                    // Invalidar para garantir que os marcadores apareçam
                    invalidate()

                    // Traçar rota
                    desenharRota(origem, destinoTomar)
                }
            }
        }
    }


    // Rota via OSRM
    private fun desenharRota(origem: GeoPoint, destino: GeoPoint) {
        // Constrói URL da API com coordenadas
        val url = "https://router.project-osrm.org/route/v1/driving/" +
                "${origem.longitude},${origem.latitude};" +
                "${destino.longitude},${destino.latitude}" +
                "?overview=full&geometries=polyline"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Faz requisição à API
                val json = URL(url).readText()
                // Extrai geometria da rota
                val geometry = JSONObject(json)
                    .getJSONArray("routes")
                    .getJSONObject(0)
                    .getString("geometry")

                // Decodifica a polyline para lista de GeoPoints
                val pontos = decodificarPolyLine(geometry)

                withContext(Dispatchers.Main) {
                    map?.overlays?.add(
                        Polyline().apply {
                            setPoints(pontos)
                            setWidth(8f)
                            setColor(0xFFE91E63.toInt())
                        }
                    )
                    map?.invalidate()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    // Decodificador polyline (Google/OSRM 1e-5)
    private fun decodificarPolyLine(encoded: String): List<GeoPoint> {
        val list = mutableListOf<GeoPoint>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        // Algoritmo de decodificação padrão
        while (index < len) {
            // Decodifica latitude
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            // Decodifica longitude
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            // Adiciona ponto decodificado
            list.add(GeoPoint(lat / 1E5, lng / 1E5))
        }
        return list
    }


    // Trata resultado da solicitação de permissão
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            obterLocalEPlotar()
        }
    }


    // MapView lifecycle
    override fun onResume() {
        super.onResume()
        map?.onResume()
    }

    override fun onPause() {
        super.onPause()
        // Salvar estado atual do mapa
        map?.let {
            savedMapCenter = GeoPoint(it.mapCenter.latitude, it.mapCenter.longitude)
            savedMapZoom = it.zoomLevelDouble
        }
        map?.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        map = null
    }


    companion object {
        @JvmStatic
        fun newInstance() = FragmentoLocalizacao()
    }
}
