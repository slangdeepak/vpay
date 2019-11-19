package in.slanglabs.vpay.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import in.slanglabs.vpay.R;

public class SendActivity extends Activity {

    Button sendButton;
    Button pickContactButton;
    Button sendCancelButton;
    TextView upiIdView;
    TextView nameView;
    TextView amountView;
    TextView noteView;

    public static int REQUEST_UPI = 0;
    public static int REQUEST_CONTACT = 1;

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
        if (mode != null && mode.equals("direct")){
            upiIdView.setText(intent.getStringExtra("upiId"));
            nameView.setText(intent.getStringExtra("name"));
            amountView.setText("" + intent.getIntExtra("amount", 0));
            noteView.setText(intent.getStringExtra("note"));
            sendMoney();
            return;
        }

        sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendActivity.this.sendMoney();
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
    }

    public boolean sendMoney(){
        String upiId = SendActivity.this.upiIdView.getText().toString();
        String name = SendActivity.this.nameView.getText().toString();
        String amount = SendActivity.this.amountView.getText().toString();
        String note = SendActivity.this.noteView.getText().toString();

        if (    upiId != null && upiId.length() > 0
                &&  name != null && name.length() > 0
                &&  amount != null && amount.length() > 0
                &&  note != null && note.length() > 0)
        {
            String deeplink = "upi://pay?pa="+upiId+"&pn="+name+"&tn="+note+"&am="+amount+"&cu=INR";
            Log.i("Slang", "Sending deeplink intent -- " + deeplink);
            Intent intent = new Intent (Intent.ACTION_VIEW);
            intent.setData (Uri.parse(deeplink));
            startActivityForResult(intent, REQUEST_UPI);
        }
        else {
            Toast.makeText(SendActivity.this, "Incorrect or insufficient info. Cannot send money.", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_UPI){
            Log.i("Slang", "Received result from BHIM...");
        }
        else if (requestCode == REQUEST_CONTACT){
            Log.i("Slang", "Received result from Contacts");
            String name = data.getStringExtra("name");
            String upiId = data.getStringExtra("upiId");
            nameView.setText(name);
            upiIdView.setText(upiId);
        }

    }
}
