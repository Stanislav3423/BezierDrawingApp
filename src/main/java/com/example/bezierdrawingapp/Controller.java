package com.example.bezierdrawingapp;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.Group;

import java.net.URL;
import java.util.*;

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
    private ChoiceBox<String> methodList;

    private final String[] methodsStr = {"Parameters", "Matrix"};

    //
    @FXML
    private TextField aTf;

    @FXML
    private TextField bTf;

    @FXML
    private TextField stepTf;

    //
    @FXML
    private TextField pxAddTf;

    @FXML
    private TextField pyAddTf;

    //
    @FXML
    private TextField pxDeleteTf;

    @FXML
    private TextField pyDeleteTf;

    //
    @FXML
    private ColorPicker polygonColorChooser;

    @FXML
    private ColorPicker curveColorChooser;

    @FXML
    private ColorPicker pointsColorChooser;

    //
    @FXML
    private TextArea pointListTf;

    //Groups

    @FXML
    private Group inputStepGroup;
    @FXML
    private Group newPointCoordGroup;
    @FXML
    private Group deletePointCoordGroup;
    @FXML
    private Group colorChooseGroup;
    @FXML
    private Group pointListGroup;

    private List<Point> pointList;
    private List<Circle> circleList;
    private boolean isDragging = false;
    private Circle selectedCircle;  // Вибране коло для перетягування
    double a, b, step;
    private Color polygonColor;
    private Color curveColor;
    private Color pointColor;

    /**
     * Функція для ініціалізації початкових значень та викликів функцій під час запуску вікна
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        step=-1;
        polygonColor = Color.BLACK;
        curveColor = Color.BLUE;
        pointColor = Color.BISQUE;
        pointList = new ArrayList<>();
        circleList = new ArrayList<>();

        methodList.getItems().addAll(methodsStr);

        drawGraph();

        Graph.setOnMouseMoved(this::handleMouseHover);

        Graph.setOnMouseClicked(this::handleMouseClick);
        Graph.setOnMousePressed(this::handleMiddleMousePressed);
        Graph.setOnMouseDragged(this::handleMiddleMouseDragged);
        Graph.setOnScroll(new ZoomHandler());

        autoFill();

        // visible groups
        inputStepGroup.setVisible(false);
        newPointCoordGroup.setVisible(false);
        pointListGroup.setVisible(false);
        colorChooseGroup.setVisible(false);
        deletePointCoordGroup.setVisible(false);
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


    // Button panel buttons
    @FXML
    void onCancelButtonClick(ActionEvent event) {

    }

    @FXML
    void onSaveButtonClick(ActionEvent event) {

    }

    @FXML
    void onClearButtonClick(ActionEvent event) {
        Graph.getChildren().clear();
        pointList.clear();
        circleList.clear();
        drawGraph();
    }

    // Main Panel buttons
    @FXML
    void onChooseMethodButtonClicked(ActionEvent event) {
        if (Objects.equals(methodList.getValue(), "Parameters")) {
            inputStepGroup.setVisible(true);
            newPointCoordGroup.setVisible(true);
            pointListGroup.setVisible(true);
            colorChooseGroup.setVisible(true);
            deletePointCoordGroup.setVisible(true);
        }
    }

    @FXML
    void onInputButtonClick(ActionEvent event) {
        if (isStepFieldsCorrectFilled()) {
            a = Double.parseDouble(aTf.getText());
            b = Double.parseDouble(bTf.getText());
            step = Double.parseDouble(stepTf.getText());
            updateGraph();
        }
    }

    public boolean isStepFieldsCorrectFilled() {
        if (aTf.getText().isEmpty() || bTf.getText().isEmpty() || stepTf.getText().isEmpty()) {
            return false;
        }
        try {
            double aTemp = Double.parseDouble(aTf.getText());
            double bTemp = Double.parseDouble(bTf.getText());
            double stepTemp = Double.parseDouble(stepTf.getText());

            if (aTemp < 0 || aTemp >= 1 || bTemp <= 0 || bTemp > 1 || stepTemp <= 0 || aTemp-bTemp>=0) {
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            // Input Label
            return false;
        }
    }

    public boolean isStepValuesCorrect() {
        return !(step <= 0) && !(a < 0) && !(a >= 1) && !(b <= 0) && !(b > 1);
    }


    @FXML
    void onAddButtonClick(ActionEvent event) {
        if (!isStepValuesCorrect()) {
            return;
        }
        double x = Double.parseDouble(pxAddTf.getText());
        double y = Double.parseDouble(pyAddTf.getText());

        Point point = new Point(x, y, 1);
        point.calculationGraphicsCoord(Graph, LINES_FREQUENCY);

        /*List<Point> tempList = pointList;
        List<Circle> tempCircList = circleList;*/

        Circle pointCircle = new Circle(point.getxGraph(), point.getyGraph(), 6, pointColor);
        /*tempList.add(point);
        tempCircList.add(pointCircle);*/

        pointList.add(point);
        circleList.add(pointCircle);

        updateGraph();
    }

    /**
     * Масштабування графіка із заданими у полях значеннями
     */
    public void scalingToUserValue(List<Point> points, List<Circle> circles) {
        int[] scaleFrequency = new int[points.size()];
        for (int i = 0; i < points.size(); i++) {
            scaleFrequency[i] = LINES_FREQUENCY;
            points.get(i).calculationGraphicsCoord(Graph, LINES_FREQUENCY);
            if (points.get(i).getxGraph() > Graph.getPrefWidth()) {
                int temp;
                temp = (int) ((Graph.getPrefWidth() / points.get(i).getxCart()));
                if (temp < scaleFrequency[i] && temp >= 6) {
                    scaleFrequency[i] = temp;
                }
                System.out.println("Scale: " + scaleFrequency);
            }
            if (points.get(i).getxGraph() < 0) {
                int temp;
                temp = (int) ((Graph.getPrefWidth() / (-points.get(i).getxCart())));
                if (temp < scaleFrequency[i] && temp >= 6) {
                    scaleFrequency[i] = temp;
                }
                System.out.println("Scale: " + scaleFrequency);
            }
            if (points.get(i).getyGraph() > Graph.getPrefHeight()) {
                int temp;
                temp = (int) ((Graph.getPrefHeight() / -points.get(i).getyCart()));
                if (temp < scaleFrequency[i] && temp >= 6) {
                    scaleFrequency[i] = temp;
                }
                System.out.println("Scale: " + scaleFrequency);
            }
            if (points.get(i).getyGraph() < 0) {
                int temp;
                temp = (int) ((Graph.getPrefHeight() / points.get(i).getyCart()));
                if (temp < scaleFrequency[i] && temp >= 6) {
                    scaleFrequency[i] = temp;
                }
                System.out.println("Scale: " + scaleFrequency);
            }
        }

        int minValue = scaleFrequency[0];

        for (int i = 1; i < scaleFrequency.length; i++) {
            if (scaleFrequency[i] < minValue) {
                minValue = scaleFrequency[i];
            }
        }
        LINES_FREQUENCY = minValue;

        for (int i = 0; i < points.size(); i++) {
            points.get(i).calculationGraphicsCoord(Graph, LINES_FREQUENCY);
            circles.get(i).setCenterX(points.get(i).getxGraph());
            circles.get(i).setCenterY(points.get(i).getyGraph());

        }
    }

    @FXML
    void onDeleteButtonClick(ActionEvent event) {
        if (!isStepValuesCorrect()) {
            return;
        }
        double x = Double.parseDouble(pxDeleteTf.getText());
        double y = Double.parseDouble(pyDeleteTf.getText());

        Point point = new Point(x, y, 1);
        calculateCartCoordinateOfTheList(pointList);
        for (int i = 0; i <pointList.size(); i++) {
            if (pointList.get(i).equals(point)) {
                pointList.remove(i);
                circleList.remove(i);
            }
        }
        updateGraph();
    }

    @FXML
    void onPolygonColorButtonClick(ActionEvent event) {
        polygonColor = polygonColorChooser.getValue();
        updateGraph();
    }

    @FXML
    void onCurveColorButtonClick(ActionEvent event) {
        curveColor = curveColorChooser.getValue();
        updateGraph();
    }

    @FXML
    void onPointColorButtonClick(ActionEvent event) {
        pointColor = pointsColorChooser.getValue();
        for (Circle circle:circleList) {
            circle.setFill(pointColor);
        }
        updateGraph();
    }
    private class ZoomHandler implements EventHandler<ScrollEvent> {
        private final double MAX_ZOOM = 300;
        private final double MIN_ZOOM = 6;

        //public double tempLinesFreq = LINES_FREQUENCY;
        @Override
        public void handle(ScrollEvent event) {
            double tempLinesFreq = LINES_FREQUENCY;
            calculateCartCoordinateOfTheList(pointList);
            if (event.getDeltaY() == 0) {
                return;
            } else if (event.getDeltaY() > 0 && tempLinesFreq * 1.1 < MAX_ZOOM) {
                if (beyondLimits()) {
                    return;
                }
                tempLinesFreq *= 1.1;
                System.out.println("Top: " + LINES_FREQUENCY);
            } else if (event.getDeltaY() < 0 && tempLinesFreq * 0.9 > MIN_ZOOM) {
                tempLinesFreq *= 0.9;
                System.out.println("Bottom: " + LINES_FREQUENCY);
            }
            LINES_FREQUENCY = (int) Math.round(tempLinesFreq);
            calculateGraphCoordinateOfTheList(pointList, circleList);
            System.out.println("Origin: " + LINES_FREQUENCY);
            updateGraph();
        }
    }

    public void calculateGraphCoordinateOfTheList(List<Point> pointList, List<Circle> circleList){
        for (int i = 0; i<pointList.size(); i++) {
            pointList.get(i).calculationGraphicsCoord(Graph, LINES_FREQUENCY);
            circleList.get(i).setCenterX(pointList.get(i).getxGraph());
            circleList.get(i).setCenterY(pointList.get(i).getyGraph());
        }
    }

    public void calculateCartCoordinateOfTheList(List<Point> pointList){
        for (Point point: pointList) {
            point.calculationCartesianCoord(Graph, LINES_FREQUENCY);
        }
    }

    public boolean beyondLimits() {
        int temp = 0;
        for (Point entry: pointList) {
            if (entry.getxGraph() > Graph.getPrefWidth() - 30 || entry.getxGraph() < 30 || entry.getyGraph() >
                    Graph.getPrefHeight() - 30 || entry.getyGraph() < 30) {
                temp++;
            }
        }
        return temp > 0;
    }

    /**
     * Функція обробки події наведення курсору мишки на панель
     */
    private void handleMouseHover(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        Point point = new Point((int) x, (int) y);
        point.calculationCartesianCoord(Graph, LINES_FREQUENCY);
        CoordinateLabel.setText("x: " + point.getxCart() + " , y: " + point.getyCart());
    }

    private void handleMiddleMousePressed(MouseEvent event) {
        if (event.getButton() == MouseButton.MIDDLE) {
            for (Circle circle : circleList) {
                if (circle.getBoundsInParent().contains(event.getX(), event.getY())) {
                    selectedCircle = circle;
                    break;
                }
            }
        }
    }

    private void handleMiddleMouseDragged(MouseEvent event) {
        if (selectedCircle != null && event.getButton() == MouseButton.MIDDLE) {
            double newMouseX = event.getX();
            double newMouseY = event.getY();

            int index = circleList.indexOf(selectedCircle);
            if (index != -1 && index < pointList.size()) {
                pointList.get(index).setxGraph(newMouseX);
                pointList.get(index).setyGraph(newMouseY);
                selectedCircle.setCenterX(newMouseX);
                selectedCircle.setCenterY(newMouseY);
            }

            // Викликати оновлення графіку та інших елементів, якщо потрібно
            updateGraph();
        }
    }

    private void handleMouseClick(MouseEvent event) {
        if (!isStepValuesCorrect()) {
            return;
        }
        if (!isDragging && event.getButton() == MouseButton.PRIMARY) {
            double x = event.getX();
            double y = event.getY();

            Point point = new Point((int) x, (int) y);
            pointList.add(point);

            Circle pointCircle = new Circle(x, y, 6, pointColor);
            circleList.add(pointCircle);

            //Graph.getChildren().add(pointCircle);

            updateGraph();
            return;
        }

        if (!isDragging && event.getButton() == MouseButton.SECONDARY) {
            for (int i = 0; i < circleList.size(); i++) {
                Circle circle = circleList.get(i);
                if (circle.getBoundsInParent().contains(event.getX(), event.getY())) {
                    circleList.remove(i);
                    pointList.remove(i);
                    updateGraph();
                    return;
                }
            }
        }
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

    public void updateGraph() {
        Graph.getChildren().clear();
        drawGraph();
        drawBezierCurveByParameters();
    }

    private void drawBezierCurve(GraphicsContext gcBezier, GraphicsContext gcPolygon) {
        if (!isStepValuesCorrect()) {
            return;
        }

        double[] controlPointsX = pointList.stream().mapToDouble(Point::getxGraph).toArray();
        double[] controlPointsY = pointList.stream().mapToDouble(Point::getyGraph).toArray();

        gcPolygon.setLineWidth(2.0);
        gcPolygon.setStroke(polygonColor);
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
        gcBezier.setStroke(curveColor);
        gcBezier.beginPath();

        for (double i = a; i <= b;) {
            double t = i;
            double x = calculateBezierCoordinate(t, controlPointsX, n);
            double y = calculateBezierCoordinate(t, controlPointsY, n);

            if (i == 0) {
                gcBezier.moveTo(x, y);
            } else {
                gcBezier.lineTo(x, y);
            }

            i+= step;
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