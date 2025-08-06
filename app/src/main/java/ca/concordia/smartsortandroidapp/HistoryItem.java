package ca.concordia.smartsortandroidapp;

public class HistoryItem {
    private String type;
    private String dateTime;
    public HistoryItem() { }

    public HistoryItem(String type, String dateTime, double volume) {
        this.type = type;
        this.dateTime = dateTime;
    }

    public String getType() { return type; }
    public String getDateTime() { return dateTime; }
}
