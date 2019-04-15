package io.icode.concareghadmin.application.activities.models;

public class Order {

    private String username;
    private String telephone_Number;
    private String campus;
    private String location;
    private String other_location;
    private String residence;
    private String contraceptive;
    private String other_contraceptive;
    private String hostel_name;
    private String room_number;

    //default constructor
    public Order(){ }

    //constructor with one or more parameters
    public Order(String username, String telephone_Number, String campus,
                 String location, String other_location, String residence,
                 String contraceptive, String other_contraceptive, String hostel_name, String room_number)
    {
        this.username = username;
        this.telephone_Number = telephone_Number;
        this.campus = campus;
        this.location = location;
        this.other_location = other_location;
        this.residence = residence;
        this.contraceptive = contraceptive;
        this.other_contraceptive = other_contraceptive;
        this.hostel_name = hostel_name;
        this.room_number = room_number;
    }

    //Getter and Setter Method for telephone number
    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    //Getter and Setter Method for telephone number
    public void setTelephone_Number(String telephone_Number){
        this.telephone_Number = telephone_Number;
    }
    public String getTelephone_Number(){
        return telephone_Number;
    }

    //Getter and Setter Method for campus
    public void setCampus(String campus){
        this.campus = campus;
    }
    public String getCampus(){
        return campus;
    }

    //Getter and Setter Method for address
    public void setLocation(String location){
        this.location = location;
    }
    public String getLocation(){
        return location;
    }

    //Getter and Setter Method for other location
    public void setOther_location(String other_location){
        this.other_location = other_location;
    }
    public String getOther_location(){
        return other_location;
    }

    //Getter and Setter Method for residence
    public void setResidence(String residence){
        this.residence = residence;
    }

    public String getResidence(){
        return residence;
    }

    //Getter and Setter Method for gender type of contraceptive
    public void setContraceptive(String contraceptive){
        this.contraceptive = contraceptive;
    }
    public String getContraceptive(){
        return contraceptive;
    }

    //Getter and Setter Method for other contraceptive
    public void setOther_contraceptive(String other_contraceptive){
        this.other_contraceptive = other_contraceptive;
    }
    public String getOther_contraceptive(){
        return other_contraceptive;
    }

    public void setHostel_name(String hostel_name){
        this.hostel_name = hostel_name;
    }
    public String getHostel_name(){
        return hostel_name;
    }

    public void setRoom_number(String room_number){
        this.room_number = room_number;
    }
    public String getRoom_number(){
        return room_number;
    }

}
