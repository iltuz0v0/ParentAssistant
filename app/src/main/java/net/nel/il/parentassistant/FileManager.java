package net.nel.il.parentassistant;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class FileManager {

    private final String SPACE = " ";

    public FileManager(String fileName) {
        this.usedFile = fileName;
    }

    private String usedFile;

    private boolean existInternalFile(Context context, String filename) {
        File file = new File(context.getFilesDir().getAbsolutePath() + "/" + filename);
        return file.exists();
    }

    private boolean createInternalFile(Context context, String filename) {
        boolean result = false;
        File file = new File(context.getFilesDir().getAbsolutePath() + "/" + filename);
        try {
            result = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public ArrayList<HashMap<String, Object>> getUserData(Context context) {
        ArrayList<HashMap<String, Object>> userData = new ArrayList<>();
        if (existInternalFile(context, usedFile)) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.openFileInput(usedFile)))) {
                String name;
                String value;
                HashMap<String, Object> dataElement = new HashMap<>();
                while ((name = reader.readLine()) != null) {
                    if (name.equals(SPACE)) {
                        userData.add(dataElement);
                        dataElement = new HashMap<>();
                    } else {
                        if ((value = reader.readLine()) != null) {
                            dataElement.put(name, value);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            createInternalFile(context, usedFile);
        }
        return userData;
    }

    public void saveUserData(Context context, String[] mapTags, String[] data) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(context.openFileOutput(usedFile, Context.MODE_APPEND)))) {
            for (int element = 0; element < mapTags.length; element++) {
                writer.write(mapTags[element] + "\n");
                writer.write(data[element] + "\n");
            }
            writer.write(SPACE + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void deleteUserData(Context context, int position) {
        ArrayList<String> fileAsArray = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.openFileInput(usedFile)))) {
            String result;
            while ((result = reader.readLine()) != null) {
                fileAsArray.add(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int spacePosition = -1;
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(context.openFileOutput(usedFile, Context.MODE_PRIVATE)))) {
            for (int element = 0; element < fileAsArray.size(); element++) {
                if (fileAsArray.get(element).equals(SPACE)) {
                    spacePosition++;
                }
                if (spacePosition != position) {
                    writer.write(fileAsArray.get(element) + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
