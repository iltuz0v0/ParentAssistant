package net.nel.il.parentassistant.model;

import java.util.Objects;

public class InfoAccount {

    private Integer identifier;

    private String name;

    private String age;

    private String hobby;

    private String photo;

    private Double latitude;

    private Double longitude;

    private Integer status;

    public InfoAccount() {

    }

    public InfoAccount(Integer identifier, Double latitude, Double longitude, Integer status) {
        this.identifier = identifier;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }


    public InfoAccount(Integer identifier, String name, String age, String hobby, String photo, Double latitude, Double longitude, Integer status) {
        this.identifier = identifier;
        this.name = name;
        this.age = age;
        this.hobby = hobby;
        this.photo = photo;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }

    public Integer getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Integer identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getHobby() {
        return hobby;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InfoAccount account = (InfoAccount) o;
        return Objects.equals(identifier, account.identifier) && Objects.equals(name, account.name) && Objects.equals(age, account.age) && Objects.equals(hobby, account.hobby) && Objects.equals(photo, account.photo) && Objects.equals(latitude, account.latitude) && Objects.equals(longitude, account.longitude) && Objects.equals(status, account.status);
    }

    @Override
    public int hashCode() {

        return Objects.hash(identifier, name, age, hobby, photo, latitude, longitude, status);
    }
}
