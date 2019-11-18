package in.slanglabs.vpay.controller;

import android.content.Context;
import android.content.Intent;

import in.slanglabs.vpay.activities.MainActivity;
import in.slanglabs.vpay.activities.SendActivity;

public class AppActions {
    private static AppActions _instance;
    public static AppActions getInstance(){
        if (_instance == null){
            _instance = new AppActions();
        }
        return _instance;
    }

    public TransactionStatus sendMoney(String upiId, String name, String amount, String note, Context context){
        Intent sendIntent = new Intent(context, SendActivity.class);
        sendIntent.putExtra("upiId", upiId);
        sendIntent.putExtra("name", name);
        sendIntent.putExtra("amount", amount);
        sendIntent.putExtra("note", note);
        sendIntent.putExtra("mode", "direct");
        context.startActivity(sendIntent);

        //TODO: transaction status is not tracked properly yet...
        //currently, always returns true
        return new TransactionStatus();
    }
}
