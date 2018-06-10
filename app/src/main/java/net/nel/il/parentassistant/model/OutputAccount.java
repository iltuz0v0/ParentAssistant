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

    private List<Float> peopleLatitudes;

    private List<Float> peopleLongitudes;

    private List<String> messages;

    public OutputAccount() {
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

    public void setLists(List<String> names, List<String> ages, List<String> hobbies,
                         List<String> photos, List<Float> peopleLatitudes,
                         List<Float> peopleLongitudes, List<Integer> peopleIdentifiers,
                         List<Integer> peopleStatuses){
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
}
