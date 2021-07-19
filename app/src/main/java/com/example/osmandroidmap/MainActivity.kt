package com.example.osmandroidmap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*


class MainActivity : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private var map: MapView? = null
    private lateinit var locationOverlay: MyLocationNewOverlay

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        val context = applicationContext
        Configuration.getInstance()
            .load(context, PreferenceManager.getDefaultSharedPreferences(context))
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's
        //tile servers will get you banned based on this string

        //inflate and create the map
        setContentView(R.layout.activity_main)
        map = findViewById<View>(R.id.map) as MapView

        setUpMap()
//        map!!.setTileSource(TileSourceFactory.MAPNIK)
//        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
//        locationOverlay.enableMyLocation()
//        map!!.getOverlays().add(this.locationOverlay);

        requestPermissionsIfNecessary(
            arrayOf( // if you need to show the current location, uncomment the line below
                // Manifest.permission.ACCESS_FINE_LOCATION,
                // WRITE_EXTERNAL_STORAGE is required in order to show the map
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }

    public override fun onResume() {
        super.onResume()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        map!!.onResume() //needed for compass, my location overlays, v6.0.0 and up
    }

    public override fun onPause() {
        super.onPause()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);

        map!!.onPause() //needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionsToRequest = ArrayList<String>()
        for (i in grantResults.indices) {
            permissionsToRequest.add(permissions[i])
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest = ArrayList<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    protected fun setUpMap() {

        map!!.setMultiTouchControls(true)

        val DEFAULT_LOCATION = GeoPoint(59.962447, 30.441147)
        val maxZoomLvl = 19
        val minZoomLvl = 18
        val defZoomLvl = 18


        // макс, мин и текущий zoom
        val mapController = map!!.controller
        map!!.setMaxZoomLevel(maxZoomLvl.toDouble())
        map!!.setMinZoomLevel(minZoomLvl.toDouble())
        mapController.setZoom(defZoomLvl.toDouble())

        // установим центр карты
        mapController.setCenter(DEFAULT_LOCATION)

        // устанвливает тайлы карты
        val isYandexTile = true
        if (isYandexTile) {
            val aBaseUrlYandex = arrayOf(
                "https://core-renderer-tiles.maps.yandex.net/vmap2/tiles?lang=ru_RU&x=%s&y=%s&z=%s&zmin=%s&zmax=%s&v=21.07.19-1-b210701140430"
//                "https://vec04.maps.yandex.net/tiles?l=map&v=4.55.2&x=%s&y=%s&z=%s&lang=ru-RU",
//                "https://vec03.maps.yandex.net/tiles?l=map&v=4.55.2&x=%s&y=%s&z=%s&lang=ru-RU",
//                "https://vec02.maps.yandex.net/tiles?l=map&v=4.55.2&x=%s&y=%s&z=%s&lang=ru-RU",
//                "https://vec01.maps.yandex.net/tiles?l=map&v=4.55.2&x=%s&y=%s&z=%s&lang=ru-RU"
            )

            val tileSourceYandex = object : XYTileSource(
                "Yandex",
                minZoomLvl, maxZoomLvl, 256, ".png", aBaseUrlYandex
            ) {
                override fun getTileURLString(pTileIndex: Long): String {
                    val url = java.lang.String.format(
                        baseUrl,
                        MapTileIndex.getX(pTileIndex),
                        MapTileIndex.getY(pTileIndex),
                        MapTileIndex.getZoom(pTileIndex),
                        19,
                        19
                    )
                    Log.d("TAG", "getTileURLString: $url" )

                    return url
                }
            }



            val tileProviderYandex = MapTileProviderBasic(applicationContext)
            tileProviderYandex.tileSource = tileSourceYandex
            tileProviderYandex.setTileRequestCompleteHandler(map!!.tileRequestCompleteHandler)
            val tilesOverlayYandex: TilesOverlay =
                TilesOverlay(tileProviderYandex, this.baseContext)


            map!!.overlayManager.tilesOverlay = tilesOverlayYandex
            map!!.setTileSource(tileSourceYandex)

        } else {
            map!!.setTileSource(TileSourceFactory.CLOUDMADESTANDARDTILES)
        }
    }
}