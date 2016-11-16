package capstone.wumaps;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.Manifest;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

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
    private String last;
    private GoogleApiClient mGoogleApiClient;
    private CameraPosition pos;
    public static final String TAG = MapsActivity.class.getSimpleName();
    private HashMap<String, Building> buildings = new HashMap<>();
    private HashMap<String, String> pointers = new HashMap<>();
    private Button selectBuildingButton;
    Polyline line;
    private boolean following;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        this.selectBuildingButton = (Button) findViewById(R.id.selectBuildingButton);
        this.selectBuildingButton.setOnClickListener(new MyListener());

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5 * 1000)
                .setFastestInterval(1 * 1000);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        //Initialize Google Play Services
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

        readFile();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        LatLngBounds campus = new LatLngBounds(new LatLng(39.029671, -95.706220), new LatLng(39.036934, -95.696822));
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(campus, 0));
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        Bundle extrasBundle= getIntent().getExtras();
        if(extrasBundle != null)
        {
            String building = extrasBundle.getString("building");
            Log.i("!!!!!!!", building);
            if(building != null)
                createPath(pointers.get(building));
            //roomNum = extrasBundle.getString("roomNumber","none");
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
            createPath(marker.getTitle());
            return true;
        }
        last = marker.getTitle();
        marker.showInfoWindow();
        return true;
    }

    private void createPath (String building)
    {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            following = true;
            mMap.clear();
            Building goal = buildings.get(building);
            mMap.addMarker(new MarkerOptions()
                    .position(goal.getLocation())
                    .title(goal.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
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

                Element building = (Element)places.item(i);
                String name = building.getAttribute("name");
                String abbr = building.getAttribute("abbr");
                String temp1 = building.getAttribute("location");
                String[] loc = temp1.split(",");
                Building current = new Building(name,
                                                abbr,
                                                new LatLng(Double.parseDouble(loc[0]), Double.parseDouble(loc[1])));

                buildings.put(name, current);
                pointers.put(abbr, name);
                if(building.getAttribute("default").equals("T"))
                    mMap.addMarker(new MarkerOptions()
                            .position(current.getLocation())
                            .title(name)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                NodeList doors = building.getElementsByTagName("entrance");
                for(int j = 0; j < doors.getLength(); j++)
                {
                    Element door = (Element)doors.item(j);
                    String[] temp2 = door.getTextContent().split(",");
                    current.addEntrance(new LatLng(Double.parseDouble(temp2[0]), Double.parseDouble(temp2[1])));
                }
            }
            Log.i("!!!!!", pointers.toString());
        }
        catch(Exception e)
        {
            Log.i(TAG, e.toString());
        }
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
        return val;
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
            if(location.hasBearing())
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
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >
    {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData)
        {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try
            {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result)
        {
            ArrayList<LatLng> points = null;

            // Traversing through all the routes
            //for(int i=0;i<result.size();i++){
            points = new ArrayList<LatLng>();
            PolylineOptions lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(0);

            // Fetching all the points in i-th route
            for(int j=0;j<path.size();j++)
            {
                HashMap<String,String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

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

        // Downloading data in non-ui thread
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

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    private class DirectionsJSONParser
    {

        /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
        public List<List<HashMap<String,String>>> parse(JSONObject jObject){

            List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>() ;
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONArray jSteps = null;

            try
            {

                jRoutes = jObject.getJSONArray("routes");

                /** Traversing all routes */
                for(int i=0;i<jRoutes.length();i++)
                {
                    jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                    List path = new ArrayList<HashMap<String, String>>();

                    /** Traversing all legs */
                    for(int j=0;j<jLegs.length();j++)
                    {
                        jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");

                        /** Traversing all steps */
                        for(int k=0;k<jSteps.length();k++)
                        {
                            String polyline = "";
                            polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);

                            /** Traversing all points */
                            for(int l=0;l<list.size();l++)
                            {
                                HashMap<String, String> hm = new HashMap<String, String>();
                                hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                                hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
                                path.add(hm);
                            }
                        }
                        routes.add(path);
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
            return routes;
        }

        private List<LatLng> decodePoly(String encoded)
        {

            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len)
            {
                int b, shift = 0, result = 0;
                do
                {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do
                {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }
            return poly;
        }
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
    class MyListener implements View.OnClickListener {

        public void onClick(View v) {


            doPopup(v);
        }
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
                                if (((String) item.getTitle()).equals(name))
                                    createPath(name);
                            return true;
                        }
                        else
                            return false;
                    }
                }
        );
        popupMenu.show();
    }
}
