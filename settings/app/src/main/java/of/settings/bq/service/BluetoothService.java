package of.settings.bq.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BluetoothService extends Service {

    final private static String TAG = "BT.BluetoothService";

    final private int HEADSET_CLIENT = 16;

    /* Identifies an Intent which send to Activity/Service */
    final static public String INTENT_TO_ACTIVITY = "of.settings.bq.bt.IntentToActivity";
    final static public String INTENT_TO_SERVICE  = "of.settings.bq.bt.IntentToService";

    /* Identifiers for extra data on Intents broadcast to the Activity */
    final static public String ARG1 = "bt.arg1";
    final static public String ARG2 = "bt.arg2";

    /*
     * Activity ==> Service
     */
    final static public String BT_CTRL       = "bt_ctrl";
    final static public String BT_BOND       = "bt_bond";
    final static public String BT_REMOVE     = "bt_remove";
    final static public String BT_START_SCAN = "bt_start_scan";

    /*
     * Service ==> Activity
     */
    final static public String BT_STATUS_CHANGE     = "bt_status_change";
    final static public String BT_SEARCHED_CHANGE   = "bt_searched_change";

    /* Arg for BT_SEARCHED_CHANGE */
    final static public String BT_SEARCH_FINISHED   = "bt_search_finished";
    final static public String BT_SEARCH_GOINGON    = "bt_search_goingon";

    /* Used by Activities */
    static public List<BluetoothDevice> savedList = new ArrayList<>();
    static public List<BluetoothDevice> searchedList = new ArrayList<>();
    static public BluetoothDevice connectedDevice = null;

    static public boolean isSavedListConnect = false;
    static public BluetoothDevice selectedDevice = null;

    /* Local */
    /* bluetooth stuff */
    static private BluetoothAdapter bluetoothAdapter = null;

    // Broadcast receiver for all changes to states of various profiles
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = null;

            if (intent.getAction() == null) {
                return;
            }

            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    //intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    Log.d(TAG, "BT Status is " + bluetoothAdapter.getState());
                    if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                        getBondedDevices();
                    }
                    sendToActivity(BluetoothService.BT_STATUS_CHANGE, null);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Log.d(TAG, "Discovery Started");
                    searchedList = new ArrayList<>();
                    //sendToActivity(BluetoothService.BT_SEARCHED_CHANGE, BT_SEARCH_GOINGON);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.d(TAG, "Discovery Finished");
                    sendToActivity(BluetoothService.BT_SEARCHED_CHANGE, BT_SEARCH_FINISHED);
                    break;
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                    int hfp_state = bluetoothAdapter.getProfileConnectionState(HEADSET_CLIENT);
                    Log.d(TAG, "HFP status: " + hfp_state);
                    //if (hfp_state == BluetoothAdapter.STATE_DISCONNECTED)
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    searchedList.add(device);
                    Log.d(TAG, "Device find: " + device.toString());
                    sendToActivity(BluetoothService.BT_SEARCHED_CHANGE, BT_SEARCH_GOINGON);
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    Log.d(TAG, "Bond change ");
                    //device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    if (bondState == BluetoothDevice.BOND_BONDED) {
                        getBondedDevices();
                    }
                    break;
                case BluetoothService.INTENT_TO_SERVICE:
                    String arg1 = intent.getStringExtra(BluetoothService.ARG1);
                    String arg2 = intent.getStringExtra(BluetoothService.ARG2);
                    Log.d(TAG, "Message from activity: " + arg1 + "/" + arg2);

                    if (arg1.equals(BluetoothService.BT_CTRL)) {
                        Log.d(TAG, "BT Ctrl");
                        if (getBtEnabled()) {
                            Log.d(TAG, "Disable bluetooth");
                            bluetoothAdapter.disable();
                        }
                        else {
                            Log.d(TAG, "Enable bluetooth");
                            bluetoothAdapter.enable();
                        }
                    }
                    else if (arg1.equals(BluetoothService.BT_REMOVE)) {
                        Log.d(TAG, "Remove " + arg2);
                        bluetoothAdapter.startDiscovery();
                    }
                    else if (arg1.equals(BluetoothService.BT_BOND)) {
                        Log.d(TAG, "Connect " + arg2);
                        bluetoothAdapter.startDiscovery();
                    }
                    else if (arg1.equals(BluetoothService.BT_START_SCAN)) {
                        Log.d(TAG, "Start discovery");
                        if (!bluetoothAdapter.isDiscovering()) {
                            bluetoothAdapter.startDiscovery();
                        }
                    }
                    break;
                default:
                    Log.e(TAG, "Received unexpected intent, action=" + intent.getAction());
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            /* in simulator, nothing to do */
            return;
        }

        /* The first time get the bluetooth status */
        Log.d(TAG, "Bluetooth is enabled/" + getBtEnabled());

        new Thread(() -> {
            getBondedDevices();
            // Notify UI to update
            sendToActivity(BluetoothService.BT_STATUS_CHANGE, null);
        }).start();

        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothService.INTENT_TO_SERVICE);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
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

    static public boolean getBtEnabled() {
        if (bluetoothAdapter == null) {
            return false;
        }
        else {
            return bluetoothAdapter.isEnabled();
        }
    }

    static public int getBtState() {
        if (bluetoothAdapter == null) {
            return BluetoothAdapter.STATE_OFF;
        }
        else {
            return bluetoothAdapter.getState();
        }
    }

    private void getBondedDevices() {
        savedList = new ArrayList<>();
        if (getBtEnabled()) {
            savedList = new ArrayList<>(bluetoothAdapter.getBondedDevices());
        }
    }

    private void sendToActivity(String arg1, String arg2) {
        Intent intent = new Intent(BluetoothService.INTENT_TO_ACTIVITY);

        if (arg1 != null) {
            intent.putExtra(BluetoothService.ARG1, arg1);
            if (arg2 != null) {
                intent.putExtra(BluetoothService.ARG2, arg2);
            }
            sendBroadcast(intent);
        }
    }
}
