package xyz.morecraft.dev.malmo.util;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.TreeMap;

import static xyz.morecraft.dev.malmo.util.Blocks.*;

public class GridVisualizer extends JFrame {

    private GridVisualizerPanel gridVisualizerPanel;

    public GridVisualizer() throws HeadlessException {
        this(false, false);
    }

    public GridVisualizer(boolean isVisible, boolean alwaysOnTop) throws HeadlessException {
        setTitle("GridVisualizer");
        setSize(300, 300);
        setResizable(false);
        setLayout(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width - getWidth() - 64, 128);

        setUILookAndFeel();

        setLayout(new GridLayout(1, 1));

        gridVisualizerPanel = new GridVisualizerPanel();
        add(gridVisualizerPanel);

        setAlwaysOnTop(alwaysOnTop);
        setVisible(isVisible);
    }

//    private Dimension moveToLastDisplay() {
//        setVisible(false);
//        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//        GraphicsDevice[] screens = ge.getScreenDevices();
//        GraphicsDevice screen = screens[screens.length - 1];
//        JFrame dummy = new JFrame(screen.getDefaultConfiguration());
//        setLocationRelativeTo(dummy);
//        dummy.dispose();
//        setVisible(true);
//        final Rectangle bounds = screen.getDefaultConfiguration().getBounds();
//        return new Dimension(bounds.width + screens[0].getDefaultConfiguration().getBounds().width, bounds.height);
//    }

    public synchronized void updateGrid(String[][] grid) {
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

    public void drawAngle(double angle) {
        this.gridVisualizerPanel.setAngle(angle);
        this.repaint();
    }

    public void drawDir(int goDirection) {
        this.gridVisualizerPanel.setDir(goDirection);
        this.repaint();
    }

    private static class GridVisualizerPanel extends JPanel {

        private static final Map<String, Color> colorMap;

        private String[][] grid;
        private double angle;
        private int dir;

        private void setGrid(String[][] grid) {
            this.grid = grid;
        }

        private void setAngle(double angle) {
            this.angle = angle;
        }

        private void setDir(int dir) {
            this.dir = dir;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            final Graphics2D g2d = (Graphics2D) g;

            if (grid == null) {
                return;
            }

            final int n = grid.length;
            final int maxDim = this.getHeight() > this.getWidth() ? this.getWidth() : this.getHeight();
            final int dim = maxDim * 9 / 10;
            final int margin = (maxDim - dim) / 2;
            final int step = dim / n;
            final int middle = margin + dim / 2;

            g2d.setStroke(new BasicStroke(3));

            for (int i1 = 0; i1 <= n; i1++) {
                g2d.drawLine(margin + (i1 * step), margin, margin + (i1 * step), dim + margin - 2);
                g2d.drawLine(margin, margin + (i1 * step), dim + margin - 2, margin + i1 * step);
            }

            for (int i = 0; i < grid.length; i++) {
                for (int j = 0; j < grid[i].length; j++) {
                    g2d.setColor(colorMap.getOrDefault(grid[i][j], Color.BLACK));
                    g2d.fillRect(margin + (j * step) + 2, margin + (i * step) + 2, step - 3, step - 3);
                }
            }

            g2d.setColor(Color.GREEN);

            final int dirStep = step / 3;
            g2d.drawString("0", middle - 2, middle - dirStep + 3);
            g2d.drawString("1", middle + dirStep - 2, middle + 2);
            g2d.drawString("2", middle - 2, middle + dirStep + 3);
            g2d.drawString("3", middle - dirStep - 2, middle + 2);

            int ovalWidth = step / 5;
            //noinspection SuspiciousNameCombination
            g2d.fillOval(margin + ((n / 2) * step) + ovalWidth * 2, margin + ((n / 2) * step) + ovalWidth * 2, ovalWidth, ovalWidth);

            if (angle >= 0) {
                double newAngle = Math.abs(360 - angle);
                newAngle += 270;
                newAngle = newAngle % 360;
                newAngle = Math.toRadians(newAngle);
                int angleWidth = (dim / 2) - margin;
                int endX = (int) (middle + angleWidth * Math.sin(newAngle));
                int endY = (int) (middle + angleWidth * Math.cos(newAngle));
                g2d.setColor(Color.GREEN);
                g2d.drawLine(middle, middle, endX, endY);
            }

        }

        static {
            colorMap = new TreeMap<>(String::compareToIgnoreCase);
            colorMap.put(BLOCK_AIR, Color.LIGHT_GRAY);
            colorMap.put(BLOCK_STONE, Color.GRAY);
            colorMap.put(BLOCK_GLOWSTONE, Color.YELLOW);
            colorMap.put(BLOCK_GRASS, new Color(0, 100, 0));
            colorMap.put(BLOCK_DIRT, new Color(139, 69, 19));
        }

    }

}
