package main;

import java.awt.geom.Line2D;
import java.io.*;
import java.util.List;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.geom.Ellipse2D;
import java.util.logging.Logger;
import java.util.logging.Level;


//git
//git_R4
// GUI交互式
public class Main {
    private Graph graph;
    private JFrame frame;
    private JTextArea textArea;
    private JTextField word1Field, word2Field, startWordField, endWordField;
    private JTextArea newTextField;
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new Main().createAndShowGUI();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Exception occurred: {0}", e.getMessage());
            }
        });
    }

    private void createAndShowGUI() throws IOException {
        graph = new Graph();
        // 设置日志级别
        logger.setLevel(Level.ALL);
        // UI
        frame = new JFrame("Graph Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // 初始化文本区域，用于显示结果
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // 创建控制面板
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);


        // 创建并添加控件到控制面板
        addControl(controlPanel, gbc, 0, 0, 2, new JButton("Load Text File"), new LoadButtonListener());
        addControl(controlPanel, gbc, 0, 1, 2, new JButton("Display Graph"), new DisplayButtonListener());

        addControl(controlPanel, gbc, 0, 2, new JLabel("Word 1:"));
        word1Field = new JTextField();
        addControl(controlPanel, gbc, 1, 2, word1Field);

        addControl(controlPanel, gbc, 0, 3, new JLabel("Word 2:"));
        word2Field = new JTextField();
        addControl(controlPanel, gbc, 1, 3, word2Field);

        addControl(controlPanel, gbc, 0, 4, 2, new JButton("Find Bridge Words"), new BridgeWordsButtonListener());

        addControl(controlPanel, gbc, 0, 5, new JLabel("New Text:"));
        newTextField = new JTextArea(5, 20); // 设置行和列的数量以控制大小
        JScrollPane newTextScrollPane = new JScrollPane(newTextField);
        {
            gbc.gridx = 1; // 设置组件在网格中的起始列位置为第 1 列
            gbc.gridy = 5; // 设置组件在网格中的起始行位置为第 5 行
            gbc.gridwidth = 2; // 设置组件跨越两列
            gbc.fill = GridBagConstraints.BOTH; // 组件填充其显示区域，不仅水平填充，还包括垂直填充
            gbc.weightx = 1.0; // 设置水平扩展权重为 1.0，意味着在调整窗口大小时，该组件会水平扩展
            gbc.weighty = 1.0; // 设置垂直扩展权重为 1.0，意味着在调整窗口大小时，该组件会垂直扩展
            controlPanel.add(newTextScrollPane, gbc); // 将配置好的组件添加到控制面板中
            gbc.gridwidth = 1; // 重置 gridwidth 为 1，准备为下一个组件配置
            gbc.fill = GridBagConstraints.HORIZONTAL; // 重置 fill 为水平填充，准备为下一个组件配置
            gbc.weightx = 0; // 重置水平扩展权重为 0，准备为下一个组件配置
            gbc.weighty = 0; // 重置垂直扩展权重为 0，准备为下一个组件配置
        }
        addControl(controlPanel, gbc, 0, 6, 2, new JButton("Generate New Text"), new GenerateTextButtonListener());

        addControl(controlPanel, gbc, 0, 7, new JLabel("Start Word:"));
        startWordField = new JTextField();
        addControl(controlPanel, gbc, 1, 7, startWordField);

        addControl(controlPanel, gbc, 0, 8, new JLabel("End Word:"));
        endWordField = new JTextField();
        addControl(controlPanel, gbc, 1, 8, endWordField);

        addControl(controlPanel, gbc, 0, 9, 2, new JButton("Shortest Path"), new ShortestPathButtonListener());
        addControl(controlPanel, gbc, 0, 10, 2, new JButton("Random Walk"), new RandomWalkButtonListener());

        frame.add(controlPanel, BorderLayout.NORTH);
        frame.setVisible(true);
    }
    private void addControl(JPanel panel, GridBagConstraints gbc, int x, int y, JComponent component) {
        gbc.gridx = x;
        gbc.gridy = y;
        panel.add(component, gbc);
    }
    private void addControl(JPanel panel, GridBagConstraints gbc, int x, int y, int width, JComponent component, ActionListener listener) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        if (component instanceof JButton) {
            ((JButton) component).addActionListener(listener);
        }
        panel.add(component, gbc);
        gbc.gridwidth = 1; // 重置为默认值
    }
    private class LoadButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    graph.readFromFile(fileChooser.getSelectedFile().getPath());
                    textArea.setText("File loaded successfully.\n");
                } catch (IOException ex) {
                    textArea.setText("Error reading file: " + ex.getMessage() + "\n");
                }
            }
        }
    }

    private class DisplayButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                graph.saveGraphToFile("outputPng.png");
                textArea.setText("Graph displayed and saved as outputPng.png.\n");
                // 显示保存的图片
                // 读取并显示保存的图片
                BufferedImage bufferedImage = ImageIO.read(new File("outputPng.png"));
                ImageIcon imageIcon = new ImageIcon(bufferedImage);
                JLabel imageLabel = new JLabel(imageIcon);
                JOptionPane.showMessageDialog(frame, imageLabel, "Graph Image", JOptionPane.PLAIN_MESSAGE);
            } catch (IOException ex) {
                textArea.setText("Error saving graph: " + ex.getMessage() + "\n");
            }
        }
    }

    private class BridgeWordsButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String word1 = word1Field.getText();
            String word2 = word2Field.getText();
            String result;
            try {
                result = graph.queryBridgeWords(word1, word2);
            } catch (GraphException ex) {
                result = ex.getMessage();
            }
            textArea.setText(result + "\n");
        }
    }

    private class GenerateTextButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String newText = newTextField.getText();
            String result;
            try {
                result = graph.generateNewText(newText);
            } catch (GraphException ex) {
                result = ex.getMessage();
            }
            textArea.setText(result + "\n");
        }
    }

    private class ShortestPathButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String startWord = startWordField.getText();
            String endWord = endWordField.getText();
            String result;
            try {
                result = graph. calcShortestPath(startWord, endWord);
            } catch (GraphException ex) {
                result = ex.getMessage();
            }
            textArea.setText(result + "\n");
        }
    }

    private class RandomWalkButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String result;
            try {
                result = graph.randomWalk();
                OutputStream f = new FileOutputStream("randomWalk.txt");
                f.write(result.getBytes());
                f.close();
            } catch (GraphException ex) {
                result = ex.getMessage();
            }catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            textArea.setText(result + "\n");
        }
    }
}
// 命令行交互式
//public class Main {
//    public static void main(String[] args) {
//        Graph graph = new Graph();
//        Scanner scanner = new Scanner(System.in);
//        try {
//            System.out.print("Enter text file path (Absolute path or relative path in this project):");
//            String readTxtFilePath = scanner.nextLine();
//            System.out.println(readTxtFilePath);
//            graph.readFromFile(readTxtFilePath);
//            graph.displayGraph();
//            graph.saveGraphToFile("resources/outputPng.png");
//            while (true) {
//                System.out.println("Enter command (1: Bridge Words, 2: Generate New Text, 3: Shortest Path, 4: Random Walk, 5: Exit):");
//                int command = scanner.nextInt();
//                scanner.nextLine(); // consume newline
//
//                switch (command) {
//                    case 1:
//                        System.out.println("Enter word1:");
//                        String word1 = scanner.nextLine();
//                        System.out.println("Enter word2:");
//                        String word2 = scanner.nextLine();
//                        System.out.println(graph.findBridgeWords(word1, word2));
//                        break;
//                    case 2:
//                        System.out.println("Enter new text:");
//                        String newText = scanner.nextLine();
//                        System.out.println(graph.generateNewText(newText));
//                        break;
//                    case 3:
//                        System.out.println("Enter start word:");
//                        String startWord = scanner.nextLine();
//                        System.out.println("Enter end word:");
//                        String endWord = scanner.nextLine();
//                        System.out.println(graph.shortestPath(startWord, endWord));
//                        break;
//                    case 4:
//                        System.out.println("Enter start word for random walk:");
//                        System.out.println(graph.randomWalk());
//                        break;
//                    case 5:
//                        scanner.close();
//                        return;
//                    default:
//                        System.out.println("Invalid command.");
//                }
//            }
//        } catch (IOException e) {
//            System.err.println("Error reading file: " + e.getMessage());
//        } catch (GraphException e) {
//            System.err.println("Graph error: " + e.getMessage());
//        }
//    }
//}


