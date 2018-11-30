package of.settings.bq.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import of.settings.bq.bean.WiFi;
import of.settings.bq.R;

public class WiFiSearchedListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<WiFi> wiFiList;

    public WiFiSearchedListAdapter(Context context, List<WiFi> lists) {
        this.mInflater = LayoutInflater.from(context);
        this.wiFiList = lists;
    }

    @Override
    public int getCount() {
        return wiFiList.size();
    }

    @Override
    public Object getItem(int position) {
        return wiFiList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.wifi_item, parent, false);
            holder = new ViewHolder();

            holder.choosed = convertView.findViewById(R.id.wifi_saved_item_choosed);
            holder.signal = convertView.findViewById(R.id.wifi_saved_item_signal);
            holder.ssid = convertView.findViewById(R.id.wifi_saved_item_ssid);
            holder.security = convertView.findViewById(R.id.wifi_item_security);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (wiFiList.size() > 0) {
            WiFi wiFi = wiFiList.get(position);

            holder.choosed.setVisibility(View.INVISIBLE);

            switch (wiFi.getSignal()) {
                case 0:
                    holder.signal.setImageResource(R.drawable.bw_wifi_weak);
                    holder.signal.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    holder.signal.setImageResource(R.drawable.bw_wifi_min);
                    holder.signal.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    holder.signal.setImageResource(R.drawable.bw_wifi_strong);
                    holder.signal.setVisibility(View.VISIBLE);
                    break;
                default:
                    holder.signal.setVisibility(View.INVISIBLE);
                    break;
            }
            holder.ssid.setText(wiFi.getSsid());

            if (wiFi.getSecurity().equals("wpa")) {
                holder.security.setImageResource(R.drawable.bw_lock);
                holder.security.setVisibility(View.VISIBLE);
            }
            else if (wiFi.getSecurity().equals("wep")) {
                // Not supported by WiFi now.
            }
            else if (wiFi.getSecurity().equals("")) {
                holder.security.setVisibility(View.INVISIBLE);
            }
        }
        return convertView;
    }

    private class ViewHolder {
        ImageView choosed;
        ImageView signal;
        TextView  ssid;
        ImageView  security;
    }

}