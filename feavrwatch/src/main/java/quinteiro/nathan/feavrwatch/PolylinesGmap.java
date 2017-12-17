package quinteiro.nathan.feavrwatch;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 * Created by degiovan on 11/11/2017.
 */

public class PolylinesGmap {
    private static final String TAG = "PolylinesGmap";
    private ArrayList<Polyline> mPolylines = new ArrayList<Polyline>();
    private ArrayList<String> mPolylinesTag = new ArrayList<>();


    private GoogleMap mMap;
    private Polyline polyline;


    public PolylinesGmap(GoogleMap mMap){
        this.mMap = mMap;
    }


    public void addPolyline(Polyline polyline, String polylineTag){
        mPolylines.add(polyline);
        mPolylinesTag.add(polylineTag);
    }

    public Polyline getPolylineByTag(String polylineTag){
        return  mPolylines.get(mPolylinesTag.indexOf(polylineTag));
    }

    public boolean redrawRoute(boolean isPolylineLoaded, PolylineOptions polylineOptions, ArrayList<LatLng> routeGmaps, String polylineTag) {

        //Adding all the points in the route to LineOptions
        polylineOptions.addAll(routeGmaps);

        //Drawing polyline in the Google map for the i-th route
        if (!isPolylineLoaded) {
            addPolyline(mMap.addPolyline(polylineOptions), polylineTag);
        } else {
            polyline = getPolylineByTag(polylineTag);
            if (polyline != null)
                polyline.setPoints(routeGmaps);
        }

        return true;
    }
}
