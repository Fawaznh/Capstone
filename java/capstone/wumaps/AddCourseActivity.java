package capstone.wumaps;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class AddCourseActivity extends AppCompatActivity
{

    private ArrayList<String> programsList,courseList,sectionList;
    private SpinnerAdapter customAdapter1,customAdapter2,customAdapter3;
    private Spinner progSpinner,courseSpinner,secSpinner;
    private Button addBtn, cancelBtn;

    private WUCourses.Department selectedProgram;
    private WUCourses.Course selectedCourse;
    private ArrayList<WUCourses.Section> cSections;

    private MyCoursesActivity.MyCourse newCourse;
    private ArrayList<MyCoursesActivity.MyCourse> myCourses;

    private SharedPrefs sharedPrefs;


    private WUCourses wuc;

    public class SpinnerAdapter extends ArrayAdapter<String>
    {
        public SpinnerAdapter(Activity context, ArrayList<String> list)
        {
            super(context, android.R.layout.simple_spinner_dropdown_item, list);

        }
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent)
        {
            View view = super.getView(position, convertView, parent);
            view.setBackgroundColor(Color.parseColor("#DEDEDE"));
            return view;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addcourse);

        myCourses = new ArrayList<>();
        sharedPrefs = new SharedPrefs(this.getApplicationContext());
        myCourses= sharedPrefs.load();



        addBtn = (Button)findViewById(R.id.addBtn);
        cancelBtn = (Button)findViewById(R.id.cancelBtn2);


         progSpinner = (Spinner) findViewById(R.id.programSpinner);
         courseSpinner = (Spinner) findViewById(R.id.courseSpinner);
         secSpinner = (Spinner) findViewById(R.id.sectionSpinner);

        wuc = new WUCourses(this, "Fall16courses");
        programsList = new ArrayList<>();

        courseList = new ArrayList<>();
        sectionList = new ArrayList<>();
        programsList.addAll(wuc.getDepList());



        customAdapter1 = new SpinnerAdapter(this, programsList);
        customAdapter2 = new SpinnerAdapter(this, courseList);
        customAdapter3 = new SpinnerAdapter(this, sectionList);


        View.OnClickListener btnListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (view.getId() == addBtn.getId())
                {
                    myCourses = sharedPrefs.load();
                    if(!hasCourse(newCourse.crn))
                    {
                        myCourses.add(newCourse);
                        sharedPrefs.save(myCourses);
                        finish();
                    }
                    else
                    {
                        Toast.makeText(AddCourseActivity.this,"This course already exists!", Toast.LENGTH_SHORT).show();
                    }
                }
                else if(view.getId() == cancelBtn.getId())
                {
                    finish();
                }
            }
        };

        addBtn.setOnClickListener(btnListener);
        cancelBtn.setOnClickListener(btnListener);



        AdapterView.OnItemSelectedListener itemListener = new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {

                if(parent.getId() == R.id.programSpinner)
                {

                    String selectedProg = programsList.get(position);
                    selectedProgram = wuc.getDepartment(selectedProg);

                    ArrayList<WUCourses.Course> courses = wuc.getCourses(selectedProgram);

                    courseList.clear();

                    for(WUCourses.Course c: courses)
                    {
                        courseList.add(c.name);
                    }

                    // set first item by default
                    updateSectionsSpinner(0);
                    saveSelected(0);

                }
                else if(parent.getId() == R.id.courseSpinner)
                {
                    updateSectionsSpinner(position);
                    saveSelected(0);

                }
                else if(parent.getId() == R.id.sectionSpinner)
                {
                    saveSelected(position);
                    Log.d("onItemSelected", "new section selected");

                }
                customAdapter1.notifyDataSetChanged();
                customAdapter2.notifyDataSetChanged();
                customAdapter3.notifyDataSetChanged();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        };

        // only works on emulator??
        setSpinnerHeight(progSpinner,1000);
        setSpinnerHeight(courseSpinner,800);
        setSpinnerHeight(secSpinner,500);

        progSpinner.setAdapter(customAdapter1);
        courseSpinner.setAdapter(customAdapter2);
        secSpinner.setAdapter(customAdapter3);

        progSpinner.setOnItemSelectedListener(itemListener);
        courseSpinner.setOnItemSelectedListener(itemListener);
        secSpinner.setOnItemSelectedListener(itemListener);

    }

    private void updateSectionsSpinner(int position)
    {
        sectionList.clear();
        selectedCourse = wuc.getCourseByName(selectedProgram,courseList.get(position));

        cSections = selectedCourse.getSections();
        for(WUCourses.Section sect: cSections)
        {
            String sf = sect.sect + "    "+ sect.startTime + " - "+sect.endTime
                    + "     "+sect.bldg+ " "+ sect.room;

            sectionList.add(sf);
        }

        if(cSections.size() ==1)
        {
            secSpinner.setEnabled(false);
        }
        else
        {
            secSpinner.setEnabled(true);
        }

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        myCourses = sharedPrefs.load();
    }

    private void saveSelected(int position)
    {
        WUCourses.Section sect = cSections.get(position);

        newCourse = new MyCoursesActivity.MyCourse(sect.crn,selectedProgram.abbr+" "+selectedCourse.number
                ,sect.sect+" - "+selectedCourse.name, sect.bldg,sect.room, sect.startTime + " - "+sect.endTime,false);
    }


    private void setSpinnerHeight(Spinner spn, int height)
    {
        try
        {
            Field mPopup = Spinner.class.getDeclaredField("mPopup");
            mPopup.setAccessible(true);
            ((android.widget.ListPopupWindow)mPopup.get(spn)).setHeight(height);
        } catch (Exception e) {}
    }


    private boolean hasCourse(String crn)
    {
        for(MyCoursesActivity.MyCourse cour: myCourses)
            if(cour.crn.equals(crn)) return true;

        return false;
    }

}
