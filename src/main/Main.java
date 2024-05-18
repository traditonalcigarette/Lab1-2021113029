package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.awt.geom.Ellipse2D;


public class Main {
    public static void main(String[] args) {
        Graph graph = new Graph();
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.print("Enter text file path (Absolute path or relative path in this project):");
            String readTxtFilePath = scanner.nextLine();
            System.out.println(readTxtFilePath);
            graph.readFromFile(readTxtFilePath);
            graph.displayGraph();
            graph.saveGraphToFile("resources/outputPng.png");
            while (true) {
                System.out.println("Enter command (1: Bridge Words, 2: Generate New Text, 3: Shortest Path, 4: Random Walk, 5: Exit):");
                int command = scanner.nextInt();
                scanner.nextLine(); // consume newline

                switch (command) {
                    case 1:
                        System.out.println("Enter word1:");
                        String word1 = scanner.nextLine();
                        System.out.println("Enter word2:");
                        String word2 = scanner.nextLine();
                        System.out.println(graph.findBridgeWords(word1, word2));
                        break;
                    case 2:
                        System.out.println("Enter new text:");
                        String newText = scanner.nextLine();
                        System.out.println(graph.generateNewText(newText));
                        break;
                    case 3:
                        System.out.println("Enter start word:");
                        String startWord = scanner.nextLine();
                        System.out.println("Enter end word:");
                        String endWord = scanner.nextLine();
                        System.out.println(graph.shortestPath(startWord, endWord));
                        break;
                    case 4:
                        System.out.println("Enter start word for random walk:");
                        System.out.println(graph.randomWalk());
                        break;
                    case 5:
                        scanner.close();
                        return;
                    default:
                        System.out.println("Invalid command.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (GraphException e) {
            System.err.println("Graph error: " + e.getMessage());
        }
    }
}

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
        g2d.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
        int labelX = (startPoint.x + endPoint.x) / 2;
        int labelY = (startPoint.y + endPoint.y) / 2;
        g2d.drawString(String.valueOf(weight), labelX, labelY);
    }
    public void readFromFile(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            processLine(line);
        }
        reader.close();
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

    public String findBridgeWords(String word1, String word2) throws GraphException {
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

    public String generateNewText(String newText) throws GraphException {
        newText = newText.replaceAll("[^a-zA-Z\\s]", " ").toLowerCase();
        String[] words = newText.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i];
            String word2 = words[i + 1];
            result.append(word1).append(" ");
            String bridgeWordResult = findBridgeWords(word1, word2);
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

    public String shortestPath(String startWord, String endWord) throws GraphException {
        if (!nodes.containsKey(startWord) || !nodes.containsKey(endWord)) {
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
        distances.put(startWord, 0);

        Node startNode = nodes.get(startWord);
        startNode.setDistance(0);
        queue.add(startNode);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (current.getWord().equals(endWord)) {
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

        if (distances.get(endWord) == Integer.MAX_VALUE) {
            return "No path from " + startWord + " to " + endWord + "!";
        }

        List<String> path = new LinkedList<>();
        for (String at = endWord; at != null; at = previousNodes.get(at)) {
            path.add(0, at);
        }

        return "Shortest path from " + startWord + " to " + endWord + " is: " + String.join(" -> ", path) +
                " with total weight " + distances.get(endWord);
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
