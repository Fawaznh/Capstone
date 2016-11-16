package capstone.wumaps;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.app.ActionBar.LayoutParams;
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
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

public class MyClassMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener{

    private GoogleMap mMap;
    private Marker last;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MapsActivity.class.getSimpleName();
    private Marker mCurrLocationMarker;
    private ArrayList<Marker> buildings = new ArrayList<>();
    private HashMap<String, ArrayList<LatLng>> entrances;
    private ArrayList<LatLng> rooms;
    Polyline line;
    /*These are the building name and room # passed in the bundle from FindClassActivity*/
    private String buildName;
    private String roomNum;
    /*The GroundOverlay is the actual thing that is put on the map. In order for it to be added, you need
    * the information that goes along with it (GroundOverlayOptions). This includes the image file and the location. For some reason
    * I made an array list of GroundOverlayOptions and then add those to an array list of actual overlays, because
    * when you add them, they are put on the map. I'm sure there's a better way to do it, but it works. The reason
    * why I keep them, is because I found out that in the case of the user hitting the back button and trying to
    * select a different class, if the previous overlays aren't removed, it crashes. The dotOverlay, being used only once,
    * obviously doesn't need to be an array, but it does need to be removed prior to its use.*/
    private GroundOverlay dotOverlay;
    private ArrayList<GroundOverlay> buildingOverlay = new ArrayList<>();
    private ArrayList<GroundOverlay> startingBuildingOverlay = new ArrayList<>();
    private ArrayList<GroundOverlayOptions> overlayOptionsList = new ArrayList<>();
    private ArrayList<GroundOverlayOptions> startingOverlayOptionsList = new ArrayList<>();
    /*These are the layouts that I put the buttons in. They are defined in the XML, but are empty so I
    * can add buttons to them. I'm not sure if there needs to be two, though. Maybe we can reuse one, but
    * I think I need two because there will always be two buildings*/
    private LinearLayout startingBuildingLayout;
    private LinearLayout endingBuildingLayout;
    /*These are the rules for the buttons that I make when they are inside the layout. These buttons change the
    *transparency of the overlays to show the user the floor they want.*/
    private LinearLayout.LayoutParams params;



    /*These are the bounds of where the images are overlayed in Google maps. There is a contains() method that we
    *can use to see if the mMap.getCameraPosition().target is looking at the building to set the button layouts to
    * VISIBLE or INVISIBLE depending*/
    private static final LatLngBounds HENDERSONBOUNDS = new LatLngBounds(
            new LatLng(39.033286, -95.703490),       // South west corner
            new LatLng(39.033964, -95.702771));      // North east corner
    private static final LatLngBounds STOFFERBOUNDS = new LatLngBounds(
            new LatLng(39.035901, -95.699198),       // South west corner
            new LatLng(39.036284, -95.698082));

