package in.slanglabs.vpay.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

import in.slanglabs.vpay.R;
import in.slanglabs.vpay.controller.SlangInterface;
import in.slanglabs.vpay.model.AppData;
import in.slanglabs.vpay.model.PhoneData;

public class ContactDetailActivity extends Activity {

    Button cancelButton;
    Button actionButton;
    Button deleteButton;
    TextView contactName;
    TextView contactUpiId;
    TextView contactTitle;
    AppData appData;
    boolean editing = false;
    String oldName;
    String oldUpiId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);
        appData = AppData.getInstance();

        contactName = findViewById(R.id.contactDetailName);
        contactUpiId = findViewById(R.id.contactDetailUpiId);
        contactTitle = findViewById(R.id.contactDetailTitle);

        cancelButton = findViewById(R.id.contactCancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        actionButton = findViewById(R.id.contactActionButton);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = contactName.getText().toString();
                String upiId = contactUpiId.getText().toString();
                if (name == null || name.length() == 0 || upiId == null || upiId.length() == 0){
                    Toast.makeText(ContactDetailActivity.this, "Invalid contact details. Try again.", Toast.LENGTH_LONG).show();
                }
                else {
                    if (ContactDetailActivity.this.editing){
                        boolean contactUpdated = appData.editContact(ContactDetailActivity.this.oldName, ContactDetailActivity.this.oldUpiId,
                                name, upiId);
                        if (contactUpdated){
                            Toast.makeText(ContactDetailActivity.this, "Saved contact.", Toast.LENGTH_SHORT).show();
                            Intent data = new Intent();
                            setResult(RESULT_OK, data);
                            updateSlang();
                            finish();
                        }
                        else {
                            Toast.makeText(ContactDetailActivity.this, "Contact save failed. Either a contact with this name and UPI ID already exists or invalid values provided.", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        boolean contactAdded = appData.addContact(name, upiId);
                        if (contactAdded){
                            Toast.makeText(ContactDetailActivity.this, "Created new contact", Toast.LENGTH_SHORT).show();
                            Intent data = new Intent();
                            setResult(RESULT_OK, data);
                            updateSlang();
                            finish();
                        }
                        else {
                            Toast.makeText(ContactDetailActivity.this, "Contact already exists. Try with different values for name or UPI ID.", Toast.LENGTH_LONG).show();
                        }
                    }

                }

            }
        });

        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");
        deleteButton = findViewById(R.id.contactDeleteButton);
        if (mode.equals("create")){
            deleteButton.setVisibility(View.GONE);
        }
        else {
            String name = intent.getStringExtra("name");
            String upiId = intent.getStringExtra("upiId");
            if (name != null || upiId != null){
                if (name != null) contactName.setText(name);
                if (upiId != null) contactUpiId.setText(upiId);
                actionButton.setText("SAVE");
                contactTitle.setText("EDIT CONTACT");
                editing = true;
                oldName = name;
                oldUpiId = upiId;
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ContactDetailActivity.this.appData.deleteContact(oldName, oldUpiId);
                        Toast.makeText(ContactDetailActivity.this, "Deleted contact.", Toast.LENGTH_SHORT).show();
                        Intent data = new Intent();
                        setResult(RESULT_OK, data);
                        updateSlang();
                        finish();
                    }
                });

            }
        }

    }

    private void updateSlang() {
        try {
            Set<String> customerNames = new HashSet<>();
            customerNames.addAll(PhoneData.getInstance().getContactNames());
            customerNames.addAll(AppData.getInstance().getContactNames());

            SlangInterface.setCustomerNames(customerNames);
        } catch (Exception e) {
            //pass
        }
    }
}
