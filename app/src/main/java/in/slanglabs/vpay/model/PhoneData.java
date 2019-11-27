package in.slanglabs.vpay.model;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PhoneData {
    private static PhoneData _instance;
    public static PhoneData getInstance(){
        if (_instance == null){
            _instance = new PhoneData();
        }
        return _instance;
    }

    private Map<String, Contact> phoneContacts;

    public Set<String> getContactNames() {
        if (null != phoneContacts) return phoneContacts.keySet();
        else return new HashSet<>();
    }

    public Contact getContactForName(String name){
        return phoneContacts.get(name);
    }

    public void loadPhoneData(Context context) {
        phoneContacts = new HashMap<>();
        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext())
        {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phoneNumber = phoneNumber.replaceAll("[^\\d.]", "");
            if (phoneNumber.length() > 10) phoneNumber = phoneNumber.substring(phoneNumber.length()- 10);
            phoneContacts.put(name, new Contact(name, phoneNumber + "@upi"));
        }
        phones.close();
    }
}
