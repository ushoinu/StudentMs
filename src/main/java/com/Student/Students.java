package com.Student;

public class Students {
    private String name;
    private int student_id;
    private String date_of_birth;
    private String address;
    private String gender;
    private int batch;
    private String phone;
    private String password;

    /* ── New fields for Student Dashboard ─────────────────────── */
    private String email;             // optional
    private String registrationDate;  // registration_date from DB

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getStudent_id() { return student_id; }
    public void setStudent_id(int student_id) { this.student_id = student_id; }

    public String getDate_of_birth() { return date_of_birth; }
    public void setDate_of_birth(String date_of_birth) { this.date_of_birth = date_of_birth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getBatch() { return batch; }
    public void setBatch(int batch) { this.batch = batch; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
}