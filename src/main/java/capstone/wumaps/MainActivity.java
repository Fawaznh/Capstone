package capstone.wumaps;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button addClassButton;
    private Button buildingSearchButton;
    private Button eventsButton;

    class MyListener implements OnClickListener {

        public void onClick(View v) {

            if(v.getId()==R.id.addClassButton) {
                Intent intent = new Intent(v.getContext(), FindClassActivity.class);
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
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }
}
