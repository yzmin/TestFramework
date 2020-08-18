package com.sw.yzmin.testframework;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Telephony;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Administrator on 2019/6/3.
 */

public class LaunchReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction().toString();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Toast.makeText(context, "boot completed action has got", Toast.LENGTH_LONG).show();
            Intent ootStartIntent = new Intent(context, MainActivity.class);
            ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //context.startActivity(ootStartIntent);
            return;

        }else if(action.equals("com.rchat.apnnode")){
            ContentValues values = new ContentValues();
            // Add a dummy name "Untitled", if the user exits the screen without adding a name but
            // entered other information worth keeping.
            values.put(Telephony.Carriers.NAME, "display");
            values.put(Telephony.Carriers.APN, "name");
            values.put(Telephony.Carriers.PROXY, "proxy");
            values.put(Telephony.Carriers.PORT, "port");
            values.put(Telephony.Carriers.MMSPROXY, "");
            values.put(Telephony.Carriers.MMSPORT, "");
            values.put(Telephony.Carriers.USER, "");
            values.put(Telephony.Carriers.SERVER, "");
            values.put(Telephony.Carriers.PASSWORD, "");
            values.put(Telephony.Carriers.MMSC, "");

            int authtype = 0;
            values.put(Telephony.Carriers.AUTH_TYPE, authtype);

            values.put(Telephony.Carriers.PROTOCOL, 3);
            values.put(Telephony.Carriers.ROAMING_PROTOCOL, 3);

            values.put(Telephony.Carriers.TYPE, "apntype");

            values.put(Telephony.Carriers.MCC, "460");
            values.put(Telephony.Carriers.MNC, "02");

            values.put(Telephony.Carriers.NUMERIC, "460" + "02");
            values.put(Telephony.Carriers.CURRENT, 1);
            values.put(Telephony.Carriers.CARRIER_ENABLED, 1);
            String arg[]={"display"};
            //context.getContentResolver().update(getUri(Telephony.Carriers.CONTENT_URI), values,Telephony.Carriers.NAME+"=?",arg);
            context.getContentResolver().insert(getUri(Telephony.Carriers.CONTENT_URI), values);
            Log.d("YZM",""+values.toString());
        }
    }
    private Uri getUri(Uri uri) {
        return Uri.withAppendedPath(uri, "/subId/" + SubscriptionManager.getDefaultDataSubId());
    }
}
