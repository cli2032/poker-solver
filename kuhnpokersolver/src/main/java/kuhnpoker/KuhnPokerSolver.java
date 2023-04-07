package kuhnpoker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.TreeMap;
import java.util.HashMap;

/**
 * JavaFX KuhnPokerSolver
 */
public class KuhnPokerSolver extends Application {

    private static Scene scene;
    public static final int PASS = 0, BET = 1, NUM_ACTIONS = 2;
    public static final Random random = new Random();
    public TreeMap<String, Node> nodeMap = new TreeMap<String, Node>();
    public static double[][][] strategiesOverTime = new double[20][12][2];
    public static int totalIterations;
    public static HashMap<Integer, XYChart.Series> lines = new HashMap<>();
    
    class Node {
        String infoSet;
        double[] regretSum = new double[NUM_ACTIONS], 
                 strategy = new double[NUM_ACTIONS], 
                 strategySum = new double[NUM_ACTIONS];
        
        private double[] getStrategy(double realizationWeight) {
            double normalizingSum = 0;
            for (int a = 0; a < NUM_ACTIONS; a++) {
                strategy[a] = regretSum[a] > 0 ? regretSum[a] : 0;
                normalizingSum += strategy[a];
            }
            for (int a = 0; a < NUM_ACTIONS; a++) {
                if (normalizingSum > 0)
                    strategy[a] /= normalizingSum;
                else
                    strategy[a] = 1.0 / NUM_ACTIONS;
                strategySum[a] += realizationWeight * strategy[a];
            }
            return strategy;
        }
        
        public double[] getAverageStrategy() {
            double[] avgStrategy = new double[NUM_ACTIONS];
            double normalizingSum = 0;
            for (int a = 0; a < NUM_ACTIONS; a++)
                normalizingSum += strategySum[a];
            for (int a = 0; a < NUM_ACTIONS; a++) 
                if (normalizingSum > 0)
                    avgStrategy[a] = strategySum[a] / normalizingSum;
                else
                    avgStrategy[a] = 1.0 / NUM_ACTIONS;
            return avgStrategy;
        }
        
        public String toString() {
                return String.format("%4s: %s", infoSet, Arrays.toString(getAverageStrategy()));
        }

    }
    
    public void train(int iterations) {
        int[] cards = {1, 2, 3};
        double util = 0;
        for (int i = 0; i < iterations; i++) {
            if (i % (iterations / 20) == 0) {
                int j = 0;

                for (Node n : nodeMap.values()) {
                    strategiesOverTime[i / (iterations / 20)][j] = n.getAverageStrategy();
                    j++;
                }
            }

            for (int c1 = cards.length - 1; c1 > 0; c1--) { 
                int c2 = random.nextInt(c1 + 1);
                int tmp = cards[c1];
                cards[c1] = cards[c2];
                cards[c2] = tmp;
            }

            util += cfr(cards, "", 1, 1);
        }
        System.out.println("EV of Player 1: " + util / iterations);
        System.out.println("EV of Player 2: " + util / iterations * -1);

        for (Node n : nodeMap.values())
            System.out.println(n);
    }
    
    private double cfr(int[] cards, String history, double p0, double p1) {
        int plays = history.length();
        int player = plays % 2;
        int opponent = 1 - player;
        if (plays > 1) {
            boolean terminalPass = history.charAt(plays - 1) == 'p';
            boolean doubleBet = history.substring(plays - 2, plays).equals("bb");
            boolean isPlayerCardHigher = cards[player] > cards[opponent];
            if (terminalPass)
                if (history.equals("pp"))
                    return isPlayerCardHigher ? 1 : -1;
                else
                    return 1;
            else if (doubleBet)
                return isPlayerCardHigher ? 2 : -2;
        }               

        String infoSet = cards[player] + history;
        Node node = nodeMap.get(infoSet);
        if (node == null) {
            node = new Node();
            node.infoSet = infoSet;
            nodeMap.put(infoSet, node);
        }

        double[] strategy = node.getStrategy(player == 0 ? p0 : p1);
        double[] util = new double[NUM_ACTIONS];
        double nodeUtil = 0;
        for (int a = 0; a < NUM_ACTIONS; a++) {
            String nextHistory = history + (a == 0 ? "p" : "b");
            util[a] = player == 0 
                ? - cfr(cards, nextHistory, p0 * strategy[a], p1)
                : - cfr(cards, nextHistory, p0, p1 * strategy[a]);
            nodeUtil += strategy[a] * util[a];
        }

        for (int a = 0; a < NUM_ACTIONS; a++) {
            double regret = util[a] - nodeUtil;
            node.regretSum[a] += (player == 0 ? p1 : p0) * regret;
        }

        return nodeUtil;
    }

    @Override
    public void start(Stage stage) throws IOException {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis(0.0, 1.0, 0.001);

        xAxis.setLabel("Iteration Count");
        yAxis.setLabel("Bet Frequency");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);

        XYChart.Series<Number, Number> ONE = new XYChart.Series<>();
        XYChart.Series<Number, Number> ONEb = new XYChart.Series<>();
        XYChart.Series<Number, Number> ONEp = new XYChart.Series<>();
        XYChart.Series<Number, Number> ONEpb = new XYChart.Series<>();
        XYChart.Series<Number, Number> TWO = new XYChart.Series<>();
        XYChart.Series<Number, Number> TWOb = new XYChart.Series<>();
        XYChart.Series<Number, Number> TWOp = new XYChart.Series<>();
        XYChart.Series<Number, Number> TWOpb = new XYChart.Series<>();
        XYChart.Series<Number, Number> THREE = new XYChart.Series<>();
        XYChart.Series<Number, Number> THREEb = new XYChart.Series<>();
        XYChart.Series<Number, Number> THREEp = new XYChart.Series<>();
        XYChart.Series<Number, Number> THREEpb = new XYChart.Series<>();

        lines.put(0, ONE);
        lines.put(1, ONEb);
        lines.put(2, ONEp);
        lines.put(3, ONEpb);
        lines.put(4, TWO);
        lines.put(5, TWOb);
        lines.put(6, TWOp);
        lines.put(7, TWOpb);
        lines.put(8, THREE);
        lines.put(9, THREEb);
        lines.put(10, THREEp);
        lines.put(11, THREEpb);

        ONE.setName("1");
        ONEb.setName("1b");
        ONEp.setName("1p");
        ONEpb.setName("1pb");
        TWO.setName("2");
        TWOb.setName("2b");
        TWOp.setName("2p");
        TWOpb.setName("2pb");
        THREE.setName("3");
        THREEb.setName("3b");
        THREEp.setName("3p");
        THREEpb.setName("3pb");

        for (int i = 0; i < strategiesOverTime.length; i++) {
            for (int j = 0; j < strategiesOverTime[0].length; j++) {
                lines.get(j).getData().add(new XYChart.Data(totalIterations / 20 * i, strategiesOverTime[i][j][1]));
            }
        }

        lineChart.getData().add(ONE);
        lineChart.getData().add(ONEb);
        lineChart.getData().add(ONEp);
        lineChart.getData().add(ONEpb);
        lineChart.getData().add(TWO);
        lineChart.getData().add(TWOb);
        lineChart.getData().add(TWOp);
        lineChart.getData().add(TWOpb);
        lineChart.getData().add(THREE);
        lineChart.getData().add(THREEb);
        lineChart.getData().add(THREEp);
        lineChart.getData().add(THREEpb);

        Scene scene = new Scene(lineChart, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(KuhnPokerSolver.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        totalIterations = 1000;
        new KuhnPokerSolver().train(totalIterations);
        launch();
    }

}