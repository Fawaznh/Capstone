package capstone.wumaps;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Building
{
    private String name;
    private String abbr;
    private LatLng location;
    private ArrayList<LatLng> entrances = new ArrayList<>();

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
}
