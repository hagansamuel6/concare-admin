package io.icode.concareghadmin.application.activities.models;

public class Admin {

    //field in the database
    private String email;
    private String username;
    private String adminUid;
    private String password;
    private String gender;
    private String phoneNumber;
    private String imageUrl;
    private String status;


    //default constructor
    public Admin(){}

    //constructor with one or more parameters
    public Admin(String email, String username, String adminUid, String gender, String phoneNumber, String imageUrl, String status){
        this.email = email;
        this.username = username;
        this.adminUid = adminUid;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.imageUrl = imageUrl;
        this.status = status;

    }

    //Getter and Setter Method for Username
    public void setEmail(String email){
        this.email = email;
    }

    public String getEmail(){
        return email;
    }

    //Getter and Setter Method for Username
    public void setUsername(String username){
        this.username = username;
    }

    public String getUsername(){
        return username;
    }

    //Getter and Setter Method for uid
    public String getAdminUid() {
        return adminUid;
    }

    public void setAdminUid(String adminUid) {
        this.adminUid = adminUid;
    }

    //Getter and Setter Method for Gender
    public void setGender(String gender){
        this.gender = gender;
    }

    public String getGender(){
        return gender;
    }

    //Getter and Setter Method for TelephoneNumber
    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber(){
        return phoneNumber;
    }

    public void setImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }

    public String getImageUrl(){
        return imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
