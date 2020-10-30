package com.jccode.mycorrespondence.models;

public class User {
    String fname;
    String lname;
    String address;
    String permanentAddress;
    String email;
    String mobile;
    String branch;
    String gender;
    String post;

    public User(String fname, String lname, String address, String permanentAddress, String email, String mobile, String branch, String gender, String post) {
        this.fname = fname;
        this.lname = lname;
        this.address = address;
        this.permanentAddress = permanentAddress;
        this.email = email;
        this.mobile = mobile;
        this.branch = branch;
        this.gender = gender;
        this.post = post;
    }

    public String getGender() {
        return gender;
    }

    public String getPost() {
        return post;
    }

    public String getFname() {
        return fname;
    }

    public String getLname() {
        return lname;
    }

    public String getAddress() {
        return address;
    }

    public String getPermanentAddress() {
        return permanentAddress;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public String getBranch() {
        return branch;
    }
}
