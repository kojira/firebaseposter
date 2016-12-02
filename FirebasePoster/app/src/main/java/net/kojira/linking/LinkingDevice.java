package net.kojira.linking;

public class LinkingDevice {
    private String name;
    private String address;
    private int feature;

    public LinkingDevice(String name, String address, int feature) {
        this.name = name;
        this.address = address;
        this.feature = feature;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getFeature() {
        return feature;
    }

    public void setFeature(int feature) {
        this.feature = feature;
    }
}