class Node {
    private final String word;
    private int distance;

    public Node(String word) {
        this.word = word;
        this.distance = Integer.MAX_VALUE;
    }

    public String getWord() {
        return word;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
}
class GraphException extends Exception {
    public GraphException(String message) {
        super(message);
    }
}
class Graph {

    private final Map<String, Node> nodes = new HashMap<>();
    private final Map<String, Map<String, Integer>> adjList = new HashMap<>();
    public void saveGraphToFile(String filePath) throws IOException {
        // 计算图的宽度和高度
        int width = 800;
        int height = 600;

        // 创建图形对象
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // 清空背景
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // 设置字体
        Font font = new Font("Arial", Font.PLAIN, 12);
        g2d.setFont(font);

        // 计算节点位置
        Map<String, Point> nodePositions = calculateNodePositions();

        // 绘制边
        g2d.setColor(Color.BLACK);
        for (String node : adjList.keySet()) {
            Point startPoint = nodePositions.get(node);
            Map<String, Integer> neighbors = adjList.get(node);
            for (Map.Entry<String, Integer> entry : neighbors.entrySet()) {
                Point endPoint = nodePositions.get(entry.getKey());
                int weight = entry.getValue();
                drawEdge(g2d, startPoint, endPoint, weight);
            }
        }

        // 绘制节点
        for (String node : nodes.keySet()) {
            Point position = nodePositions.get(node);
            drawNode(g2d, position, node);
        }

        // 保存图形文件
        ImageIO.write(image, "PNG", new File(filePath));
        System.out.println("Saved graph to " + filePath);
        // 释放资源
        g2d.dispose();
    }

