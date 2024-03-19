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
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.Group;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Клас-контроллер головного вікна
 */
public class Controller implements Initializable {

    /**
     * Ціна поділки у пікселях
     */
    private int LINES_FREQUENCY = 40;

    /**
     * Натиск ліній графікічної площини
     */
    private final double MIN_LINES_STROKE = 0.2;

    /**
     * Радіус кола точки
     */
    private final int CIRCLE_RADIUS = 6;

    /**
     * Максимальна кількість точок побудови кривої матричним методом
     */
    private final int MAX_MATRIX_POINTS_NUMBER = 30;

    /**
     * Максимальна кількість точок побудови кривої параметричним методом
     */
    private final int MAX_PARAMETERS_POINTS_NUMBER = 101;

    /**
     * Змінна для присвоєння максимальних значень кількості точок
     */
    private int MAX_POINTS = 0;

    // Віконні елементи
    @FXML
    private Label CoordinateLabel;
    @FXML
    private Label maxPointsNumLabel;
    @FXML
    private Label addLabelWarning;
    @FXML
    private Label deleteLabelWarning;
    @FXML
    private Label tLabelWarning;
    @FXML
    private Label editLabelWarning;
    @FXML
    private Label readLabelWarning;
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
    private TextField readPointsTf;

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
    @FXML
    private TextField pnDeleteTf;

    //
    @FXML
    private TextField pnEditTf;
    @FXML
    private TextField pxEditTf;
    @FXML
    private TextField pyEditTf;

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
    @FXML
    private TextArea diagonalPointListTf;
    @FXML
    private TextField diagonalSumTf1;
    @FXML
    private TextField diagonalSumTf2;

    //Groups
    @FXML
    private Group inputStepGroup;
    @FXML
    private Group fileReadGroup;
    @FXML
    private Group newPointCoordGroup;
    @FXML
    private Group deletePointCoordGroup;
    @FXML
    private Group colorChooseGroup;
    @FXML
    private Group pointListGroup;
    @FXML
    private Group diagonalPointListGroup;
    @FXML
    private Group diagonalSumGroup;
    @FXML
    private Group editPointGroup;

    /**
     * Список точок кривої
     */
    private List<Point> pointList;

    /**
     * Список кіл, що обводять точки кривої
     */
    private List<Circle> circleList;

    /**
     * Змінна, що відображає статус процесу перетягування точки
     */
    private boolean isDragging = false;

    /**
     * Вибране коло для перетягування
     */
    private Circle selectedCircle;

    /**
     * Змінні проміжків та шляху
     */
    double a, b, step;

    /**
     * Змінні кольрів елементів кривої
     */
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

