package com.Student;

public class Students {

    private String  name;
    private Integer student_id;
    private String  date_of_birth;
    private String  address;
    private String  gender;
    private Integer batch;           // stored as Integer internally
    private String  phone;
    private String  password;

    /* ── Extra fields (Student Dashboard) ─────────────────────── */
    private String email;
    private String registrationDate;

    /* ── Getters / Setters ─────────────────────────────────────── */

    public String getName()               { return name; }
    public void   setName(String name)    { this.name = name; }

    public Integer getStudent_id()                    { return student_id; }
    public void    setStudent_id(Integer student_id)  { this.student_id = student_id; }

    public String getDate_of_birth()                        { return date_of_birth; }
    public void   setDate_of_birth(String date_of_birth)    { this.date_of_birth = date_of_birth; }

    public String getAddress()               { return address; }
    public void   setAddress(String address) { this.address = address; }

    public String getGender()              { return gender; }
    public void   setGender(String gender) { this.gender = gender; }

    /**
     * Returns batch as String so JSF EL (#{s.batch}) and
     * h:inputText bind without a converter.
     * Internally the value is still stored as Integer.
     */
    public String getBatch() {
        return batch == null ? "" : batch.toString();
    }

    /** Accepts a String from JSF and converts to Integer. */
    public void setBatch(String batch) {
        if (batch == null || batch.trim().isEmpty()) {
            this.batch = null;
        } else {
            try {
                this.batch = Integer.parseInt(batch.trim());
            } catch (NumberFormatException e) {
                this.batch = null;
            }
        }
    }

    /** Convenience setter used by DAO (reads int from ResultSet). */
    public void setBatch(Integer batch) { this.batch = batch; }

    /** Raw integer accessor for DAO INSERT / UPDATE. */
    public Integer getBatchAsInt() { return batch; }

    public String getPhone()             { return phone; }
    public void   setPhone(String phone) { this.phone = phone; }

    public String getPassword()                { return password; }
    public void   setPassword(String password) { this.password = password; }

    public String getEmail()             { return email; }
    public void   setEmail(String email) { this.email = email; }

    public String getRegistrationDate()                        { return registrationDate; }
    public void   setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
}