    /*The GroundOverlayOptions don't need to be defined here. We can put all of this in a thread to save memory.*/
    private GroundOverlayOptions henderson1stFloor;
    private GroundOverlayOptions henderson2ndFloor;
    private GroundOverlayOptions stoffer1stFloor;
    private GroundOverlayOptions stoffer2ndFloor;
    private GroundOverlayOptions stoffer3rdFloor;
    private GroundOverlayOptions roomLocationDot;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_class_maps);
        Bundle extrasBundle= getIntent().getExtras();
        if(!extrasBundle.isEmpty())
        {
            buildName=extrasBundle.getString("buildingName","none");
            roomNum=extrasBundle.getString("roomNumber","none");
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        endingBuildingLayout = (LinearLayout)findViewById(R.id.buttonLayout);

        startingBuildingLayout = (LinearLayout)findViewById(R.id.startingButtonLayout);


        params = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
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
        mMap.getUiSettings().setMapToolbarEnabled(false);
        populate();

        /*This definitely can be done in a thread.*/
        henderson1stFloor = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.henderson1stfloor))
                .positionFromBounds(HENDERSONBOUNDS);
        henderson2ndFloor = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.henderson2ndfloor))
                .positionFromBounds(HENDERSONBOUNDS);
        stoffer1stFloor = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.stoffer1stfloor))
                .positionFromBounds(STOFFERBOUNDS);
        stoffer2ndFloor = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.stoffer2ndfloor))
                .positionFromBounds(STOFFERBOUNDS);
        stoffer3rdFloor = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.stoffer3rdfloor))
                .positionFromBounds(STOFFERBOUNDS);


        Log.d("What building", buildName);
        Log.d("What Room",roomNum);


    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        LatLngBounds campus = new LatLngBounds(new LatLng(39.029671, -95.706220), new LatLng(39.036934, -95.696822));
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(campus, 0));
        if(!buildName.equals("none"))
            findMyClass();
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
            mGoogleApiClient.disconnect();
        if(line != null)
            line.remove();
        if(mCurrLocationMarker != null)
            mCurrLocationMarker.remove();
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
        if(marker.equals(last))
        {
            if(line != null)
                line.remove();
            if(mCurrLocationMarker != null)
                mCurrLocationMarker.remove();
            createPath(marker);
            return true;
        }
        last = marker;
        marker.showInfoWindow();
        return true;

    }

    private void createPath (Marker marker)
    {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            //Log.i("!!!!!!!!!", location.toString());
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            mCurrLocationMarker = mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            //String url = getDirectionsUrl(latLng, getClosestEntrance(marker, latLng));
            String url = getDirectionsUrl(latLng, getClosestEntrance(marker, latLng));
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);


        }
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest)
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

    private void populate()
    {
        //Not sure if we need to hold this data in memory.  Easier, but also takes up a lot of space.
        entrances = new HashMap<>();
        buildings = new ArrayList<>();
        rooms = new ArrayList<>();
        readFile();
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
                String abbr = building.getAttribute("abbr");
                String temp1 = building.getAttribute("location");
                String[] loc = temp1.split(",");
                buildings.add(mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(loc[0]), Double.parseDouble(loc[1])))
                        .title(building.getAttribute("name"))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));
                buildings.get(i).setTag(abbr);

                entrances.put(abbr, new ArrayList<LatLng>());

                NodeList doors = building.getElementsByTagName("entrance");
                for(int j = 0; j < doors.getLength(); j++)
                {
                    Element door = (Element)doors.item(j);
                    String[] temp2 = door.getTextContent().split(",");
                    entrances.get(abbr).add(new LatLng(Double.parseDouble(temp2[0]), Double.parseDouble(temp2[1])));
                }
            }

        }
        catch(Exception e)
        {
            Log.i(TAG, e.toString());
        }
    }

    private LatLng getClosestEntrance(Marker marker, LatLng user)
    {
        LatLng val = entrances.get((String)marker.getTag()).get(0);
        double distance = DistanceCalculator.calc(entrances.get((String)marker.getTag()).get(0), user);
        for(int i = 1; i < entrances.get((String)marker.getTag()).size(); i++)
        {
            double check = DistanceCalculator.calc(entrances.get((String) marker.getTag()).get(i), user);
            if (check < distance)
            {
                val = entrances.get((String) marker.getTag()).get(i);
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
    /*This is an idea I had for a background thread to check to see if the camera is looking at the
    *buildings to show the buttons. It's obviously not right, but if we got it into a real background
    *task, I think it would work.*/
    private class MyCameraPosition extends Thread {
        public void run() {

            try {
                while(!isInterrupted()) {
                    sleep(300);
                    if(HENDERSONBOUNDS.contains(mMap.getCameraPosition().target))
                    {
                        startingBuildingLayout.setVisibility(View.INVISIBLE);
                        endingBuildingLayout.setVisibility(View.VISIBLE);

                    }else if(STOFFERBOUNDS.contains(mMap.getCameraPosition().target))
                    {
                        startingBuildingLayout.setVisibility(View.VISIBLE);
                        endingBuildingLayout.setVisibility(View.INVISIBLE);
                    }else
                    {
                        startingBuildingLayout.setVisibility(View.INVISIBLE);
                        endingBuildingLayout.setVisibility(View.INVISIBLE);
                    }

                }

            }catch(InterruptedException ie)
            {
            }


        }
    }
    public void findMyClass()
    {
        /*I have these loops there so things don't get screwed up when trying to go from class to class. If we made
        * sure it was a new session every time, we wouldn't need these. I'm just making sure they are empty so we
        * don't get a null pointer.*/
        if(buildingOverlay.size()!=0)
        {
            for(int k=0;k<buildingOverlay.size();k++)
            buildingOverlay.get(k).remove();
        }
        if(startingBuildingOverlay.size()!=0)
        {
            for(int n=0;n<startingBuildingOverlay.size();n++)
                startingBuildingOverlay.get(n).remove();
        }

        for (int i = 0; i < buildings.size(); i++) {
            /*This finds the building in the CampusLocations and matches it with the building name sent from the
            * bundle in the FindClassActivity*/
            if (buildings.get(i).getTag().toString().equals(buildName.trim())) {
                /*We can use the createPath() method. I tried, but it crashed, so I left it like this.*/
                last = buildings.get(i);
                onMarkerClick(buildings.get(i));
            }
        }
        /*This is just to force the stoffer overlay to happen. Realistically, we would check to see if the
        * user's current location is within the STOFFERBOUNDS. We would just use if(STOFFERBOUNDS.contains(user'sLocation.getLatitude())
         * && STOFFERBOUNDS.contains(user'sLocation.getLongitude()). Unless, of course, it recognizes LatLng objects. */
        if(true)
        {
            startingOverlayOptionsList.add(stoffer1stFloor);
            startingOverlayOptionsList.add(stoffer2ndFloor);
            //startingOverlayOptionsList.add(stoffer3rdFloor);
            startingOverlayIndoorMap(startingOverlayOptionsList);
        }

        /*I think this is relatively realistic. We would have to do this for every building and we could efficiently
        * create the GroundOverlayOptions in a thread, but I think this is good enough.*/
        if(buildName.equals("HC")) {
            overlayOptionsList.add(henderson1stFloor);
            overlayOptionsList.add(henderson2ndFloor);
             /*Depending on the building name, theoretically, different GroundOverlayOptions objects would be sent.*/
            overlayIndoorMap(overlayOptionsList);
        }

    }

    private void overlayIndoorMap(ArrayList<GroundOverlayOptions> overlayOptions)
    {
        /*I have this hard coded for testing purposes, but I also started a RoomLocations XML that we could parse.
        * We would need to check that the building is right, and then get the coordinates for the correct room.*/
        LatLng ROOM104 = new LatLng(39.033681, -95.703065);

        /*This adds the actual overlays to the map. This could be done in a thread, I think. This is probably what
        * makes my stuff crash boom bang.*/
        for(int k=0;k<overlayOptions.size();k++) {
            buildingOverlay.add(mMap.addGroundOverlay(overlayOptions.get(k)));
        }

        /*This is hard coded for testing purposes. Really, all we would have to do is create the dot using the
        * coordinates of the room based on the RoomLocation XML.*/
        if(roomNum.equals("104"))
        {

            GroundOverlayOptions roomLocationDot = new GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromResource(R.drawable.roomdot))
                    .position(ROOM104, 5f);

            dotOverlay = mMap.addGroundOverlay(roomLocationDot);
            /*Once  again, I remove the thing if it already exists for safety purposes. If this was set up properly,
            * I wouldn't need it.*/
        }else if(dotOverlay!=null)
        {
           dotOverlay.remove();
        }
        /*This loop just sets the proper floor of the room. It needs improving, but I did it this way to test.*/
        for(int j=1;j<buildingOverlay.size();j++) {
            if (roomNum.startsWith("1")) {
                buildingOverlay.get(j).setTransparency(1f);
            } else {
                buildingOverlay.get(j).setTransparency(1f);
            }
        }
        /*This creates the buttons for the floors. The more floors, the more buttons. However, I think my
        * button layouts only look good up to 3 buttons. To fix this, I just need to increase the size of
        * the overlay.*/
        for(int i=1;i<buildingOverlay.size()+1;i++)
        {

            Button myButton = new Button(this);
            myButton.setId(i - 0);
            myButton.setText("Floor " + i);
            myButton.setLayoutParams(params);
            final int id_ = myButton.getId();
            myButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (int i = 0; i < buildingOverlay.size(); i++) {
                        if (i == id_ - 1) {
                            buildingOverlay.get(id_ - 1).setTransparency(0f);
                            /*Setting the dot transparency is important. If I don't, the dot appears on every
                            * floor, and that's dumb. I what I was doing doesn't quite work.*/
                            //dotOverlay.setTransparency(0f);
                        } else {
                            buildingOverlay.get(i).setTransparency(1f);
                            //dotOverlay.setTransparency(1f);
                        }
                    }
                }

            });
            /*Adding the button to the layout. I'm pretty sure this has to be done here. I mean, I think
            * it's best to set the visibility of the layouts as opposed to adding the buttons when we need them.*/
            endingBuildingLayout.addView(myButton);


        }

    }
    private void startingOverlayIndoorMap(ArrayList<GroundOverlayOptions> overlayOptions)
    {

        /*Adding the stoffer overlay*/
        for(int k=0;k<overlayOptions.size();k++) {
            startingBuildingOverlay.add(mMap.addGroundOverlay(overlayOptions.get(k)));
        }


        /*Making buttons for stoffer overlay*/
        for(int i=1;i<startingBuildingOverlay.size()+1;i++)
        {
            Button myButton = new Button(this);
            myButton.setId(i - 0);
            myButton.setText("Floor " + i);
            myButton.setLayoutParams(params);
            final int id_ = myButton.getId();
            myButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (int i = 0; i < startingBuildingOverlay.size(); i++) {
                        if (i == id_ - 1) {
                            startingBuildingOverlay.get(id_ - 1).setTransparency(0f);
                        } else {
                            startingBuildingOverlay.get(i).setTransparency(1f);
                        }
                    }
                }

            });
            startingBuildingLayout.addView(myButton);
            //startingBuildingLayout.setVisibility(View.INVISIBLE);

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
            catch (JSONException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
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

}
