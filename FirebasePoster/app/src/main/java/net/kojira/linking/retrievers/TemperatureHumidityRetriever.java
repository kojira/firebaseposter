package net.kojira.linking.retrievers;

import android.content.Context;
import android.content.IntentFilter;

import com.nttdocomo.android.sdaiflib.BeaconData;
import com.nttdocomo.android.sdaiflib.BeaconScanner;
import com.nttdocomo.android.sdaiflib.Define;

import net.kojira.linking.BeaconConst;
import net.kojira.linking.ExBeaconReceiver;
import net.kojira.linking.SensorData;

import java.util.Date;

public class TemperatureHumidityRetriever extends SensorDataRetriever {
    private int mCount;
    private BeaconScanner mScanner;
    private ExBeaconReceiver mReceiver;
    private float mPrevTemperature;
    private float mPrevHumidity;
    private SensorDataListener mSensorDataListener;

    public TemperatureHumidityRetriever(Context context, String address) {
        super(context, address);
    }

    @Override
    public void requestSensorData(final SensorDataListener listener) {
        int[] request;
        request = new int[]{
                BeaconConst.SERVICE_ID_TEMP,
                BeaconConst.SERVICE_ID_HUMID
        };
        mCount = 0;
        stopRetrieveSensorData();
        mReceiver = new ExBeaconReceiver();
        mSensorDataListener = listener;

        IntentFilter filter = new IntentFilter();
        filter.addAction(Define.filterBeaconScanResult);
        filter.addAction(Define.filterBeaconScanState);
        mReceiver.setOnBeaconUpdateListener(new ExBeaconReceiver.BeaconUpdateListener() {
            @Override
            public void onBeaconUpdateListener(String address, BeaconData beaconData) {
                mPrevTemperature = beaconData.getTemperature() == null ? mPrevTemperature : beaconData.getTemperature();
                mPrevHumidity = beaconData.getHumidity() == null ? mPrevHumidity : beaconData.getHumidity();
                mCount++;
                if (mCount >= 2 && mPrevTemperature != 0 && mPrevHumidity != 0) {
                    SensorData data = new SensorData();
                    if (beaconData.getTimestamp() > 0) {
                        data.timeStamp = beaconData.getTimestamp();
                    } else {
                        data.timeStamp = (new Date()).getTime();
                    }
                    data.sensorType = SensorData.SENSOR_TYPE_TEMPERATURE_HUMIDITY;
                    data.temperature = mPrevTemperature;
                    data.humidity = mPrevHumidity;
                    listener.onSensorData(data);
                }
            }
        });
        mContext.registerReceiver(mReceiver, filter);
        mScanner = new BeaconScanner(mContext);
        mScanner.startScan(request);

    }

    @Override
    public void stopRetrieveSensorData() {
        if (mSensorDataListener != null) {
            mSensorDataListener.onStop();
        }
        if (mScanner != null) {
            mScanner.stopScan();
            mScanner = null;
        }
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }
}
