package com.example.bezierdrawingapp;

import javafx.scene.layout.Pane;

/**
 * Клас, описує точку на координатній площині
 */
public class Point {

    // Точки на декартовій площмні
    private double xCart;
    private double yCart;

    // Точки на графічній площмні
    private double xGraph;
    private double yGraph;

    public Point(double x, double y) {
        this.xGraph = x;
        this.yGraph = y;
    }

    public Point(double x, double y, int i) {
        this.xCart = x;
        this.yCart = y;
    }

    public double getxCart() {
        return xCart;
    }
    public double getyCart() {
        return yCart;
    }

    public void setxCart(double xCart) {
        this.xCart = xCart;
    }
    public void setyCart(double yCart) {
        this.yCart = yCart;
    }

    public void setxGraph(double xGraph) {
        this.xGraph = xGraph;
    }
    public void setyGraph(double yGraph) {
        this.yGraph = yGraph;
    }

    public double getxGraph() {
        return xGraph;
    }
    public double getyGraph() {
        return yGraph;
    }

    /**
     * Метод обчислення Декартових координат на основі координат полотна
     */
    public void calculationCartesianCoord(Pane graph, int linesFrequency) {
        xCart = (xGraph - graph.getPrefWidth()/2)/linesFrequency*2;
        xCart = (double) Math.round(xCart * 100) /100;
        yCart = -(yGraph - graph.getPrefHeight()/2)/linesFrequency*2;
        yCart = (double) Math.round(yCart * 100) /100;
    }
    /**
     * Метод обчислення координат полотна на основі Декартових
     */
    public void calculationGraphicsCoord(Pane graph, int linesFrequency) {
        xGraph = xCart * linesFrequency / 2 + graph.getPrefWidth() / 2;
        yGraph = -yCart * linesFrequency / 2 + graph.getPrefHeight() / 2;
    }

    @Override
    public String toString() {
        return "Point{x=" + xCart + ", y=" + yCart + '}';
    }
}
