package capstone.wumaps;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by FN on 11/13/2016.
 */

public class SharedPrefs extends ContextWrapper
{
    private Gson gson;
    private SharedPreferences prefs;
    private final String PREF_KEY = "MyCoursesPref";

    public SharedPrefs(Context context)
    {
        super(context);
        gson = new Gson();
        prefs = getSharedPreferences("WUMAP_PREF", MODE_PRIVATE);
    }

    public ArrayList<MyCoursesActivity.MyCourse> load()
    {
        String json = prefs.getString(PREF_KEY,"");
        Type type = new TypeToken<List<MyCoursesActivity.MyCourse>>(){}.getType();

        ArrayList<MyCoursesActivity.MyCourse> temp1 = gson.fromJson(json, type);
        if(temp1 != null)
        {
            return temp1;
        }

        return new ArrayList<>();

    }

    public void save(ArrayList<MyCoursesActivity.MyCourse> list)
    {
        SharedPreferences.Editor prefsEditor = prefs.edit();
        String json = gson.toJson(list);
        prefsEditor.putString(PREF_KEY, json);
        prefsEditor.commit();
    }

}
