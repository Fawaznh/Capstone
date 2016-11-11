package capstone.wumaps;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class EventsActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        Intent intent = getIntent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_events, menu);
        return true;
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

    private class EventParser extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Document doc = Jsoup.connect("http://go.activecalendar.com/washburn").get();
                Elements wrapper = doc.select("section.list-event-preview");

                for (Element element : wrapper) {
                    Elements links = element.getElementsByTag("a");
                    Log.i("!!!!!", links.first().attr("title"));
                    Document doc2 = Jsoup.connect(links.first().attr("href")).get();
                    Elements wrapper2 = doc2.select("section.list-event-locale");
                    if (!wrapper2.isEmpty()) {
                        Elements links2 = wrapper2.first().getElementsByTag("span");
                        String[] first = links2.first().toString().split(">");
                        String[] second = first[1].split("</");
                        Log.i("!!!!!", second[0]);
                    }
                }
            } catch (Exception e) {
                Log.i("!!!!!", e.toString());
            }
            return null;
        }
    }
}
