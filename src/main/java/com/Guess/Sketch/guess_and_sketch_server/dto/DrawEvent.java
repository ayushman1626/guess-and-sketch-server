package com.Guess.Sketch.guess_and_sketch_server.dto;

import java.util.List;

public class DrawEvent {

    private String type; // "start", "draw", "end"

    private List<Point> points; // for draw

    private Integer x; // for start
    private Integer y;

    private String color;
    private Integer size;

    // getters/setters

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

}
