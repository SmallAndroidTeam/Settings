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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import of.settings.bq.R;
import of.settings.bq.adapter.BluetoothSavedListAdapter;
import of.settings.bq.adapter.BluetoothSearchedListAdapter;
import of.settings.bq.service.BluetoothService;

public class BluetoothConnectFragment extends Fragment {

    final private static String TAG = "BT.ConnectFragment";

    private ImageView backButton;
    private LinearLayout btSavedLayout;
    private LinearLayout btSearchedLayout;

    private LinearLayout btInfoLayout;
    private LinearLayout btKeyboardLayout;

    private ListView btSavedListView;
    private ListView btSearchedListView;

    private TextView btConnectName;
    private TextView btConnectTip;
    private Button btConnectAction;
    private Button btConnectCancel;


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
        View view = inflater.inflate(R.layout.fragment_bluetooth_connect, container, false);
        initView(view);

        return view;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");

        if (BluetoothService.isSavedListConnect) {
            initSavedListView();
        }
        else {
            initSearchedListView();
        }

        super.onStart();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        getContext().unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void initView(View view) {
        backButton = view.findViewById(R.id.bt_connect_back);
        backButton.setOnClickListener(v -> backToBluetoothFragment());

        btSavedLayout = view.findViewById(R.id.bt_connect_saved_layout);
        btSearchedLayout = view.findViewById(R.id.bt_connect_searched_layout);

        btSavedListView = view.findViewById(R.id.bt_connect_saved_list);
        btSearchedListView = view.findViewById(R.id.bt_connect_searched_list);

        btConnectName = view.findViewById(R.id.bt_connect_name);
        btConnectTip = view.findViewById(R.id.bt_connect_text);
        btConnectAction = view.findViewById(R.id.bt_connect_action);
        btConnectCancel = view.findViewById(R.id.bt_connect_cancel);

        btConnectAction.setOnClickListener(v -> doAction(false));
        btConnectCancel.setOnClickListener(v -> doAction(true));

        btInfoLayout = view.findViewById(R.id.bt_connect_info_layout);
        btKeyboardLayout = view.findViewById(R.id.bt_connect_keyboard_layout);

        btInfoLayout.setVisibility(View.VISIBLE);
        btKeyboardLayout.setVisibility(View.GONE);

        updateConnectUI();
    }

    private void doAction(boolean isCancel) {
        // User cancel the action, go back to BluetoothFragment
        if (isCancel) {
            backToBluetoothFragment();
        }

        BluetoothDevice selectedDevice = BluetoothService.selectedDevice;
        if (BluetoothService.isSavedListConnect) {
            if (selectedDevice.getAddress().equals(BluetoothService.connectedDevice.getAddress())) {
                // Remove selected bt
                Log.d(TAG, "Remove " + selectedDevice.getName());
                sendToSerivce(BluetoothService.BT_REMOVE, null);
            }
            else {
                // Bond selected bt
                Log.d(TAG, "Bond " + selectedDevice.getName());
                sendToSerivce(BluetoothService.BT_BOND, null);
            }
        }
        else {
            // Bond selected bt
            Log.d(TAG, "Bond " + selectedDevice.getName());
            sendToSerivce(BluetoothService.BT_BOND, null);
        }
    }

    private void updateConnectUI() {
        BluetoothDevice selectedDevice = BluetoothService.selectedDevice;

        btConnectName.setText(selectedDevice.getName());
        if (BluetoothService.isSavedListConnect) {
            btConnectTip.setText(R.string.bt_remove_tip);
            btConnectAction.setText(R.string.bt_remove);

            btSavedLayout.setVisibility(View.VISIBLE);
            btSearchedLayout.setVisibility(View.GONE);
        }
        else {
            btConnectTip.setText(R.string.bt_bond_tip);
            btConnectAction.setText(R.string.bt_bond);

            btSavedLayout.setVisibility(View.GONE);
            btSearchedLayout.setVisibility(View.VISIBLE);
        }
    }

    private void initSavedListView() {
        btSavedListView.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothService.selectedDevice = (BluetoothDevice) savedListAdapter.getItem(position);
            updateConnectUI();
        });

        savedListAdapter = new BluetoothSavedListAdapter(getContext(), BluetoothService.savedList);
        btSavedListView.setAdapter(savedListAdapter);
    }

    private void initSearchedListView() {
        btSearchedListView.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothService.selectedDevice = (BluetoothDevice) searchedListAdapter.getItem(position);
            updateConnectUI();
        });

        searchedListAdapter = new BluetoothSearchedListAdapter(getContext(), BluetoothService.searchedList);
        btSearchedListView.setAdapter(searchedListAdapter);
    }

    private void updateViewOnUiThread() {
        getActivity().runOnUiThread(() -> {
            int bt_status = BluetoothService.getBtState();

            // Show BluetoothFragment when bt is not enabled
            if (bt_status != BluetoothAdapter.STATE_ON) {
                backToBluetoothFragment();
            }
            else {
                return;
            }

            // Anything to do?
            if (BluetoothService.isSavedListConnect) {
                initSavedListView();
            }
            else {
                initSearchedListView();
            }
        });
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

    private void backToBluetoothFragment() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.mainFragment, new BluetoothFragment()).commit();
    }
}