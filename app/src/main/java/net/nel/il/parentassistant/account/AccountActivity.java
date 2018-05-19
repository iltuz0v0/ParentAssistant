package net.nel.il.parentassistant.account;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import net.nel.il.parentassistant.FileManager;
import net.nel.il.parentassistant.R;

import java.util.ArrayList;
import java.util.HashMap;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener {

    private final int choosingPhotoRequestCode = 10;

    private ListView dataList;

    private AccountSimpleAdapter accountAdapter;

    private AccountDataHandler accountDataHandler;

    private ArrayList<HashMap<String, Object>> data;

    private String[] mapTags;

    private int[] ids =
            new int[]{R.id.photo, R.id.name, R.id.age, R.id.hobbies};

    private Button addElementButton;

    private LinearLayout addElementField;

    private ImageView uploadImageView;

    private EditText nameEditText;

    private EditText ageEditText;

    private EditText hobbiesEditText;

    private ImageButton closeNoteImageButton;

    private Button saveNoteButton;

    private FileManager fileManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        variablesInitialization();
        data = accountDataHandler.handleData(getApplicationContext(), mapTags);
        accountAdapter = new AccountSimpleAdapter(this,
                data, R.layout.data_element, mapTags, ids);
        accountAdapter.setViewBinder(new AccountSimpleAdapterBinder(getApplicationContext()));
        dataList.setAdapter(accountAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case choosingPhotoRequestCode:
                if (resultCode == RESULT_OK) {
                    Bitmap uploadedBitmap = accountDataHandler.setBitmap(
                            getApplicationContext(), data);
                    uploadImageView.setImageBitmap(uploadedBitmap);
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_element_button:
                addElement();
                break;
            case R.id.close_note_image_button:
                closeNote();
                break;
            case R.id.save_note_button:
                saveNote();
                break;
            case R.id.upload_image_view:
                uploadImage();
                break;
        }
    }

    public void deleteNote(int position) {
        data.remove(position);
        accountAdapter.notifyDataSetChanged();
        accountDataHandler.deleteNote(this, position);
    }

    private void accountInformationUpdated() {
        SharedPreferences preferences = getSharedPreferences(
                getResources().getString(R.string.network_data_file),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(getString(R.string.shared_update_account), true);
        editor.apply();
    }

    private void saveNote() {
        accountInformationUpdated();
        accountDataHandler.saveNote(this,
                nameEditText.getText().toString(),
                ageEditText.getText().toString(),
                hobbiesEditText.getText().toString(), mapTags);
        data.clear();
        data = accountDataHandler.handleData(this, mapTags);
        accountAdapter.notifyDataSetChanged();
    }

    private void closeNote() {
        addElementField.setVisibility(View.INVISIBLE);
        addElementButton.setVisibility(View.VISIBLE);
    }

    private void addElement() {
        addElementButton.setVisibility(View.INVISIBLE);
        addElementField.setVisibility(View.VISIBLE);
        addElementField.setBackgroundColor(
                getResources().getColor(R.color.add_note_color));
    }

    private void uploadImage() {
        Intent chosePhoto = new Intent(Intent.ACTION_PICK);
        chosePhoto.setType(getResources().getString(R.string.image_type));
        startActivityForResult(chosePhoto, choosingPhotoRequestCode);
    }

    private void variablesInitialization() {
        mapTags = getResources().getStringArray(R.array.mapTags);
        fileManager = new FileManager(getString(R.string.user_data_file));
        accountDataHandler = new AccountDataHandler(fileManager);
        data = new ArrayList<>();
        dataList = (ListView) findViewById(R.id.dataList);
        addElementButton = (Button) findViewById(R.id.add_element_button);
        addElementButton.setOnClickListener(this);
        addElementField = (LinearLayout) findViewById(R.id.add_element_field);
        uploadImageView = (ImageView) findViewById(R.id.upload_image_view);
        uploadImageView.setOnClickListener(this);
        nameEditText = (EditText) findViewById(R.id.name_edit_text);
        ageEditText = (EditText) findViewById(R.id.age_edit_text);
        hobbiesEditText = (EditText) findViewById(R.id.hobbies_edit_text);
        closeNoteImageButton = (ImageButton) findViewById(R.id.close_note_image_button);
        closeNoteImageButton.setOnClickListener(this);
        saveNoteButton = (Button) findViewById(R.id.save_note_button);
        saveNoteButton.setOnClickListener(this);
    }
}
