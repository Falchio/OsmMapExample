package com.example.osmandroidmap.vers6

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.osmandroidmap.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.MapTile
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
//import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
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

//        map!!.onResume() //needed for compass, my location overlays, v6.0.0 and up
    }

    public override fun onPause() {
        super.onPause()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);

//        map!!.onPause() //needed for compass, my location overlays, v6.0.0 and up
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
        val minZoomLvl = 6
        val defZoomLvl = 10


        // макс, мин и текущий zoom
        val mapController = map!!.controller
        map!!.setMaxZoomLevel(maxZoomLvl)
        map!!.setMinZoomLevel(minZoomLvl)
        mapController.setZoom(defZoomLvl)

        // установим центр карты
        mapController.setCenter(DEFAULT_LOCATION)

        val aBaseUrlYandexTraffic = arrayOf(
            "https://core-jams-rdr.maps.yandex.net/1.1/tiles?trf&l=trf,trfe&lang=ru_RU&x=%s&y=%s&z=%s&scale=1&tm=%s"
        )
        val yandexTrafficTile = getYandexTrafficTile(
            "YaTraffic",
            minZoomLvl,
            maxZoomLvl,
            256,
            ".png",
            aBaseUrlYandexTraffic
        )

        if (false) {
            mainOverlay(yandexTrafficTile)
        } else {
            paOverlay(yandexTrafficTile)
        }
    }

    private fun mainOverlay(yandexTrafficTile: XYTileSource) {
        val tileTrafficProvider = MapTileProviderBasic(this, yandexTrafficTile)
        val trafficOverlay = TilesOverlay(tileTrafficProvider, this)
        map!!.setTileSource(TileSourceFactory.MAPNIK)
        map!!.overlays.add(0,trafficOverlay)
    }

    private fun paOverlay(yandexTrafficTile: XYTileSource) {
        val tileTrafficProvider = MapTileProviderBasic(this, yandexTrafficTile)
        val trafficOverlay = YandexTilesOverlay(tileTrafficProvider, this)

        val tileProvider = MapTileProviderBasic(this, TileSourceFactory.MAPNIK)
        val tileOverlay = TilesOverlay(tileProvider, this)

//        map!!.overlays.add(trafficOverlay)
        map!!.setTileSource(TileSourceFactory.MAPNIK)
        map!!.overlays.add(trafficOverlay)
    }


    private fun getYandexTrafficTile(
        name: String,
        minZoomLvl: Int,
        maxZoomLvl: Int,
        tilePixelSize: Int,
        imageFilenameEnding: String,
        baseUrl: Array<String>
    ) = object :
        XYTileSource(name, minZoomLvl, maxZoomLvl, tilePixelSize, imageFilenameEnding, baseUrl) {
        override fun getTileURLString(aTile: MapTile?): String {
            return String.format(
                getBaseUrl(), aTile?.x, aTile?.y, aTile?.zoomLevel, System.currentTimeMillis()
            )
        }
        //        override fun getTileURLString(pMapTileIndex: Long): String {
//
//            val x = MapTileIndex.getX(pMapTileIndex)
//            val y = MapTileIndex.getY(pMapTileIndex)
//            val zoom = MapTileIndex.getZoom(pMapTileIndex)
//
//            return String.format(
//                getBaseUrl(), x, y, zoom, System.currentTimeMillis()
//            )
//        }
    }

}