package ru.dyatel.tsuschedule.parsing;

public class Teacher {

    private String name;
    private String education;
    private String department;

    public Teacher(String name, String education, String department) {
        this.name = name;
        this.education = education;
        this.department = department;
    }

    public String getName() {
        return name;
    }

    public String getEducation() {
        return education;
    }

    public String getDepartment() {
        return department;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public boolean equals(String name, String education, String department) {
        return this.name.equals(name) &&
                this.education.equals(education) &&
                this.department.equals(department);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Teacher) && ((Teacher) o).equals(name, education, department);
    }

}
