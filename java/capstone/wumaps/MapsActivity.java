package capstone.wumaps;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.Manifest;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener,
        LocationListener {

    private GoogleMap mMap;
    private CameraPosition pos;
    private String last;
    private int index;
    private int check;
    private boolean handicapOnly;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MapsActivity.class.getSimpleName();
    private TreeMap<String, Building> buildings = new TreeMap<>();
    private HashMap<String, String> pointers = new HashMap<>();
    private GroundOverlayOptions dotOverlay;
    private GroundOverlay dot;
    private ArrayList<GroundOverlay> buildingOverlay = new ArrayList<>();
    private ArrayList<GroundOverlayOptions> overlayOptionsList = new ArrayList<>();
    private Button selectBuildingButton;
    private Button upButton;
    private Button downButton;
    private Polyline line;
    private boolean following;
    private String building;
    private int roomNum = 999;
    private LocationRequest mLocationRequest;
    private Toast toast;
    private Building goal;
    private LatLngBounds arrival;
    private final LatLngBounds campus = new LatLngBounds(new LatLng(39.029671, -95.706220), new LatLng(39.036934, -95.696822));

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        selectBuildingButton = (Button) findViewById(R.id.selectBuildingButton);
        selectBuildingButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                doPopup(v);
            }
        });

        upButton = (Button) findViewById(R.id.UpButton);
        upButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                downButton.setVisibility(View.VISIBLE);
                adjustButtons(1);
                if(index == goal.getFloors() - 1)
                    upButton.setVisibility(View.INVISIBLE);
                else
                    upButton.setText("Floor" + (index + 2));
                downButton.setText("Floor" + index );

            }
        });
        upButton.setVisibility(View.INVISIBLE);

        downButton = (Button) findViewById(R.id.DownButton);
        downButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                upButton.setVisibility(View.VISIBLE);
                adjustButtons(-1);
                if(index == 0)
                    downButton.setVisibility(View.INVISIBLE);
                else
                    downButton.setText("Floor" + (index));
                upButton.setText("Floor" + (index + 2));
            }
        });
        downButton.setVisibility(View.INVISIBLE);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(3 * 1000)
                .setFastestInterval(1 * 1000);

        handicapOnly = MainActivity.isHandicapEnabled();
        Bundle extrasBundle= getIntent().getExtras();
        if(extrasBundle != null)
        {
            building = extrasBundle.getString("building");
            roomNum = Integer.parseInt(extrasBundle.getString("room"));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
                mMap.setMyLocationEnabled(true);
        }
        else
            mMap.setMyLocationEnabled(true);

        mMap.setOnMarkerClickListener(this);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(campus, 0));
        mMap.setLatLngBoundsForCameraTarget(campus);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        readFile();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng latLng)
            {
                if(arrival != null)
                    if(arrival.contains(latLng))
                        showBuilding();
            }
        });

        if(building != null)
        {
            goal = buildings.get(pointers.get(building));
            setUpMap();
            new OverlayTask().execute();
        }
        else
        {
            toast = Toast.makeText(getApplicationContext(), "             Click a marker for building name  \n" +
                    " or press SELECT BUILDING below to search", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0 , 0);
            toast.show();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (mGoogleApiClient.isConnected())
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        if(line != null)
            line.remove();
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        Log.i(TAG, "Connection Failed");
    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        if(marker.getTitle().equals(last))
        {
            if(line != null)
                line.remove();
            goal = buildings.get(marker.getTitle());
            roomNum = 999;
            setUpMap();
            new OverlayTask().execute();
            return true;
        }
        last = marker.getTitle();
        marker.showInfoWindow();
        toast.setText("Click Marker again to route");
        toast.show();
        return true;
    }

    private void setUpMap()
    {
        mMap.clear();
        cleanUpOverlays();
        upButton.setVisibility(View.INVISIBLE);
        downButton.setVisibility(View.INVISIBLE);
        createPath();
    }

    private void createPath()
    {
        if (locationCheck())
            makePath();
        else
        {
            toast.setText("Unable to route.\nPlease enable location in your device settings");
            toast.show();
        }
    }

    private void makePath()
    {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if(!isContained(latLng))
            {
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setText("Routing only available on Washburn campus");
                toast.show();
                return;
            }
            following = true;

            pos = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(20)
                    .tilt(75)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(pos));

            String url = getDirections(latLng, getClosestEntrance(goal, latLng));
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);
        }
    }

    private String getDirections(LatLng origin, LatLng dest)
    {
        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&mode=walking";

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    private void readFile()
    {
        try
        {
            InputStream xmlFile = getApplicationContext().getAssets().open("CampusLocations");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(xmlFile);

            NodeList places = doc.getElementsByTagName("building");
            for(int i = 0; i < places.getLength(); i++)
            {
                Element nextBuilding = (Element)places.item(i);
                String name = nextBuilding.getAttribute("name");
                String abbr = nextBuilding.getAttribute("abbr");
                String temp1 = nextBuilding.getAttribute("location");
                String[] loc = temp1.split(",");
                Building current = new Building(name,
                                                abbr,
                                                new LatLng(Double.parseDouble(loc[0]), Double.parseDouble(loc[1])));

                buildings.put(name, current);
                pointers.put(abbr, name);

                String sw = nextBuilding.getElementsByTagName("SWBounds").item(0).getTextContent();
                String ne = nextBuilding.getElementsByTagName("NEBounds").item(0).getTextContent();
                current.setBounds(sw, ne);

                NodeList doors = nextBuilding.getElementsByTagName("entrance");
                for(int k = 0; k < doors.getLength(); k++)
                {
                    Element door = (Element)doors.item(k);
                    if(handicapOnly)
                    {
                        handicapCheck(door, current);
                        continue;
                    }
                    String[] temp2 = door.getTextContent().split(",");
                    current.addEntrance(new LatLng(Double.parseDouble(temp2[0]), Double.parseDouble(temp2[1])));
                }

                if(nextBuilding.getAttribute("default").equals("T"))
                    mMap.addMarker(new MarkerOptions()
                            .position(current.getLocation())
                            .title(name)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                NodeList floors = nextBuilding.getElementsByTagName("floor");
                buildings.get(current.getName()).setFloors(floors.getLength());
            }
        }
        catch(Exception e)
        {
            Log.i(TAG, e.toString());
        }
    }

    private boolean locationCheck()
    {
        LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean net_enabled = false;

        try
        {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch (Exception ex)
        {
            Log.e(TAG,"Exception gps_enabled");
        }

        try
        {
            net_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        catch (Exception ex)
        {
            Log.e(TAG,"Exception network_enabled");
        }

        return gps_enabled || net_enabled;
    }

    private boolean isContained(LatLng loc)
    {
        if(campus.contains(loc))
            return true;
        return false;
    }

    private LatLng getClosestEntrance(Building building, LatLng user)
    {
        ArrayList<LatLng> entrances = building.getEntrances();
        LatLng val = entrances.get(0);
        double distance = DistanceCalculator.calc(entrances.get(0), user);
        for(int i = 1; i < entrances.size(); i++)
        {
            double check = DistanceCalculator.calc(entrances.get(i), user);
            if (check < distance)
            {
                val = entrances.get(i);
                distance = check;
            }
        }
        arrival = new LatLngBounds(new LatLng(val.latitude - .0001, val.longitude - .0001),
                                   new LatLng(val.latitude + .0001, val.longitude + .0001) );
        return val;
    }

    private void handicapCheck(Element door, Building current)
    {
        if(door.getAttribute("h").equals("T"))
        {
            String[] temp2 = door.getTextContent().split(",");
            current.addEntrance(new LatLng(Double.parseDouble(temp2[0]), Double.parseDouble(temp2[1])));
        }

    }

    private String downloadUrl(String strUrl) throws IOException
    {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try
        {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null)
                sb.append(line);

            data = sb.toString();
            br.close();

        }
        catch(Exception e)
        {
            Log.d("Problem downloading url", e.toString());
        }
        finally
        {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if(following)
        {
            check++;
            if(check % 3 == 0)
            {
                if (location.hasBearing())
                {
                    pos = new CameraPosition.Builder()
                            .target(new LatLng(location.getLatitude(), location.getLongitude()))
                            .zoom(20)
                            .tilt(75)
                            .bearing(location.getBearing())
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(pos));
                }
                else
                {
                    pos = new CameraPosition.Builder()
                            .target(new LatLng(location.getLatitude(), location.getLongitude()))
                            .zoom(20)
                            .tilt(75)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(pos));
                }
                check = 0;
            }
            if(arrival != null)
                if(arrival.contains(new LatLng(location.getLatitude(), location.getLongitude())))
                {

                    showBuilding();
                }
        }
    }

    private void showBuilding()
    {
        overlayIndoorMap();
        if(dotOverlay != null)
        {
            dot = mMap.addGroundOverlay(dotOverlay);
            if((roomNum / 100) != 1)
                dot.setVisible(false);
        }
        if(goal.getBounds() != null)
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(goal.getBounds(), 0));
        upButton.setVisibility(View.VISIBLE);
        downButton.setVisibility(View.INVISIBLE);
        following = false;
    }

    private void doPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(this,v);
        int i = 0;
        for(String key : buildings.keySet())
            popupMenu.getMenu().add(Menu.NONE, i, i++, key);

        popupMenu.setOnMenuItemClickListener(
                new PopupMenu.OnMenuItemClickListener()
                {
                    @Override
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        if(item.getItemId()!=-1)
                        {
                            for (String name : buildings.keySet())
                                if ((item.getTitle()).equals(name))
                                {
                                    roomNum = 999;
                                    goal = buildings.get(name);
                                    setUpMap();
                                    new OverlayTask().execute();
                                }
                            return true;
                        }
                        else
                            return false;
                    }
                }
        );
        popupMenu.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void overlayIndoorMap()
    {
        for(int i = 0; i < overlayOptionsList.size(); i++)
        {
            buildingOverlay.add(mMap.addGroundOverlay(overlayOptionsList.get(i)));
            buildingOverlay.get(i).setVisible(false);
        }
        buildingOverlay.get(0).setVisible(true);
    }

    private void cleanUpOverlays()
    {
        buildingOverlay.clear();
        overlayOptionsList.clear();
        dotOverlay = null;
        dot = null;
    }

    private void adjustButtons(int direction)
    {
        if(dot != null)
            dot.setVisible(false);
        index += direction;
        if(index == -1 || index == goal.getFloors())
        {
            index -= direction;
            return;
        }

        buildingOverlay.get(index - direction).setVisible(false);
        buildingOverlay.get(index).setVisible(true);
        if(index == (roomNum / 100) - 1)
            if(dot != null)
                dot.setVisible(true);
    }

    private class OverlayTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            findMyClass();
            return null;
        }

        private void getRoom()
        {
            try
            {
                InputStream xmlFile = getApplicationContext().getAssets().open("CampusLocations");
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(xmlFile);
                NodeList places = doc.getElementsByTagName("building");
                for(int i = 0; i < places.getLength(); i++)
                {
                    Element next = (Element)places.item(i);
                    String name = next.getAttribute("name");
                    if(name.equals(goal.getName()))
                    {
                        NodeList rooms =  ((Element)(next.getElementsByTagName("floor")
                                                         .item((roomNum / 100) - 1)))
                                                         .getElementsByTagName("room");
                        for(int k = 0; k < rooms.getLength(); k++)
                        {
                            Element room = (Element)rooms.item(k);
                            int num = Integer.parseInt(room.getAttribute("num"));
                            if (num == roomNum)
                            {
                                String temp1 = room.getTextContent();
                                String[] loc = temp1.split(",");
                                dotOverlay = new GroundOverlayOptions()
                                        .image(BitmapDescriptorFactory.fromResource(R.drawable.roomdot))
                                        .position(new LatLng(Double.parseDouble(loc[0]), Double.parseDouble(loc[1])), 5f);
                            }
                        }
                    }
                }
            }
            catch(Exception e)
            {
                Log.i("!!ERROR", e.toString());
            }
        }

        private void findMyClass()
        {
            LatLngBounds bounds = goal.getBounds();
            if(goal.getFloors() > 1)
            {
                for (int i = 1; i < goal.getFloors() + 1; i++)
                {
                    String fileName = goal.getName() + i + ".gif";
                    overlayOptionsList.add(new GroundOverlayOptions()
                            .image(BitmapDescriptorFactory.fromAsset(fileName))
                            .positionFromBounds(bounds)
                            .transparency(0f));
                }
                if(roomNum != 999)
                    getRoom();
            }
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<double[]> >
    {

        // Parsing the data in non-ui thread
        @Override
        protected List<double[]> doInBackground(String... jsonData)
        {
            JSONObject jObject;
            List<double[]> routes = null;

            try
            {
                jObject = new JSONObject(jsonData[0]);
                DirectionsParser parser = new DirectionsParser();

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.i("!!!!", routes.toString());
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<double[]> result)
        {
            ArrayList<LatLng> points = new ArrayList<>();

            for(int j = 0; j < result.size(); j++)
            {
                LatLng position = new LatLng(result.get(j)[0], result.get(j)[1]);
                points.add(position);
            }

            line = mMap.addPolyline(new PolylineOptions()
                    .addAll(points)
                    .width(5)
                    .color(Color.RED));
        }
    }

    private class DownloadTask extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... url)
        {

            // For storing data from web service
            String data = "";

            try
            {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }
            catch(Exception e){
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    private class DirectionsParser
    {
        /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
        public List<double[]> parse(JSONObject jObject){

            List<double[]> route = new ArrayList<>() ;

            try
            {
                JSONArray legs = ((JSONObject)jObject.getJSONArray("routes").get(0)).getJSONArray("legs");

                for(int i = 0; i < legs.length(); i++)
                {
                    JSONArray steps = ( (JSONObject)legs.get(i)).getJSONArray("steps");

                    for(int j = 0; j < steps.length(); j++)
                    {
                        String polyline = (String)((JSONObject)((JSONObject)steps.get(j)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        for(int k = 0; k < list.size(); k++)
                        {
                            double[] vals = {list.get(k).latitude, list.get(k).longitude};
                            route.add(vals);
                        }
                    }
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return route;
        }

        private List<LatLng> decodePoly(String encoded)
        {

            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;
            while (index < len)
            {
                int currentChar, shift = 0, result = 0;

                do
                {
                    currentChar = encoded.charAt(index++) - 63;
                    result |= (currentChar & 0x1f) << shift;
                    shift += 5;
                    //Log.i(TAG, String.valueOf(b));
                } while (currentChar >= 0x20);
                //Log.i(TAG, "Lat done");
                lat += ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));

                shift = 0;
                result = 0;

                do
                {
                    currentChar = encoded.charAt(index++) - 63;
                    result |= (currentChar & 0x1f) << shift;
                    shift += 5;
                    //Log.i(TAG, String.valueOf(b));
                } while (currentChar >= 0x20);
                //Log.i(TAG, "Lng done");
                lng += ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                poly.add(new LatLng((((double) lat / 1E5)), (((double) lng / 1E5))));
            }
            return poly;
        }
    }
}
