package com.example.osmandroidmap.vers6;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
//import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.TilesOverlay;

import androidx.annotation.Nullable;


public class YandexTilesOverlay extends TilesOverlay
{
    private final Rect mTileRect = new Rect();
    private final Point mTilePos = new Point();

    @Nullable private ColorFilter filter = null;

    private short alpha = -1;

    @Override public void setColorFilter(ColorFilter filter)
    {
        super.setColorFilter(filter);
        this.filter = filter;
    }

    /** Конструктор */
    public YandexTilesOverlay(MapTileProviderBase aTileProvider, Context aContext)
    {
        super(aTileProvider, aContext);
    }

    /** Конструктор с альфой для слоя */
    public YandexTilesOverlay(MapTileProviderBase aTileProvider, Context aContext, short alpha)
    {
        super(aTileProvider, aContext);
        this.alpha = alpha;
    }



    //переопределим метод draw для отрисовки Yandex тайлов
    @Override public void draw(Canvas canvas, MapView mapView, boolean shadow)
    {
        //текущий масштаб
        int zoom = mapView.getZoomLevel();
        final Projection mapViewProjection = mapView.getProjection();
        //координаты углов видимой части карты
        BoundingBox mapViewBoundingBox = mapView.getBoundingBox();
        //получаем координаты верхнего левого и нижнего правого тайла
        double[] MercatorTL = YandexUtil.geoToMercator(new double[]
               { mapViewBoundingBox.getLonWest() , mapViewBoundingBox.getLatNorth() });
        double[] TilesTL = YandexUtil.mercatorToTiles(MercatorTL);
        long[] TileTL = YandexUtil.getTile(TilesTL, zoom);


        double[] MercatorRB = YandexUtil.geoToMercator(new double[]
                { mapViewBoundingBox.getLonEast(), mapViewBoundingBox.getLatSouth() });
        double[] TilesRB = YandexUtil.mercatorToTiles(MercatorRB);
        long[] TileRB = YandexUtil.getTile(TilesRB, zoom);
        mTileProvider.ensureCapacity((int) ((TileRB[1] - TileTL[1] + 1) * (TileRB[0] - TileTL[0] + 1)));

        //геокоординаты верхнего левого тайла Yandex
        double[] reTiles = YandexUtil.ReGetTile(new long[]{TileTL[0],
                TileTL[1]}, zoom);
        long xx = (long) reTiles[0];
        long yy = (long) reTiles[1];
        double[] reMercator = YandexUtil.tileToMercator(new long[]{xx, yy});
        double[] tmp = YandexUtil.mercatorToGeo(reMercator);
        //геокоординаты верхнего левого тайла Yandex переводим в экранные координаты osmdroid
        GeoPoint geoPoint = new GeoPoint(tmp[1], tmp[0]);
        mapViewProjection.toPixels(geoPoint, mTilePos);
        //в цикле отрисовываем все видимые тайлы Yandex
        for (int y = (int) TileTL[1]; y <= TileRB[1]; y++)
        {
            int xcount = 0;

            for (int x = (int) TileTL[0]; x <= TileRB[0]; x++)
            {

                final MapTile tile = new MapTile(zoom, x, y);
                final Drawable currentMapTile = mTileProvider.getMapTile(tile);
                if (currentMapTile != null)
                {

                    mTileRect.set(mTilePos.x, mTilePos.y, mTilePos.x + 256,mTilePos.y + 256);
                    currentMapTile.setBounds(mTileRect);

                    if (alpha > 0)
                        currentMapTile.setAlpha(alpha);

                    if (filter != null)
                        currentMapTile.setColorFilter(filter);

                    currentMapTile.draw(canvas);
                }
                xcount++;
                mTilePos.x += 256;
            }
            mTilePos.x -= xcount * 256;
            mTilePos.y += 256;
        }
    }
}