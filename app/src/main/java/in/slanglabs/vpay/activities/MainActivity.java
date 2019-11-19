package in.slanglabs.vpay.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {

    EditText upiIDEditText;
    ImageView sendButton;
    ImageView getButton;
    View contactsButton;

    AppData appData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(this.getSupportActionBar()!=null)
            this.getSupportActionBar().hide();
        setContentView(R.layout.main_activity);

        appData = AppData.getInstance();
        appData.init(this);

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

    private static Set<String> getCustomerNames() {
        //TODO: Get the list from phone contact
        String[] names = new String[] {
                "Kumar Rangarajan",
                "Giridhar Murthy",
                "Satish Gupta",
                "Satish Chandra Gupta",
                "Phaniraj Raghavendra",
                "Ved Mathai",
                "Satheesh Anbalagan",
                "Deepak Srinivasa",
                "Ritinkar Pramanik",
                "Ankit Tiwari",
                "Anshaj Khare",
                "Manikant Thakur",
                "Mohsin Mumtaz",
                "Nissim Dsilva",
                "Vinayak Jhunjunwala"
        };
        return new HashSet<>(Arrays.asList(names));
    }
}
