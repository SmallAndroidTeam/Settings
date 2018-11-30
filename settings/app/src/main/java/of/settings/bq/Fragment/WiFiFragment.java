package of.settings.bq.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
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

import of.settings.bq.adapter.WiFiSavedListAdapter;
import of.settings.bq.adapter.WiFiSearchedListAdapter;
import of.settings.bq.bean.WiFi;
import of.settings.bq.R;
import of.settings.bq.service.WiFiService;

public class WiFiFragment extends Fragment {

    final private static String TAG = "WiFi.WiFiFragment";

    private TextView wifiStatus;
    private TextView wifiSSID;

    private ImageButton wifiEnabler;

    private ListView wifiSavedListView;
    private ListView wifiSearchedListView;

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
        View view = inflater.inflate(R.layout.fragment_wifi, container, false);
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
        wifiStatus = view.findViewById(R.id.wifi_status);
        wifiSSID = view.findViewById(R.id.wifi_ssid);
        wifiEnabler = view.findViewById(R.id.wifi_enabler);
        wifiSavedListView = view.findViewById(R.id.wifi_saved_list);
        wifiSearchedListView = view.findViewById(R.id.wifi_searched_list);

        wifiEnabler.setOnClickListener(v -> {
            sendToSerivce(WiFiService.WIFI_CTRL, null);
            wifiEnabler.setAlpha(0.5f);
            wifiEnabler.setEnabled(false);
        });
    }

    private void initSavedListView() {
        wifiSavedListView.setOnItemClickListener((parent, view, position, id) -> {
            WiFiService.selectedWifi = (WiFi)savedListAdapter.getItem(position);
            goToWiFiConnectFragment(true);
        });

        savedListAdapter = new WiFiSavedListAdapter(getContext(), WiFiService.savedList);
        wifiSavedListView.setAdapter(savedListAdapter);

        wifiSavedListView.setVisibility(View.VISIBLE);
    }

    private void initSearchedListView() {
        wifiSearchedListView.setOnItemClickListener((parent, view, position, id) -> {
            WiFiService.selectedWifi = (WiFi)searchedListAdapter.getItem(position);
            goToWiFiConnectFragment(false);
        });

        searchedListAdapter = new WiFiSearchedListAdapter(getContext(), WiFiService.searchedList);
        wifiSearchedListView.setAdapter(searchedListAdapter);

        wifiSearchedListView.setVisibility(View.VISIBLE);
    }

    private void updateViewOnUiThread() {
        getActivity().runOnUiThread(() -> {
            updateView();
            if (WiFiService.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                initSavedListView();
                initSearchedListView();
            }
        });
    }

    private void updateView() {
        int wifi_status = WiFiService.getWifiState();

        Log.d(TAG, "Wifi is enabled/" + (wifi_status == WifiManager.WIFI_STATE_ENABLED));

        if (wifi_status == WifiManager.WIFI_STATE_DISABLED) {
            wifiEnabler.setAlpha(1f);
            wifiEnabler.setEnabled(true);
            wifiStatus.setText(getText(R.string.wifi_closed));
            wifiSSID.setVisibility(View.INVISIBLE);
            wifiEnabler.setImageResource(R.drawable.bw_button_close);
        }
        else if (wifi_status == WifiManager.WIFI_STATE_DISABLING) {
            wifiEnabler.setAlpha(0.5f);
            wifiEnabler.setEnabled(false);
            wifiStatus.setText(getText(R.string.wifi_closing));
            wifiSSID.setVisibility(View.INVISIBLE);
            wifiEnabler.setImageResource(R.drawable.bw_button_close);
        }
        else if (wifi_status == WifiManager.WIFI_STATE_ENABLED) {
            wifiEnabler.setAlpha(1f);
            wifiEnabler.setEnabled(true);
            wifiStatus.setText(getText(R.string.wifi_opened));
            wifiSSID.setVisibility(View.VISIBLE);
            if (WiFiService.connectedWifi.getSsid() != null) {
                wifiSSID.setText(WiFiService.connectedWifi.getSsid() + "\n" + WiFiService.connectedWifi.getStatus());
            }
            wifiEnabler.setImageResource(R.drawable.bw_button_open);
        }
        else if (wifi_status == WifiManager.WIFI_STATE_ENABLING) {
            wifiEnabler.setAlpha(0.5f);
            wifiEnabler.setEnabled(false);
            wifiStatus.setText(getText(R.string.wifi_opening));
            wifiSSID.setVisibility(View.INVISIBLE);
            wifiEnabler.setImageResource(R.drawable.bw_button_open);
        }

        // Show Saved list and Searched List only when WiFi is enabled
        if (wifi_status == WifiManager.WIFI_STATE_ENABLED) {
            wifiSavedListView.setVisibility(View.VISIBLE);
            wifiSearchedListView.setVisibility(View.VISIBLE);
        }
        else {
            wifiSavedListView.setVisibility(View.INVISIBLE);
            wifiSearchedListView.setVisibility(View.INVISIBLE);
        }
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

    private void goToWiFiConnectFragment(boolean isSavedListConnect) {
        WiFiService.isSavedListConnect = isSavedListConnect;
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.mainFragment, new WiFiConnectFragment()).commit();
    }
}
