package ca.concordia.smartsortandroidapp;

public class HistoryItem {
    private String type;
    private String dateTime;
    private double volume;

    public HistoryItem() { }

    public HistoryItem(String type, String dateTime, double volume) {
        this.type = type;
        this.dateTime = dateTime;
        this.volume = volume;
    }

    public String getType() { return type; }
    public String getDateTime() { return dateTime; }
    public double getVolume() { return volume; }
}
