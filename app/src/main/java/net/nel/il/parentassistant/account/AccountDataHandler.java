package net.nel.il.parentassistant.account;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;

import net.nel.il.parentassistant.FileManager;
import net.nel.il.parentassistant.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class AccountDataHandler {

    private final String EMPTY = "";

    private final String SPACE = " ";

    private final String DOUBLE_SPACE = "  ";

    private Bitmap uploadedBitmap = null;

    private String bitmapName;

    private FileManager fileManager;

    private ArrayList<HashMap<String, Object>> data;

    private final String DEFAULT_NAME = "name";

    private final String DEFAULT_EXPANSION = ".jpg";

    private final int QUALITY_COMPRESS = 50;

    private final String NULL = "null";

    public AccountDataHandler(FileManager fileManager) {
        data = new ArrayList<>();
        this.fileManager = fileManager;
    }

    public ArrayList<HashMap<String, Object>> handleData(Context context,
                                                         String[] mapTags) {
        ArrayList<HashMap<String, Object>> dataCopy;
        dataCopy = fileManager.getUserData(context);
        int photoIndex = 0;
        for (int element = 0; element < dataCopy.size(); element++) {
            HashMap<String, Object> dataElement = dataCopy.get(element);
            String photoSource = (String) dataElement.get(mapTags[photoIndex]);
            if (photoSource == null || photoSource.equals(NULL)) {
                dataElement.remove(mapTags[photoIndex]);
                dataElement.put(mapTags[photoIndex], getDefaultBitmap(context));
            } else {
                dataElement.remove(mapTags[photoIndex]);
                dataElement.put(mapTags[photoIndex], uploadBitmap(context, photoSource));
            }
            for (int fieldElement = 1; fieldElement < mapTags.length; fieldElement++) {
                if (dataElement.get(mapTags[fieldElement]) == null) {
                    dataElement.put(mapTags[fieldElement], EMPTY);
                }
            }
            this.data.add(dataElement);
        }
        return data;
    }

    public void saveNote(Context context, String name, String age,
                         String hobbies, String[] mapTags) {
        String photo;
        if (name.equals(EMPTY) || name.equals(SPACE)) {
            name = DOUBLE_SPACE;
        }
        if (age.equals(EMPTY) || age.equals(SPACE)) {
            age = DOUBLE_SPACE;
        }
        if (hobbies.equals(EMPTY) || hobbies.equals(SPACE)) {
            hobbies = DOUBLE_SPACE;
        }
        if (uploadedBitmap == null) {
            photo = NULL;
        } else {
            photo = bitmapName;
        }
        fileManager.saveUserData(context, mapTags,
                new String[]{photo, name, age, hobbies});
    }

    public void deleteNote(Context context, int position) {
        fileManager.deleteUserData(context, position);
    }

    public Bitmap setBitmap(Context context, Intent data) {
        bitmapName = uploadBitmapFromGallery(context, data);
        return uploadedBitmap;
    }

    private String uploadBitmapFromGallery(Context context, Intent data) {
        Uri selectedImage = data.getData();
        try (BufferedOutputStream writer = new BufferedOutputStream(
                context.openFileOutput(DEFAULT_NAME + this.data.size()
                        + DEFAULT_EXPANSION, Context.MODE_PRIVATE))) {
            uploadedBitmap = MediaStore.Images.Media.getBitmap(
                    context.getContentResolver(), selectedImage);
            uploadedBitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY_COMPRESS, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return DEFAULT_NAME + this.data.size() + DEFAULT_EXPANSION;
    }

    public Bitmap getDefaultBitmap(Context context) {
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.default_face);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private Bitmap uploadBitmap(Context context, String photoSource) {
        Bitmap photoBitmap = getDefaultBitmap(context);
        try (BufferedInputStream inputStream =
                     new BufferedInputStream(context.openFileInput(photoSource))) {
            photoBitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return photoBitmap;
    }

}
