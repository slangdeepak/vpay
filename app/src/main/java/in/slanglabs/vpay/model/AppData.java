package in.slanglabs.vpay.model;

import android.content.Context;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AppData {
    //SINGLETON STUFF
    private static AppData _instance;
    public static AppData getInstance(){
        if (_instance == null){
            _instance = new AppData();
        }
        return _instance;
    }

    //PROPERTIES
    private String userUpiId;
    private ArrayList<Contact> contacts;
    private Context context;
    private boolean initialized = false;

    public AppData(){

    }

    public void init(Context context){
        if (!initialized){
            this.context = context;
            loadData();
            initialized = true;
        }
    }

    public String getUserUpiId(){
        return userUpiId;
    }

    public void setUserUpiId(String newUpiId){
        userUpiId = newUpiId;
        saveData();
    }

    public List<Contact> getAllContacts(){
        return contacts;
    }

    public boolean addContact(String name, String upiId){
        for (Contact contact : contacts){
            if (contact.name.equals(name) && contact.upiId.equals(upiId)){
                return false;
            }
        }
        contacts.add(new Contact(name, upiId));
        saveData();
        return true;
    }

    public List<Contact> getContactsForName(String name){
        ArrayList<Contact> result = new ArrayList<Contact>();
        for (Contact contact : contacts){
            if (contact.name.equals(name)){
                result.add(new Contact(contact.name, contact.upiId));
            }
        }
        return result;
    }

    public boolean editContact(String oldName, String oldUpiId, String newName, String newUpiId){
        Contact candidate = null;
        for (Contact contact : contacts){
            if (contact.name.equals(oldName) && contact.upiId.equals(oldUpiId)){
                candidate = contact;
                break;
            }
        }
        if (candidate != null){
            candidate.name = newName;
            candidate.upiId = newUpiId;
            saveData();
            return true;
        }
        else {
            return false;
        }
    }

    public boolean deleteContact(String name, String upiId){
        Contact candidate = null;
        for (Contact contact : contacts){
            if (contact.name.equals(name) && contact.upiId.equals(upiId)){
                candidate = contact;
                break;
            }
        }
        if (candidate != null){
            contacts.remove(candidate);
            saveData();
            return true;
        }
        else {
            return false;
        }
    }

    public void loadData() {
        //temp init
        userUpiId = "9901299770@upi";
        contacts = new ArrayList<Contact>();
        contacts.add(new Contact("Phaniraj Raghavendra", "phanir@okhdfcbank"));
        contacts.add(new Contact("Ved Mathai", "vedu29@okicici"));
        contacts.add(new Contact("Deepak Srinivasa", "9901299770@upi"));

        //TODO: should load data from internal file storage...
        try {
            FileInputStream fis = context.openFileInput("data.json");
            int bytesAvailable = fis.available();
            if (bytesAvailable == 0) return;
            Scanner scanner = new Scanner(fis);
            scanner.useDelimiter("\\Z");
            String data = scanner.next();
            scanner.close();

            if (data != null && data.length() > 0) {
                JSONObject jdata = new JSONObject(data);
                userUpiId = jdata.getString("userUpiId");
                JSONArray jcontacts = jdata.getJSONArray("contacts");
                contacts = new ArrayList<Contact>();
                for (int i = 0; i < jcontacts.length(); i++) {
                    JSONObject jcontact = jcontacts.getJSONObject(i);
                    Contact contact = new Contact(jcontact.getString("name"), jcontact.getString("upiId"));
                    contacts.add(contact);
                }
            }

        } catch (Exception e) {
            Log.e("Slang", e.getMessage());
            Log.e("Slang", "Could not load data from internal storage");
        }
    }

    public void saveData(){
        try {
            JSONObject result = new JSONObject();
            result.put("userUpiId", userUpiId);
            JSONArray jcontacts = new JSONArray();
            for (Contact contact : contacts){
                JSONObject jcontact = new JSONObject();
                jcontact.put("name", contact.name);
                jcontact.put("upiId", contact.upiId);
                jcontacts.put(jcontact);
            }
            result.put("contacts", jcontacts);
            String data = result.toString(4);
            //TODO: should store into internal file storage...
            FileOutputStream outStream = context.openFileOutput("data.json", Context.MODE_PRIVATE);
            PrintWriter writer = new PrintWriter(outStream);
            writer.write(data);
            writer.flush();
            writer.close();

            loadData();
        } catch(Exception e){
            Log.e("Slang", e.getMessage());
            Log.e("Slang", "Could not save data to internal storage");
        }




    }



}
