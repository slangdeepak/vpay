package in.slanglabs.vpay.model;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

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
    private Map<String, Contact> appContacts;
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

    public Collection<Contact> getAppContacts() {
        return appContacts.values();
    }

    public Set<String> getContactNames() {
        return appContacts.keySet();
    }

    public Contact getContactForName(String name){
        return appContacts.get(name);
    }

    public boolean addContact(String name, String upiId){
        if (appContacts.containsKey(name) && upiId.equals(appContacts.get(name).upiId)) return false;
        appContacts.put(name, new Contact(name, upiId));
        saveData();
        return true;
    }

    public boolean editContact(String oldName, String oldUpiId, String newName, String newUpiId){
        Contact candidate = appContacts.get(oldName);
        if (candidate != null){
            candidate.name = newName;
            candidate.upiId = newUpiId;
            appContacts.remove(oldName);
            appContacts.put(newName, candidate);
            saveData();
            return true;
        } else {
            return false;
        }
    }

    public boolean deleteContact(String name, String upiId){
        if (appContacts.containsKey(name)){
            appContacts.remove(name);
            saveData();
            return true;
        } else {
            return false;
        }
    }

    public void loadData() {
        //temp init
        userUpiId = "9901299770@upi";
        appContacts = new HashMap<>();
        appContacts.put("Phaniraj Raghavendra", new Contact("Phaniraj Raghavendra", "phanir@okhdfcbank"));
        appContacts.put("Ved Mathai", new Contact("Ved Mathai", "vedu29@okicici"));
        appContacts.put("Deepak Srinivasa", new Contact("Deepak Srinivasa", "9901299770@upi"));

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
                JSONArray jcontacts = jdata.getJSONArray("appContacts");
                appContacts = new HashMap<>();
                for (int i = 0; i < jcontacts.length(); i++) {
                    JSONObject jcontact = jcontacts.getJSONObject(i);
                    Contact contact = new Contact(jcontact.getString("name"), jcontact.getString("upiId"));
                    appContacts.put(jcontact.getString("name"), contact);
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
            for (String name : appContacts.keySet()){
                JSONObject jcontact = new JSONObject();
                jcontact.put("name", appContacts.get(name).name);
                jcontact.put("upiId", appContacts.get(name).upiId);
                jcontacts.put(jcontact);
            }
            result.put("appContacts", jcontacts);
            String data = result.toString(4);
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
