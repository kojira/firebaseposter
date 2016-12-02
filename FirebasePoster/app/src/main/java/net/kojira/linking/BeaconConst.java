package net.kojira.linking;


import com.nttdocomo.android.sdaiflib.BeaconData;

import net.kojira.util.L;

import java.util.Calendar;

public class BeaconConst {

    public static final boolean DBG = false;
    /**
     * intentにサービスIDをセットする際のExtraキー
     **/
    public static final String INTENT_EXTRA_SERVICE_ID = "service_id";
    /**
     * 開発者ボードのビーコン情報を取得するIDです。
     * 他のデバイスのビーコン情報取得時に使用するサービスIDは
     * デバイス仕様書に従った値を参照すること。
     */
    public static final int SERVICE_ID_DEFAULT = 0;     //なし（デバイスIDを取得）
    public static final int SERVICE_ID_TEMP = 1;        //気温を取得
    public static final int SERVICE_ID_HUMID = 2;       //湿度を取得
    public static final int SERVICE_ID_PRESSURE = 3;    //気圧を取得
    public static final int SERVICE_ID_BATTERY = 4;     //要充電フラグ、電池残量を取得
    public static final int SERVICE_ID_BUTTON = 5;      //押下されたボタンのIDを取得
    public static final int SERVICE_ID_RAW = 15;        //Rawデータ（12bit）を取得
    private static final String TAG = "BeaconConst";

    /**
     * 検出されたビーコンから[時間＋デバイスID＋取得したい情報]に変換する
     *
     * @param serviceId ビーコンリクエスト時のサービスID
     * @param data      検出したビーコン情報
     * @return フォーマット後のログ
     */
    public static String getLogFormat(int serviceId, BeaconData data) {

        /** 取得したビーコンから各データを取得 **/
        if (DBG) L.d(
                "ビーコン検出 "
                        + "時間:" + data.getTimestamp()
                        + " ベンダID:" + data.getVendorId()
                        + " デバイス固有ID:" + data.getExtraId()
                        + " RSSI:" + data.getRssi()
                        + " バージョンId:" + data.getVersion()
                        + " 距離:" + data.getDistance()
                        + " TxPower:" + data.getTxPower()
                        + " 温度:" + data.getTemperature()
                        + " 湿度:" + data.getHumidity()
                        + " 気圧:" + data.getAtmosphericPressure()
                        + " 要充電フラグ:" + data.getLowBattery()
                        + " 電力残量:" + data.getBatteryPower()
                        + " ボタンID:" + data.getButtonId()
                        + " rawデータ:" + data.getRawData()
        );

        String log = "";
        String time = timeLogFormat(System.currentTimeMillis());

        switch (serviceId) {
            case SERVICE_ID_DEFAULT:
                log = String.format("%s \nId[%d] RSSI[%d]", time, data.getExtraId(), data.getRssi());
                break;
            case SERVICE_ID_TEMP:
                log = String.format("%s \nId[%d] 気温[%.02f]", time, data.getExtraId(), data.getTemperature());
                break;
            case SERVICE_ID_HUMID:
                log = String.format("%s \nId[%d] 湿度[%.02f]", time, data.getExtraId(), data.getHumidity());
                break;
            case SERVICE_ID_PRESSURE:
                log = String.format("%s \nId[%d] 気圧[%.02f]", time, data.getExtraId(), data.getAtmosphericPressure());
                break;
        }

        return log;
    }

    /**
     * ミリ秒表示で与えられた時間を見やすくフォーマットする
     *
     * @param data System.currentTimeMillis()などで取得された値
     * @return 引数で与えられた時間を[MM-DD hh:mm:ss]に変換した文字列
     */
    public static String timeLogFormat(long data) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(data);
        String time = String.format("%02d-%02d %02d:%02d:%02d",
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
        return time;
    }
}
