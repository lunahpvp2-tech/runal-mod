package com.runal.client;

public class PlayerScaleState {
    public static final PlayerScaleState INSTANCE = new PlayerScaleState();

    public String target = "Self";
    private float xScale = 1.0f;
    private float yScale = 1.0f;
    private float zScale = 1.0f;

    private PlayerScaleState() {}

    public float getScale() { return (xScale + yScale + zScale) / 3.0f; }
    public void setScale(float value) { xScale = value; yScale = value; zScale = value; }
    public float getXScale() { return xScale; }
    public float getYScale() { return yScale; }
    public float getZScale() { return zScale; }
    public void setXScale(float value) { xScale = value; }
    public void setYScale(float value) { yScale = value; }
    public void setZScale(float value) { zScale = value; }
    public boolean isScaled() { return Math.abs(xScale - 1.0f) > 0.01f || Math.abs(yScale - 1.0f) > 0.01f || Math.abs(zScale - 1.0f) > 0.01f; }
}
