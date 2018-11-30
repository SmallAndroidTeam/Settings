package of.settings.bq.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import of.settings.bq.R;
import of.settings.bq.bean.WiFi;
import of.settings.bq.service.BluetoothService;
import of.settings.bq.service.WiFiService;

public class BluetoothSavedListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<BluetoothDevice> btList;

    public BluetoothSavedListAdapter(Context context, List<BluetoothDevice> lists) {
        this.mInflater = LayoutInflater.from(context);
        this.btList = lists;
    }

    @Override
    public int getCount() {
        return btList.size();
    }

    @Override
    public Object getItem(int position) {
        return btList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.bluetooth_item, parent, false);
            holder = new ViewHolder();

            holder.choosed = convertView.findViewById(R.id.bt_saved_item_choosed);
            holder.name = convertView.findViewById(R.id.bt_saved_item_name);
            holder.arrow = convertView.findViewById(R.id.bt_item_security);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (btList.size() > 0) {
            BluetoothDevice device = btList.get(position);
            if ((BluetoothService.connectedDevice != null) && device.getAddress().equals(BluetoothService.connectedDevice.getAddress())) {
                holder.choosed.setVisibility(View.VISIBLE);
            } else {
                holder.choosed.setVisibility(View.INVISIBLE);
            }
            holder.name.setText(device.getName());
            holder.arrow.setImageResource(R.drawable.bw_arrow);
        }
        return convertView;
    }

    private class ViewHolder {
        ImageView choosed;
        TextView  name;
        ImageView arrow;
    }

}