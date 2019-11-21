package in.slanglabs.vpay.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import in.slanglabs.vpay.R;
import in.slanglabs.vpay.controller.AppActions;
import in.slanglabs.vpay.controller.SlangInterface;
import in.slanglabs.vpay.model.AppData;
import in.slanglabs.vpay.model.Contact;
import in.slanglabs.vpay.model.PhoneData;

public class MainActivity extends AppCompatActivity {

    EditText upiIDEditText;
    ImageView sendButton;
    ImageView getButton;
    View contactsButton;
    AppData appData;
    PhoneData phoneData;

    private static final int REQUEST_READ_CONTACTS = 3333;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(this.getSupportActionBar()!=null)
            this.getSupportActionBar().hide();
        setContentView(R.layout.main_activity);

        appData = AppData.getInstance();
        appData.init(this);

        phoneData = PhoneData.getInstance();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            phoneData.loadPhoneData(this);
        } else {
            requestPermission();
        }

        upiIDEditText = findViewById(R.id.upiID);
        upiIDEditText.setText(appData.getUserUpiId());
        upiIDEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.i("Slang", "Setting upiId to: " + v.getText());
                appData.setUserUpiId(v.getText().toString());
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
        });

        sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Slang", "Sending money...");
                Intent sendIntent = new Intent(MainActivity.this, SendActivity.class);
                MainActivity.this.startActivity(sendIntent);
            }
        });

        getButton = findViewById(R.id.getButton);
        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Slang", "Getting money...");
                Intent sendIntent = new Intent(MainActivity.this, GetActivity.class);
                MainActivity.this.startActivity(sendIntent);
            }
        });

        contactsButton = findViewById(R.id.contactsButton);
        contactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Slang", "Showing contacts...");
                Intent sendIntent = new Intent(MainActivity.this, ContactsActivity.class);
                MainActivity.this.startActivity(sendIntent);
            }
        });

        SlangInterface.init(getApplication(), getCustomerNames(), AppActions.getInstance(this)); //TODO:
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)) {
            // show UI part if you want here to show some rationale !!!
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    phoneData.loadPhoneData(this);
                    SlangInterface.setCustomerNames(getCustomerNames());
                } else {
                    // permission denied,Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private Set<String> getCustomerNames() {
        Set<String> customerNames = new HashSet<>();
        customerNames.addAll(phoneData.getContactNames());
        customerNames.addAll(appData.getContactNames());
        Log.e("CustomerNames", customerNames.toString());
        return customerNames;
    }
}
