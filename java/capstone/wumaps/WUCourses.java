package capstone.wumaps;

import android.content.Context;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Dan on 10/11/2016.
 */
public class WUCourses
{
    private HashMap<Department, ArrayList<WUCourse>> depMap = new HashMap<>();

    public WUCourses(Context context,String filename)
    {
        try
        {
            // File  xmlFile = new File(filename);
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

                    WUCourse newCourse = new WUCourse(cName,cNum);
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
        if(!depMap.containsKey(dep))
        {
            depMap.put(dep, new ArrayList<WUCourse>());
        }

    }

    private void addCourse(Department dep,WUCourse course)
    {
        if(depMap.containsKey(dep))
        {
            depMap.get(dep).add(course);
        }
    }

    private void addSection(Department dep,WUCourse course,Section section)
    {
        WUCourse cour = getCourseByName(dep,course.toString());
        if(cour != null)
            cour.addSection(section);
    }
    public List<Department> getDepList()
    {
        List<Department> dList = new ArrayList<Department>(depMap.keySet());

        //sort the list by name
        Collections.sort(dList, new Comparator<Department>() {
            public int compare(Department o1, Department o2) {
                return o1.abbr.compareTo(o2.abbr);
            }
        });

        return dList;
    }

    public ArrayList<WUCourse> getCourses(Department dep)
    {
        return depMap.get(dep);
    }

    public WUCourse getCourseByName(Department dep, String cName)
    {
        ArrayList<WUCourse> cours = depMap.get(dep);
        WUCourse ret = null;
        for (int i = 0;i<cours.size();i++)
        {
            if(cName.equalsIgnoreCase(cours.get(i).name) )
            {
                ret = cours.get(i);
                break;
            }
        }

        return ret;
    }

    public WUCourse getCourseByNumber(Department dep, String number)
    {
        ArrayList<WUCourse> cours = depMap.get(dep);
        WUCourse ret = null;
        for (int i = 0;i<cours.size();i++)
        {
            if(number.equalsIgnoreCase(cours.get(i).number) )
            {
                ret = cours.get(i);
                break;
            }
        }
        return ret;
    }

}
