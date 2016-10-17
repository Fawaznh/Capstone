package capstone.wumaps;

import com.google.android.gms.maps.model.LatLng;

public class DistanceCalculator
{
    private final static double RADIUS = 6371;

    public static double calc(LatLng from, LatLng to)
    {
        double latDiff = Math.toRadians(to.latitude - from.latitude);
        double lngDiff = Math.toRadians(to.longitude - from.longitude);
        double latDiffSin = Math.sin(latDiff / 2);
        double lngDiffSin = Math.sin(lngDiff / 2);
        double a = Math.pow(latDiffSin, 2) + Math.pow(lngDiffSin, 2)
                * Math.cos(Math.toRadians(from.latitude)) * Math.cos(Math.toRadians(to.latitude));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = RADIUS * c;
        return distance;
    }
}
