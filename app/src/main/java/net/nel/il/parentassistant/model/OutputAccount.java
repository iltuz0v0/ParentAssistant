package net.nel.il.parentassistant.model;

import java.util.List;

public class OutputAccount {

    private Integer id;

    private Integer identifier;

    private Integer companionId;

    private Integer type;

    private List<String> names;

    private List<String> ages;

    private List<String> hobbies;

    private List<String> photos;

    private List<Integer> peopleStatuses;

    private List<Integer> peopleIdentifiers;

    private List<Double> peopleLatitudes;

    private List<Double> peopleLongitudes;

    private List<String> messages;

    private List<Double> pointOneLat;

    private List<Double> pointOneLng;

    private List<Double> pointTwoLat;

    private List<Double> pointTwoLng;

    private List<String> from;

    private List<String> to;

    public OutputAccount() {
    }

    public OutputAccount(OutputAccount outputAccount) {
        this.id = outputAccount.getId();
        this.identifier = outputAccount.getIdentifier();
        this.companionId = outputAccount.getCompanionId();
        this.type = outputAccount.getType();
        this.names = outputAccount.getNames();
        this.ages = outputAccount.getAges();
        this.hobbies = outputAccount.getHobbies();
        this.photos = outputAccount.getPhotos();
        this.peopleStatuses = outputAccount.getPeopleStatuses();
        this.peopleIdentifiers = outputAccount.getPeopleIdentifiers();
        this.peopleLatitudes = outputAccount.getPeopleLatitudes();
        this.peopleLongitudes = outputAccount.getPeopleLongitudes();
        this.messages = outputAccount.getMessages();
        this.pointOneLat = outputAccount.getPointOneLat();
        this.pointOneLng = outputAccount.getPointOneLng();
        this.pointTwoLat = outputAccount.getPointTwoLat();
        this.pointTwoLng = outputAccount.getPointTwoLng();
        this.from = outputAccount.from;
        this.to = outputAccount.getTo();
    }

    public List<String> getName() {
        return names;
    }

    public void setName(List<String> name) {
        this.names = name;
    }

    public List<String> getAge() {
        return ages;
    }

    public void setAge(List<String> age) {
        this.ages = age;
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

    public List<Integer> getPeopleStatuses() {
        return peopleStatuses;
    }

    public void setPeopleStatuses(List<Integer> peopleStatuses) {
        this.peopleStatuses = peopleStatuses;
    }

    public List<Integer> getPeopleIdentifiers() {
        return peopleIdentifiers;
    }

    public void setPeopleIdentifiers(List<Integer> peopleIdentifiers) {
        this.peopleIdentifiers = peopleIdentifiers;
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

    public Integer getCompanionId() {
        return companionId;
    }

    public void setCompanionId(Integer companionId) {
        this.companionId = companionId;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public List<String> getAges() {
        return ages;
    }

    public void setAges(List<String> ages) {
        this.ages = ages;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public List<Double> getPeopleLatitudes() {
        return peopleLatitudes;
    }

    public List<Double> getPeopleLongitudes() {
        return peopleLongitudes;
    }

    public void setPeopleLongitudes(List<Double> peopleLongitudes) {
        this.peopleLongitudes = peopleLongitudes;
    }

    public void setLists(List<String> names, List<String> ages, List<String> hobbies, List<String> photos, List<Double> peopleLatitudes, List<Double> peopleLongitudes, List<Integer> peopleIdentifiers, List<Integer> peopleStatuses) {
        this.names = names;
        this.ages = ages;
        this.hobbies = hobbies;
        this.photos = photos;
        this.peopleLatitudes = peopleLatitudes;
        this.peopleLongitudes = peopleLongitudes;
        this.peopleIdentifiers = peopleIdentifiers;
        this.peopleStatuses = peopleStatuses;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setPeopleLatitudes(List<Double> peopleLatitudes) {
        this.peopleLatitudes = peopleLatitudes;
    }

    public List<Double> getPointOneLat() {
        return pointOneLat;
    }

    public void setPointOneLat(List<Double> pointOneLat) {
        this.pointOneLat = pointOneLat;
    }

    public List<Double> getPointOneLng() {
        return pointOneLng;
    }

    public void setPointOneLng(List<Double> pointOneLng) {
        this.pointOneLng = pointOneLng;
    }

    public List<Double> getPointTwoLat() {
        return pointTwoLat;
    }

    public void setPointTwoLat(List<Double> pointTwoLat) {
        this.pointTwoLat = pointTwoLat;
    }

    public List<Double> getPointTwoLng() {
        return pointTwoLng;
    }

    public void setPointTwoLng(List<Double> pointTwoLng) {
        this.pointTwoLng = pointTwoLng;
    }

    public List<String> getFrom() {
        return from;
    }

    public void setFrom(List<String> from) {
        this.from = from;
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }
}
