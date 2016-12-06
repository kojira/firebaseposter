package net.kojira.linking.retrievers;

import android.content.Context;

import com.nttdocomo.android.sdaiflib.ControlSensorData;

import net.kojira.linking.PairingConst;
import net.kojira.linking.SensorData;
import net.kojira.util.L;

public class AccelerometerRetriever extends SensorDataRetriever {
    private ControlSensorData mSensorData;

    private ControlSensorData.SensorDataInterface mReceiver = new ControlSensorData.SensorDataInterface() {
        @Override
        public void onStopSensor(String bdAddress, int type, int reason) {

        }

        @Override
        public void onSensorData(String bdAddress, int type, float x, float y, float z,
                                 byte[] originalData, long time) {
            if (mOnSensorDataListener != null) {
                SensorData data = new SensorData();
                data.sensorType = SensorData.SENSOR_TYPE_ACCELEROMETER;
                data.timeStamp = time;
                data.x = x;
                data.y = y;
                data.z = z;
                mOnSensorDataListener.onSensorData(data);
            }
        }
    };

    public AccelerometerRetriever(Context context, String address) {
        super(context, address);
    }

    @Override
    public void requestSensorData(SensorDataListener listener) {
        L.e(">>>");
        mOnSensorDataListener = listener;
        mSensorData = new ControlSensorData(mContext, new ControlSensorData.SensorRequestInterface() {
            @Override
            public void onStartSensorResult(String s, int i, int i1) {
            }
        }, mReceiver);
        mSensorData.setType(PairingConst.SENSOR_ID_ACCEL); //リクエストするセンサID
        mSensorData.setInterval(1000);        //取得間隔
        mSensorData.setBDaddress(mBdAddress);  //リクエストするペアリングデバイスのBDアドレス
        mSensorData.start();
    }

    @Override
    public void stopRetrieveSensorData() {
        if (mSensorData != null) {
            mSensorData.stop();
            mSensorData.release();
            mSensorData = null;
        }
    }
}
