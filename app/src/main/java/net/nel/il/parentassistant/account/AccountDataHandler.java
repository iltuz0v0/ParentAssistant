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
import java.util.List;

public class AccountDataHandler {

    public Bitmap uploadedBitmap = null;

    private String bitmapName;

    private FileManager fileManager;

    private ArrayList<HashMap<String, Object>> data;

    private final String DEFAULT_NAME = "name";

    private final String DEFAULT_EXPANSION = ".jpg";

    private final int QUALITY_COMPRESS = 50;

    private final String DEFAULT_VALUE = "null";

    private final String EMPTY = "";

    private final String SPACE = " ";

    private final String DOUBLE_SPACE = "  ";

    private final static int ACCOUNT_FIELDS_AMOUNT = 4;

    public AccountDataHandler(FileManager fileManager) {
        data = new ArrayList<>();
        this.fileManager = fileManager;
    }

    public List<Note>getData(Context context, String[] mapTags){
        List<Note> notes = new ArrayList<>();
        ArrayList<HashMap<String, Object>> data = handleData(context, mapTags);
        for(HashMap<String, Object> dataElement : data){
            int index = 0;
            Note note = new Note();
            note.deletingResource = R.drawable.delete_element;
            note.photo = (Bitmap) dataElement.get(mapTags[index]);
            index++;
            note.name = (String) dataElement.get(mapTags[index]);
            index++;
            note.age = (String) dataElement.get(mapTags[index]);
            index++;
            note.hobbies = (String) dataElement.get(mapTags[index]);
            notes.add(note);
        }
        return notes;
    }

    public ArrayList<HashMap<String, Object>> handleData(Context context,
                                                         String[] mapTags) {
        data.clear();
        ArrayList<HashMap<String, Object>> dataCopy;
        dataCopy = fileManager.getUserData(context);
        int photoIndex = 0;
        for (int element = 0; element < dataCopy.size(); element++) {
            HashMap<String, Object> dataElement = dataCopy.get(element);
            String photoSource = (String) dataElement.get(mapTags[photoIndex]);
            if (photoSource == null || photoSource.equals(DEFAULT_VALUE)) {
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

    public void saveNote(Context context, List<String> accountData,
                         String[] mapTags) {
        String[] accountDataCopy = new String[ACCOUNT_FIELDS_AMOUNT];
        if (uploadedBitmap == null) {
            accountDataCopy[0] = DEFAULT_VALUE;
        } else {
            accountDataCopy[0] = bitmapName;
        }
        for(int element = 0; element < accountData.size(); element++){
            String accountElement = accountData.get(element);
            if (accountElement.equals(EMPTY) || accountElement.equals(SPACE)) {
                accountDataCopy[element+1] = DOUBLE_SPACE;
            }
            else{
                accountDataCopy[element+1] = accountElement;
            }
        }
        fileManager.saveUserData(context, mapTags, accountDataCopy);
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

    private Bitmap getDefaultBitmap(Context context) {
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

    public void clearData(){
        data.clear();
    }

}
