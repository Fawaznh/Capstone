package capstone.wumaps;

public class Course {
    private String degreeProgram;
    private String courseNumber;
    private String days;
    private String startTime;
    private String endTime;
    private String building;
    private String roomNumber;
    private float latitude;
    private float longitude;
    private int idNumber;
    public Course()
    {

    }
    public void setIdNumber(int idNumber) {
        this.idNumber=idNumber;
    }
    public void setDegreeProgram(String degreeProgram){
        this.degreeProgram=degreeProgram;
    }
    public void setCourseNumber(String courseNumber){
        this.courseNumber=courseNumber;
    }

    public void setLongitude(float longitude){
        this.longitude=longitude;
    }
    public void setLatitude(float latitude){
        this.latitude=latitude;
    }
    public void setDays(String days) {
        this.days=days;
    }
    public void setStartTime(String startTime){
        this.startTime=startTime;
    }
    public void setEndTime(String endTime){
        this.endTime=endTime;
    }

    public void setBuilding(String building){
        this.building=building;
    }
    public void setRoomNumber(String roomNumber){
        this.roomNumber=roomNumber;
    }

    public String getDegreeProgram(){
        return degreeProgram;
    }
    public String getCourseNumber(){
        return courseNumber;
    }
    public float getLatitude(){
        return latitude;
    }
    public float getLongitude(){
        return longitude;
    }
    public int getIdNumber(){
        return idNumber;
    }

    public String getDays(){
        return days;
    }
    public String getStartTime(){
        return startTime;
    }
    public String getEndTime(){
        return endTime;
    }
    public String getBuilding(){
        return building;
    }
    public String getRoomNumber(){
        return roomNumber;
    }

}