    private Map<String, Point> calculateNodePositions() {
        int numNodes = nodes.size();
        int radius = 200;
        int centerX = 400;
        int centerY = 300;
        double angleIncrement = 2 * Math.PI / numNodes;

        Map<String, Point> nodePositions = new HashMap<>();
        int i = 0;
        for (String node : nodes.keySet()) {
            double angle = i * angleIncrement;
            int x = (int) (centerX + radius * Math.cos(angle));
            int y = (int) (centerY + radius * Math.sin(angle));
            nodePositions.put(node, new Point(x, y));
            i++;
        }

        return nodePositions;
    }

    private void drawNode(Graphics2D g2d, Point position, String label) {
        int radius = 20;
        int x = position.x - radius;
        int y = position.y - radius;
        g2d.setColor(Color.BLACK);
        g2d.fill(new Ellipse2D.Double(x, y, 2 * radius, 2 * radius));
        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        int labelWidth = fm.stringWidth(label);
        int labelHeight = fm.getHeight();
        g2d.drawString(label, x + (2 * radius - labelWidth) / 2, y + radius + labelHeight / 2);
    }

    private void drawEdge(Graphics2D g2d, Point startPoint, Point endPoint, int weight) {
        // 绘制线条
        g2d.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
        // 绘制权重标签
        int labelX = (startPoint.x + endPoint.x) / 2;
        int labelY = (startPoint.y + endPoint.y) / 2;
        g2d.drawString(String.valueOf(weight), labelX, labelY);
        // 计算箭头
        drawArrowHead(g2d, startPoint, endPoint);
    }
    private void drawArrowHead(Graphics2D g2d, Point startPoint, Point endPoint) {
        double phi = Math.toRadians(20);
        int barb = 15;

        double dy = endPoint.y - startPoint.y;
        double dx = endPoint.x - startPoint.x;
        double theta = Math.atan2(dy, dx);
        double rho = theta + phi;

        int radius = 20;
        // 修正箭头方位
        int arrowX = endPoint.x - (int) (radius * Math.cos(theta));
        int arrowY = endPoint.y - (int) (radius * Math.sin(theta));
        for (int j = 0; j < 2; j++) {
            double x = arrowX - barb * Math.cos(rho);
            double y = arrowY - barb * Math.sin(rho);
            g2d.draw(new Line2D.Double(arrowX, arrowY, x, y));
            rho = theta - phi;
        }
    }

    public void readFromFile(String filePath) throws IOException {
        init();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            processLine(line);
        }
        reader.close();
    }
    private void init() {
        // Clear Graph at last time
        nodes.clear();
        adjList.clear();
    }

