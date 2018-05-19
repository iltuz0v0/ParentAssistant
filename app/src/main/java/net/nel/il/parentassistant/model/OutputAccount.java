package net.nel.il.parentassistant.model;

import java.util.List;

public class OutputAccount {

    private Integer id;

    private Integer identifier;

    private Integer companionId;

    private List<String> names;

    private List<String> ages;

    private List<String> hobbies;

    private List<String> photos;

    private List<Integer> peopleStatuses;

    private List<Integer> peopleIdentifiers;

    private List<Float> peopleLatitudes;

    private List<Float> peopleLongitudes;

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

    public List<Float> getPeopleLatitudes() {
        return peopleLatitudes;
    }

    public void setPeopleLatitudes(List<Float> peopleLatitudes) {
        this.peopleLatitudes = peopleLatitudes;
    }

    public List<Float> getPeopleLongitudes() {
        return peopleLongitudes;
    }

    public void setPeopleLongitudes(List<Float> peopleLongitudes) {
        this.peopleLongitudes = peopleLongitudes;
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
}
