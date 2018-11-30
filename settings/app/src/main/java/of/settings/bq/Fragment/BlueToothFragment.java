package of.settings.bq.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import of.settings.bq.adapter.BluetoothSavedListAdapter;
import of.settings.bq.adapter.BluetoothSearchedListAdapter;
import of.settings.bq.R;
import of.settings.bq.service.BluetoothService;

public class BluetoothFragment extends Fragment {

    final private static String TAG = "BT.BluetoothFragment";

    private TextView btStatus;
    private TextView btname;

    private ImageButton btEnabler;

    private ListView btSavedListView;
    private ListView btSearchedListView;

    private BluetoothSavedListAdapter savedListAdapter;
    private BluetoothSearchedListAdapter searchedListAdapter;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothService.INTENT_TO_ACTIVITY)) {
                String arg1 = intent.getStringExtra(BluetoothService.ARG1);
                String arg2 = intent.getStringExtra(BluetoothService.ARG2);

                Log.d(TAG, "Message from service: " + arg1 + "/" + arg2);

                if (arg1.equals(BluetoothService.BT_STATUS_CHANGE)) {
                    updateViewOnUiThread();
                }
                else if(arg1.equals(BluetoothService.BT_SEARCHED_CHANGE)) {
                    updateViewOnUiThread();
                    if ((arg2 != null) && arg2.equals(BluetoothService.BT_SEARCH_FINISHED)) {
                        sendToSerivce(BluetoothService.BT_START_SCAN, null);
                    }
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothService.INTENT_TO_ACTIVITY);
        getContext().registerReceiver(receiver, filter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        initView(view);

        return view;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");

        updateView();
        initSavedListView();
        initSearchedListView();

        super.onStart();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        getContext().unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void initView(View view) {
        btStatus = view.findViewById(R.id.bt_status);
        btname = view.findViewById(R.id.bt_name);
        btEnabler = view.findViewById(R.id.bt_enabler);
        btSavedListView = view.findViewById(R.id.bt_saved_list);
        btSearchedListView = view.findViewById(R.id.bt_searched_list);

        btEnabler.setOnClickListener(v -> {
            sendToSerivce(BluetoothService.BT_CTRL, null);
            btEnabler.setAlpha(0.5f);
            btEnabler.setEnabled(false);
        });
    }

    private void initSavedListView() {
        btSavedListView.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothService.selectedDevice = (BluetoothDevice) savedListAdapter.getItem(position);
            goToBluetoothConnectFragment(true);
        });

        savedListAdapter = new BluetoothSavedListAdapter(getContext(), BluetoothService.savedList);
        btSavedListView.setAdapter(savedListAdapter);

        btSavedListView.setVisibility(View.VISIBLE);
    }

    private void initSearchedListView() {
        btSearchedListView.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothService.selectedDevice = (BluetoothDevice)searchedListAdapter.getItem(position);
            goToBluetoothConnectFragment(false);
        });

        searchedListAdapter = new BluetoothSearchedListAdapter(getContext(), BluetoothService.searchedList);
        btSearchedListView.setAdapter(searchedListAdapter);

        btSearchedListView.setVisibility(View.VISIBLE);
    }

    private void updateViewOnUiThread() {
        getActivity().runOnUiThread(() -> {
            updateView();
            if (BluetoothService.getBtState() == BluetoothAdapter.STATE_ON) {
                initSavedListView();
                initSearchedListView();
            }
        });
    }

    private void updateView() {
        int bt_status = BluetoothService.getBtState();

        Log.d(TAG, "BT is enabled/" + (bt_status == BluetoothAdapter.STATE_ON));

        if (bt_status == BluetoothAdapter.STATE_OFF) {
            btEnabler.setAlpha(1f);
            btEnabler.setEnabled(true);
            btStatus.setText(getText(R.string.bt_closed));
            btname.setVisibility(View.INVISIBLE);
            btEnabler.setImageResource(R.drawable.bw_button_close);
        }
        else if (bt_status == BluetoothAdapter.STATE_TURNING_OFF) {
            btEnabler.setAlpha(0.5f);
            btEnabler.setEnabled(false);
            btStatus.setText(getText(R.string.bt_closing));
            btname.setVisibility(View.INVISIBLE);
            btEnabler.setImageResource(R.drawable.bw_button_close);
        }
        else if (bt_status == BluetoothAdapter.STATE_ON) {
            btEnabler.setAlpha(1f);
            btEnabler.setEnabled(true);
            btStatus.setText(getText(R.string.bt_opened));
            btname.setVisibility(View.VISIBLE);
            if (BluetoothService.connectedDevice != null) {
                // FIXME:
                btname.setText(BluetoothService.connectedDevice.getName() + "\n" + "已连接");
            }
            btEnabler.setImageResource(R.drawable.bw_button_open);
        }
        else if (bt_status == BluetoothAdapter.STATE_TURNING_ON) {
            btEnabler.setAlpha(0.5f);
            btEnabler.setEnabled(false);
            btStatus.setText(getText(R.string.bt_opening));
            btname.setVisibility(View.INVISIBLE);
            btEnabler.setImageResource(R.drawable.bw_button_open);
        }

        // Show Saved list and Searched List only when BT is enabled
        if (bt_status == BluetoothAdapter.STATE_ON) {
            btSavedListView.setVisibility(View.VISIBLE);
            btSearchedListView.setVisibility(View.VISIBLE);

            // Do discovery
            sendToSerivce(BluetoothService.BT_START_SCAN, null);
        }
        else {
            btSavedListView.setVisibility(View.INVISIBLE);
            btSearchedListView.setVisibility(View.INVISIBLE);
        }
    }

    private void sendToSerivce(String arg1, String arg2) {
        Intent intent = new Intent(BluetoothService.INTENT_TO_SERVICE);

        if (arg1 != null) {
            intent.putExtra(BluetoothService.ARG1, arg1);
            if (arg2 != null) {
                intent.putExtra(BluetoothService.ARG2, arg2);
            }
            getContext().sendBroadcast(intent);
        }
    }

    private void goToBluetoothConnectFragment(boolean isSavedListConnect) {
        BluetoothService.isSavedListConnect = isSavedListConnect;
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.mainFragment, new BluetoothConnectFragment()).commit();
    }
}
