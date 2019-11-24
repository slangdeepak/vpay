package in.slanglabs.vpay.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

import in.slanglabs.platform.SlangBuddy;
import in.slanglabs.platform.SlangLocale;
import in.slanglabs.vpay.R;
import in.slanglabs.vpay.controller.AppActions;
import in.slanglabs.vpay.controller.SlangInterface;
import in.slanglabs.vpay.model.AppData;
import in.slanglabs.vpay.model.PhoneData;

public class MainActivity extends AppCompatActivity {

    EditText upiIDEditText;
    ImageView sendButton;
    ImageView getButton;
    View contactsButton;
    AppData appData;
    PhoneData phoneData;
    ImageView helpButton;
    private SharedPreferences sharedPreferences;
    private String locale;
    Button englishButton;
    Button hindiButton;

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
                Toast.makeText(MainActivity.this, "Hang in there, this feature is coming soon...", Toast.LENGTH_LONG).show();
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

        SlangInterface.init(getApplication(), getCustomerNames(), AppActions.getInstance(this));

        englishButton = findViewById(R.id.englishButton);
        hindiButton = findViewById(R.id.hindiButton);

        sharedPreferences = getSharedPreferences("SharedPref", MODE_PRIVATE);
        locale = sharedPreferences.getString("locale", "en");
        boolean ask = sharedPreferences.getBoolean("ask", true);
        if (ask) {
            Dialog dialog = createNoLocationDialog(true);
            dialog.show();
        }

        helpButton = findViewById(R.id.helpView);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = createNoLocationDialog(false);
                dialog.show();
            }
        });

        if (locale.equalsIgnoreCase("en")) {
            englishButton.setAlpha(1);
            hindiButton.setAlpha(0.5f);
        } else {
            englishButton.setAlpha(0.5f);
            hindiButton.setAlpha(1);
        }
        englishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SlangBuddy.getBuddyContext().setCurrentLocale(SlangLocale.LOCALE_ENGLISH_IN);
                    englishButton.setAlpha(1);
                    hindiButton.setAlpha(0.5f);
                } catch (Exception e) {
                    //pass
                }
            }
        });
        hindiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SlangBuddy.getBuddyContext().setCurrentLocale(SlangLocale.LOCALE_HINDI_IN);
                    englishButton.setAlpha(0.5f);
                    hindiButton.setAlpha(1);
                } catch (Exception e) {
                    //pass
                }
            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(listener, new IntentFilter("localeChanged"));
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

    private BroadcastReceiver listener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            locale = intent.getStringExtra("localeBroadcast");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("locale", locale);
            editor.apply();
            if (locale.equals("en")) {
                englishButton.setAlpha(1);
                hindiButton.setAlpha(0.5f);
            } else {
                englishButton.setAlpha(0.5f);
                hindiButton.setAlpha(1);
            }
        }
    };

    private Set<String> getCustomerNames() {
        Set<String> customerNames = new HashSet<>();
        customerNames.addAll(phoneData.getContactNames());
        customerNames.addAll(appData.getContactNames());
        Log.e("CustomerNames", customerNames.toString());
        return customerNames;
    }

    private Dialog createNoLocationDialog(boolean showOptout) {
        View view = View.inflate(MainActivity.this, R.layout.dialog_intro, null);
        CheckBox neverShowDialog = view.findViewById(R.id.location_never_ask_again);

        if (showOptout) {
            neverShowDialog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    // Save the preference
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("ask", !isChecked);
                    editor.apply();
                }
            });
        } else {
            neverShowDialog.setVisibility(View.GONE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.welcome_title)
                .setCancelable(false)
                .setNeutralButton("Let's jump in", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setView(view);
        return builder.create();
    }
}
