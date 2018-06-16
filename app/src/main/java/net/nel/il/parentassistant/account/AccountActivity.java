package net.nel.il.parentassistant.account;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import net.nel.il.parentassistant.FileManager;
import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.facade.AccountActivityFacade;
import net.nel.il.parentassistant.interfaces.AdapterStateListener;
import net.nel.il.parentassistant.settings.SharedPreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AccountActivity extends AppCompatActivity
        implements View.OnClickListener, AdapterStateListener {

    private final int photoChoosingRequestCode = 10;

    private RecyclerView recyclerView;

    private AccountAdapter accountAdapter;

    private AccountDataHandler accountDataHandler;

    private String[] mapTags;

    private Button elementAdditionButton;

    private ScrollView elementAdditionContainer;

    private ImageView uploadImageView;

    private EditText nameEditText;

    private EditText ageEditText;

    private EditText hobbiesEditText;

    private ImageButton noteClosingImageButton;

    private Button noteSavingButton;

    private FileManager fileManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        variablesInitialization();
        if(savedInstanceState == null){
            accountDataHandler = new AccountDataHandler(fileManager);
        }
        else{
            refreshInstance();
        }
        accountAdapter = new AccountAdapter(accountDataHandler.getData(getApplicationContext(),
                mapTags), this, getApplicationContext());
        recyclerView.setAdapter(accountAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case photoChoosingRequestCode:
                if (resultCode == RESULT_OK) {
                    Bitmap uploadedBitmap = accountDataHandler.setBitmap(
                            getApplicationContext(), data);
                    uploadImageView.setImageBitmap(uploadedBitmap);
                }
                break;
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        AccountActivityFacade accountActivityFacade = new AccountActivityFacade();
        accountActivityFacade.additionContainerState = elementAdditionContainer.getVisibility();
        accountActivityFacade.additionButtonState = elementAdditionButton.getVisibility();
        accountActivityFacade.name = nameEditText.getText().toString();
        accountActivityFacade.age = ageEditText.getText().toString();
        accountActivityFacade.hobbies = hobbiesEditText.getText().toString();
        accountActivityFacade.accountDataHandler = accountDataHandler;
        return accountActivityFacade;
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.element_addition_button:
                addElement();
                break;
            case R.id.note_closing_image_button:
                closeNote();
                break;
            case R.id.note_saving_button:
                saveNote();
                break;
            case R.id.uploading_image_view:
                uploadImage();
                break;
        }
    }

    @Override
    public void deleteNote(int position) {
        accountAdapter.remove(position);
        accountDataHandler.deleteNote(getApplicationContext(), position);
    }

    private void saveNote() {
        List<String> accountList = new ArrayList<>();
        accountList.add(nameEditText.getText().toString());
        accountList.add(ageEditText.getText().toString());
        accountList.add(hobbiesEditText.getText().toString());
        SharedPreferenceManager.accountInformationUpdated(getApplicationContext());
        accountDataHandler.saveNote(getApplicationContext(), accountList, mapTags);
        elementAdditionContainer.setVisibility(View.INVISIBLE);
        accountAdapter.setData(accountDataHandler
                .getData(getApplicationContext(), mapTags));
        setDefaultFieldValues();
    }

    private void closeNote() {
        elementAdditionContainer.setVisibility(View.INVISIBLE);
        elementAdditionButton.setVisibility(View.VISIBLE);
    }

    private void addElement() {
        uploadImageView.setImageResource(R.drawable.face);
        elementAdditionButton.setVisibility(View.INVISIBLE);
        elementAdditionContainer.setVisibility(View.VISIBLE);
        elementAdditionContainer.setBackgroundColor(
                getResources().getColor(R.color.add_note_color));
    }

    private void uploadImage() {
        Intent chosePhoto = new Intent(Intent.ACTION_PICK);
        chosePhoto.setType(getResources().getString(R.string.image_type));
        startActivityForResult(chosePhoto, photoChoosingRequestCode);
    }

    private void variablesInitialization() {
        mapTags = getResources().getStringArray(R.array.mapTags);
        fileManager = new FileManager(getString(R.string.user_data_file));

        recyclerView = (RecyclerView) findViewById(R.id.data_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        elementAdditionButton = (Button) findViewById(R.id.element_addition_button);
        elementAdditionButton.setOnClickListener(this);
        elementAdditionContainer = (ScrollView) findViewById(R.id.element_addition_container);
        uploadImageView = (ImageView) findViewById(R.id.uploading_image_view);
        uploadImageView.setOnClickListener(this);
        nameEditText = (EditText) findViewById(R.id.name_edit_text);
        ageEditText = (EditText) findViewById(R.id.age_edit_text);
        hobbiesEditText = (EditText) findViewById(R.id.hobbies_edit_text);
        noteClosingImageButton = (ImageButton) findViewById(R.id.note_closing_image_button);
        noteClosingImageButton.setOnClickListener(this);
        noteSavingButton = (Button) findViewById(R.id.note_saving_button);
        noteSavingButton.setOnClickListener(this);
    }

    private void refreshInstance(){
        AccountActivityFacade accountActivityFacade = (AccountActivityFacade)
                getLastCustomNonConfigurationInstance();
        if(accountActivityFacade != null) {
            elementAdditionContainer.setVisibility(accountActivityFacade
                    .additionContainerState);
            elementAdditionContainer.setBackgroundColor(Color.WHITE);
            elementAdditionButton.setVisibility(accountActivityFacade.additionButtonState);
            nameEditText.setText(accountActivityFacade.name);
            ageEditText.setText(accountActivityFacade.age);
            hobbiesEditText.setText(accountActivityFacade.hobbies);
            accountDataHandler = accountActivityFacade.accountDataHandler;
            accountDataHandler.clearData();
            if(accountDataHandler.uploadedBitmap != null){
                uploadImageView.setImageBitmap(accountDataHandler.uploadedBitmap);
            }
        }
    }

    public void setDefaultFieldValues(){
        nameEditText.setText("");
        ageEditText.setText("");
        hobbiesEditText.setText("");
        uploadImageView.setImageResource(R.drawable.face);
    }
}
