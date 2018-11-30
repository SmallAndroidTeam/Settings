package of.settings.bq.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import of.settings.bq.bean.WiFi;

public class WiFiService extends Service {

    final private static String TAG = "WiFi.WiFiService";

    /* Identifies an Intent which send to Activity/Service */
    final static public String INTENT_TO_ACTIVITY = "of.settings.bq.wifi.IntentToActivity";
    final static public String INTENT_TO_SERVICE  = "of.settings.bq.wifi.IntentToService";

    /* Identifiers for extra data on Intents broadcast to the Activity */
    final static public String ARG1 = "wifi.arg1";
    final static public String ARG2 = "wifi.arg2";

    /*
     * Activity ==> Service
     */
    final static public String WIFI_CTRL       = "wifi_ctrl";
    final static public String WIFI_REMOVE     = "wifi_remove";
    final static public String WIFI_CONNECT    = "wifi_connect";
    final static public String WIFI_DISCONNECT = "wifi_disconnect";
    final static public String WIFI_START_SCAN = "wifi_start_scan";

    /*
     * Service ==> Activity
     */
    final static public String WIFI_STATUS_CHANGE   = "wifi_status_change";

    /* Used by Activities */
    static public List<WiFi> savedList = new ArrayList<>();
    static public List<WiFi> searchedList = new ArrayList<>();
    static public WiFi connectedWifi = new WiFi();

    static public boolean isSavedListConnect = false;
    static public WiFi selectedWifi = new WiFi();

