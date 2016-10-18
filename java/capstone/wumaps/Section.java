package capstone.wumaps;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;


class Section
{
    public String sect;
    public String CRN;
    public String startTime,endTime;
    public String days;
    public String bldg;
    public String room;

    public Section(String sect,String CRN,String startTime,String endTime,
                   String days,String bldg, String room)
    {
        this.sect = sect;
        this.CRN = CRN;
        this.startTime = startTime;
        this.endTime = endTime;
        this.days = days;
        this.bldg = bldg;
        this.room = room;
    }
}
class Department
{
    public String name;
    public String abbr;
    public Department(String name,String abbr)
    {
        this.name = name;
        this.abbr = abbr;
    }
}

class WUCourse
{
    public String name;
    public String number;

    private ArrayList<Section> sections = new ArrayList<Section>();
    public WUCourse(String name, String number)
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

    public void printSections()
    {
        for (int i = 0; i< sections.size();i++)
        {
            Section sect = sections.get(i);
            Log.d("Babies","CRN:" + sect.CRN + " start Time:" + sect.startTime + " days:" + sect.days +
                    " bldg:" + sect.bldg + " room:" + sect.room);
        }
    }

}
