package com.sw.yzmin.testframework;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.PttPlatforms;
import android.app.PttPlatformsManager;
import android.app.backup.BackupManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

import sw.iot.droid.mylibrary.Simware;


public class MainActivity extends Activity {
    private TextView imsiTv;
    private final static int REFRESH = 0x1000;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == REFRESH) {
                StringBuffer buf = (StringBuffer) msg.obj;
                imsiTv.setText(buf);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Log.d("YZMIN","sha1 = " + sha1(this) + " ,vul = " + Simware.getVul());
        //通话音量
        /*
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int max = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_VOICE_CALL );
        int current = mAudioManager.getStreamVolume( AudioManager.STREAM_VOICE_CALL );
        Log.d("YZMIN", "max : " + max + " current : " + current);*/
        //test();

    }

    private void showListDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(R.string.app_name);

        //    指定下拉列表的显示数据
        final String[] cities = {this.getString(R.string.app_name), this.getString(R.string.app_name)+2};
        builder.setItems(cities, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "您选择的城市是:" + cities[which], Toast.LENGTH_SHORT).show();
                switch(which){
                    case 1:
                        break;
                    case 2:
                        break;
                }

            }
        });
        //    显示出该对话框
        final AlertDialog dialog = builder.create();

    }

    public void downloadApk() {
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        String apkUrl = "https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk";
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
        request.setDestinationInExternalPublicDir("dirType", "/mydownload/QQ.apk");
        // request.setTitle("TX QQ");
        // request.setDescription("This is TX QQ");
        // request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        //request.setMimeType("application/cn.trinea.download.file");

        long downloadId = downloadManager.enqueue(request);
        Log.d("YZMIN","downloadId = " + downloadId);
        Uri uri = downloadManager.getUriForDownloadedFile(downloadId);
        Log.d("YZMIN","uri = " + ((uri!=null)?uri.toString():"null"));
        //Toast.makeText(this,"uri = " + uri.toString(),Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.simware.action.SOS_LONG_PRESS");
        intentFilter.addAction("com.simware.action.SOS_SINGLE_PRESS");
        intentFilter.addAction("com.simware.action.ACTION_SOS_SINGLE_PRESS");
        intentFilter.addAction("com.simware.action.ACTION_SOS_LONG_PRESS");
        intentFilter.addAction("cn.simware.action.LOCK_SCREEN_DOWN_F2");
        intentFilter.addAction("cn.simware.action.LOCK_SCREEN_UP_F2");
        intentFilter.addAction("com.simware.action.KEYCODE_1");
        SosBroadcast sosBroadcast = new SosBroadcast();
        registerReceiver(sosBroadcast, intentFilter);
    }

    public String getVersion() {
        return Build.DISPLAY;
    }

    public static String sha1(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            String result = hexString.toString();
            return result.substring(0, result.length() - 1);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    class CellId extends Handler {
        TelephonyManager tel;
        int mRetry = 0;
        int cellid = -1;
        private Context mContext;

        public CellId(Context context) {
            mContext = context;
            tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        }

        public void start() {
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }

        public void stop() {
            mRetry = 0;
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            CellLocation cel = tel.getCellLocation();
            //移动联通 GsmCellLocation
            if (null != cel) {
                if (cel instanceof GsmCellLocation) {
                    GsmCellLocation gsmCellLocation = (GsmCellLocation) cel;
                    cellid = gsmCellLocation.getCid();
                } else if (cel instanceof CdmaCellLocation) {
                    //电信   CdmaCellLocation
                    CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cel;
                    cellid = cdmaCellLocation.getBaseStationId();
                }
            }
            if (cellid > 0) {
                Toast.makeText(mContext, "CID = " + cellid, Toast.LENGTH_SHORT).show();
                stop();
                return;
            } else if (++mRetry >= 3) {
                mRetry = 0;
                Toast.makeText(mContext, "fail", Toast.LENGTH_LONG).show();
                return;
            } else if (cellid == -1) {
                tel.enableLocationUpdates();
            }

            sendEmptyMessageDelayed(0, 1 * 1000);
        }
    }

    public static String getCELLID(Context context) {
        int cellid = -1;
        TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        CellLocation cel = tel.getCellLocation();
        //移动联通 GsmCellLocation
        if (null != cel) {
            if (cel instanceof GsmCellLocation) {
                GsmCellLocation gsmCellLocation = (GsmCellLocation) cel;
                cellid = gsmCellLocation.getCid();
            } else if (cel instanceof CdmaCellLocation) {
                //电信   CdmaCellLocation
                CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cel;
                cellid = cdmaCellLocation.getBaseStationId();
            }
        }
        return "" + cellid;
    }

    public static int getCELLIDs(Context context) {
        TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        List<CellInfo> infoLists = tel.getAllCellInfo();
        if (null != infoLists && infoLists.size() != 0) {
            for (CellInfo info : infoLists) {
                /** 1、GSM是通用的移动联通电信2G的基站。
                 2、CDMA是3G的基站。
                 3、LTE，则证明支持4G的基站。*/
                if (info instanceof CellInfoLte) {
                    CellInfoLte cellInfoLte = (CellInfoLte) info;
                    CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
                    Log.d("YZM", "LTE CID = " + cellIdentityLte.getCi());
                    int cid = cellIdentityLte.getCi();
                    if (cid != Integer.MAX_VALUE && cellIdentityLte.getCi() > 0) {
                        return cid;
                    }
                } else if (info instanceof CellInfoCdma) {
                    CellInfoCdma cellInfoCdma = (CellInfoCdma) info;
                    CellIdentityCdma cellIdentityCdma = cellInfoCdma.getCellIdentity();
                    Log.d("YZM", "CDMA CID = " + cellIdentityCdma.getBasestationId());
                } else if (info instanceof CellInfoGsm) {
                    CellInfoGsm cellInfoGsm = (CellInfoGsm) info;
                    CellIdentityGsm cellIdentityGsm = cellInfoGsm.getCellIdentity();
                    Log.d("YZM", "GSM CID = " + cellInfoGsm.getCellIdentity());
                } else if (info instanceof CellInfoWcdma) {
                    CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) info;
                    CellIdentityWcdma cellIdentityWcdma = cellInfoWcdma.getCellIdentity();
                    Log.d("YZM", "WCDMA CID = " + cellInfoWcdma.getCellIdentity());
                }
            }
        }

        return Integer.MAX_VALUE;
    }


    public void onclick(View view) {
        showListDialog();

        //startActivity(new Intent(this,DownloadActivity.class));
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                downloadApk();
            }
        }).start();*/

        //Toast.makeText(this, "CID = " + getCELLIDs(this), Toast.LENGTH_SHORT).show();
        //new CellId(this).start();

        /*try {
            File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/download");
            File file = new File(f,"file.txt");
            Log.i("feng", "------------tyr to write :"+file.getAbsolutePath());
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(new byte[40]);
            outputStream.close();
            Log.i("feng", "------------write success");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }*/


        //changeLanguage(Locale.US);
        //serdialog();
        //test();
        /*PackageManager packageManager = this.getPackageManager();
        Intent intent= packageManager.getLaunchIntentForPackage("sw.iot.droid.sms");
        startActivity(intent);
        String str = "zymin";*/


        /*Intent intent = new Intent();
        ComponentName comp = new ComponentName("sw.iot.droid.sms", "sw.iot.droid.sms.MainActivity");
        intent.setComponent(comp);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);*/
    }

    public void changeLanguage(Locale locale) {
        try {
            Class clzIActivityManager = Class.forName("android.app.IActivityManager");
            Class clzActivityManagerNative = Class.forName("android.app.ActivityManagerNative");
            Method mtdActivityManagerNative$getDefault = clzActivityManagerNative.getDeclaredMethod("getDefault");
            Object objIActivityManager = mtdActivityManagerNative$getDefault.invoke(clzActivityManagerNative);
            Method mtdIActivityManager$getConfiguration = clzIActivityManager.getDeclaredMethod("getConfiguration");
            Configuration config = (Configuration) mtdIActivityManager$getConfiguration.invoke(objIActivityManager);
            config.locale = locale;
            Class clzConfiguration = Class.forName("android.content.res.Configuration");
            java.lang.reflect.Field userSetLocale = clzConfiguration.getField("userSetLocale");
            userSetLocale.set(config, true);
            //需要声明权限<uses-permission android:name="android.permission.CHANGE_CONFIGURATION" />
            Class[] clzparams = {Configuration.class};
            Method mtdIActivityManager$updateConfiguration = clzIActivityManager.getDeclaredMethod("updateConfiguration", clzparams);
            mtdIActivityManager$updateConfiguration.invoke(objIActivityManager, config);
            BackupManager.dataChanged("com.android.providers.settings");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void test1() {
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.simware.qcomgpio");
        if (null != intent) {
            startActivity(intent);
        }
    }

    private void test() {
        imsiTv = (TextView) findViewById(R.id.imsi_tv);
        //PttPlatformsManager pttPlatformsManager = (PttPlatformsManager) getSystemService(Context.PTT_SERVICE);
        //PttPlatforms list = pttPlatformsManager.getPttPlatforms("POC");
        //Log.d("MainActivity",list.toString());

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String[] imsi = telephonyManager.getSubscriberIds();
        StringBuffer imsis = new StringBuffer("imsi:\n");
        if (null != imsi) {
            for (int i = 0; i < imsi.length; ++i) {
                Log.d("MainActivity", imsi[i]);
                imsis.append(imsi[i] + "\n");
            }
            Log.d("MainActivity", "------------------------------");
        } else {
            Log.d("MainActivity", "imsi[] = null");
            Log.d("MainActivity", "imsi = " + telephonyManager.getSubscriberId());
            imsis.append(telephonyManager.getSubscriberId() + "\n");
        }
        Message msg = Message.obtain(handler, REFRESH, imsis);
        handler.sendMessage(msg);
    }

    public void serdialog() {
        new AlertDialog.Builder(this).setTitle("提示").setMessage("当前未登录,是否进入登录页面").show();
    }


    public static class SosBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("com.simware.action.SOS_LONG_PRESS")) {
                Log.d("YZMIN", "------------> SOS_LONG_PRESS");
            } else if (action.equals("com.simware.action.SOS_SINGLE_PRESS")) {
                Log.d("YZMIN", "------------> SOS_SINGLE_PRESS <------------");
            } else if (action.equals("com.simware.action.ACTION_SOS_LONG_PRESS")) {
                Log.d("YZMIN", "-------------> ACTION_SOS_LONG_PRESS -------");
            } else if (action.equals("com.simware.action.ACTION_SOS_SINGLE_PRESS")) {
                Log.d("YZMIN", "ACTION_SOS_SINGLE_PRESS");
            } else if (action.equals("cn.simware.action.LOCK_SCREEN_DOWN_F2")) {
                Toast.makeText(context, "action = " + action, Toast.LENGTH_SHORT).show();
            } else if (action.equals("cn.simware.action.LOCK_SCREEN_UP_F2")) {
                Toast.makeText(context, "action = " + action, Toast.LENGTH_SHORT).show();
            } else if (action.equals("com.simware.action.KEYCODE_1")) {
                Toast.makeText(context, "action = " + action, Toast.LENGTH_SHORT).show();
            }
        }
    }


}
