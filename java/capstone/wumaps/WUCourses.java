package capstone.wumaps;

import android.content.Context;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class WUCourses
{


    private HashMap<String, HashMap<Department, ArrayList<Course>>> depMap = new HashMap<>();

    public static class Section
    {
        public String sect;
        public String crn;
        public String startTime,endTime;
        public String days;
        public String bldg;
        public String room;

        public Section(String sect, String crn, String startTime, String endTime,
                       String days, String bldg, String room)
        {
            this.sect = sect;
            this.crn = crn;
            this.startTime = startTime;
            this.endTime = endTime;
            this.days = days;
            this.bldg = bldg;
            this.room = room;
        }
    }

    public static class Course
    {
        public String name;
        public String number;

        private ArrayList<Section> sections = new ArrayList<Section>();
        public Course(String name, String number)
        {
            this.name = name;
            this.number = number;
        }
        public void addSection(Section section)
        {
            sections.add(section);
        }

        public String toString()
        {
            return name;
        }

        public ArrayList<Section> getSections()
        {
            return sections;
        }
    }

    public static class Department
    {
        public String name;
        public String abbr;
        public Department(String name, String abbr)
        {
            this.name = name;
            this.abbr = abbr;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }

    public WUCourses(Context context, String filename)
    {
        depMap = new HashMap<String, HashMap<Department, ArrayList<Course>>>();

        try
        {
            InputStream xmlFile = context.getAssets().open(filename);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(xmlFile);

            NodeList nList = doc.getElementsByTagName("department");
            for (int i = 0; i < nList.getLength(); i++)
            {
                Element eDep = (Element)nList.item(i);

                // get name & abbr. of each department

                String depName = eDep.getAttribute("name");
                String depAbbr = eDep.getAttribute("abbr");

                Department dep = new Department(depName, depAbbr);
                addDepartment(dep);

                NodeList nList2 = eDep.getElementsByTagName("course");
                for (int k = 0; k < nList2.getLength(); k++)
                {
                    //get all classes for the department
                    Element eCourse = (Element)nList2.item(k);

                    String cName = eCourse.getAttribute("name");
                    String cNum = eCourse.getAttribute("number");

                    Course newCourse = new Course(cName,cNum);
                    // add course to list
                    addCourse(dep, newCourse);

                    // get sections from course
                    NodeList nList3 = eCourse.getElementsByTagName("section");
                    for (int j = 0; j < nList3.getLength(); j++)
                    {
                        String[] sect = nList3.item(j).getTextContent().split("\\|");

                        // Sect | CRN | StartTime | EndTime | Days | Bldg | Room
                        Section newSect = new Section(sect[0], sect[1], sect[2]
                                , sect[3], sect[4], sect[5], sect[6]);

                        // add section to the list
                        addSection(dep,newCourse,newSect);

                    }
                }
            }
        }
        catch (Exception ex)
        {
            Log.d("WUDepartments", filename);
        }
    }


    private void addDepartment(Department dep)
    {
        if(!depMap.containsKey(dep.name))
        {
            HashMap<Department, ArrayList<Course>> newDep = new HashMap<Department, ArrayList<Course>>();
            newDep.put(dep,new ArrayList<Course>());
            depMap.put(dep.name, newDep);
        }

    }

    private void addCourse(Department dep,Course course)
    {
        if(depMap.containsKey(dep.name))
        {
            depMap.get(dep.name).get(dep).add(course);
        }

    }

    private void addSection(Department dep, Course course, Section section)
    {
        Course cour = getCourseByName(dep,course.toString());
        if(cour != null)
            cour.addSection(section);
    }


    public List<String> getDepList()
    {

        List<String> dList = new ArrayList<String>(depMap.keySet());
        Collections.sort(dList);
        return dList;
    }


    public ArrayList<Course> getCourses(Department dep)
    {
        HashMap<Department,ArrayList<Course>> tempDep = depMap.get(dep.name);
        return tempDep.entrySet().iterator().next().getValue();
    }

    public Department getDepartment(String depName)
    {
        HashMap<Department,ArrayList<Course>> tempDep = depMap.get(depName);
        return tempDep.entrySet().iterator().next().getKey();
    }


    public Course getCourseByName(Department dep, String cName)
    {
        for(Course course: getCourses(dep))
        {
            if(cName.equalsIgnoreCase(course.name))
                return course;
        }
        return null;
    }


    public Course getCourseByNumber(Department dep, String number)
    {
        for(Course course: getCourses(dep))
        {
            if(number.equalsIgnoreCase(course.number))
                return course;
        }
        return null;
    }

}
