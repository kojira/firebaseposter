package net.kojira.firebaseposter.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;

import net.kojira.linking.LinkingDevice;
import net.kojira.linking.SensorData;
import net.kojira.linking.SensorDataRetrieverFactory;
import net.kojira.linking.retrievers.SensorDataRetriever;
import net.kojira.util.L;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PosterService extends Service {
    private static final String INTENT_ACTION_ALARM = "ALARM";
    static boolean started = false;
    private static String mFirebaseUrl;
    private static String mRootKey;
    private final IBinder mBinder = new LocalBinder();
    private boolean scanning = false;
    private List<SensorDataRetriever> mSensorDataRetrieverList = new ArrayList<>();
    private List<LinkingDevice> mDeviceAddressList;

    private Firebase mFirebaseRef;
    private SensorDataListener mSensorDataListener;
    private ChildEventListener mChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            L.i("onChildAdded:" + s + " snap:key:" + dataSnapshot.getKey() + " value:" + dataSnapshot.getValue());
            if (dataSnapshot.hasChildren()) {
                Iterable<DataSnapshot> child = dataSnapshot.getChildren();
                String dataText = new String();
                int i = 0;
                for (DataSnapshot item : child) {
                    L.i("snap:key:" + item.getKey() + " value:" + item.getValue());
                    if (i > 0) {
                        dataText = dataText.concat("\n");
                    }
                    dataText = dataText.concat(item.getKey().concat(":").concat(String.valueOf(item.getValue())));
                    i++;
                }
                if (mSensorDataListener != null) {
                    mSensorDataListener.onSensorData(new SensorData(dataText));
                }
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            L.i("onChildChanged:" + s + " snap:key:" + dataSnapshot.getKey() + " value:" + dataSnapshot.getValue());
            if (dataSnapshot.hasChildren()) {
                Iterable<DataSnapshot> child = dataSnapshot.getChildren();
                for (DataSnapshot item : child) {
                    L.e("snap:key:" + item.getKey() + " value:" + item.getValue());
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            L.i("onChildRemoved:" + dataSnapshot.toString());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            L.i("onChildMoved:" + s);
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            L.i("onCancelled:" + firebaseError.toString());
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.i("start");
        Firebase.setAndroidContext(this);
        started = true;

        if (intent != null && intent.getAction() != null && intent.getAction().equals(INTENT_ACTION_ALARM)) {
            requestSensorData(mFirebaseUrl, mRootKey, mDeviceAddressList);
        }

        return START_NOT_STICKY;
    }

    private void requestSensorData(String url, String key, List<LinkingDevice> deviceList) {
        if (mFirebaseUrl != null && !mFirebaseUrl.equals(url)) {
            mFirebaseRef = new Firebase(mFirebaseUrl);
        }
        mFirebaseUrl = url;
        mRootKey = key;
        mDeviceAddressList = deviceList;
        mSensorDataRetrieverList.clear();
        for (LinkingDevice device : mDeviceAddressList) {
            final SensorDataRetriever sensorDataRetriever = SensorDataRetrieverFactory.createSensorDataRetriever(this, device);
            if (sensorDataRetriever != null) {
                mSensorDataRetrieverList.add(sensorDataRetriever);
                sensorDataRetriever.requestSensorData(new SensorDataRetriever.SensorDataListener() {
                    @Override
                    public void onSensorData(SensorData data) {
                        if (mFirebaseRef == null) {
                            mFirebaseRef = new Firebase(mFirebaseUrl);
                        }
                        Map<String, Object> post = new HashMap<String, Object>();
                        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date(data.timeStamp);
                        post.put("datetime", df.format(date));
                        post.put("timestamp", data.timeStamp);
                        String path = mRootKey;
                        switch (data.sensorType) {
                            case SensorData.SENSOR_TYPE_ACCELEROMETER:
                                post.put("x", data.x);
                                post.put("y", data.y);
                                post.put("z", data.z);
                                break;
                            case SensorData.SENSOR_TYPE_TEMPERATURE_HUMIDITY:
                                post.put("temperature", data.temperature);
                                post.put("humidity", data.humidity);
                                sensorDataRetriever.stopRetrieveSensorData();
                                stopAlarm();
                                startAlarm();
                                break;
                            case SensorData.SENSOR_TYPE_HUMIDITY:
                                post.put("humidity", data.humidity);
                                sensorDataRetriever.stopRetrieveSensorData();
                                stopAlarm();
                                startAlarm();
                                break;
                            case SensorData.SENSOR_TYPE_TEMPERATURE:
                                post.put("temperature", data.temperature);
                                sensorDataRetriever.stopRetrieveSensorData();
                                stopAlarm();
                                startAlarm();
                                break;
                        }
                        mFirebaseRef.child(path).push().setValue(post);
                    }

                    @Override
                    public void onStop() {

                    }
                });
            }
        }
    }

    public void startGetSensorData(String url, String key, List<LinkingDevice> deviceList) {
        scanning = true;

        requestSensorData(url, key, deviceList);
    }

    public void stopGetSensorData() {
        scanning = false;

        stopSensorData();
    }

    public boolean getScanning() {
        return scanning;
    }

    private void stopSensorData() {
        scanning = false;

        for (SensorDataRetriever retriever : mSensorDataRetrieverList) {
            retriever.stopRetrieveSensorData();
        }
    }

    private void startAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 30000, createAlarmIntent());
    }

    private void stopAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(createAlarmIntent());
    }

    private PendingIntent createAlarmIntent() {
        Intent intent = new Intent(this, this.getClass());
        intent.setAction(INTENT_ACTION_ALARM);
        PendingIntent alarmSender = PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        return alarmSender;
    }

    public void getSensorData(int limit, SensorDataListener listener, String firebaseUrl, String rootKey) {
        mFirebaseUrl = firebaseUrl;
        mRootKey = rootKey;
        mSensorDataListener = listener;

        if (mFirebaseRef == null) {
            mFirebaseRef = new Firebase(mFirebaseUrl);
        }

        Query recentPostsQuery = mFirebaseRef.child(mRootKey).limitToLast(limit);
        recentPostsQuery.removeEventListener(mChildEventListener);
        recentPostsQuery.addChildEventListener(mChildEventListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSensorData();
    }

    public interface SensorDataListener {
        void onSensorData(SensorData data);
    }

    public class LocalBinder extends Binder {
        public PosterService getService() {
            return PosterService.this;
        }
    }
}
