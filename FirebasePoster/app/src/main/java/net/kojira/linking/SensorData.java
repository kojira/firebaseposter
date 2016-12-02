package net.kojira.linking;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SensorData {
    public static final int SENSOR_TYPE_ACCELEROMETER = 1;
    public static final int SENSOR_TYPE_TEMPERATURE = 2;
    public static final int SENSOR_TYPE_HUMIDITY = 3;
    public static final int SENSOR_TYPE_TEMPERATURE_HUMIDITY = 4;

    public String data;
    public int sensorType;
    public long timeStamp;
    public float x;
    public float y;
    public float z;
    public float temperature;
    public float humidity;

    public SensorData() {
    }

    public SensorData(String data) {
        this.data = data;
    }

    public String toString() {
        String string = new String();
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date(timeStamp);
        switch (sensorType) {
            case SENSOR_TYPE_ACCELEROMETER:
                string = String.format("datetime:%s\nx:%f\ny:%f\nz:%f", df.format(date), z, y, z);
                break;
            case SENSOR_TYPE_TEMPERATURE:
                string = String.format("datetime:%s\ntemperature:%f℃", df.format(date), temperature);
                break;
            case SENSOR_TYPE_HUMIDITY:
                string = String.format("datetime:%s\nhumidity:%f%", df.format(date), humidity);
                break;
            case SENSOR_TYPE_TEMPERATURE_HUMIDITY:
                string = String.format("datetime:%s\ntemperature:%f℃\nhumidity:%f%", df.format(date), temperature, humidity);
                break;
        }
        return string;
    }
}