    /* Local */
    static private WifiManager wifiManager;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(WiFiService.INTENT_TO_SERVICE)) {
                String arg1 = intent.getStringExtra(WiFiService.ARG1);
                String arg2 = intent.getStringExtra(WiFiService.ARG2);

                Log.d(TAG, "Message from activity/framework: " + arg1 + "/" + arg2);

                if (arg1.equals(WiFiService.WIFI_CTRL)) {
                    if ((wifiManager == null) || !wifiManager.setWifiEnabled(!wifiManager.isWifiEnabled())) {
                        // Request cannot applied
                        sendToActivity(WIFI_STATUS_CHANGE, null);
                    }
                } else if (arg1.equals(WiFiService.WIFI_REMOVE)) {
                    if ((wifiManager == null) && (selectedWifi.getSsid().length() > 0)) {
                        List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
                        for (WifiConfiguration configuration : configurations) {
                            if (configuration.SSID.equals("\"" + selectedWifi.getSsid() + "\"")) {
                                wifiManager.removeNetwork(configuration.networkId);
                            }
                        }
                        // FixMe: Notify UI.
                    }
                } else if (arg1.equals(WiFiService.WIFI_CONNECT)) {
                    if ((wifiManager == null) && (selectedWifi.getSsid().length() > 0)) {
                        /* Find in saved list */
                        for (WifiConfiguration configuration : wifiManager.getConfiguredNetworks()) {
                            if (configuration.SSID.equals("\"" + selectedWifi.getSsid() + "\"")) {
                                wifiManager.enableNetwork(configuration.networkId, true);
                                return;
                            }
                        }

                        /* Find in searched list */
                        if (selectedWifi.getSecurity().equals("wpa") && (arg2.length() > 0)) {
                            Log.d(TAG, "Connect to " + selectedWifi.getSsid() + " with password" + arg2);
                            connectWithPassword(selectedWifi.getSsid(), arg2);
                        }
                        else if (selectedWifi.getSecurity().equals("wep")) {
                            // Not supported now.
                        }
                        else if (selectedWifi.getSecurity().equals("")) {
                            Log.d(TAG, "Connect to " + selectedWifi.getSsid() + " with no password");
                            connectNoPassword(selectedWifi.getSsid());
                        }
                        // FixMe: Notify UI.
                    }
                } else if (arg1.equals(WiFiService.WIFI_START_SCAN)) {
                    if (wifiManager == null) {
                        wifiManager.startScan();
                    }
                }

            } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                sendToActivity(WIFI_STATUS_CHANGE, null);
            } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                //Log.d(TAG, "Network state chagne: \n" + netInfo.toString());
                if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (netInfo.isConnected()) {
                        getConnectedInfo();
                    } else {
                        connectedWifi = new WiFi();
                    }
                    getSavedList();
                    getSearchedList();
                    sendToActivity(WIFI_STATUS_CHANGE, null);
                }
            } else if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                Log.d(TAG, "ScanResult available");
                // FixMe: ssid duplicate issue?
                getSearchedList();
                sendToActivity(WIFI_STATUS_CHANGE, null);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            /* in simulator, nothing to do */
            Log.d(TAG, "We're in simulator?");
            return;
        }

        new Thread(() -> {
            if (getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = (cm != null) ? cm.getActiveNetworkInfo() : null;
                if ((networkInfo != null) && networkInfo.isConnected()) {
                    getConnectedInfo();
                }
                // Get saved wifi list
                getSavedList();
            }

            // Notify UI to update
            sendToActivity(WIFI_STATUS_CHANGE, null);
        }).start();

        IntentFilter filter = new IntentFilter();
        filter.addAction(WiFiService.INTENT_TO_SERVICE);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        unregisterReceiver(receiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    static public int getWifiState() {
        if (wifiManager == null) {
            return WifiManager.WIFI_STATE_DISABLED;
        }
        else {
            return wifiManager.getWifiState();
        }
    }

    private void getConnectedInfo() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        connectedWifi.setSsid(wifiInfo.getSSID().replace("\"", ""));
        connectedWifi.setSignal(WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 3));
        connectedWifi.setNetworkId(wifiInfo.getNetworkId());
        connectedWifi.setStatus("已连接");
        //Log.d(TAG, "Connected Info:\n" + connectedWifi.toString());
    }

    private void getSavedList() {
        savedList = new ArrayList<>();

        for (WifiConfiguration configuration : wifiManager.getConfiguredNetworks()) {
            String ssid = configuration.SSID.replace("\"", "");
            WiFi wiFi = new WiFi();
            wiFi.setSsid(configuration.SSID.replace("\"", ""));
            wiFi.setNetworkId(configuration.networkId);

            if (ssid.equals(connectedWifi.getSsid())) {
                wiFi.setSignal(connectedWifi.getSignal());
                wiFi.setStatus("已连接");
            }
            savedList.add(wiFi);
        }
        Collections.sort(savedList, (WiFi w1, WiFi w2) -> w1.getNetworkId() - w2.getNetworkId());
        //Log.d(TAG, "\nSavedList:\n" + savedList.toString());
    }

    private void getSearchedList() {
        searchedList = new ArrayList<>();

        for (ScanResult scanResult : wifiManager.getScanResults()) {
            //Log.d(TAG, scanResult.toString());
            WiFi wiFi = new WiFi();
            wiFi.setSsid(scanResult.SSID.replace("\"", ""));
            if (wiFi.getSsid().equals(connectedWifi.getSsid())) {
                // Update connected wifi and ignore in searched list
                connectedWifi.setSignal(WifiManager.calculateSignalLevel(scanResult.level, 3));
                for (WiFi wf : savedList) {
                    if (wf.getSsid().equals(wiFi.getSsid())) {
                        wf.setSignal(WifiManager.calculateSignalLevel(scanResult.level, 3));
                        break;
                    }
                }
                continue;
            }
            wiFi.setSignal(WifiManager.calculateSignalLevel(scanResult.level, 3));
            if (!TextUtils.isEmpty(scanResult.capabilities)) {
                if (scanResult.capabilities.contains("WPA") || scanResult.capabilities.contains("wpa")) {
                    wiFi.setSecurity("wpa");
                } else if (scanResult.capabilities.contains("WEP") || scanResult.capabilities.contains("wep")) {
                    wiFi.setSecurity("wep");
                } else {
                    wiFi.setSecurity("");
                }
            }
            searchedList.add(wiFi);
        }
        Collections.sort(searchedList, (WiFi w1, WiFi w2) -> w1.getSignal() - w2.getSignal());
        Log.d(TAG, "SearchList:\n" + searchedList.toString());
    }

    private WifiConfiguration connectNoPassword(String ssid) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = "\"" + ssid + "\"";
        configuration.status = WifiConfiguration.Status.ENABLED;

        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        configuration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

        wifiManager.enableNetwork(wifiManager.addNetwork(configuration), true);

        return  configuration;
    }

    private WifiConfiguration connectWithPassword(String SSID, String Password) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = "\"" + SSID + "\"";
        configuration.preSharedKey = "\"" + Password + "\"";
        configuration.hiddenSSID = true;
        configuration.status = WifiConfiguration.Status.ENABLED;
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        configuration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

        wifiManager.enableNetwork(wifiManager.addNetwork(configuration), true);

        return configuration;
    }

    private void sendToActivity(String arg1, String arg2) {
        Intent intent = new Intent(INTENT_TO_ACTIVITY);

        if (arg1 != null) {
            intent.putExtra(ARG1, arg1);
            if (arg2 != null) {
                intent.putExtra(ARG2, arg2);
            }
            sendBroadcast(intent);
        }
    }

}
