package com.example.android.postexample;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.example.android.postexample.data.ApiUtils;
import com.example.android.postexample.data.model.PostResponse;
import com.example.android.postexample.data.model.SpinnerResponse;
import com.example.android.postexample.data.remote.PostService;
import com.example.android.postexample.data.remote.SpinnerService;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private EditText mName;
    private EditText mEmail;
    private EditText mPhone;
    private EditText mLocation;
    private EditText mPassword;
    private Button mButton;
    private Spinner mDepartmentSpinner;
    private ImageView mPhonePicker;
    private ImageView mLocationPicker;
    private int mDepartment;

    private SpinnerService mService;
    private PostService mPostService;
    //Identify the Contact selector intent
    private static final int REQUEST_SELECT_CONTACT = 1;
    //Identify the Place picker intent
    private static final int PLACE_PICKER_REQUEST = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mService = ApiUtils.getSpinnerService();
        mPostService = ApiUtils.getPostService();
        mName = (EditText) findViewById(R.id.name_edit_text);
        mEmail = (EditText) findViewById(R.id.email_edit_text);
        mPhone = (EditText) findViewById(R.id.phone_edit_text);
        mLocation = (EditText) findViewById(R.id.location_edit_text);
        mPassword = (EditText) findViewById(R.id.password_edit_text);
        mDepartmentSpinner = (Spinner) findViewById(R.id.spinner_department);
        mButton = (Button) findViewById(R.id.submit_button);
        mPhonePicker = (ImageView) findViewById(R.id.icon_phone);

        mLocationPicker = (ImageView) findViewById(R.id.icon_location);
        mLocationPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating a PlacePicker. Need to add a metadata with a KEY in the Manifest File
                //Also need to add the compile 'com.google.android.gms:play-services-places:11.0.2'
                //dependency
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    //needs to be in a try block as it throws the two exceptions.
                    startActivityForResult(builder.build(MainActivity.this),PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
        setupspinner();

        mPhonePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create an intent
                Intent intent = new Intent(Intent.ACTION_PICK);
                //The intent will be of type contact picker
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_SELECT_CONTACT);
                }
            }
        });
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performPostOperation();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to
        //if  it is the contact picker intent
        if (requestCode == REQUEST_SELECT_CONTACT && resultCode == RESULT_OK) {
            // Get the URI and query the content provider for the phone number
            Uri contactUri = data.getData();

            //projection = what column we want to extract. The * in SELECT * FROM
            //Here we just want the number.
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};

            //Query the ContactProvider using the uri of the contact we just selected
            Cursor cursor = getContentResolver().query(contactUri, projection,
                    null, null, null);
            // If the cursor returned is valid, get the phone number
            if (cursor != null && cursor.moveToFirst()) {
                //Find the index of the column that contains the phone number.
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                //use that index to ge the actual phone number
                String number = cursor.getString(numberIndex);
                mPhone.setText(number);
                cursor.close();
            }
        }

        //if it is the place picker intent.
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK){
            //Get the place object from the result of the intent
            Place place = PlacePicker.getPlace(this,data);
            //Get the address from the place.
            String address = String.format("%s", place.getAddress());
            mLocation.setText(address);
        }
    }

    private void performPostOperation() {
        String name = mName.getText().toString().trim();
        String email = mEmail.getText().toString().trim();
        String phone = mPhone.getText().toString().trim();
        String location = mLocation.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        Boolean emptyCheck = checkIfAnyFieldEmpty(name, email, phone, location, password);

        if (emptyCheck) {
            mPostService.savePost(name, email, phone, location, password, mDepartment).enqueue(new Callback<PostResponse>() {
                @Override
                public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                    Log.v("MainActivity", "URL: " + call.request().url());
                    if (response.isSuccessful()) {
                        showDialog(response.body().getMessage());
                        Log.v(LOG_TAG, "Post submitted to API: " + response.body().toString());
                    }
                }

                @Override
                public void onFailure(Call<PostResponse> call, Throwable t) {
                    Log.v("MainActivity", "URL: " + call.request().url());
                    Log.d("MainActivity", "error loading from API " + t.getMessage());
                }
            });
        }
    }

    private boolean checkIfAnyFieldEmpty(String name, String email, String phone, String location, String password) {
        if (TextUtils.isEmpty(name)) {
            Log.v(LOG_TAG, "No Name Value");
            showDialog("Please Enter a Name");
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            Log.v(LOG_TAG, "No Email Value");
            showDialog("Please Enter an Email");
            return false;
        }
        if (!TextUtils.isEmpty(email)) {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Log.v(LOG_TAG, "Email Format Doesn't Match");
                showDialog("Email Format Incorrect");
                return false;
            } else {
                Log.v(LOG_TAG, "Email Format Matches");
            }
        }
        if (TextUtils.isEmpty(phone)) {
            Log.v(LOG_TAG, "No Phone Value");
            showDialog("Please Enter a Phone Number");
            return false;
        }
        if (TextUtils.isEmpty(location)) {
            Log.v(LOG_TAG, "No Location Value");
            showDialog("Please Enter a Location");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            Log.v(LOG_TAG, "No Password Value");
            showDialog("Please Enter a Password");
            return false;
        }
        return true;
    }

    private void showDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setupspinner() {
        mService.getResponse().enqueue(new Callback<List<SpinnerResponse>>() {
            @Override
            public void onResponse(Call<List<SpinnerResponse>> call, Response<List<SpinnerResponse>> response) {
                Log.v("MainActivity", "URL: " + call.request().url());
                if (response.isSuccessful()) {
                    List<SpinnerResponse> spinnerResponse = response.body();
                    ArrayList<String> choices = new ArrayList<>();
                    final HashMap<String, Integer> item = new HashMap<>();
                    for (SpinnerResponse resp : spinnerResponse) {
                        choices.add(resp.getName());
                        item.put(resp.getName(), resp.getId());
                    }
                    final ArrayAdapter<String> departmentSpinnerAdapter = new ArrayAdapter<>(MainActivity.this,
                            android.R.layout.simple_spinner_item, choices);
                    departmentSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

                    mDepartmentSpinner.setAdapter(departmentSpinnerAdapter);
                    mDepartmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                            String selection = (String) adapterView.getItemAtPosition(position);
                            if (!TextUtils.isEmpty(selection)) {
                                mDepartment = item.get(selection);
                                Log.v("MainActivity", "mDepartment = " + mDepartment);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            mDepartment = 1;
                        }
                    });

                } else {
                    int statusCode = response.code();
                    Log.v("MainActivity", "Status Code: " + statusCode);
                }

            }

            @Override
            public void onFailure(Call<List<SpinnerResponse>> call, Throwable t) {
                Log.v("MainActivity", "URL: " + call.request().url());
                Log.d("MainActivity", "error loading from API " + t.getMessage());
            }
        });
    }
}
