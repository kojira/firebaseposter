package net.kojira.linking;

import android.content.Context;

import net.kojira.linking.retrievers.AccelerometerRetriever;
import net.kojira.linking.retrievers.HumidityRetriever;
import net.kojira.linking.retrievers.SensorDataRetriever;
import net.kojira.linking.retrievers.TemperatureHumidityRetriever;
import net.kojira.linking.retrievers.TemperatureRetriever;

public class SensorDataRetrieverFactory {
    static public SensorDataRetriever createSensorDataRetriever(Context context, LinkingDevice device) {
        SensorDataRetriever sensorDataRetriever = null;

        if (DeviceFeature.hasFeature(device.getFeature(), DeviceFeature.ACCELEROMETER)) {
            sensorDataRetriever = new AccelerometerRetriever(context, device.getAddress());
        } else if (DeviceFeature.hasFeature(device.getFeature(), DeviceFeature.HUMIDITY)) {
            if (DeviceFeature.hasFeature(device.getFeature(), DeviceFeature.TEMPERATURE)) {
                sensorDataRetriever = new TemperatureHumidityRetriever(context, device.getAddress());
            } else {
                sensorDataRetriever = new HumidityRetriever(context, device.getAddress());
            }
        } else if (DeviceFeature.hasFeature(device.getFeature(), DeviceFeature.TEMPERATURE)) {
            sensorDataRetriever = new TemperatureRetriever(context, device.getAddress());
        }

        return sensorDataRetriever;
    }
}
