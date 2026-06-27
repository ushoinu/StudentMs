package com.Student;

public class Subject {
    private int    id;
    private String subjectCode;
    private String subjectName;

    public Subject() {}

    public Subject(int id, String subjectCode, String subjectName) {
        this.id          = id;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
    }

    public int    getId()                      { return id; }
    public void   setId(int id)                { this.id = id; }
    public String getSubjectCode()             { return subjectCode; }
    public void   setSubjectCode(String code)  { this.subjectCode = code; }
    public String getSubjectName()             { return subjectName; }
    public void   setSubjectName(String name)  { this.subjectName = name; }
}