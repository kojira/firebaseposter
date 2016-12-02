package net.kojira.linking;

import com.nttdocomo.android.sdaiflib.BeaconData;
import com.nttdocomo.android.sdaiflib.BeaconReceiverBase;

import net.kojira.util.L;

public class ExBeaconReceiver extends BeaconReceiverBase {
    public String mAddress;
    BeaconData receivedData;
    BeaconUpdateListener mBeaconUpdateListener;

    @Override
    protected void onReceiveScanResult(BeaconData beaconData) {
        L.i("onReceiveScanResult");
        receivedData = beaconData;
        if (mBeaconUpdateListener != null) {
            mBeaconUpdateListener.onBeaconUpdateListener(mAddress, beaconData);
        }
    }

    @Override
    protected void onReceiveScanState(int scanState, int detail) {
        String state = "";

        if (scanState == 0) {
            if (detail == 0) {
                state = "スキャン実行中";
            } else {
                state = "エラーが発生しました：" + detail;
            }
        } else {
            if (detail == 0) {
                state = "スキャン要求に失敗しました : " + detail;
            } else if (detail == 1) {
                state = "スキャン要求がタイムアウトしました";
            }
        }
        L.i("onReceiveScanState:" + detail + ":" + state);

    }

    public void setOnBeaconUpdateListener(BeaconUpdateListener listener) {
        L.i("setOnBeaconUpdateListener");
        mBeaconUpdateListener = listener;
    }

    public interface BeaconUpdateListener {
        void onBeaconUpdateListener(String address, BeaconData beaconData);
    }
}

