package com.Guess.Sketch.guess_and_sketch_server.dto;

public class DrawEvent {

    private int prevX;
    private int prevY;
    private int currentX;
    private int currentY;
    private String color;

    public int getPrevX() { return prevX; }
    public void setPrevX(int prevX) { this.prevX = prevX; }

    public int getPrevY() { return prevY; }
    public void setPrevY(int prevY) { this.prevY = prevY; }

    public int getCurrentX() { return currentX; }
    public void setCurrentX(int currentX) { this.currentX = currentX; }

    public int getCurrentY() { return currentY; }
    public void setCurrentY(int currentY) { this.currentY = currentY; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
