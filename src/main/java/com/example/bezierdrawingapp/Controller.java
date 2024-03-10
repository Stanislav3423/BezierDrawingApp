package com.example.bezierdrawingapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    /**
     * Ціна поділки у пікселях
     */
    private int LINES_FREQUENCY = 40;

    /**
     * Натиск ліній графікічної площини
     */
    private final double MIN_LINES_STROKE = 0.2;

    @FXML
    private Label CoordinateLabel;

    @FXML
    private Pane Graph;

    @FXML
    private Pane Graph1;

    @FXML
    private AnchorPane GraphPane;

    @FXML
    private AnchorPane ManagementPane;

    @FXML
    private Button cancelButton;

    @FXML
    private Button clearButton;

    @FXML
    private ColorPicker colorChooser;

    @FXML
    private Button createButton;

    @FXML
    private Button drawAutoButton;

    @FXML
    private Button fillButton;

    @FXML
    private TextField p0x;

    @FXML
    private TextField p0y;

    @FXML
    private Button saveButton;

    @FXML
    private TextField aTf;

    @FXML
    private TextField bTf;

    @FXML
    private TextField stepTf;

    private List<Point> pointList;
    private List<Circle> circleList;
    private boolean isDragging = false;

    /**
     * Функція для ініціалізації початкових значень та викликів функцій під час запуску вікна
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointList = new ArrayList<>();
        circleList = new ArrayList<>();
        drawGraph();
        addMouseHoverEvent();
        addMouseClickEvent();
        autoFill();
    }

    private void drawBezierCurveByParameters() {
        Canvas canvasBezier = new Canvas(Graph.getPrefWidth(), Graph.getPrefHeight());
        Canvas canvasPolygon = new Canvas(Graph.getPrefWidth(), Graph.getPrefHeight());
        Graph.getChildren().addAll(canvasBezier, canvasPolygon);
        GraphicsContext gcBezier = canvasBezier.getGraphicsContext2D();
        GraphicsContext gcPolygon = canvasPolygon.getGraphicsContext2D();
        drawBezierCurve(gcBezier, gcPolygon);
    }

    public void autoFill(){
        aTf.setText(0+"");
        bTf.setText(1+"");
        stepTf.setText(0.001 +"");
    }

    @FXML
    void onAutoDrawButtonClick(ActionEvent event) {

    }

    @FXML
    void onCancelButtonClick(ActionEvent event) {

    }

    @FXML
    void onClearButtonClick(ActionEvent event) {

    }

    @FXML
    void onCreateButtonClicked(ActionEvent event) {

    }

    @FXML
    void onFillButtonClick(ActionEvent event) {

    }

    @FXML
    void onSaveButtonClick(ActionEvent event) {

    }

    /**
     * Функція обробки події наведення курсору мишки на панель
     */
    private void addMouseHoverEvent() {
        Graph.setOnMouseMoved((MouseEvent event) -> {
            CoordinateLabel.setText("");
            double x = event.getX();
            double y = event.getY();
            Point point = new Point(x, y);
            point.calculationCartesianCoord(Graph, LINES_FREQUENCY);
            CoordinateLabel.setText("x: " + point.getxCart() + " , y: " + point.getyCart());
        });
    }

    private void handleCircleDragged(Circle circle, MouseEvent event) {
        double newMouseX = event.getX();
        double newMouseY = event.getY();

        // Оновлюємо координати точки та кола
        int index = circleList.indexOf(circle);
        if (index != -1 && index < pointList.size()) {
            pointList.get(index).setxGraph(newMouseX);
            pointList.get(index).setyGraph(newMouseY);
            circle.setCenterX(newMouseX);
            circle.setCenterY(newMouseY);
        }
    }

    /**
     * Функція обробки події натиску на кнопку миші
     */
    private void addMouseClickEvent() {
        Graph.setOnMouseClicked((MouseEvent event) -> {
            if (!isDragging) {
                double x = event.getX();
                double y = event.getY();

                Point point = new Point(x, y);
                point.calculationCartesianCoord(Graph, LINES_FREQUENCY);

                pointList.add(point);
                Circle pointCircle = new Circle(point.getxGraph(), point.getyGraph(), 3, Color.BISQUE);
                //pointCircle.setOnMouseClicked(e -> handleCircleClick(circle));
                pointCircle.setOnMouseDragged(e -> handleCircleDragged(pointCircle, e));
                circleList.add(pointCircle);

                Graph.getChildren().clear();
                drawGraph();
                drawBezierCurveByParameters();
            }
        });

        Graph.setOnMouseReleased(event -> {
            // Скидаємо флаг і перебудовуємо графік після завершення перетягування
            if (isDragging) {
                Graph.getChildren().clear();
                drawGraph();
                drawBezierCurveByParameters();
                isDragging = false;
            }
        });
    }

    /**
     * Створення графіку
     */
    public void drawGraph() {
        // Draw vertical and horizontal markup lines
        int linesPerRow = (int) Graph.getPrefWidth() / 2 / LINES_FREQUENCY;
        //System.out.println(linesPerRow);
        double remainderRow = (Graph.getPrefWidth() / 2 / LINES_FREQUENCY - linesPerRow) * LINES_FREQUENCY;
        int tempRow = linesPerRow;
        linesPerRow += linesPerRow + 1;
        //System.out.println(remainderRow);

        int linesPerColumn = (int) Graph.getPrefHeight() / 2 / LINES_FREQUENCY;
        //System.out.println(linesPerColumn);
        double remainderColumn = (Graph.getPrefHeight() / 2 / LINES_FREQUENCY - linesPerColumn) * LINES_FREQUENCY;
        int tempColumn = linesPerColumn;
        linesPerColumn += linesPerColumn + 1;
        //System.out.println(remainderColumn);

        for (int i = 0; i < linesPerRow; i++) {
            Line graphMarkupLine = new Line(
                    i * LINES_FREQUENCY + remainderRow, 0,
                    i * LINES_FREQUENCY + remainderRow, Graph.getPrefHeight());
            if (i == tempRow) {
                Polygon triangle = new Polygon();
                triangle.getPoints().addAll(
                        Graph.getPrefWidth() / 2, 0.0,
                        Graph.getPrefWidth() / 2 - 5, 10.0,
                        Graph.getPrefWidth() / 2 + 5, 10.0
                );

                Label coordNumb = new Label("0");
                coordNumb.setLayoutX(Graph.getPrefWidth() / 2 + 3);
                coordNumb.setLayoutY(Graph.getPrefHeight() / 2);
                Graph.getChildren().addAll(triangle, coordNumb, graphMarkupLine);
            } else {
                // Row
                Line divisionLine = new Line(
                        i * LINES_FREQUENCY + remainderRow, Graph.getPrefHeight() / 2 - 4,
                        i * LINES_FREQUENCY + remainderRow, Graph.getPrefHeight() / 2 + 4);
                graphMarkupLine.setStrokeWidth(MIN_LINES_STROKE);

                Label coordNumb = new Label("" + (2 * (tempColumn - i)));
                coordNumb.setLayoutX(Graph.getPrefWidth() / 2 + 3);
                coordNumb.setLayoutY(i * LINES_FREQUENCY + remainderColumn);

                Graph.getChildren().addAll(graphMarkupLine, divisionLine, coordNumb);
            }
        }
        for (int i = 0; i < linesPerColumn; i++) {
            Line graphMarkupLine = new Line(
                    0, i * LINES_FREQUENCY + remainderColumn,
                    Graph.getPrefWidth(), i * LINES_FREQUENCY + remainderColumn);
            if (i == tempColumn) {
                //graphMarkupLine.setStrokeWidth(1);
                Polygon triangle = new Polygon();
                triangle.getPoints().addAll(
                        Graph.getPrefWidth(), Graph.getPrefHeight() / 2,
                        Graph.getPrefWidth() - 10, Graph.getPrefHeight() / 2 - 5,
                        Graph.getPrefWidth() - 10, Graph.getPrefHeight() / 2 + 5
                );
                Graph.getChildren().addAll(triangle, graphMarkupLine);
            } else {
                // Column
                Line divisionLine = new Line(
                        Graph.getPrefWidth() / 2 - 4, i * LINES_FREQUENCY + remainderColumn,
                        Graph.getPrefWidth() / 2 + 4, i * LINES_FREQUENCY + remainderColumn);
                graphMarkupLine.setStrokeWidth(MIN_LINES_STROKE);

                Label coordNumb = new Label("" + (2 * (-(tempRow - i))));
                coordNumb.setLayoutX(i * LINES_FREQUENCY + remainderRow);
                coordNumb.setLayoutY(Graph.getPrefHeight() / 2 + 3);

                Graph.getChildren().addAll(graphMarkupLine, divisionLine, coordNumb);
            }
        }
    }

    private void drawBezierCurve(GraphicsContext gcBezier, GraphicsContext gcPolygon) {

        double[] controlPointsX = pointList.stream().mapToDouble(Point::getxGraph).toArray();
        double[] controlPointsY = pointList.stream().mapToDouble(Point::getyGraph).toArray();

        gcPolygon.setLineWidth(2.0);
        gcPolygon.beginPath();
        for (int i = 0; i<pointList.size(); i++) {
            Graph.getChildren().add(circleList.get(i));
            if (i == 0) {
                gcPolygon.moveTo(controlPointsX[i], controlPointsY[i]);
            } else {
                gcPolygon.lineTo(controlPointsX[i], controlPointsY[i]);
            }
        }
        gcPolygon.stroke();

        int n = controlPointsX.length - 1;
        gcBezier.setLineWidth(2.0);
        gcBezier.setStroke(Color.BLUE);
        gcBezier.beginPath();

        for (double i = Double.parseDouble(aTf.getText()); i <= Double.parseDouble(bTf.getText());) {
            double t = i;
            double x = calculateBezierCoordinate(t, controlPointsX, n);
            double y = calculateBezierCoordinate(t, controlPointsY, n);

            if (i == 0) {
                gcBezier.moveTo(x, y);
            } else {
                gcBezier.lineTo(x, y);
            }

            i+= Double.parseDouble(stepTf.getText());
        }

        gcBezier.stroke();
    }

    private double calculateBezierCoordinate(double t, double[] controlPoints, int n) {
        double result = 0;
        for (int i = 0; i <= n; i++) {
            result += controlPoints[i] * binomialCoefficient(n, i) * Math.pow(t, i) * Math.pow(1 - t, n - i);
        }
        return result;
    }

    private double binomialCoefficient(int n, int i) {
        if (i == 0 || i == n) {
            return 1;
        } else {
            return factorial(n)/(factorial(i)*factorial(n-i));
        }
    }

    public double factorial(double f) {
        double result = 1;
        for (int i = 1; i <= f; i++) {
            result = result * i;
        }
        return result;
    }

}