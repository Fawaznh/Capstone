package capstone.wumaps;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class MyCoursesActivity extends AppCompatActivity
{
    private Button removeBtn,doneBtn,cancelBtn,addBtn;
    private ListView listView;
    private CourseViewAdapter courseViewAdapter;
    private ArrayList<MyCourse> values;
    private ArrayList<MyCourse> myCourses;
    private SharedPrefs sharedPrefs;


    public static class MyCourse
    {
        public String crn;
        public String number;
        public String name;
        public String bldg,room;
        public String time;
        public boolean selected;

        public MyCourse(String crn, String number, String name, String bldg, String room,
                        String time, boolean selected)
        {
            this.crn= crn;
            this.number = number;
            this.name = name;
            this.bldg = bldg;
            this.room = room;
            this.time = time;
            this.selected = selected;
        }
    }

    public static class ViewHolder
    {
        public TextView courseNumber;
        public TextView courseName;
        public TextView courseLocation;
        public TextView courseTime;
        public CheckBox checkbox1;
        public TextView locIcon;
    }

    public class CourseViewAdapter extends ArrayAdapter<MyCourse>
    {
        private boolean inEditMode = false;

        public CourseViewAdapter(Activity context, int res, ArrayList<MyCourse> list)
        {
            super(context,res,list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            MyCourse mycourse = getItem(position);
            ViewHolder viewHolder;

            if(convertView == null)
            {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_course,parent,false);


                viewHolder.courseNumber =  (TextView)convertView.findViewById(R.id.courseNumber);
                viewHolder.courseName =  (TextView)convertView.findViewById(R.id.courseName);
                viewHolder.courseLocation =  (TextView)convertView.findViewById(R.id.courseLocation);
                viewHolder.courseTime =  (TextView)convertView.findViewById(R.id.courseTime);
                viewHolder.checkbox1 =  (CheckBox)convertView.findViewById(R.id.checkBox1);
                //viewHolder.locIcon = (TextView)convertView.findViewById(R.id.locIcon);

                convertView.setTag(viewHolder);

                viewHolder.checkbox1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CheckBox cb = (CheckBox)view;
                        MyCourse courseChecked = (MyCourse)cb.getTag();
                        courseChecked.selected = cb.isChecked();
                    }
                });

                if (position % 2 == 0) {
                    convertView.setBackgroundColor(Color.parseColor("#f2f2f2"));
                }
            }
            else
            {
                viewHolder = (ViewHolder)convertView.getTag();
            }

            viewHolder.courseNumber.setText(mycourse.number);
            viewHolder.courseName.setText(mycourse.name);
            viewHolder.courseLocation.setText(mycourse.bldg + " " + mycourse.room);
            viewHolder.courseTime.setText(mycourse.time);
            viewHolder.checkbox1.setChecked(mycourse.selected);
            viewHolder.checkbox1.setTag(mycourse);

            if(inEditMode)
            {
                viewHolder.checkbox1.setVisibility(View.VISIBLE);
            }
            else
            {
                viewHolder.checkbox1.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }

        @Override
        public int getCount()
        {
            return super.getCount();
        }

        @Override
        public int getPosition(MyCourse item)
        {
            return super.getPosition(item);
        }

        public void setEditMode(boolean flag)
        {
            inEditMode = flag;
            notifyDataSetChanged();
        }

        public boolean getEditMode()
        {
            return inEditMode;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_addcourse, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.action_add)
        {
            Intent intent = new Intent(getApplicationContext(), AddCourseActivity.class);
            startActivity(intent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mycourses);

        myCourses = new ArrayList<>();
        sharedPrefs = new SharedPrefs(this.getApplicationContext());
        myCourses = sharedPrefs.load();


        listView = (ListView)findViewById(R.id.courselist);
        listView.setEmptyView(findViewById(android.R.id.empty));

        removeBtn = (Button)findViewById(R.id.removeBtn);

        doneBtn = (Button)findViewById(R.id.doneBtn);
        cancelBtn = (Button)findViewById(R.id.cancelBtn);


        courseViewAdapter = new CourseViewAdapter(this,android.R.layout.activity_list_item,myCourses);
        courseViewAdapter.notifyDataSetChanged();


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(doneBtn.getVisibility() == View.VISIBLE) return;

                onCourseSelected(position, view);

            }
        });


        View.OnClickListener remBtnListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(view.getId() == R.id.removeBtn) {
                    boolean isEditable = courseViewAdapter.getEditMode();

                    if (!isEditable) {
                        courseViewAdapter.setEditMode(true);

                        removeBtn.setVisibility(View.GONE);
                        doneBtn.setVisibility(View.VISIBLE);
                        cancelBtn.setVisibility(View.VISIBLE);
                    }
                }
            }
        };

        View.OnClickListener editListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                removeBtn.setVisibility(View.VISIBLE);

                doneBtn.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
                courseViewAdapter.setEditMode(false);

                if(view.getId() == doneBtn.getId())
                {
                    // add all to check list
                    ArrayList<MyCourse> checked = new ArrayList<>();
                    for(int i = 0; i < myCourses.size(); i++)
                    {
                        if(myCourses.get(i).selected)
                        {
                            checked.add(myCourses.get(i));
                        }
                    }

                    // remove all at once
                    for(int i = 0; i < checked.size(); i++)
                    {
                        MyCourse item = checked.get(i);
                        //Log.d("MyLog", "label: "+item.number+" selected: "+item.selected);

                        if(checked.get(i).selected == true)
                        {
                            courseViewAdapter.remove(item);
                        }
                    }

                    courseViewAdapter.notifyDataSetChanged();
                    sharedPrefs.save(myCourses);
                }
                else if(view.getId() == cancelBtn.getId())
                {
                    //Log.d("MyLog", "cancel button click");

                    for(int i = 0; i < myCourses.size(); i++)
                    {
                        myCourses.get(i).selected = false;
                    }
                    courseViewAdapter.notifyDataSetChanged();
                }
            }
        };

        removeBtn.setOnClickListener(remBtnListener);
        doneBtn.setOnClickListener(editListener);
        cancelBtn.setOnClickListener(editListener);

        listView.setAdapter(courseViewAdapter);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(listView != null)
        {
            updateListView();
        }
    }

    private void updateListView()
    {
        myCourses = sharedPrefs.load();
        courseViewAdapter = new CourseViewAdapter(this,android.R.layout.activity_list_item,myCourses);
        listView.setAdapter(courseViewAdapter);
    }

    private void onCourseSelected(int position, View view)
    {
        MyCourse myc = myCourses.get(position);
        Intent intent = new Intent(view.getContext(), MapsActivity.class);
        intent.putExtra("building", myc.bldg);
        intent.putExtra("room", myc.room);
        Log.d("ItemClicked", "pos: "+position + " bldg: "+myc.bldg+ " room: "+myc.room);
        startActivity(intent);

    }

}
