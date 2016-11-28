package capstone.wumaps;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.HashMap;

public class Building
{
    private String name;
    private String abbr;
    private LatLng location;
    private ArrayList<LatLng> entrances = new ArrayList<>();
    private LatLngBounds bounds;
    private int numFloors;

    public Building(String bName, String abbreviation, LatLng loc)
    {
        name = bName;
        abbr = abbreviation;
        location = loc;
    }

    public void addEntrance(LatLng loc)
    {
        entrances.add(loc);
    }

    public String getName()
    {
        return name;
    }

    public String getAbbr()
    {
        return abbr;
    }

    public LatLng getLocation()
    {
        return location;
    }

    public ArrayList<LatLng> getEntrances()
    {
        return entrances;
    }

    public void setBounds(LatLngBounds b)
    {
        bounds = b;
    }

    public void setFloors(int floors)
    {
        numFloors = floors;
    }

    public int getFloors()
    {
        return numFloors;
    }

    public void setBounds(String sw, String ne)
    {
        if(sw.trim().equals("placeholder"))
            return;
        String[] swCorner = sw.split(",");
        String[] neCorner = ne.split(",");
        bounds = new LatLngBounds(new LatLng(Double.parseDouble(swCorner[0]), Double.parseDouble(swCorner[1])),
                                  new LatLng(Double.parseDouble(neCorner[0]), Double.parseDouble(neCorner[1])));
    }

    public LatLngBounds getBounds()
    {
        return bounds;
    }
}
