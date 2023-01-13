package it.unibo.stradivarius.wp4temperatureapplication;

public class TemperatureDataPoint {

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    private String timestamp;
    private float value;

    public TemperatureDataPoint(){

    }

    public TemperatureDataPoint(String timestamp, float value) {
        this.timestamp = timestamp;
        this.value = value;
    }


}
