package capstone.wumaps;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class FindClassActivity extends AppCompatActivity {
    private Button findDegreeProgramButton;
    private Button findCourseLevelButton;
    private Button findTimesButton;
    private Button submitButton;
    private Button editButton;
    private Button removeButton;
    private String[] courseNumberArray;
    private String[] degreeProgramArray;
    private String[] itemsSelectedArray;
    private ArrayList<Course> myCourses;
    private ArrayList<Course> userCourses;
    //private Button Button;
    private int numberOfCourses;
    private Course myCourse;
    private Course[] myCourseArray;
    private Button showMyCoursesButton;
    //private LinearLayout linear;
    private PopupMenu degreePopupMenu;
    private PopupMenu coursesPopupMenu;
    private PopupMenu myCoursesPopupMenu;
    private PopupMenu timesPopupMenu;
    private SharedPreferences prefs;
    public static final String KEY_CONNECTIONS = "KEY_CONNECTIONS";

    private WUCourses wuc;

    class MyListener implements View.OnClickListener {


        public void onClick(View v) {

            if(v.getId()==R.id.findDegreeProgramButton) {
                coursesPopupMenu.getMenu().clear();
                degreePopupMenu.show();
            }else if(v.getId()==R.id.findCourseLevelButton) {

                timesPopupMenu.getMenu().clear();
                coursesPopupMenu.show();
            }else if(v.getId()==R.id.findTimesButton) {

                timesPopupMenu.show();
            } else if(v.getId()==R.id.submitButton) {

                if(itemsSelectedArray[0]!=null&&itemsSelectedArray[1]!=null) {

                    findClass();
                    itemsSelectedArray[0]=null;
                    itemsSelectedArray[1]=null;
                    itemsSelectedArray[2]=null;
                    //saveObjectToSharedPreference(FindClassActivity.this, "userCourses", "mObjectKey", myCourses);

                }else{
                    //alertdialog
                    return;
                }

            }else if(v.getId()==R.id.editButton)
            {
                return;

            }else if(v.getId()==R.id.removeButton)
            {
                return;
            }else if(v.getId()==R.id.showMyCoursesButton){
                if(myCourses==null)
                {
                    return;
                }else
                {
                    myCoursesPopupMenu.show();
                }
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_class);
        MyListener ml=new MyListener();
        //linear=(LinearLayout)findViewById(R.id.layout);
        this.findDegreeProgramButton = (Button) findViewById(R.id.findDegreeProgramButton);
        this.findDegreeProgramButton.setOnClickListener(ml);
        this.findTimesButton = (Button) findViewById(R.id.findTimesButton);
        this.findTimesButton.setOnClickListener(ml);
        this.findCourseLevelButton = (Button) findViewById(R.id.findCourseLevelButton);
        this.findCourseLevelButton.setOnClickListener(ml);
        this.submitButton=(Button)findViewById(R.id.submitButton);
        this.submitButton.setOnClickListener(ml);
        this.showMyCoursesButton=(Button)findViewById(R.id.showMyCoursesButton);
        this.showMyCoursesButton.setOnClickListener(ml);
        this.editButton=(Button)findViewById(R.id.editButton);
        this.editButton.setOnClickListener(ml);
        this.removeButton=(Button)findViewById(R.id.removeButton);
        this.removeButton.setOnClickListener(ml);
        degreePopupMenu=new PopupMenu(this,findDegreeProgramButton);
        myCoursesPopupMenu=new PopupMenu(this, showMyCoursesButton);
        coursesPopupMenu=new PopupMenu(this,findCourseLevelButton);
        timesPopupMenu=new PopupMenu(this,findTimesButton);


        //SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        //List<Connection> connections = myCourse.getConnections(


        courseNumberArray=new String[2];
        degreeProgramArray=new String[2];
        itemsSelectedArray=new String[3];
        myCourses=new ArrayList<>();
        courseNumberArray[0]="101";
        courseNumberArray[1]="201";
        degreeProgramArray[0]="CIS";
        degreeProgramArray[1]="MA";

        // Load the xml file into the class
        Log.d("WUCourses", "Loading xml");
        wuc = new WUCourses(this, "Fall16courses");


        numberOfCourses=0;

        //myCourses = getSavedObjectFromPreference(FindClassActivity.this, "userCourses", "mObjectKey", ArrayList.class);

        degreeProgramSelectionPopup();


        Intent intent = getIntent();
        //String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        //extView textView = new TextView(this);
        //textView.setTextSize(40);
        //textView.setText(message);

        //ViewGroup layout = (ViewGroup) findViewById(R.id.activity_find_class);
        //layout.addView(textView);
    }
    public static void saveObjectToSharedPreference(Context context, String preferenceFileName, String serializedObjectKey, Object object) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFileName, 0);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        final Gson gson = new Gson();
        String serializedObject = gson.toJson(object);
        sharedPreferencesEditor.putString(serializedObjectKey, serializedObject);
        sharedPreferencesEditor.apply();
    }
    public static <GenericClass> GenericClass getSavedObjectFromPreference(Context context, String preferenceFileName, String preferenceKey, Class<GenericClass> classType) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFileName, 0);
        if (sharedPreferences.contains(preferenceKey)) {
            final Gson gson = new Gson();
            return gson.fromJson(sharedPreferences.getString(preferenceKey, ""), classType);
        }
        return null;
    }
    private void degreeProgramSelectionPopup() {

        int id = 1;

        for(Department dep : wuc.getDepList())
        {

            degreePopupMenu.getMenu().add(Menu.NONE, id, id, dep.name);
            id++;
        }

        degreePopupMenu.setOnMenuItemClickListener(
                new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId()!=-1){
                            itemsSelectedArray[0] = (String)item.getTitle();
                            courseSelectionPopUp();
                            return true;
                        } else {
                            return false;
                        }
                    }

                }
        );
        //MenuInflater inflater = popupMenu.getMenuInflater();
        //inflater.inflate(R.menu.my_popup_menu, popupMenu.getMenu());

        //for(int i=0;i<buildings.length;i++)
        //this.displayBuildingsTextView.append(this.buildings[i] + "\n");

    }
    private void showClasses(View v) {


        //MenuInflater inflater = popupMenu.getMenuInflater();
        //inflater.inflate(R.menu.my_popup_menu, popupMenu.getMenu());
        //myCoursesPopupMenu.show();
        //for(int i=0;i<buildings.length;i++)
        //this.displayBuildingsTextView.append(this.buildings[i] + "\n");

    }
    private void courseSelectionPopUp() {

        int id = 1;

        for(Department dep : wuc.getDepList())
        {
            if(dep.name.equals(itemsSelectedArray[0])) {
                for(WUCourse course : wuc.getCourses(dep))
                {
                    coursesPopupMenu.getMenu().add(Menu.NONE, id, id, course.name);
                    id++;

                }
            }

        }

        coursesPopupMenu.setOnMenuItemClickListener(
                new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() != -1) {
                            itemsSelectedArray[1] = (String) item.getTitle();
                            timeSelectionPopUp();
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
        );
        //MenuInflater inflater = popupMenu.getMenuInflater();
        //inflater.inflate(R.menu.my_popup_menu, popupMenu.getMenu());

        //for(int i=0;i<buildings.length;i++)
        //this.displayBuildingsTextView.append(this.buildings[i] + "\n");

    }
    private void timeSelectionPopUp() {

        int id = 1;

        for(Department dep : wuc.getDepList())
        {
            if(dep.name.equals(itemsSelectedArray[0])) {
                for(WUCourse course : wuc.getCourses(dep)) {
                    if(course.name.equals(itemsSelectedArray[1])) {
                        for (int i = 0; i < course.getSections().size(); i++) {
                            timesPopupMenu.getMenu().add(Menu.NONE, id, id, course.getSections().get(i).days + " " + course.getSections().get(i).startTime);
                            id++;
                        }
                    }
                }

            }

        }

        timesPopupMenu.setOnMenuItemClickListener(
                new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId()!=-1) {
                            itemsSelectedArray[2] = (String) item.getTitle();
                            Log.d("the thing",itemsSelectedArray[2]);
                            return true;
                        }else
                        {
                            return false;
                        }
                    }
                }
        );
        //MenuInflater inflater = popupMenu.getMenuInflater();
        //inflater.inflate(R.menu.my_popup_menu, popupMenu.getMenu());

        //for(int i=0;i<buildings.length;i++)
        //this.displayBuildingsTextView.append(this.buildings[i] + "\n");

    }
    private void findClass()
    {

        LinearLayout.LayoutParams params;
        //String delims = "[ ]";
        //String[] tokens = itemsSelectedArray[3].split(delims);
        myCourse = new Course();
        numberOfCourses=numberOfCourses+1;
        myCourse.setIdNumber(numberOfCourses);
        myCourse.setCourseNumber(itemsSelectedArray[1]);
        myCourse.setDegreeProgram(itemsSelectedArray[0]);
        myCourse.setDays(itemsSelectedArray[2]);
        //myCourse.setStartTime(tokens[1]);
        myCourses.add(myCourse);
        for(Department dep : wuc.getDepList())
        {
            if(dep.name.equals(myCourse.getDegreeProgram())) {
                for(WUCourse course : wuc.getCourses(dep))
                {
                    if(course.name.equals(myCourse.getCourseNumber())) {
                        for(int i=0;i<course.getSections().size();i++)
                        {
                            if(course.getSections().get(i).days.equals(myCourse.getDays())&&course.getSections().get(i).startTime.equals(myCourse.getStartTime()))
                            {
                                myCourse.setBuilding(course.getSections().get(i).bldg);
                                myCourse.setRoomNumber(course.getSections().get(i).room);
                            }
                        }
                    }
                }
            }

        }

        myCoursesPopupMenu.getMenu().add(Menu.NONE, numberOfCourses, numberOfCourses, myCourse.getDegreeProgram() + " " + myCourse.getCourseNumber());
        myCoursesPopupMenu.setOnMenuItemClickListener(
                new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() != -1) {
                            for (int i=0;i<myCourses.size();i++) {


                            }
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
        );
            /*
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            Button btn = new Button(this);
            btn.setId(numberOfCourses + 1);
            final int id_ = btn.getId();
            btn.setText("" + myCourse.getDegreeProgram() + " " + myCourse.getCourseNumber());
            //btn.setBackgroundColor(Color.rgb(70, 80, 90));
            linear.addView(btn, params);
            courseButton = ((Button) findViewById(id_));
            courseButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    showClasses(view,id_-1);
                }
            });*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_find_class, menu);
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
}