        addLabelWarning.setText("");
        deleteLabelWarning.setText("");
        tLabelWarning.setText("");
        readLabelWarning.setText("");

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
        diagonalPointListGroup.setVisible(false);
        diagonalSumGroup.setVisible(false);
        editPointGroup.setVisible(false);
        fileReadGroup.setVisible(false);
    }

    /**
     * Функція малювання кривої на панелі за параметричним методом
     */
    private void drawBezierCurveByParameters() {
        Canvas canvasBezier = new Canvas(Graph.getPrefWidth(), Graph.getPrefHeight());
        Canvas canvasPolygon = new Canvas(Graph.getPrefWidth(), Graph.getPrefHeight());
        Graph.getChildren().addAll(canvasBezier, canvasPolygon);
        GraphicsContext gcBezier = canvasBezier.getGraphicsContext2D();
        GraphicsContext gcPolygon = canvasPolygon.getGraphicsContext2D();
        drawBezierCurveParameters(gcBezier, gcPolygon);
    }

    /**
     * Функція малювання кривої на панелі за матричним методом
     */
    private void drawBezierCurveByMatrix() {
        Canvas canvasBezier = new Canvas(Graph.getPrefWidth(), Graph.getPrefHeight());
        Canvas canvasPolygon = new Canvas(Graph.getPrefWidth(), Graph.getPrefHeight());
        Graph.getChildren().addAll(canvasBezier, canvasPolygon);
        GraphicsContext gcBezier = canvasBezier.getGraphicsContext2D();
        GraphicsContext gcPolygon = canvasPolygon.getGraphicsContext2D();
        drawBezierCurveMatrix(gcBezier, gcPolygon);
    }

    public void autoFill(){
        aTf.setText(0+"");
        bTf.setText(1+"");
        stepTf.setText(0.001 +"");
    }


    /**
     * Функція-слухач для кнопки Clear (очиищення вікна)
     */
    @FXML
    void onClearButtonClick(ActionEvent event) {
        clearPane();
    }

    /**
     * Функція очищення панелі й списків точок
     */
    public void clearPane() {
        Graph.getChildren().clear();
        pointList.clear();
        circleList.clear();
        nullStepValue();
        clearFields();
        drawGraph();
    }

    /**
     * Функція очищення текстових полів вікна
     */
    public void clearFields() {
        aTf.clear();
        bTf.clear();
        stepTf.clear();
        pxAddTf.clear();
        pyAddTf.clear();
        pyDeleteTf.clear();
        pxDeleteTf.clear();
        pointListTf.clear();
        diagonalPointListTf.clear();
        diagonalSumTf1.clear();
        diagonalSumTf2.clear();
        addLabelWarning.setText("");
        deleteLabelWarning.setText("");
        tLabelWarning.setText("");
        editLabelWarning.setText("");

    }

    /**
     * Функція-слухач для кнопки Choose (вибір методу побудови кривої)
     */
    @FXML
    void onChooseMethodButtonClicked(ActionEvent event) {
        if (Objects.equals(methodList.getValue(), "Parameters")) {
            clearPane();
            //
            autoFill();
            //
            maxPointsNumLabel.setText("Max order of curve " + (MAX_PARAMETERS_POINTS_NUMBER-1));
            MAX_POINTS = MAX_PARAMETERS_POINTS_NUMBER;
            diagonalSumGroup.setVisible(false);
            diagonalPointListGroup.setVisible(false);
            inputStepGroup.setVisible(true);
            newPointCoordGroup.setVisible(true);
            pointListGroup.setVisible(true);
            colorChooseGroup.setVisible(true);
            deletePointCoordGroup.setVisible(true);
            editPointGroup.setVisible(true);
            fileReadGroup.setVisible(true);
        } else if (Objects.equals(methodList.getValue(), "Matrix")) {
            clearPane();
            //
            autoFill();
            //
            maxPointsNumLabel.setText("Max order of curve " + (MAX_MATRIX_POINTS_NUMBER-1));
            MAX_POINTS = MAX_MATRIX_POINTS_NUMBER;
            diagonalSumGroup.setVisible(true);
            inputStepGroup.setVisible(true);
            newPointCoordGroup.setVisible(true);
            pointListGroup.setVisible(true);
            colorChooseGroup.setVisible(true);
            deletePointCoordGroup.setVisible(true);
            diagonalPointListGroup.setVisible(true);
            editPointGroup.setVisible(true);
            fileReadGroup.setVisible(true);
        }
    }

    /**
     * Функція-слухач для кнопки Input (встановлення проміжку та кроку)
     */
    @FXML
    void onInputButtonClick(ActionEvent event) {
        if (isStepFieldsCorrectFilled()) {
            tLabelWarning.setText("");
            a = Double.parseDouble(aTf.getText());
            b = Double.parseDouble(bTf.getText());
            step = Double.parseDouble(stepTf.getText());
            updateGraph();
        }
    }

    /**
     * Функція-перевірка на коректне заповнення полів вводу кроку та проміжку
     */
    public boolean isStepFieldsCorrectFilled() {
        if (aTf.getText().isEmpty() || bTf.getText().isEmpty() || stepTf.getText().isEmpty()) {
            tLabelWarning.setText("*Field empty");
            return false;
        }
        try {
            double aTemp = Double.parseDouble(aTf.getText());
            double bTemp = Double.parseDouble(bTf.getText());
            double stepTemp = Double.parseDouble(stepTf.getText());

            if (aTemp < 0 || aTemp >= 1 || bTemp <= 0 || bTemp > 1 || stepTemp <= 0 || aTemp-bTemp>=0) {
                tLabelWarning.setText("*Input correct values");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            tLabelWarning.setText("*Incorrect input");
            return false;
        }
    }

    /**
     * Функція-перевірка на коректні зачення проміжку та кроку
     */
    public boolean isStepValuesCorrect() {
        return !(step <= 0) && !(a < 0) && !(a >= 1) && !(b <= 0) && !(b > 1);
    }

    /**
     * Функція занулення значень кроку та проміжку
     */
    public void nullStepValue() {
        step=-1;
        a=-1;
        b=-1;
    }

    @FXML
    void onReadButtonClick(ActionEvent event) {
        if (!isStepValuesCorrect()) {
            showAlert("Warning", "Input step and interval");
            return;
        }
        if(!isReadNumberFieldsCorrectFilled()) {
            return;
        }
        int length = Integer.parseInt(readPointsTf.getText());
        readLabelWarning.setText("");
        readPointsTf.clear();
        readingFile(length);
    }

    /**
     * Функція-перевірка на коректні значення номера точки для видалення
     */
    public boolean isReadNumberFieldsCorrectFilled() {
        if (readPointsTf.getText().isEmpty()) {
            readLabelWarning.setText("*Field empty");
            return false;
        }
        try {
            int nTemp = Integer.parseInt(readPointsTf.getText());
            if (pointList.size()+nTemp<=MAX_POINTS && nTemp>0) {
                return true;
            }
            readLabelWarning.setText("*The value is outside the allowed points");
            return false;
        } catch (NumberFormatException e) {
            readLabelWarning.setText("*Incorrect input");
            return false;
        }
    }

    /**
     * Функція зчитування інформації з файлу
     */
    public void readingFile(int length) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose reading file");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter
                    ("TXT files (*.txt)", "*.txt");
            fileChooser.getExtensionFilters().add(extFilter);
            File selectedFile = fileChooser.showOpenDialog(new Stage());
            String readFilePath = selectedFile.getAbsolutePath();
            readingFromFile(readFilePath, length);
            scalingToUserValue(pointList, circleList);
            if (!pointList.isEmpty()) {
                System.out.println("Finish reading");
            } else {
                showAlert("Reading Error", "Incorrect position of fields or empty file");
                System.out.println("Unsuccessful reading");
            }
            updateGraph();
        } catch (NullPointerException e) {
            System.out.println("Unsuccessful reading: " + e.getMessage());
        }
    }

    /**
     * Функція зчитування списку точок
     */
    public void readingFromFile(String readFilePath, int length) {
        File readFile = new File(readFilePath);
        try (BufferedReader br = new BufferedReader(new FileReader(readFile))) {
            String line;
            int temp = 0;
            while ((line = br.readLine()) != null && temp<length) {
                try {
                    String[] parts = line.split(", ");
                    double x = Double.parseDouble(parts[0].substring(2));
                    double y = Double.parseDouble(parts[1].substring(2));

                    Point point = new Point(x, y, 1);
                    point.calculationGraphicsCoord(Graph, LINES_FREQUENCY);
                    Circle pointCircle = new Circle(point.getxGraph(), point.getyGraph(), CIRCLE_RADIUS, pointColor);
                    pointList.add(point);
                    circleList.add(pointCircle);
                    temp++;
                } catch (NumberFormatException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
            if (temp<length) {
                readLabelWarning.setText("*File contain only " + temp + " elements");
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Функція-слухач для кнопки Add (додавання нової точки до кривої)
     */
    @FXML
    void onAddButtonClick(ActionEvent event) {
        if (!isStepValuesCorrect()) {
            addLabelWarning.setText("*Input step and interval");
            return;
        }
        if (!isAddCoordinatesFieldsCorrectFilled()) {
            return;
        }
        if (pointList.size()>=MAX_POINTS) {
            showAlert("Warning", "There is maximum order of curve " + (MAX_POINTS-1) +"/"+(MAX_POINTS-1));
            return;
        }
        addLabelWarning.setText("");
        double x = Double.parseDouble(pxAddTf.getText());
        double y = Double.parseDouble(pyAddTf.getText());

        Point point = new Point(x, y, 1);
        point.calculationGraphicsCoord(Graph, LINES_FREQUENCY);

        Circle pointCircle = new Circle(point.getxGraph(), point.getyGraph(), CIRCLE_RADIUS, pointColor);

        pointList.add(point);
        circleList.add(pointCircle);
        scalingToUserValue(pointList, circleList);

        updateGraph();

        pxAddTf.clear();
        pyAddTf.clear();
    }

    /**
     * Функція-перевірка на коректні значення координат нової точки
     */
    public boolean isAddCoordinatesFieldsCorrectFilled() {
        if (pxAddTf.getText().isEmpty() || pyAddTf.getText().isEmpty()) {
            addLabelWarning.setText("*Field empty");
            return false;
        }
        try {
            double xTemp = Double.parseDouble(pxAddTf.getText());
            double yTemp = Double.parseDouble(pyAddTf.getText());

            if (xTemp < -100 || xTemp > 100 || yTemp < -100 || yTemp > 100) {
                addLabelWarning.setText("*Values must be between -100 and 100");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            addLabelWarning.setText("*Incorrect input");
            return false;
        }
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

    /**
     * Функція-слухач для кнопки Delete (видалення точки кривої за  координатами)
     */
    @FXML
    void onDeleteButtonClick(ActionEvent event) {
        if (!isStepValuesCorrect()) {
            deleteLabelWarning.setText("*Input step and interval");
            return;
        }
        if (!isDeleteCoordinatesFieldsCorrectFilled()) {
            return;
        }
        deleteLabelWarning.setText("");
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
        pyDeleteTf.clear();
        pxDeleteTf.clear();
    }

    /**
     * Функція-перевірка на коректні значення координат точки для видалення
     */
    public boolean isDeleteCoordinatesFieldsCorrectFilled() {
        if (pxDeleteTf.getText().isEmpty() || pyDeleteTf.getText().isEmpty()) {
            deleteLabelWarning.setText("*Field empty");
            return false;
        }
        try {
            double xTemp = Double.parseDouble(pxDeleteTf.getText());
            double yTemp = Double.parseDouble(pyDeleteTf.getText());
            Point point = new Point(xTemp, yTemp, 1);
            for (int i = 0; i <pointList.size(); i++) {
                if (pointList.get(i).equals(point)) {
                    return true;
                }
            }
            deleteLabelWarning.setText("*The point does not exist");
            return false;
        } catch (NumberFormatException e) {
            deleteLabelWarning.setText("*Incorrect input");
            return false;
        }
    }

    /**
     * Функція-слухач для кнопки Delete (видалення точки кривої за номером точки)
     */
    @FXML
    void onDeleteNumberButtonClick(ActionEvent event) {
        if (!isStepValuesCorrect()) {
            deleteLabelWarning.setText("*Input step and interval");
            return;
        }
        if (!isDeleteNumberFieldsCorrectFilled()) {
            return;
        }
        deleteLabelWarning.setText("");
        int n = Integer.parseInt(pnDeleteTf.getText());
        pointList.remove(n-1);
        circleList.remove(n-1);
        updateGraph();
        pnDeleteTf.clear();
    }

    /**
     * Функція-перевірка на коректні значення номера точки для видалення
     */
    public boolean isDeleteNumberFieldsCorrectFilled() {
        if (pnDeleteTf.getText().isEmpty()) {
            deleteLabelWarning.setText("*Field empty");
            return false;
        }
        try {
            int nTemp = Integer.parseInt(pnDeleteTf.getText());
            if (pointList.size()>=nTemp && nTemp>0) {
                return true;
            }
            deleteLabelWarning.setText("*The point does not exist");
            return false;
        } catch (NumberFormatException e) {
            deleteLabelWarning.setText("*Incorrect input");
            return false;
        }
    }

    /**
     * Функція-слухач для кнопки Edit (редагування точки за розташуванням)
     */
    @FXML
    void onEditButtonClick(ActionEvent event) {
        if (!isStepValuesCorrect()) {
            editLabelWarning.setText("*Input step and interval");
            return;
        }
        if (!isEditFieldsCorrectFilled()) {
            return;
        }
        editLabelWarning.setText("");
        int n = Integer.parseInt(pnEditTf.getText());
        double x = Double.parseDouble(pxEditTf.getText());
        double y = Double.parseDouble(pyEditTf.getText());

        pointList.get(n-1).setxCart(x);
        pointList.get(n-1).setyCart(y);
        calculateGraphCoordinateOfTheList(pointList, circleList);
        scalingToUserValue(pointList, circleList);
        updateGraph();
        pnEditTf.clear();
        pxEditTf.clear();
        pyEditTf.clear();
    }

    /**
     * Функція-перевірка на коректні значення номеру точки та нових координат для редагування розміщення
     */
    public boolean isEditFieldsCorrectFilled() {
        if (pnEditTf.getText().isEmpty() || pxEditTf.getText().isEmpty() || pyEditTf.getText().isEmpty() ) {
            editLabelWarning.setText("*Field empty");
            return false;
        }
        try {
            int nTemp = Integer.parseInt(pnEditTf.getText());
            double xTemp = Double.parseDouble(pxEditTf.getText());
            double yTemp = Double.parseDouble(pyEditTf.getText());

            if (pointList.size()<nTemp || nTemp<=0) {
                editLabelWarning.setText("*The point does not exist");
                return false;
            }
            if (xTemp < -100 || xTemp > 100 || yTemp < -100 || yTemp > 100) {
                editLabelWarning.setText("*Values must be between -100 and 100");
                return false;
            }
            editLabelWarning.setText("*The point does not exist");
            return true;
        } catch (NumberFormatException e) {
            editLabelWarning.setText("*Incorrect input");
            return false;
        }
    }

    /**
     * Функція-слухач для кнопки Choose вибору нового кольору сторін многокутника
     */
    @FXML
    void onPolygonColorButtonClick(ActionEvent event) {
        polygonColor = polygonColorChooser.getValue();
        updateGraph();
    }

    /**
     * Функція-слухач для кнопки Choose вибору нового кольору кривої
     */
    @FXML
    void onCurveColorButtonClick(ActionEvent event) {
        curveColor = curveColorChooser.getValue();
        updateGraph();
    }

    /**
     * Функція-слухач для кнопки Choose вибору нового кольору керуючих точок
     */
    @FXML
    void onPointColorButtonClick(ActionEvent event) {
        pointColor = pointsColorChooser.getValue();
        for (Circle circle:circleList) {
            circle.setFill(pointColor);
        }
        updateGraph();
    }

    /**
     * Вбудований клас для обробки події скролінгу панелі з графіком
     */
    private class ZoomHandler implements EventHandler<ScrollEvent> {
        private final double MAX_ZOOM = 300;
        private final double MIN_ZOOM = 6;

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

    /**
     * Функція обчислення графічних координат списку точок
     */
    public void calculateGraphCoordinateOfTheList(List<Point> pointList, List<Circle> circleList){
        for (int i = 0; i<pointList.size(); i++) {
            pointList.get(i).calculationGraphicsCoord(Graph, LINES_FREQUENCY);
            circleList.get(i).setCenterX(pointList.get(i).getxGraph());
            circleList.get(i).setCenterY(pointList.get(i).getyGraph());
        }
    }

    /**
     * Функція обчислення декартових координат списку точок
     */
    public void calculateCartCoordinateOfTheList(List<Point> pointList){
        for (Point point: pointList) {
            point.calculationCartesianCoord(Graph, LINES_FREQUENCY);
        }
    }

    /**
     * Функція-перевірка на вихід точок зі списку за межі координатної площини
     */
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

    /**
     * Функція обробки підії вибору точки для перетягування натиском на середю кнопку миші
     */
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

    /**
     * Функція обробки події перетягування точки за допомогою зажатої середньої кнопки миші
     */
    private void handleMiddleMouseDragged(MouseEvent event) {
        if (selectedCircle != null && event.getButton() == MouseButton.MIDDLE) {
            double newMouseX = event.getX();
            double newMouseY = event.getY();
            Point point = new Point(newMouseX, newMouseY);
            if (pointBeyondLimit(point)) {
                return;
            }

            int index = circleList.indexOf(selectedCircle);
            if (index != -1 && index < pointList.size()) {
                pointList.get(index).setxGraph(newMouseX);
                pointList.get(index).setyGraph(newMouseY);
                selectedCircle.setCenterX(newMouseX);
                selectedCircle.setCenterY(newMouseY);
            }

            updateGraph();
        }
    }

    /**
     * Функція-переввірка чи точка виходить за межі координатної площини
     */
    public boolean pointBeyondLimit(Point point) {
        if (point.getxGraph()>Graph.getPrefWidth() || point.getxGraph()<0
                || point.getyGraph()>Graph.getPrefHeight() || point.getyGraph()<0) {
            return true;
        }
        return false;
    }

    /**
     * Функція обробки події натиску на кнопки миші (ліва, права)
     */
    private void handleMouseClick(MouseEvent event) {
        if (!isStepValuesCorrect()) {
            showAlert("Warning", "Input step and interval");
            return;
        }
        if (!isDragging && event.getButton() == MouseButton.PRIMARY) {
            if (pointList.size()>=MAX_POINTS) {
                showAlert("Warning", "There is maximum order of curve " + (MAX_POINTS-1) +"/"+(MAX_POINTS-1));
                return;
            }
            double x = event.getX();
            double y = event.getY();

            Point point = new Point((int) x, (int) y);

            if (getPointBetween(point)) {
            }else {
                pointList.add(point);
                Circle pointCircle = new Circle(x, y, CIRCLE_RADIUS, pointColor);
                circleList.add(pointCircle);
            }

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
     * Функція вставлення точки між 2 іншими точками
     */
    public boolean getPointBetween(Point point) {
        for (int i = 0; i<pointList.size()-1; i++) {
            double dist = Math.sqrt(Math.pow(pointList.get(i + 1).getxGraph() - pointList.get(i).getxGraph(), 2) +
                    Math.pow(pointList.get(i + 1).getyGraph() - pointList.get(i).getyGraph(), 2));
            System.out.println(dist);
            if (isPointInRange(point, pointList.get(i), pointList.get(i + 1), dist)) {
                System.out.println("Yes");
                Point middlePoint = calculateMidpoint(pointList.get(i), pointList.get(i + 1));
                pointList.add(i+1, middlePoint);
                Circle pointCircle = new Circle(middlePoint.getxGraph(), middlePoint.getyGraph(), CIRCLE_RADIUS, pointColor);
                circleList.add(i+1, pointCircle);
                return true;
            }
        }
        return false;
    }

    /**
     * Перевірка на правильне розміщення точки для вставлення
     */
    public boolean isPointInRange(Point point, Point first, Point second, double dist) {
        double percent = CIRCLE_RADIUS/dist;
        Point[] points = new Point[4];
        points[0] = new Point(
                first.getxGraph() + percent * (second.getxGraph() - first.getxGraph()),
                first.getyGraph() - percent * (second.getyGraph() - first.getyGraph()));
        points[1] = new Point(
                first.getxGraph() - percent * (second.getxGraph() - first.getxGraph()),
                first.getyGraph() + percent * (second.getyGraph() - first.getyGraph()));
        points[2] = new Point(
                second.getxGraph() + percent * (first.getxGraph() - second.getxGraph()),
                second.getyGraph() - percent * (first.getyGraph() - second.getyGraph()));
        points[3] = new Point(
                second.getxGraph() - percent * (first.getxGraph() - second.getxGraph()),
                second.getyGraph() + percent * (first.getyGraph() - second.getyGraph()));

        /*for (Point pointT : points) {
            System.out.println(pointT.getxGraph() + " " + pointT.getyGraph() + "\n");
            Graph.getChildren().add(new Circle(pointT.getxGraph(), pointT.getyGraph(), 2, Color.BLUE));
        }*/

        System.out.println(point.getxGraph() +" " + point.getyGraph());

        Polygon polygon = new Polygon(
                points[0].getxGraph(), points[0].getyGraph(),
                points[1].getxGraph(), points[1].getyGraph(),
                points[2].getxGraph(), points[2].getyGraph(),
                points[3].getxGraph(), points[3].getyGraph()
        );
        Graph.getChildren().add(polygon);
        return polygon.contains(point.getxGraph(), point.getyGraph());
    }

    /**
     * Функція обчислення середньої точки між 2 іншими
     */
    public Point calculateMidpoint(Point point1, Point point2) {
        double midX = (point1.getxGraph() + point2.getxGraph()) / 2.0;
        double midY = (point1.getyGraph() + point2.getyGraph()) / 2.0;

        return new Point(midX, midY);
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

    /**
     * Функція оновлення графіка з кривою
     */
    public void updateGraph() {
        Graph.getChildren().clear();
        drawGraph();
        if (Objects.equals(methodList.getValue(), "Parameters")) {
            drawBezierCurveByParameters();
        } else if (Objects.equals(methodList.getValue(), "Matrix")) {
            drawBezierCurveByMatrix();
        }
        fillLabelListOfPoint();
    }

    /**
     * Функція заповнення поля списку точок кривої
     */
    public void fillLabelListOfPoint() {
        String str = "";
        calculateCartCoordinateOfTheList(pointList);
        for (Point point : pointList) {
            str += (pointList.indexOf(point)+1) + ") x:" + point.getxCart() + ", y: " + point.getyCart() + "\n";
        }
        pointListTf.setText(str);
    }

    /**
     * Функція малювання кривої параметричним методом
     */
    private void drawBezierCurveParameters(GraphicsContext gcBezier, GraphicsContext gcPolygon) {
        if (!isStepValuesCorrect()) {
            return;
        }

        double[] controlPointsX = pointList.stream().mapToDouble(Point::getxGraph).toArray();
        double[] controlPointsY = pointList.stream().mapToDouble(Point::getyGraph).toArray();

        gcPolygon.setLineWidth(2.0);
        gcPolygon.setStroke(polygonColor);
        gcPolygon.beginPath();
        for (int i = 0; i<pointList.size(); i++) {

            Label number = new Label((i+1)+"");
            number.setLayoutX(circleList.get(i).getCenterX()-2*circleList.get(i).getRadius());
            number.setLayoutY(circleList.get(i).getCenterY()+circleList.get(i).getRadius());
            if (i == 0) {
                gcPolygon.moveTo(controlPointsX[i], controlPointsY[i]);
            } else {
                gcPolygon.lineTo(controlPointsX[i], controlPointsY[i]);
            }
            circleList.get(i).setFill(pointColor);
            if (i==0 || i==pointList.size()-1) {
                circleList.get(i).setFill(Color.MAROON);
            }
            Graph.getChildren().addAll(circleList.get(i), number);
        }
        gcPolygon.stroke();

        int n = controlPointsX.length - 1;
        gcBezier.setLineWidth(2.0);
        gcBezier.setStroke(curveColor);
        gcBezier.beginPath();

        for (double i = a; i <= b;) {
            double t = i;
            double x = calculateBezierParametersCoordinate(t, controlPointsX, n);
            double y = calculateBezierParametersCoordinate(t, controlPointsY, n);

            if (i == 0) {
                gcBezier.moveTo(x, y);
            } else {
                gcBezier.lineTo(x, y);
            }

            i+= step;
        }

        gcBezier.stroke();
    }

    /**
     * Функція обчислення координат точки кривої
     */
    private double calculateBezierParametersCoordinate(double t, double[] controlPoints, int n) {
        double result = 0;
        for (int i = 0; i <= n; i++) {
            result += controlPoints[i] * binomialCoefficient(n, i) * Math.pow(t, i) * Math.pow(1 - t, n - i);
        }
        return result;
    }

    /**
     * Функція обчислення коефіцієнта
     */
    private static double binomialCoefficient(int n, int i) {
        if (i == 0 || i == n) {
            return 1;
        } else {
            return factorial(n)/(factorial(i)*factorial(n-i));
        }
    }

    public static double factorial(double f) {
        double result = 1;
        for (int i = 1; i <= f; i++) {
            result = result * i;
        }
        return result;
    }

    /**
     * Функція побудови кривої матричним методом
     */
    private void drawBezierCurveMatrix(GraphicsContext gcBezier, GraphicsContext gcPolygon) {
        if (!isStepValuesCorrect()) {
            return;
        }

        double[] controlPointsX = pointList.stream().mapToDouble(Point::getxGraph).toArray();
        double[] controlPointsY = pointList.stream().mapToDouble(Point::getyGraph).toArray();

        gcPolygon.setLineWidth(2.0);
        gcPolygon.setStroke(polygonColor);
        gcPolygon.beginPath();
        for (int i = 0; i<pointList.size(); i++) {
            Label number = new Label((i+1)+"");
            number.setLayoutX(circleList.get(i).getCenterX()-2*circleList.get(i).getRadius());
            number.setLayoutY(circleList.get(i).getCenterY()+circleList.get(i).getRadius());

            if (i == 0) {
                gcPolygon.moveTo(controlPointsX[i], controlPointsY[i]);
            } else {
                gcPolygon.lineTo(controlPointsX[i], controlPointsY[i]);
            }

            circleList.get(i).setFill(pointColor);
            if (i==0 || i==pointList.size()-1) {
                circleList.get(i).setFill(Color.MAROON);
            }
            Graph.getChildren().addAll(circleList.get(i), number);
        }
        gcPolygon.stroke();

        int n = controlPointsX.length - 1;
        gcBezier.setLineWidth(2.0);
        gcBezier.setStroke(curveColor);
        gcBezier.beginPath();

        double[][] coefficientsMatrix = computeMatrix(n);
        System.out.println("Coefficients Matrix:");
        printMatrix(coefficientsMatrix);
        setMatrixLabel(coefficientsMatrix);

        for (double i = a; i <= b;) {
            double t = i;
            double x = calculateBezierMatrixCoordinate(t, coefficientsMatrix, controlPointsX, n);
            double y = calculateBezierMatrixCoordinate(t, coefficientsMatrix, controlPointsY, n);

            if (i == 0) {
                gcBezier.moveTo(x, y);
            } else {
                gcBezier.lineTo(x, y);
            }

            i+= step;
        }

        gcBezier.stroke();
    }

    /**
     * Функція обчислення координат точки кривої
     */
    private double calculateBezierMatrixCoordinate(double t, double[][] coefficientsMatrix, double[] controlPoints, int n) {
        double[] tMatrix = computeTMatrix(t, n);

        double[] intermediateResult = new double[n + 1];
        for (int i = 0; i < n+1; i++) {
            for (int j = 0; j < n+1; j++) {
                intermediateResult[i] += coefficientsMatrix[i][j] * tMatrix[j];
            }
        }

        double result = 0;
        for (int i = 0; i <= n; i++) {
            result += intermediateResult[i] * controlPoints[i];
        }

        return result;
    }

    /**
     * Функція обчислення матриці коефіцієнтів
     */
    public static double[][] computeMatrix(int n) {
        double[][] coefficients = new double[n + 1][n + 1];
        Arrays.stream(coefficients).forEach(row -> Arrays.fill(row, 0));

        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= n-i; j++) {
                coefficients[i][j] = binomialCoefficient(n, j) * binomialCoefficient(n-j, n-i-j) * Math.pow(-1, n-i-j);
            }
        }

        return coefficients;
    }

    /**
     * Функція обчислення матриці T
     */
    public static double[] computeTMatrix(double t, int n) {
        double[] tMatrix = new double[n + 1];
        for (int i = 0; i < tMatrix.length; i++) {
            tMatrix[i] = Math.pow(t, n-i);
        }

        return tMatrix;
    }

    /**
     * Функція виведення матриці коефіцієнтів у консоль
     */
    public static void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            for (double element : row) {
                System.out.print(element + " ");
            }
            System.out.println();
        }
    }

    /**
     * Функція виведення матриці коефіцієнтів на вікно
     */
    public void setMatrixLabel(double[][] matrix) {
        String str = "";
        double diagonal1 = 0;
        String diagonal1SumStr = "";
        double diagonal2 = 0;
        String diagonal2SumStr = "";
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j<matrix.length-i; j++) {
                str+= ((double) Math.round(matrix[i][j] * 100) /100 )+ "  ";

                if (i==j) {
                    diagonal1SumStr += matrix[i][j]+" + ";
                    diagonal1+=matrix[i][j];
                }
                if (j == matrix.length-i-1) {
                    diagonal2SumStr += matrix[i][j]+" + ";
                    diagonal2+=matrix[i][j];
                }
            }
            str+= "\n";
        }
        diagonal1SumStr += "= " + diagonal1;
        diagonalSumTf1.setText(diagonal1+"");
        diagonal2SumStr += "= " + diagonal2;
        diagonalSumTf2.setText(diagonal2+"");
        diagonalPointListTf.setText(str);
    }

    /**
     * Відображення вікна оповіщення
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}