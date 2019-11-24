package in.slanglabs.vpay.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import in.slanglabs.platform.SlangBuddy;
import in.slanglabs.platform.SlangLocale;
import in.slanglabs.platform.SlangSession;
import in.slanglabs.platform.prompt.SlangMessage;
import in.slanglabs.vpay.activities.SendActivity;
import in.slanglabs.vpay.model.AppData;
import in.slanglabs.vpay.model.Contact;
import in.slanglabs.vpay.model.PhoneData;

public class AppActions implements SlangInterface.AppActionHandler {
    private static Activity sActivity;
    private static AppActions sInstance;
    public static AppActions getInstance(Activity activity){
        sActivity = activity;
        if (sInstance == null){
            sInstance = new AppActions();
        }
        return sInstance;
    }

    @Override
    public void onVoiceInitialised() {
        SlangBuddy.getBuiltinUI().show(sActivity);
    }

    @Override
    public void resolveContact(
            List<String> customerNames,
            final SlangSession session,
            final SlangInterface.AppActionListener handler
    ) {
        String[] items = new String[customerNames.size()];
        customerNames.toArray(items);
        AlertDialog.Builder builder = new AlertDialog.Builder(session.getCurrentActivity());
        builder.setTitle("Select Contact");
        builder.setSingleChoiceItems(items, 0, null);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                ListView lv = ((AlertDialog) dialog).getListView();
                Object checkedItem = lv.getAdapter().getItem(lv.getCheckedItemPosition());
                String name = String.valueOf(checkedItem);
                handler.notifyContactResolution(name, session);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
                SlangBuddy.getBuiltinUI().cancel();
                try {
                    SlangBuddy.notifyUser(getCancelMessage());
                } catch (SlangBuddy.UninitializedUsageException e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            SlangBuddy.notifyUser(getCustomerMessage());
        } catch (SlangBuddy.UninitializedUsageException ignored) {}
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static SlangMessage getCustomerMessage() {
        Map<Locale, String> msg = new HashMap<>();
        msg.put(SlangLocale.LOCALE_ENGLISH_IN, "Please select the right customer from this list");
        msg.put(SlangLocale.LOCALE_HINDI_IN, "कृपया इस सूची से सही ग्राहक चुनें");
        return SlangMessage.create(msg, true);
    }

    private static SlangMessage getCancelMessage() {
        Map<Locale, String> msg = new HashMap<>();
        msg.put(SlangLocale.LOCALE_ENGLISH_IN, "No problem, click the mic button and let us know whenever you are ready");
        msg.put(SlangLocale.LOCALE_HINDI_IN, "कृपया इस सूची से सही ग्राहक चुनें");
        return SlangMessage.create(msg, true);
    }

    public void sendMoney(
            String name,
            int amount,
            String notes,
            final SlangSession session,
            final SlangInterface.AppActionListener handler
    ) {
        name = getCleanedUpValue(name);
        Log.e("sendMoney", "name:" + name);
        boolean isPhoneNumber = false;
        if (name.matches("^[\\d ]+$")) {
            Log.e("sendMoney", "isNumber");
            String phoneNumber = name.replaceAll("\\D", "");
            if (phoneNumber.matches("^[6-9]\\d{9}$")) {
                Log.e("sendMoney", "isPhoneNumber");
                isPhoneNumber = true;
                name = phoneNumber;
            }
        }

        Intent sendIntent = new Intent(session.getCurrentActivity(), SendActivity.class);
        sendIntent.putExtra("name", name);
        Contact contact = AppData.getInstance().getContactForName(name);
        if (null == contact) contact = PhoneData.getInstance().getContactForName(name);
        if (null != contact) sendIntent.putExtra("upiId", contact.upiId);
        else if (isPhoneNumber) sendIntent.putExtra("upiId", name + "@upi");
        else sendIntent.putExtra("upiId", "");
        sendIntent.putExtra("amount", amount);
        sendIntent.putExtra("note", notes);
        sendIntent.putExtra("mode", "voice");
        session.getCurrentActivity().startActivity(sendIntent);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.notifyActionComplete(session);
            }
        }, 10);
    }

    public void receiveMoney(
            final String name,
            int amount,
            String notes,
            final SlangSession session,
            final SlangInterface.AppActionListener handler
    ) {
        //TODO:
        displayMessage("TODO: Receive from:" + name + ", amount:" + amount + ", notes:" + notes, session.getCurrentActivity());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.notifyContactResolution(name, session);
            }
        }, 10);
    }

    private void displayMessage(final String message, final Context context)  {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        }, 10);
    }

    private static String getCleanedUpValue(String value) {
        String[] parts = value.split("\\s");
        List<String> validWords = new ArrayList<>();
        String[] stopWordsArray = {
                "a","able","about","across","after","all","almost","also","am","among","an","and",
                "any","are","as","at","be","because","been","but","by","can","cannot","could","dear",
                "did","do","does","either","else","ever","every","for","from","get","got","had","has",
                "have","he","her","hers","him","his","how","however","i","if","in","into","is","it",
                "its","just","least","let","like","likely","may","me","might","most","must","my",
                "neither","no","nor","not","of","off","often","on","only","or","other","our","own",
                "rather","said","say","says","she","should","since","so","some","than","that","the",
                "their","them","then","there","these","they","this","tis","to","too","twas","us",
                "wants","was","we","were","what","when","where","which","while","who","whom","why",
                "will","with","would","yet","you","your", "add", "put", "view", "I'm", "myself",
                "name", "address", "door", "street", "city", "district", "state", "pin", "code",
                "phone", "contact", "marital", "religion", "send", "receive", "take", "give", "giving",
                "advance", "advanced", "compensate", "compensated", "credit", "credited", "deposit",
                "desposited", "disburse", "disbursed", "disbursement", "given","loan", "loaned", "paid",
                "pay", "payment", "recompensate", "recompesated", "refund", "refunded", "reimburse",
                "reimbursed", "reimbursement", "remit", "remittance", "remitted", "return", "returned",
                "settle", "settled", "settlement", "transfer", "transfered", "transferring", "debit",
                "get", "receiving","taking"
        };
        Set<String> stopWordsSet = new HashSet<>(Arrays.asList(stopWordsArray));

        for (String part : parts) {
            if (!stopWordsSet.contains(part.trim().toLowerCase())) {
                validWords.add(part);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String word: validWords) {
            sb.append(word).append(" ");
        }
        return sb.toString().trim();
    }
}
