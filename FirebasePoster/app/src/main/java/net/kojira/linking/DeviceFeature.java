package net.kojira.linking;

public class DeviceFeature {
    public static int JAYRO = 1;
    public static int ACCELEROMETER = 2;
    public static int COMPASS = 3;
    public static int BATTERY = 4;
    public static int TEMPERATURE = 5;
    public static int HUMIDITY = 6;
    public static int ATOMOSPHERIC_PRESSURE = 7;

    public static boolean hasFeature(int deviceFeature, int targetFeature) {
        return ((deviceFeature & (1 << targetFeature)) != 0);
    }
}
