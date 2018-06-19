package net.nel.il.parentassistant.account;

import android.graphics.Bitmap;

public class Note {

    int deletingResource;

    Bitmap photo;

    String name;

    String age;

    String hobbies;

    public Note() {

    }

    public Note(int deletingResource, Bitmap photo, String name, String age, String hobbies) {
        this.deletingResource = deletingResource;
        this.photo = photo;
        this.name = name;
        this.age = age;
        this.hobbies = hobbies;
    }
}
