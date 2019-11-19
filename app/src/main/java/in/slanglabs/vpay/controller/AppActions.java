package in.slanglabs.vpay.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.ListView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import in.slanglabs.platform.SlangBuddy;
import in.slanglabs.platform.SlangLocale;
import in.slanglabs.platform.SlangSession;
import in.slanglabs.platform.prompt.SlangMessage;
import in.slanglabs.vpay.activities.SendActivity;

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
        builder.setTitle("Select Customer");
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
                handler.notifyContactResolution(null, session);
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
        return SlangMessage.create(msg, true);
    }

    public TransactionStatus sendMoney(
            String name,
            int amount,
            String notes,
            final SlangSession session,
            final SlangInterface.AppActionListener handler
    ) {
        //TODO: Check "name" for whether it is a phone number or contact name and take action  accordingly.
        Intent sendIntent = new Intent(session.getCurrentActivity(), SendActivity.class);
        sendIntent.putExtra("name", name);
        sendIntent.putExtra("amount", amount);
        sendIntent.putExtra("note", notes);
        sendIntent.putExtra("mode", "direct");
        session.getCurrentActivity().startActivity(sendIntent);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.notifyActionComplete(session);
            }
        }, 10);
        //TODO: transaction status is not tracked properly yet...
        //currently, always returns true
        return new TransactionStatus();
    }

    public TransactionStatus receiveMoney(
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
        return new TransactionStatus();
    }

    private void displayMessage(final String message, final Context context)  {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        }, 10);
    }
}
