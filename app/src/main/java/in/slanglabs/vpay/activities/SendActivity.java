package in.slanglabs.vpay.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import in.slanglabs.platform.SlangBuddy;
import in.slanglabs.platform.SlangLocale;
import in.slanglabs.platform.prompt.SlangMessage;
import in.slanglabs.vpay.R;
import in.slanglabs.vpay.controller.TransactionStatus;

public class SendActivity extends Activity {

    Button sendButton;
    Button pickContactButton;
    Button sendCancelButton;
    TextView upiIdView;
    TextView nameView;
    TextView amountView;
    TextView noteView;
    ImageView helpButton;
    ImageView langButton;
    private SharedPreferences sharedPreferences;
    private String locale;
    public static int REQUEST_UPI_VOICE = 1;
    public static int REQUEST_UPI_TOUCH = 2;
    public static int REQUEST_CONTACT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        upiIdView = findViewById(R.id.getUpiId);
        nameView = findViewById(R.id.getName);
        amountView = findViewById(R.id.getAmount);
        noteView = findViewById(R.id.sendNote);

        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");
        if (mode != null && mode.equals("voice")){
            upiIdView.setText(intent.getStringExtra("upiId"));
            nameView.setText(intent.getStringExtra("name"));
            amountView.setText("" + intent.getIntExtra("amount", 0));
            noteView.setText(intent.getStringExtra("note"));
            if (!intent.getStringExtra("upiId").isEmpty() && intent.getIntExtra("amount", 0) > 0) {
                sendMoney(true);
            } else {
                try {
                    SlangMessage message = new SlangMessage(new HashMap<Locale, String>() {
                        {
                            put(SlangLocale.LOCALE_ENGLISH_IN, "Please enter the missing details and proceed");
                            put(SlangLocale.LOCALE_HINDI_IN, "Please enter the missing details and proceed");
                        }
                    });
                    message.overrideIsSpoken(true);
                    SlangBuddy.notifyUser(message);
                } catch (SlangBuddy.UninitializedUsageException e) {
                    e.printStackTrace();
                }
            }

            return;
        }

        sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendActivity.this.sendMoney(false);
            }
        });

        pickContactButton = findViewById(R.id.pickButton);
        pickContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickContactIntent = new Intent(SendActivity.this, ContactsActivity.class);
                pickContactIntent.putExtra("mode", "pick");
                SendActivity.this.startActivityForResult(pickContactIntent, REQUEST_CONTACT);
            }
        });

        sendCancelButton = findViewById(R.id.sendCancelButton);
        sendCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        helpButton = findViewById(R.id.helpView);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = createNoLocationDialog(false);
                dialog.show();
            }
        });

        sharedPreferences = getSharedPreferences("SharedPref", MODE_PRIVATE);
        locale = sharedPreferences.getString("locale", "en");
        langButton = findViewById(R.id.lang_select);
        if (locale.equalsIgnoreCase("en")) {
            langButton.setImageResource(R.drawable.english);
        } else {
            langButton.setImageResource(R.drawable.hindi);
        }
        langButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (locale.equalsIgnoreCase("en")) {
                        SlangBuddy.getBuddyContext().setCurrentLocale(SlangLocale.LOCALE_HINDI_IN);
                    } else {
                        SlangBuddy.getBuddyContext().setCurrentLocale(SlangLocale.LOCALE_ENGLISH_IN);
                    }
                } catch (Exception e) {
                    //pass
                }
            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(listener, new IntentFilter("localeChanged"));
    }

    private String mLastReceiver;
    private String mLastAmount;
    public boolean sendMoney(boolean isVoice){
        String upiId = SendActivity.this.upiIdView.getText().toString();
        String name = SendActivity.this.nameView.getText().toString();
        String amount = SendActivity.this.amountView.getText().toString();
        String note = SendActivity.this.noteView.getText().toString();

        if (    upiId != null && upiId.length() > 0
                &&  name != null && name.length() > 0
                &&  amount != null && amount.length() > 0
                &&  note != null && note.length() > 0)
        {
            mLastReceiver = name;
            mLastAmount = amount;
            String deepLink = "upi://pay?pa="+upiId+"&pn="+name+"&tn="+note+"&am="+amount+"&cu=INR";
            Log.i("Slang", "Sending deeplink intent -- " + deepLink);
            final Intent intent = new Intent (Intent.ACTION_VIEW);
            intent.setData (Uri.parse(deepLink));
            if (intent.resolveActivity(getPackageManager()) != null) {
                if (isVoice) {
                    long actionDelayInMillis = 3000;
                    if (name.matches("^[0-9]+$")) actionDelayInMillis = 5000;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivityForResult(intent, REQUEST_UPI_VOICE);
                        }
                    }, actionDelayInMillis);
                } else {
                    startActivityForResult(intent, REQUEST_UPI_TOUCH);
                }
            } else {
                Toast.makeText(this, "No application available to handle this request!", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(SendActivity.this, "Incorrect or insufficient info. Cannot send money.", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.e("VPay Sender", "Received result [" + resultCode + "]");
        if (requestCode == REQUEST_UPI_VOICE || requestCode == REQUEST_UPI_TOUCH) {
            if (null != data) {
                String response = data.getStringExtra("response");
                if(response == null) {
                    handleCancel(requestCode == REQUEST_UPI_VOICE, null);
                } else {
                    TransactionStatus txnStatus = getTransactionStatus(response);
                    //Check if success, submitted or failed
                    try {
                        if (txnStatus.mStatus.toLowerCase().equals("success")) {
                            handleSuccess(requestCode == REQUEST_UPI_VOICE, txnStatus);
                        } else if (txnStatus.mStatus.toLowerCase().equals("submitted")) {
                            //TODO: Submitted
                        } else {
                            handleFail(requestCode == REQUEST_UPI_VOICE, txnStatus);
                        }
                    } catch (Exception e) {
                        handleFail(requestCode == REQUEST_UPI_VOICE, txnStatus);
                    }
                }
            } else {
                handleCancel(requestCode == REQUEST_UPI_VOICE, null);
            }
        } else if (requestCode == REQUEST_CONTACT){
            Log.e("Slang", "Received result from Contacts");
            String name = data.getStringExtra("name");
            String upiId = data.getStringExtra("upiId");
            nameView.setText(name);
            upiIdView.setText(upiId);
        }
    }

    private void displayMessage(final String message)  {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void handleCancel(boolean isVoice, TransactionStatus txnStatus) {
        if (isVoice) {
            HashMap<Locale, String> strings = new HashMap<>();
            strings.put(SlangLocale.LOCALE_ENGLISH_IN, "Sorry, transaction seems to have got canceled. Click the mic button and try again");
            strings.put(SlangLocale.LOCALE_HINDI_IN, "क्षमा करें, ऐसा लगता है कि लेनदेन रद्द हो गया है। माइक बटन पर क्लिक करें और फिर से प्रयास करें");
            SlangMessage message = SlangMessage.create(strings);
            try {
                message.overrideIsSpoken(true);
                SlangBuddy.notifyUser(message);
            } catch (SlangBuddy.UninitializedUsageException e) {
                SharedPreferences sharedPreferences = getSharedPreferences("SharedPref", MODE_PRIVATE);
                String locale = sharedPreferences.getString("locale", "en");
                if (locale.equalsIgnoreCase("en")) {
                    displayMessage("Sorry, transaction seems to have got canceled. Please try again!");
                } else {
                    displayMessage("क्षमा करें, ऐसा लगता है कि लेनदेन रद्द हो गया है। कृपया पुन: प्रयास करें");
                }
            }
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences("SharedPref", MODE_PRIVATE);
            String locale = sharedPreferences.getString("locale", "en");
            if (locale.equalsIgnoreCase("en")) {
                displayMessage("Sorry, transaction seems to have got canceled. Please try again!");
            } else {
                displayMessage("क्षमा करें, ऐसा लगता है कि लेनदेन रद्द हो गया है। कृपया पुन: प्रयास करें");
            }
        }
    }

    private void handleSuccess(boolean isVoice, TransactionStatus txnStatus) {
        SharedPreferences sharedPreferences = getSharedPreferences("SharedPref", MODE_PRIVATE);
        String locale = sharedPreferences.getString("locale", "en");
        if (isVoice) {
            HashMap<Locale, String> strings = new HashMap<>();
            strings.put(SlangLocale.LOCALE_ENGLISH_IN, "Congratulations, you've successfully sent the money!");
            strings.put(SlangLocale.LOCALE_HINDI_IN, "बधाई हो, आपने पैसे सफलतापूर्वक भेज दिए हैं");
            SlangMessage message = SlangMessage.create(strings);
            try {
                message.overrideIsSpoken(true);
                SlangBuddy.notifyUser(message);
            } catch (SlangBuddy.UninitializedUsageException e) {
                if (locale.equalsIgnoreCase("en")) {
                    displayMessage("Congratulations, you've successfully sent the money");
                } else {
                    displayMessage("बधाई हो, आपने पैसे सफलतापूर्वक भेज दिए हैं");
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Congratulations!");
        if (null != mLastReceiver && null != mLastAmount && !mLastReceiver.isEmpty() && !mLastAmount.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            if (locale.equalsIgnoreCase("en")) {
                sb.append("You've successfully sent rupees " + mLastAmount + " to " + mLastReceiver);
                if (null != txnStatus) sb.append("\n\nBelow are transaction details:\n");
            } else {
                sb.append("आपने " + mLastReceiver + " को सफलतापूर्वक " + mLastAmount + " रुपये भेजे हैं");
                if (null != txnStatus) sb.append("\n\nनीचे लेन-देन का विवरण है:\n");
            }
            if (null != txnStatus) {
                sb.append("Transaction Number:" + txnStatus.mTxnId);
                sb.append("\nUPI Response Code:" + txnStatus.mResponseCode);
                sb.append("\nUPI Approval Reference Number:" + txnStatus.mApprovalRefNo);
            }
        } else {
            if (locale.equalsIgnoreCase("en")) {
                builder.setMessage("Congratulations, you've successfully sent the money");
            } else {
                builder.setMessage("बधाई हो, आपने पैसे सफलतापूर्वक भेज दिए हैं");
            }
        }
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                finish();
            }
        });
    }

    private void handleFail(boolean isVoice, TransactionStatus txnStatus) {
        if (isVoice) {
            HashMap<Locale, String> strings = new HashMap<>();
            strings.put(SlangLocale.LOCALE_ENGLISH_IN, "Sorry, transaction failed, please try again or say cancel");
            strings.put(SlangLocale.LOCALE_HINDI_IN, "क्षमा करें, विफलता के कारण हुई असुविधा के लिए खेद है, कृपया पुनः प्रयास करें या कहें कि रद्द करें");
            final SlangMessage message = SlangMessage.create(strings);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        SlangBuddy.startConversation(message, true);
                    } catch (SlangBuddy.UninitializedUsageException | SlangBuddy.SlangDisabledException e) {
                        SharedPreferences sharedPreferences = getSharedPreferences("SharedPref", MODE_PRIVATE);
                        String locale = sharedPreferences.getString("locale", "en");
                        if (locale.equalsIgnoreCase("en")) {
                            displayMessage("Sorry, transaction failed, please try again");
                        } else {
                            displayMessage("क्षमा करें, विफलता के कारण हुई असुविधा के लिए खेद है, कृपया पुनः प्रयास करेंं");
                        }
                    }
                }
            }, 200);
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences("SharedPref", MODE_PRIVATE);
            String locale = sharedPreferences.getString("locale", "en");
            if (locale.equalsIgnoreCase("en")) {
                displayMessage("Sorry, transaction failed, please try again");
            } else {
                displayMessage("क्षमा करें, विफलता के कारण हुई असुविधा के लिए खेद है, कृपया पुनः प्रयास करेंं");
            }
        }
    }

    private TransactionStatus getTransactionStatus(String response) {
        Map<String, String> result = new HashMap<>();
        String[] params = response.split("&");
        for ( int i = 0; i < params.length; i++) {
            String[] paramParts = params[i].split("=");
            if (paramParts.length > 1) {
                result.put(paramParts[0], paramParts[1]);
            }
        }

        String txnId = result.get("txnId");
        String responseCode = result.get("responseCode");
        String status = result.get("Status");
        String txnRef = result.get("txnRef");
        String approvalRefNo = result.get("ApprovalRefNo");

        return new TransactionStatus(txnId, responseCode, status, txnRef, approvalRefNo);
    }

    private Dialog createNoLocationDialog(boolean showOptout) {
        View view = View.inflate(SendActivity.this, R.layout.dialog_intro, null);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(SendActivity.this)
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

    private BroadcastReceiver listener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            locale = intent.getStringExtra("localeBroadcast");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("locale", locale);
            editor.apply();
            if (locale.equals("en")) {
                langButton.setImageResource(R.drawable.english);
            } else {
                langButton.setImageResource(R.drawable.hindi);
            }
        }
    };
}
