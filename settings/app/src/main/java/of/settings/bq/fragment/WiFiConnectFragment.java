package of.settings.bq.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Method;

import of.settings.bq.adapter.WiFiSavedListAdapter;
import of.settings.bq.adapter.WiFiSearchedListAdapter;
import of.settings.bq.bean.WiFi;
import of.settings.bq.R;
import of.settings.bq.service.WiFiService;
import of.settings.bq.view.KeyboardUtil;

public class WiFiConnectFragment extends Fragment {

    final private static String TAG = "WiFi.ConnectFragment";

    private ImageView backButton;
    private LinearLayout wifiSavedLayout;
    private LinearLayout wifiSearchedLayout;

    private LinearLayout wifiInfoLayout;
    private LinearLayout wifiKeyboardLayout;

    private ListView wifiSavedListView;
    private ListView wifiSearchedListView;

    private TextView wifiConnectSsid;
    private TextView wifiConnectTip;
    private Button wifiConnectAction;
    private Button wifiConnectCancel;

    private EditText passwordText;
    private KeyboardUtil keyboardUtil;

    private WiFiSavedListAdapter savedListAdapter;
    private WiFiSearchedListAdapter searchedListAdapter;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(WiFiService.INTENT_TO_ACTIVITY)) {
                String arg1 = intent.getStringExtra(WiFiService.ARG1);
                String arg2 = intent.getStringExtra(WiFiService.ARG2);

                Log.d(TAG, "Message from service: " + arg1 + "/" + arg2);

                if (arg1.equals(WiFiService.WIFI_STATUS_CHANGE)) {
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
        filter.addAction(WiFiService.INTENT_TO_ACTIVITY);
        getContext().registerReceiver(receiver, filter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifi_connect, container, false);
        initView(view);

        return view;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");

        if (WiFiService.isSavedListConnect) {
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
        backButton = view.findViewById(R.id.wifi_connect_back);
        backButton.setOnClickListener(v -> backToWiFiFragment());

        wifiSavedLayout = view.findViewById(R.id.wifi_connect_saved_layout);
        wifiSearchedLayout = view.findViewById(R.id.wifi_connect_searched_layout);

        wifiSavedListView = view.findViewById(R.id.wifi_connect_saved_list);
        wifiSearchedListView = view.findViewById(R.id.wifi_connect_searched_list);

        wifiConnectSsid = view.findViewById(R.id.wifi_connect_ssid);
        wifiConnectTip = view.findViewById(R.id.wifi_connect_text);
        wifiConnectAction = view.findViewById(R.id.wifi_connect_action);
        wifiConnectCancel = view.findViewById(R.id.wifi_connect_cancel);

        wifiConnectAction.setOnClickListener(v -> doAction(false));
        wifiConnectCancel.setOnClickListener(v -> doAction(true));

        wifiInfoLayout = view.findViewById(R.id.wifi_connect_info_layout);
        wifiKeyboardLayout = view.findViewById(R.id.wifi_connect_keyboard_layout);

        passwordText = view.findViewById(R.id.wifi_connect_password);
        keyboardUtil = new KeyboardUtil(view, R.id.wifi_keyboard_view, passwordText);
        keyboardUtil.setKeyboardListener(() -> {
            // Do action
            Log.d(TAG, "Password: " + passwordText.getText());
            sendToSerivce(WiFiService.WIFI_CONNECT, passwordText.getText().toString());
        });

        // Make sure system soft keyboard did not popup
        if (android.os.Build.VERSION.SDK_INT <= 10) {
            passwordText.setInputType(InputType.TYPE_NULL);
        } else {
            Class<EditText> cls = EditText.class;
            Method method;
            try {
                method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(passwordText, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //wifiInfoLayout.setVisibility(View.VISIBLE);
        //wifiKeyboardLayout.setVisibility(View.GONE);
        wifiInfoLayout.setVisibility(View.GONE);
        wifiKeyboardLayout.setVisibility(View.VISIBLE);

        updateConnectUI();
    }

    private void doAction(boolean isCancel) {
        // User cancel the action, go back to WiFiFragment
        if (isCancel) {
            backToWiFiFragment();
        }

        WiFi selectedWifi = WiFiService.selectedWifi;
        if (WiFiService.isSavedListConnect) {
            if (selectedWifi.getSsid().equals(WiFiService.connectedWifi.getSsid())) {
                //  Disconnect current connected wifi
                Log.d(TAG, "Disconnect " + selectedWifi.getSsid());
                sendToSerivce(WiFiService.WIFI_DISCONNECT, null);
            }
            else {
                if (selectedWifi.getSignal() == -1) {
                    // Delete selected wifi
                    Log.d(TAG, "Delete " + selectedWifi.getSsid());
                    sendToSerivce(WiFiService.WIFI_REMOVE, null);
                }
                else {
                    // Connect selected wifi
                    Log.d(TAG, "Connect " + selectedWifi.getSsid());
                    sendToSerivce(WiFiService.WIFI_CONNECT, null);
                }
            }
        }
        else {
            if (selectedWifi.getSecurity().equals("wpa")) {
                // Need password, pop up the soft keyboard
                wifiInfoLayout.setVisibility(View.GONE);
                wifiKeyboardLayout.setVisibility(View.VISIBLE);
            }
            else if (selectedWifi.getSecurity().equals("")) {
                // Connect selected wifi with no password
                Log.d(TAG, "Connect " + selectedWifi.getSsid());
                sendToSerivce(WiFiService.WIFI_CONNECT, null);
            }
            else {
                // We don't know what to do!
            }
        }
    }

    private void updateConnectUI() {
        WiFi selectedWifi = WiFiService.selectedWifi;

        wifiConnectSsid.setText(selectedWifi.getSsid());
        if (WiFiService.isSavedListConnect) {
            if (selectedWifi.getSsid().equals(WiFiService.connectedWifi.getSsid())) {
                wifiConnectTip.setText(R.string.wifi_disconnect_tip);
                wifiConnectAction.setText(R.string.wifi_disconnect);
            }
            else {
                if (selectedWifi.getSignal() == -1) {
                    wifiConnectTip.setText(R.string.wifi_delete_tip);
                    wifiConnectAction.setText(R.string.wifi_delete);
                }
                else {
                    wifiConnectTip.setText(R.string.wifi_connect_tip);
                    wifiConnectAction.setText(R.string.wifi_connect);
                }
            }

            wifiSavedLayout.setVisibility(View.VISIBLE);
            wifiSearchedLayout.setVisibility(View.GONE);
        }
        else {
            wifiConnectTip.setText(R.string.wifi_connect_tip);
            wifiConnectAction.setText(R.string.wifi_connect);

            wifiSavedLayout.setVisibility(View.GONE);
            wifiSearchedLayout.setVisibility(View.VISIBLE);
        }
    }

    private void initSavedListView() {
        wifiSavedListView.setOnItemClickListener((parent, view, position, id) -> {
            WiFiService.selectedWifi = (WiFi) savedListAdapter.getItem(position);
            updateConnectUI();
        });

        savedListAdapter = new WiFiSavedListAdapter(getContext(), WiFiService.savedList);
        wifiSavedListView.setAdapter(savedListAdapter);
    }

    private void initSearchedListView() {
        wifiSearchedListView.setOnItemClickListener((parent, view, position, id) -> {
            WiFiService.selectedWifi = (WiFi) searchedListAdapter.getItem(position);
            updateConnectUI();
        });

        searchedListAdapter = new WiFiSearchedListAdapter(getContext(), WiFiService.searchedList);
        wifiSearchedListView.setAdapter(searchedListAdapter);
    }

    private void updateViewOnUiThread() {
        getActivity().runOnUiThread(() -> {
            int wifi_status = WiFiService.getWifiState();

            // Show WiFiFragment when WiFi is not enabled
            if (wifi_status != WifiManager.WIFI_STATE_ENABLED) {
                backToWiFiFragment();
            }
            else {
                return;
            }

            // Anything to do?
            if (WiFiService.isSavedListConnect) {
                initSavedListView();
            }
            else {
                initSearchedListView();
            }
        });
    }

    private void sendToSerivce(String arg1, String arg2) {
        Intent intent = new Intent(WiFiService.INTENT_TO_SERVICE);

        if (arg1 != null) {
            intent.putExtra(WiFiService.ARG1, arg1);
            if (arg2 != null) {
                intent.putExtra(WiFiService.ARG2, arg2);
            }
            getContext().sendBroadcast(intent);
        }
    }

    private void backToWiFiFragment() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.mainFragment, new WiFiFragment()).commit();
    }
}
