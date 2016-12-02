package net.kojira.linking.retrievers;

import android.content.Context;

import net.kojira.linking.SensorData;

public abstract class SensorDataRetriever {
    protected SensorDataListener mOnSensorDataListener;
    protected Context mContext;
    protected String mBdAddress;

    public SensorDataRetriever(Context context, String address) {
        mContext = context;
        mBdAddress = address;
    }

    public abstract void requestSensorData(SensorDataListener listener);

    public abstract void stopRetrieveSensorData();

    public interface SensorDataListener {
        void onSensorData(SensorData data);

        void onStop();
    }
}
