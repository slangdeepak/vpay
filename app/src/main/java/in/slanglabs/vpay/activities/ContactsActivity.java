package in.slanglabs.vpay.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.slanglabs.vpay.R;
import in.slanglabs.vpay.model.AppData;
import in.slanglabs.vpay.model.Contact;

public class ContactsActivity extends Activity {

    private Button backButton;
    private Button addContactButton;
    private ListView contactsList;
    private ContactsAdapter contactsAdapter;
    private AppData appData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        appData = AppData.getInstance();

        backButton = findViewById(R.id.backToPaymentsButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addContactButton = findViewById(R.id.addContactButton);
        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Slang", "Create new contact...");
                Intent addContactIntent = new Intent(ContactsActivity.this, ContactDetailActivity.class);
                addContactIntent.putExtra("mode", "create");
                ContactsActivity.this.startActivityForResult(addContactIntent, 1);
            }
        });

        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");
        contactsList = findViewById(R.id.contactsList);
        if (mode != null && mode.equals("pick")){
            contactsAdapter = new ContactsAdapter(this, 0, new ArrayList<>(appData.getAppContacts()), mode);
        }
        else {
            contactsAdapter = new ContactsAdapter(this, 0, new ArrayList<>(appData.getAppContacts()), "normal");
        }

        contactsList.setAdapter(contactsAdapter);


    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Log.i("Slang", "Contacts list about to be refreshed...");
                contactsAdapter.notifyDataSetChanged();
            }
        }
    }

    private class ContactsAdapter extends ArrayAdapter<Contact> {
        private List<Contact> contacts;
        private Context context;
        private String mode;

        ContactsAdapter(Context context, int resource, List<Contact> objects, String mode){
            super(context, resource);
            this.context = context;
            contacts = objects;
            this.mode = mode;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View listItem = convertView;
            if (listItem == null){
                listItem = LayoutInflater.from(context).inflate(R.layout.contact_item, parent,false);
            }
            Contact currentContact = contacts.get(position);
            TextView nameView = listItem.findViewById(R.id.contactDetailName);
            nameView.setText(currentContact.name);
            TextView upiView = listItem.findViewById(R.id.contactDetailUpiId);
            upiView.setText(currentContact.upiId);
            if (mode.equals("normal")){
                listItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView nameView = v.findViewById(R.id.contactDetailName);
                        TextView upiView = v.findViewById(R.id.contactDetailUpiId);
                        Log.i("Slang", "name:" + nameView.getText().toString() + " upi:" + upiView.getText().toString());
                        Intent editContactIntent = new Intent(ContactsActivity.this, ContactDetailActivity.class);
                        editContactIntent.putExtra("name", nameView.getText().toString());
                        editContactIntent.putExtra("upiId", upiView.getText().toString());
                        editContactIntent.putExtra("mode", "edit");
                        ContactsActivity.this.startActivityForResult(editContactIntent, 1);
                    }
                });
            }
            else if (mode.equals("pick")){
                listItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView nameView = v.findViewById(R.id.contactDetailName);
                        TextView upiView = v.findViewById(R.id.contactDetailUpiId);
                        Log.i("Slang", "name:" + nameView.getText().toString() + " upi:" + upiView.getText().toString());
                        Intent resultData = new Intent();
                        resultData.putExtra("name", nameView.getText().toString());
                        resultData.putExtra("upiId", upiView.getText().toString());
                        setResult(SendActivity.REQUEST_CONTACT, resultData);
                        finish();
                    }
                });
            }


            return listItem;
        }

        @Override
        public int getCount(){
            return contacts.size();
        }


    }
}


