package xyz.morecraft.dev.malmo.util;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.TreeMap;

public class GridVisualizer extends JFrame {

    private GridVisualizerPanel gridVisualizerPanel;

    public GridVisualizer() throws HeadlessException {
        setTitle("GridVisualizer");
        setSize(800, 605);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setUILookAndFeel();

        setLayout(new GridLayout(1, 1));

        gridVisualizerPanel = new GridVisualizerPanel();
        add(gridVisualizerPanel);

    }

    public void updateGrid(String[][][] grid) {
        this.gridVisualizerPanel.setGrid(grid);
        this.repaint();
    }

    private static void setUILookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    private static class GridVisualizerPanel extends JPanel {

        private String[][][] grid;
        private static final Map<String, Color> colorMap;

        public void setGrid(String[][][] grid) {
            this.grid = grid;
            this.repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            final Graphics2D g2d = (Graphics2D) g;

            if (grid == null) {
                return;
            }

            final String[][] gr = grid[0];
            final int n = gr.length;
            final int maxDim = this.getHeight() > this.getWidth() ? this.getWidth() : this.getHeight();
            final int dim = maxDim * 9 / 10;
            final int margin = (maxDim - dim) / 2;
            final int step = dim / n;

            g2d.setStroke(new BasicStroke(3));

            for (int i1 = 0; i1 <= n; i1++) {
                g2d.drawLine(margin + (i1 * step), margin, margin + (i1 * step), dim + margin - 2);
                g2d.drawLine(margin, margin + (i1 * step), dim + margin - 2, margin + i1 * step);
            }

            for (int i = 0; i < gr.length; i++) {
                for (int j = 0; j < gr[i].length; j++) {
                    g2d.setColor(colorMap.get(gr[i][j]));
                    g2d.fillRect(margin + (j * step) + 2, margin + (i * step) + 2, step - 3, step - 3);
                }
            }

            g2d.setColor(Color.GREEN);

            int ovalWidth = step / 5;
            g2d.fillOval(margin + ((n / 2 + 1) * step) + ovalWidth*2, margin + ((n / 2 + 1) * step) + ovalWidth*2, ovalWidth, ovalWidth);

        }

        static {
            colorMap = new TreeMap<>(String::compareToIgnoreCase);
            colorMap.put("stone", Color.GRAY);
            colorMap.put("dirt", new Color(139, 69, 19));
        }

    }

}
