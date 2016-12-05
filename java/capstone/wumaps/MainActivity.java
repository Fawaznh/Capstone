package capstone.wumaps;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;


public class MainActivity extends AppCompatActivity {

    private Button addClassButton;
    private Button buildingSearchButton;
    private Button eventsButton;

    private static SharedPreferences prefs;
    private boolean handicapCheck = true;

    class MyListener implements OnClickListener {

        public void onClick(View v) {

            if(v.getId()==R.id.addClassButton) {
                Intent intent = new Intent(v.getContext(), MyCoursesActivity.class);
                //EditText editText = (EditText) findViewById(R.id.edit_message);
                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);
            }else if(v.getId()==R.id.buildingSearchButton) {
                Intent intent = new Intent(v.getContext(), MapsActivity.class);
                //EditText editText = (EditText) findViewById(R.id.edit_message);
                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);
            }else if(v.getId()==R.id.eventsButton) {
                Intent intent = new Intent(v.getContext(), EventsActivity.class);
                //EditText editText = (EditText) findViewById(R.id.edit_message);
                //String message = editText.getText().toString();
                //intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);
            }

        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_main);
        this.addClassButton = (Button) findViewById(R.id.addClassButton);
        this.addClassButton.setOnClickListener(new MyListener());
        this.buildingSearchButton = (Button) findViewById(R.id.buildingSearchButton);
        this.buildingSearchButton.setOnClickListener(new MyListener());
        this.eventsButton = (Button) findViewById(R.id.eventsButton);
        this.eventsButton.setOnClickListener(new MyListener());
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            findViewById(R.id.choicesTextView).setBackgroundResource(R.drawable.hbckgrnd);


        //init shared preferences with main key for this App
        prefs = getSharedPreferences("WUMAP_PREF", MODE_PRIVATE);


    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_settings)
        {
            settingsDialog();
            return true;
        }
        else if (item.getItemId() == R.id.action_about)
        {
            aboutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void aboutDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About");
        View view = getLayoutInflater().inflate(R.layout.about_dialog, null, false);

        builder.setView(view);

        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int buttonId) {
                dialog.dismiss();
            }
        });

        builder.show();
    }


    // to be called by MapsActivity
    public static boolean isHandicapEnabled()
    {
        // value passed inside getBoolean is the default
        return prefs.getBoolean("handicap",false);
    }

    private void settingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Settings");
        View view = getLayoutInflater().inflate(R.layout.settings_dialog, null, false);
        Switch handicapSwitch = (Switch) view.findViewById(R.id.handicapSwitch1);

        //load switch state from prefs
        handicapCheck = isHandicapEnabled();
        handicapSwitch.setChecked(handicapCheck);


        CompoundButton.OnCheckedChangeListener sListen = new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                // save prefs here too?
                handicapCheck = isChecked;
            }
        };

        // register listener
        handicapSwitch.setOnCheckedChangeListener(sListen);


        builder.setView(view);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int buttonId) {

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("handicap", handicapCheck);
                editor.commit();

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int buttonId) {
                dialog.dismiss();
            }
        });

        builder.show();
    }



}
