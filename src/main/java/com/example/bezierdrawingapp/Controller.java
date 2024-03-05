package com.example.bezierdrawingapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    /**
     * Ціна поділки у пікселях
     */
    private int LINES_FREQUENCY = 67;

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
    private TextField p1x;

    @FXML
    private TextField p1y;

    @FXML
    private TextField p2x;

    @FXML
    private TextField p2y;

    @FXML
    private TextField p3x;

    @FXML
    private TextField p3y;

    @FXML
    private Button saveButton;

    @FXML
    private Label statusLabel;

    /**
     * Функція для ініціалізації початкових значень та викликів функцій під час запуску вікна
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        drawGraph();
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
     * Створення графіку
     */
    public void drawGraph() {
        // Draw vertical and horizontal markup lines
        int linesPerRow = (int) Graph.getPrefWidth() / 2 / LINES_FREQUENCY;
        System.out.println(linesPerRow);
        double remainderRow = (Graph.getPrefWidth() / 2 / LINES_FREQUENCY - linesPerRow) * LINES_FREQUENCY;
        int tempRow = linesPerRow;
        linesPerRow += linesPerRow + 1;
        System.out.println(remainderRow);

        int linesPerColumn = (int) Graph.getPrefHeight() / 2 / LINES_FREQUENCY;
        System.out.println(linesPerColumn);
        double remainderColumn = (Graph.getPrefHeight() / 2 / LINES_FREQUENCY - linesPerColumn) * LINES_FREQUENCY;
        int tempColumn = linesPerColumn;
        linesPerColumn += linesPerColumn + 1;
        System.out.println(remainderColumn);

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

}