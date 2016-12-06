package net.kojira.firebaseposter.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.nttdocomo.android.sdaiflib.DeviceInfo;
import com.nttdocomo.android.sdaiflib.GetDeviceInformation;

import net.kojira.firebaseposter.R;
import net.kojira.firebaseposter.adapter.SensorListAdapter;
import net.kojira.firebaseposter.constants.PreferenceKeys;
import net.kojira.firebaseposter.service.PosterService;
import net.kojira.linking.DeviceFeature;
import net.kojira.linking.LinkingDevice;
import net.kojira.linking.SensorData;
import net.kojira.util.L;
import net.kojira.util.Prefs;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int[] mCheckBoxIds = {
            R.id.checkAccelerometer,
            R.id.checkHumidity,
            R.id.checkTemperature,
    };
    private static final int[] mFeatures = {
            DeviceFeature.ACCELEROMETER,
            DeviceFeature.HUMIDITY,
            DeviceFeature.TEMPERATURE,
    };
    Button mButtonStart;
    EditText mRootPath;
    List<LinkingDevice> mDeviceList;
    ArrayAdapter<String> mDeviceListAdapter;
    Spinner mDeviceListSpinner;
    private ServiceConnection mConnection;
    private PosterService mService;
    private ListView mSensorList = null;
    private List<SensorData> mSensorDataList = null;
    private SensorListAdapter mSensorListAdapter;
    private List<CheckBox> mCheckBoxList;

    private int mDevicePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButtonStart = (Button) findViewById(R.id.buttonStart);
        mButtonStart.setEnabled(false);
        mButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRootPath.getText().length() <= 0) {
                    Toast.makeText(MainActivity.this, R.string.alert_not_input_path, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!mService.getScanning()) {
                    startScan();
                } else {
                    stopScan();
                }
                if (mService.getScanning()) {
                    mButtonStart.setText(getString(R.string.stop));
                } else {
                    mButtonStart.setText(getString(R.string.start));
                }
            }
        });
        mSensorList = (ListView) findViewById(R.id.sensorList);
        mSensorDataList = new ArrayList<>();
        mSensorListAdapter = new SensorListAdapter(this, mSensorDataList);
        mSensorList.setAdapter(mSensorListAdapter);

        mDeviceListAdapter = new ArrayAdapter<>(this, R.layout.spinner_selected_item);
        mDeviceListAdapter.setDropDownViewResource(R.layout.spinner_item);
        mDeviceListSpinner = (Spinner) findViewById(R.id.spinnerSensorDevice);
        mDeviceListSpinner.setAdapter(mDeviceListAdapter);
        mDeviceList = new ArrayList<>();
        //Linkingペアリングデバイス情報を取得
        GetDeviceInformation deviceInfo = new GetDeviceInformation(this);
        List<DeviceInfo> devices = deviceInfo.getInformation();
        mDeviceListAdapter.add("-------------");
        for (DeviceInfo linkingDevice : devices) {
            L.d("address:" + linkingDevice.getBdaddress());
            L.d("name:" + linkingDevice.getName());
            mDeviceList.add(new LinkingDevice(linkingDevice.getName(), linkingDevice.getBdaddress(), linkingDevice.getFeature()));
            mDeviceListAdapter.add(linkingDevice.getBdaddress() + " " + linkingDevice.getName());
        }
        mDeviceListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDevicePosition = position;
                updateView(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mDevicePosition = 0;
                updateView(0);
            }
        });

        mCheckBoxList = new ArrayList<>();
        for (int i = 0; i < mCheckBoxIds.length; i++) {
            CheckBox checkBox = (CheckBox) findViewById(mCheckBoxIds[i]);
            mCheckBoxList.add(checkBox);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    updateStartButton();
                }
            });
        }
    }

    private void updateView(int position) {
        for (CheckBox check : mCheckBoxList) {
            check.setChecked(false);
            check.setEnabled(false);
        }
        if (position == 0) {
            return;
        }
        LinkingDevice device = mDeviceList.get(position - 1);
        if (DeviceFeature.hasFeature(device.getFeature(), DeviceFeature.ACCELEROMETER)) {
            mCheckBoxList.get(0).setEnabled(true);
        }
        if (DeviceFeature.hasFeature(device.getFeature(), DeviceFeature.HUMIDITY)) {
            mCheckBoxList.get(1).setEnabled(true);
        }
        if (DeviceFeature.hasFeature(device.getFeature(), DeviceFeature.TEMPERATURE)) {
            mCheckBoxList.get(2).setEnabled(true);
        }
    }

    private void updateStartButton() {
        boolean isChecked = false;
        for (CheckBox check : mCheckBoxList) {
            if (check.isChecked()) {
                isChecked = true;
            }
        }
        mButtonStart.setEnabled(isChecked);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mConnection == null || mService == null) {
            mConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    L.i("onServiceConnected");
                    mService = ((PosterService.LocalBinder) service).getService();
                    if (mService.getScanning()) {
                        mButtonStart.setText(getString(R.string.stop));
                    } else {
                        mButtonStart.setText(getString(R.string.start));
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            };
            startService(new Intent(MainActivity.this, PosterService.class));

            bindService(new Intent(MainActivity.this, PosterService.class),
                    mConnection,
                    Context.BIND_AUTO_CREATE
            );
        } else {
            if (mService.getScanning()) {
                mButtonStart.setText(getString(R.string.stop));
            } else {
                mButtonStart.setText(getString(R.string.start));
            }
        }
        mRootPath = (EditText) findViewById(R.id.editRootPath);
        String path = Prefs.getString(this, PreferenceKeys.PREF_KEY_FIREBASE_ROOT_PATH);
        L.i("path:" + path);
        if (path != null) {
            mRootPath.setText(path);
        }


        Button button = (Button) findViewById(R.id.buttonGetData);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRootPath.getText().length() <= 0) {
                    Toast.makeText(MainActivity.this, R.string.alert_not_input_path, Toast.LENGTH_SHORT).show();
                    return;
                }
                mSensorDataList.clear();
                mSensorListAdapter.notifyDataSetChanged();
                mService.getSensorData(10, new PosterService.SensorDataListener() {
                    @Override
                    public void onSensorData(final SensorData data) {
                        L.i("onSensorData:" + data.data);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSensorDataList.add(data);
                                mSensorListAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }, mRootPath.getText().toString());
                Prefs.putString(MainActivity.this, PreferenceKeys.PREF_KEY_FIREBASE_ROOT_PATH, mRootPath.getText().toString());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mConnection != null) {
            unbindService(mConnection);
            mConnection = null;
        }
    }

    private void startScan() {
        if (mService != null) {
            List<LinkingDevice> targetDeviceList = new ArrayList<>();
            LinkingDevice device = mDeviceList.get(mDevicePosition-1);
            int feature = 0;
            for (int i = 0; i < mCheckBoxList.size(); i++) {
                if (mCheckBoxList.get(i).isChecked()) {
                    feature |= (1 << mFeatures[i]);
                }
            }
            //選択したFeatureで上書き
            LinkingDevice targetDevice = new LinkingDevice(device.getName(), device.getAddress(), feature);
            targetDeviceList.add(targetDevice);
            mService.startGetSensorData(mRootPath.getText().toString(), targetDeviceList);
            Prefs.putString(MainActivity.this, PreferenceKeys.PREF_KEY_FIREBASE_ROOT_PATH, mRootPath.getText().toString());
        }
    }

    private void stopScan() {
        if (mService != null) {
            mService.stopGetSensorData();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
