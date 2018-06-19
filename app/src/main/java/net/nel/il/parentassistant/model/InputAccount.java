package net.nel.il.parentassistant.model;

import java.util.List;

public class InputAccount {

    private Integer id;

    private Integer identifier;

    private String googleIdentifier;

    private Integer type;

    private Float radius;

    private Double lat;

    private Double lng;

    private Integer status;

    private List<String> name;

    private List<String> age;

    private List<String> hobbies;

    private List<String> photos;

    private Integer companion_id;

    private List<Integer> peopleIdentifiers;

    private String message;

    private Integer messagesAmount;

    private Double pointOneLat;

    private Double pointOneLng;

    private Double pointTwoLat;

    private Double pointTwoLng;

    private String from;

    private String to;

    public InputAccount() {
    }

    public InputAccount(int id, int identifier, float radius, double lat, double lng, int companion_id) {
        this.id = id;
        this.identifier = identifier;
        this.radius = radius;
        this.lat = lat;
        this.lng = lng;
        this.companion_id = companion_id;
    }

    public InputAccount(int id, int identifier, float radius, double lat, double lng) {
        this.id = id;
        this.identifier = identifier;
        this.radius = radius;
        this.lat = lat;
        this.lng = lng;
        this.companion_id = companion_id;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Integer identifier) {
        this.identifier = identifier;
    }

    public Float getRadius() {
        return radius;
    }

    public void setRadius(Float radius) {
        this.radius = radius;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }

    public List<String> getAge() {
        return age;
    }

    public void setAge(List<String> age) {
        this.age = age;
    }

    public List<String> getHobbies() {
        return hobbies;
    }

    public void setHobbies(List<String> hobbies) {
        this.hobbies = hobbies;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }

    public List<Integer> getPeopleIdentifiers() {
        return peopleIdentifiers;
    }

    public void setPeopleIdentifiers(List<Integer> peopleIdentifiers) {
        this.peopleIdentifiers = peopleIdentifiers;
    }

    public Integer getCompanion_id() {
        return companion_id;
    }

    public void setCompanion_id(Integer companion_id) {
        this.companion_id = companion_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getMessagesAmount() {
        return messagesAmount;
    }

    public void setMessagesAmount(Integer messagesAmount) {
        this.messagesAmount = messagesAmount;
    }

    public String getGoogleIdentifier() {
        return googleIdentifier;
    }

    public void setGoogleIdentifier(String googleIdentifier) {
        this.googleIdentifier = googleIdentifier;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Double getPointOneLat() {
        return pointOneLat;
    }

    public void setPointOneLat(Double pointOneLat) {
        this.pointOneLat = pointOneLat;
    }

    public Double getPointOneLng() {
        return pointOneLng;
    }

    public void setPointOneLng(Double pointOneLng) {
        this.pointOneLng = pointOneLng;
    }

    public Double getPointTwoLat() {
        return pointTwoLat;
    }

    public void setPointTwoLat(Double pointTwoLat) {
        this.pointTwoLat = pointTwoLat;
    }

    public Double getPointTwoLng() {
        return pointTwoLng;
    }

    public void setPointTwoLng(Double pointTwoLng) {
        this.pointTwoLng = pointTwoLng;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