    private void processLine(String line) {
        // 匹配所有非字母和非空白字符，并将它们替换为空格。
        line = line.replaceAll("[^a-zA-Z\\s]", " ").toLowerCase();
        // 将处理过的字符串按空白字符分割成多个单词
        String[] words = line.split("\\s+");
        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i];
            String word2 = words[i + 1];
            if (!word1.isEmpty() && !word2.isEmpty()) {
                addEdge(word1, word2);
            }
        }
    }

    private void addEdge(String word1, String word2) {
        nodes.putIfAbsent(word1, new Node(word1));
        nodes.putIfAbsent(word2, new Node(word2));

        adjList.computeIfAbsent(word1, k -> new HashMap<>())
                .merge(word2, 1, Integer::sum);
    }

    public void displayGraph() {
        for (String node : adjList.keySet()) {
            System.out.print(node + " -> ");
            for (Map.Entry<String, Integer> entry : adjList.get(node).entrySet()) {
                System.out.print(entry.getKey() + "(" + entry.getValue() + ") ");
            }
            System.out.println();
        }
    }

    public String queryBridgeWords(String word1, String word2) throws GraphException {
        if (!nodes.containsKey(word1) || !nodes.containsKey(word2)) {
            return "No word1 or word2 in the graph!";
        }

        Set<String> bridgeWords = new HashSet<>();
        if (adjList.containsKey(word1)) {
            for (String middle : adjList.get(word1).keySet()) {
                if (adjList.containsKey(middle) && adjList.get(middle).containsKey(word2)) {
                    bridgeWords.add(middle);
                }
            }
        }

        if (bridgeWords.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        } else {
            return "The bridge words from " + word1 + " to " + word2 + " are: " +
                    String.join(", ", bridgeWords);
        }
    }

    public String generateNewText(String inputText) throws GraphException {
        inputText = inputText.replaceAll("[^a-zA-Z\\s]", " ").toLowerCase();
        String[] words = inputText.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i];
            String word2 = words[i + 1];
            result.append(word1).append(" ");
            String bridgeWordResult = queryBridgeWords(word1, word2);
            if (!bridgeWordResult.startsWith("No")) {
                String[] bridgeWords = bridgeWordResult.split(": ")[1].split(", ");
                if (bridgeWords.length > 0) {
                    result.append(bridgeWords[0]).append(" ");
                }
            }
        }
        result.append(words[words.length - 1]);
        return result.toString();
    }

    public String  calcShortestPath(String word1, String word2) throws GraphException {
        if (!nodes.containsKey(word1) || !nodes.containsKey(word2)) {
            return "No startWord or endWord in the graph!";
        }

        // Implement Dijkstra's algorithm
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previousNodes = new HashMap<>();
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(Node::getDistance));

        for (String node : nodes.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
            previousNodes.put(node, null);
        }
        distances.put(word1, 0);

        Node startNode = nodes.get(word1);
        startNode.setDistance(0);
        queue.add(startNode);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (current.getWord().equals(word2)) {
                break;
            }

            for (Map.Entry<String, Integer> neighbor : adjList.getOrDefault(current.getWord(), new HashMap<>()).entrySet()) {
                int newDist = distances.get(current.getWord()) + neighbor.getValue();
                if (newDist < distances.get(neighbor.getKey())) {
                    distances.put(neighbor.getKey(), newDist);
                    previousNodes.put(neighbor.getKey(), current.getWord());
                    Node nextNode = nodes.get(neighbor.getKey());
                    nextNode.setDistance(newDist);
                    queue.add(nextNode);
                }
            }
        }

        if (distances.get(word2) == Integer.MAX_VALUE) {
            return "No path from " + word1 + " to " + word2 + "!";
        }

        List<String> path = new LinkedList<>();
        for (String at = word2; at != null; at = previousNodes.get(at)) {
            path.add(0, at);
        }

        return "Shortest path from " + word1 + " to " + word2 + " is: " + String.join(" -> ", path) +
                " with total weight " + distances.get(word2);
    }

    public String randomWalk() throws GraphException {
        String startWord = "NO";
        Random random = new Random();
        // rand a start node
        {
            int randNodeIndex = random.nextInt(nodes.size());
            int currentNodeIndex = 1;
            for (String sword : nodes.keySet()) {
                if (currentNodeIndex == randNodeIndex) {
                    startWord = sword;
                }
                currentNodeIndex ++;
            }
        }
        if (! nodes.containsKey(startWord)) {
            return "No startWord in the graph!";
        }
        StringBuilder walk = new StringBuilder(startWord);
        String currentWord = startWord;

        while (adjList.containsKey(currentWord) && !adjList.get(currentWord).isEmpty()) {
            Map<String, Integer> neighbors = adjList.get(currentWord);
            int totalWeight = neighbors.values().stream().mapToInt(Integer::intValue).sum();
            int rand = random.nextInt(totalWeight);
            int cumulativeWeight = 0;

            for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
                cumulativeWeight += neighbor.getValue();
                if (rand < cumulativeWeight) {
                    currentWord = neighbor.getKey();
                    walk.append(" -> ").append(currentWord);
                    break;
                }
            }
        }
        return "Random walk starting from " + startWord + ": " + walk;
    }
}

