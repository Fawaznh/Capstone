package capstone.wumaps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;



import java.util.ArrayList;
import java.util.StringTokenizer;

public class FindClassActivity extends AppCompatActivity {
    private Button findDegreeProgramButton;
    private Button findCourseLevelButton;
    private Button findTimesButton;
    private Button editButton;
    private Button removeButton;
    private String[] courseNumberArray;
    private String[] degreeProgramArray;
    private String[] itemsSelectedArray;
    private ArrayList<Course> myCourses;
    private ArrayList<String> userCourses;
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
    private PopupMenu removePopupMenu;
    private boolean successfullySaved;
    private SharedPreferences prefs;


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
            }else if(v.getId()==R.id.editButton)
            {
                return;

            }else if(v.getId()==R.id.removeButton)
            {

                myCoursesPopupMenu.show();
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
        removePopupMenu=new PopupMenu(this,removeButton);
        //userCourses=new ArrayList<>();
        /*
        prefs=this.getSharedPreferences("buildings", MODE_PRIVATE);
        for(int i=1;i<10;i++) {
            userCourses.add(this.prefs.getString("degreeProgram" + i, "none"));
            userCourses.add(this.prefs.getString("courseSelection" + i, "none"));
            userCourses.add(this.prefs.getString("timeSelection" + i, "none"));
            userCourses.add(this.prefs.getString("building" + i, "none"));
            userCourses.add(this.prefs.getString("roomNumber" + i, "none"));
        }
        //SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        //List<Connection> connections = myCourse.getConnections(



        if (userCourses.get(0).equals("none")) {
            return;
        } else {
            setMyUserCoursesPopupMenu();
        }

        Log.d("hey", userCourses.get(0));
        // Load the xml file into the class


        if(userCourses.get(0).equals("none")) {
            numberOfCourses = 0;
        }else
        {
            numberOfCourses=prefs.getInt("numberOfSavedCourses",-1);

        }*/

        //myCourses = getSavedObjectFromPreference(FindClassActivity.this, "userCourses", "mObjectKey", ArrayList.class);
        itemsSelectedArray=new String[3];
        myCourses=new ArrayList<>();
        Log.d("WUCourses", "Loading xml");
        wuc = new WUCourses(this, "Fall16courses");
        updateUI();
        degreeProgramSelectionPopup();


        Intent intent = getIntent();
        //String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        //extView textView = new TextView(this);
        //textView.setTextSize(40);
        //textView.setText(message);

        //ViewGroup layout = (ViewGroup) findViewById(R.id.activity_find_class);
        //layout.addView(textView);
    }
    private void updateUI(){
        if(itemsSelectedArray[0]==null)
        {
            findDegreeProgramButton.setEnabled(true);
            findTimesButton.setEnabled(false);
            findCourseLevelButton.setEnabled(false);
        }else if(itemsSelectedArray[0]!=null&&itemsSelectedArray[1]==null)
        {
            findCourseLevelButton.setEnabled(true);
            findDegreeProgramButton.setEnabled(false);
            findTimesButton.setEnabled(false);
        }else if (itemsSelectedArray[0]!=null&&itemsSelectedArray[1]!=null)
        {
            findCourseLevelButton.setEnabled(false);
            findDegreeProgramButton.setEnabled(false);
            findTimesButton.setEnabled(true);
        }

        if(numberOfCourses==0)
        {
            showMyCoursesButton.setEnabled(false);
            removeButton.setEnabled(false);
            editButton.setEnabled(false);
        }else{
            showMyCoursesButton.setEnabled(true);
            removeButton.setEnabled(true);
            editButton.setEnabled(true);
        }
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
                            updateUI();
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
                            updateUI();
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
                            if(itemsSelectedArray[0]!=null&&itemsSelectedArray[1]!=null) {

                                findClass();
                                itemsSelectedArray[0]=null;
                                itemsSelectedArray[1]=null;
                                itemsSelectedArray[2]=null;
                                updateUI();
                                //saveObjectToSharedPreference(FindClassActivity.this, "userCourses", "mObjectKey", myCourses);

                            }
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
    private void findClass() {

        LinearLayout.LayoutParams params;
        //String delims = "[ ]";
        //String[] tokens = itemsSelectedArray[3].split(delims);
        myCourse = new Course();
        numberOfCourses = numberOfCourses + 1;
        myCourse.setIdNumber(numberOfCourses);
        myCourse.setCourseNumber(itemsSelectedArray[1]);
        myCourse.setDegreeProgram(itemsSelectedArray[0]);
        myCourse.setDays(itemsSelectedArray[2]);
        //myCourse.setStartTime(tokens[1]);
        myCourses.add(myCourse);
        for (Department dep : wuc.getDepList()) {
            if (dep.name.equals(myCourse.getDegreeProgram())) {
                for (WUCourse course : wuc.getCourses(dep)) {
                    if (course.name.equals(myCourse.getCourseNumber())) {
                        for (int i = 0; i < course.getSections().size(); i++) {
                            if (course.getSections().get(i).days.equals(myCourse.getDays()) && course.getSections().get(i).startTime.equals(myCourse.getStartTime())) {
                                myCourse.setBuilding(course.getSections().get(i).bldg);
                                myCourse.setRoomNumber(course.getSections().get(i).room);
                                myCourse.setStartTime(itemsSelectedArray[2]);
                            }
                        }
                    }
                }
            }

        }
        /*SharedPreferences.Editor prefsEditor = prefs.edit();

        prefsEditor.putString("degreeProgram" + numberOfCourses, itemsSelectedArray[0]);
        prefsEditor.putString("courseSelection" + numberOfCourses, itemsSelectedArray[1]);
        prefsEditor.putString("timeSelection" + numberOfCourses, itemsSelectedArray[2]);
        prefsEditor.putString("building" + numberOfCourses, myCourse.getBuilding());
        prefsEditor.putString("roomNumber" + numberOfCourses, myCourse.getRoomNumber());
        prefsEditor.putInt("numberOfSavedCourses",numberOfCourses);
        prefsEditor.commit();*/
        setMyCoursesPopupMenu();
    }
    private void setMyUserCoursesPopupMenu() {

        for (int i = 0; i < numberOfCourses; i++) {
            if (i == 0) {
                myCoursesPopupMenu.getMenu().add(Menu.NONE, numberOfCourses, numberOfCourses, userCourses.get(i) + " " + userCourses.get(i + 1));

            } else {
                if(userCourses.get(i).equals("none"))
                {
                    return;
                }else {
                    while (i % 6 == 0) {
                        myCoursesPopupMenu.getMenu().add(Menu.NONE, numberOfCourses, numberOfCourses, userCourses.get(i) + " " + userCourses.get(i + 1));
                    }
                }
            }
        }

    }

    private void setMyCoursesPopupMenu() {
        myCoursesPopupMenu.getMenu().add(Menu.NONE, numberOfCourses, numberOfCourses, myCourses.get(numberOfCourses - 1).getDegreeProgram() + " , " + myCourses.get(numberOfCourses - 1).getCourseNumber()+" , "+myCourses.get(numberOfCourses-1).getDays());
        myCoursesPopupMenu.setOnMenuItemClickListener(
                new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        String buildNameToSend="none";
                        String roomNumberToSend="none";
                        if (item.getItemId() != -1) {
                            for (int i = 0; i < myCourses.size(); i++) {
                                StringTokenizer tokens = new StringTokenizer((String)item.getTitle(), " , ");

                                String first = tokens.nextToken();
                                String second = tokens.nextToken();
                                String third = tokens.nextToken();



                                for(Department dep : wuc.getDepList())
                                {
                                    if(dep.name.equals(first)) {
                                        for(WUCourse course : wuc.getCourses(dep)) {
                                            if(course.name.equals(second)) {
                                                for (int j = 0; i < course.getSections().size(); i++) {
                                                    if ((course.getSections().get(j).days+" "+ course.getSections().get(j).startTime).equals(third)) {
                                                        buildNameToSend=course.getSections().get(i).bldg;
                                                        roomNumberToSend=course.getSections().get(i).room;
                                                    }
                                                }
                                            }
                                        }

                                    }

                                }


                                Intent intent = new Intent(FindClassActivity.this, MyClassMapsActivity.class);
                                Bundle bundle=new Bundle();
                                bundle.putString("buildingName",buildNameToSend);
                                bundle.putString("roomNumber",roomNumberToSend);
                                intent.putExtras(bundle);
                                //EditText editText = (EditText) findViewById(R.id.edit_message);
                                //String message = editText.getText().toString();
                                //intent.putExtra(EXTRA_MESSAGE, message);
                                startActivity(intent);

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

