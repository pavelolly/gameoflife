package gameoflife;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class GameOfLifeFrame extends JFrame {
    public GameOfLifeFrame(GameOfLifeModel game) {
        if (game == null) {
            throw new IllegalArgumentException("game is null");
        }

        this.game = game;
        this.gameResetState = game.getState().copy();

        // order matters (is it bad?)
        this.setupFrame();
        this.setupPanels();
        this.setupButtons();
        this.setupSlider();
        this.setupLayout();
        this.setupUpdateThread();

        // frame visible
        this.setVisible(true);
    }

    /* Setting up */

    private void setupFrame() {
        this.setTitle("GoL");
        this.setSize(GameOfLifeFrame.PANEL_INIT_WIDTH, GameOfLifeFrame.PANEL_INIT_HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();

                if (GameOfLifeFrame.this.pressedKeys.contains(keyCode)) {
                    return;
                }

                switch (keyCode) {
                    case KeyEvent.VK_R:
                        GameOfLifeFrame.this.Reset();
                        break;
                    case KeyEvent.VK_C:
                        GameOfLifeFrame.this.Clear();
                        break;
                    case KeyEvent.VK_SPACE:
                        GameOfLifeFrame.this.TogglePlay();
                        break;
                    case KeyEvent.VK_ENTER:
                        if (!GameOfLifeFrame.this.updating) {
                            GameOfLifeFrame.this.Step();
                        }
                        break;
                    default:
                        return;
                }

                GameOfLifeFrame.this.pressedKeys.add(keyCode);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                GameOfLifeFrame.this.pressedKeys.remove(e.getKeyCode());
            }
        });
        this.setFocusable(true);
    }

    private void setupPanels() {
        this.centralPanel.setLayout(null);
        this.centralPanel.addMouseListener(this.centralPanel);
        this.centralPanel.addMouseMotionListener(this.centralPanel);
        this.centralPanel.addMouseWheelListener(this.centralPanel);

        this.bottomPanel.setLayout(new BorderLayout());
        this.bottomPanel.setBackground(colorBottomPanelBackground);

        this.bottomPanelLeft.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        this.bottomPanelLeft.setBackground(colorBottomPanelBackground);

        this.bottomPanelRight.setLayout(new FlowLayout());
        this.bottomPanelRight.setBackground(colorBottomPanelBackground);
    }

    private void setupButtons() {
        try {
            this.buttonImages.put("Reset", ImageIO.read(new File("icons/undo512x512.png")));
            this.buttonImages.put("Play", ImageIO.read(new File("icons/play-button-arrowhead512x512.png")));
            this.buttonImages.put("Stop", ImageIO.read(new File("icons/pause512x512.png")));
            this.buttonImages.put("Step", ImageIO.read(new File("icons/right512x512.png")));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        BufferedImage resetImage = this.buttonImages.get("Reset");
        BufferedImage playImage = this.buttonImages.get("Play");
        // BufferedImage stopImage = this.buttonImages.get("Stop");
        BufferedImage stepImage = this.buttonImages.get("Step");

        this.buttons.put("Clear", new JButton("Clear"));
        this.buttons.put("Reset", new JButton());
        this.buttons.put("Play",  new JButton());
        this.buttons.put("Step",  new JButton());

        JButton clearButton = this.buttons.get("Clear");
        JButton resetButton = this.buttons.get("Reset");
        JButton playButton = this.buttons.get("Play");
        JButton stepButton = this.buttons.get("Step");

        ComponentListener componentListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                JButton button = (JButton) e.getComponent();

                if (button == resetButton) {
                    setScaledIcon(button, resetImage, 0.85);
                } else if (button == playButton) {
                    setScaledIcon(button, playImage, 0.7);
                } else if (button == stepButton) {
                    setScaledIcon(button, stepImage, 1);
                }
            }
        };

        resetButton.setPreferredSize(new Dimension(30, 30));
        playButton.setPreferredSize(new Dimension(50, 50));
        stepButton.setPreferredSize(new Dimension(30, 30));

        clearButton.addActionListener(_ -> this.Clear());
        resetButton.addActionListener(_ -> this.Reset());
        playButton.addActionListener(_ -> this.TogglePlay());
        stepButton.addActionListener(_ -> this.Step());

        for (JButton button : this.buttons.values()) {
            button.addComponentListener(componentListener);
            button.setFocusable(false);
        }
    }

    private void setupSlider() {
        JLabel left = new JLabel(GameOfLifeFrame.TIME_DELTA_MIN+" ms");
        JLabel right = new JLabel(GameOfLifeFrame.TIME_DELTA_MAX+" ms");

        left.setForeground(Color.WHITE);
        right.setForeground(Color.WHITE);

        Dictionary<Integer, JLabel> labels = new Hashtable<>();
        labels.put(GameOfLifeFrame.TIME_DELTA_MIN, left);
        labels.put(GameOfLifeFrame.TIME_DELTA_MAX, right);

        this.slider.setLabelTable(labels);
        this.slider.setMinorTickSpacing(GameOfLifeFrame.TIME_DELTA_MAX - GameOfLifeFrame.TIME_DELTA_MIN);

        this.slider.setPaintLabels(true);
        this.slider.setPaintTicks(true);

        this.slider.setOpaque(false);

        this.slider.addChangeListener(e -> GameOfLifeFrame.this.timeDelta = ((JSlider) e.getSource()).getValue());
        this.slider.setFocusable(false);
    }

    private void setupLayout() {
        this.bottomPanelLeft.add(this.buttons.get("Clear"));
        this.bottomPanelLeft.add(this.buttons.get("Reset"));
        this.bottomPanelLeft.add(this.buttons.get("Play"));
        this.bottomPanelLeft.add(this.buttons.get("Step"));

        this.bottomPanelRight.add(this.slider);

        this.bottomPanel.add(this.bottomPanelLeft, BorderLayout.WEST);
        this.bottomPanel.add(this.bottomPanelRight, BorderLayout.EAST);

        this.add(this.bottomPanel, BorderLayout.SOUTH);
        this.add(this.centralPanel, BorderLayout.CENTER);
    }

    private void setupUpdateThread() {
        this.updateThread.setDaemon(true);
        this.updateThread.start();
    }

    /**/

    private static void setScaledIcon(JButton button, BufferedImage image, double factor) {
        double width = button.getPreferredSize().width * factor;
        double height = button.getPreferredSize().height * factor;

        button.setIcon(new ImageIcon(image.getScaledInstance((int)width, (int)height, Image.SCALE_SMOOTH)));
    }


    /* Actions attached to buttons and keyboard */

    private void Clear() {
        this.game.clearField();
        this.centralPanel.repaint();
    }

    private void Reset() {
        this.game.setState(this.gameResetState);
        // this.centralPanel.resetGrid();
        this.centralPanel.repaint();
    }

    private void TogglePlay() {
        JButton play = this.buttons.get("Play");
        JButton step = this.buttons.get("Step");

        if (this.updating) {
            setScaledIcon(play, this.buttonImages.get("Play"), 0.7);
            step.setEnabled(true);
        } else {
            setScaledIcon(play, this.buttonImages.get("Stop"), 0.7);
            step.setEnabled(false);
        }

        this.updating = !this.updating;
    }

    private void Step() {
        this.game.nextStep();
        this.centralPanel.repaint();
    }

    /* Fields */

    private static final int PANEL_INIT_WIDTH = 800;
    private static final int PANEL_INIT_HEIGHT = 600;
    private static final int TIME_DELTA_MIN = 50;
    private static final int TIME_DELTA_MAX = 1000;

    private final GameOfLifeModel game;
    private GameOfLifeModel.State gameResetState;

    private int timeDelta = (TIME_DELTA_MIN + TIME_DELTA_MAX) / 2;

    private final CentralPanel centralPanel = new CentralPanel();
    private final JPanel bottomPanel        = new JPanel();
    private final JPanel bottomPanelLeft    = new JPanel();
    private final JPanel bottomPanelRight   = new JPanel();

    private volatile boolean updating = false;
    private final Thread updateThread = new Thread(() -> {
        while (true) {
            if (!updating) {
                continue;
            }

            long before = System.currentTimeMillis();

            Step();
            centralPanel.repaint();

            while (System.currentTimeMillis() - before <= timeDelta);
        }
    });

    private final Map<String, JButton> buttons = new HashMap<>();
    private final Map<String, BufferedImage> buttonImages = new HashMap<>();

    private final JSlider slider = new JSlider(JSlider.HORIZONTAL, TIME_DELTA_MIN, TIME_DELTA_MAX, timeDelta);

    private final Set<Integer> pressedKeys = new HashSet<>();

    private final Color colorBottomPanelBackground = new Color(0xff333333); // grey : darker than background
                                                                                //        lighter than dead cell

    private class CentralPanel extends JPanel implements MouseInputListener, MouseWheelListener {
        public void resetGrid() {
            this.gridX = 100;
            this.gridY = 100;
            this.cellSize = 20;
            this.mouseLastKnownPosition = null;
        }

        @Override
        public void paint(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics;

            graphics2D.setBackground(this.colorBackground);
            graphics2D.clearRect(this.getBounds().x, this.getBounds().y, this.getWidth(), this.getHeight());

            GameOfLifeModel.State state = GameOfLifeFrame.this.game.getState();

            // draw field
            for (int y = 0; y < state.field.getRows(); ++y) {
                for (int x = 0; x < state.field.getCols(); ++x) {
                    graphics2D.setPaint(state.field.get(y, x) > 0 ? this.colorAliveCell : this.colorDeadCell);
                    graphics2D.fillRect(this.gridX + x * this.cellSize, this.gridY + y * this.cellSize, this.cellSize, this.cellSize);
                }
            }

            // draw grid
            graphics2D.setPaint(Color.BLACK);
            for (int i = 0; i < state.field.getRows(); ++i) {
                graphics2D.drawLine(this.gridX, this.gridY + i * this.cellSize,
                        this.gridX + state.field.getCols() * this.cellSize, this.gridY + i * this.cellSize);
            }
            for (int i = 0; i < state.field.getCols(); ++i) {
                graphics2D.drawLine(this.gridX + i * this.cellSize, this.gridY,
                        this.gridX + i * this.cellSize, this.gridY + state.field.getCols() * this.cellSize);
            }
        }

        private static final int CELL_SIZE_MIN = 5;
        private static final int CELL_SIZE_MAX = 60;

        private int cellSize = 20;
        private int gridX = 100;
        private int gridY = 100;

        private Color colorBackground = new Color(0xff505050); // grey
        private Color colorDeadCell   = new Color(0xff252525); // dark grey
        private Color colorAliveCell  = new Color(0xffFFB60B); // yellow-ish

        private Point mouseLastKnownPosition;
        private boolean mouseInBounds;

        @Override
        public void mouseClicked(MouseEvent e) {
            if (!this.mouseInBounds) {
                return;
            }

            Point position = e.getPoint();
            GameOfLifeModel.State state = GameOfLifeFrame.this.game.getState();

            int gridWidth  = this.cellSize * state.field.getCols();
            int gridHeight = this.cellSize * state.field.getRows();

            if (new Rectangle(this.gridX, this.gridY, gridWidth, gridHeight).contains(position)) {
                position.x -= this.gridX;
                position.y -= this.gridY;

                int x = position.x / this.cellSize;
                int y = position.y / this.cellSize;

                GameOfLifeFrame.this.game.toggleCell(y, x);
                this.repaint();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (!this.mouseInBounds) {
                return;
            }

            this.mouseLastKnownPosition = e.getPoint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {
            this.mouseInBounds = true;
        }

        @Override
        public void mouseExited(MouseEvent e) {
            this.mouseInBounds = false;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (this.mouseLastKnownPosition == null) {
                return;
            }

            Point currentPosition = e.getPoint();

            int diffX = currentPosition.x - this.mouseLastKnownPosition.x;
            int diffY = currentPosition.y - this.mouseLastKnownPosition.y;

            this.gridX += diffX;
            this.gridY += diffY;

            this.mouseLastKnownPosition = currentPosition;

            this.repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {}

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            // seems like you don't need this check here
            if (!this.mouseInBounds) {
                return;
            }

            if (e.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                return;
            }

            int rotation = e.getWheelRotation();

            this.cellSize += (-rotation) * 5;

            // clamp
            this.cellSize = Math.max(CentralPanel.CELL_SIZE_MIN, Math.min(CentralPanel.CELL_SIZE_MAX, this.cellSize));

            this.repaint();
        }
    }
}
