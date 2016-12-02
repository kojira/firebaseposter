package net.kojira.firebaseposter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.kojira.firebaseposter.R;
import net.kojira.linking.SensorData;

import java.util.List;

public class SensorListAdapter extends ArrayAdapter<SensorData> {
    private LayoutInflater inflater;

    public SensorListAdapter(Context context, List<SensorData> list) {
        super(context, 0, list);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.sensor_data_list, parent, false);
        }
        final SensorData data = this.getItem(position);
        TextView tvData1 = (TextView) view.findViewById(R.id.listRow1);
        if (data != null) {
            if (data.data != null) {
                tvData1.setText(data.data);
            }
        }
        return view;
    }
}
