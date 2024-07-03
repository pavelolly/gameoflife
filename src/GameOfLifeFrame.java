import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.MouseInputListener;

public class GameOfLifeFrame extends JFrame {
    public GameOfLifeFrame(GameOfLifeModel game) {
        if (game == null) {
            throw new IllegalArgumentException("game is null");
        }

        // game info
        this.game = game;
        this.gameResetState = game.getState().copy();

        // setting up frame
        this.setTitle("GoL");
        this.setSize(PANEL_INIT_WIDTH, PANEL_INIT_HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (GameOfLifeFrame.this.pressedKeys.contains(e.getKeyCode())) {
                    return;
                }

                switch (e.getKeyCode()) {
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

                GameOfLifeFrame.this.pressedKeys.add(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                GameOfLifeFrame.this.pressedKeys.remove(e.getKeyCode());
            }
        });
        this.setFocusable(true);

        // panels
        this.centralPanel.setLayout(null);
        this.centralPanel.addMouseListener(this.centralPanel);
        this.centralPanel.addMouseMotionListener(this.centralPanel);
        this.centralPanel.addMouseWheelListener(this.centralPanel);

        this.bottomPanel.setLayout(new BorderLayout());
        this.bottomPanel.setBackground(Color.BLUE);

        this.bottomPanelLeft.setLayout(new FlowLayout());
        this.bottomPanelLeft.setBackground(Color.RED);

        this.bottomPanelRight.setLayout(new FlowLayout());
        this.bottomPanelRight.setBackground(Color.GREEN);

        // buttons
        this.buttons.put("Clear", new JButton("Clear"));
        this.buttons.put("Reset", new JButton("Reset"));
        this.buttons.put("Play",  new JButton("Play"));
        this.buttons.put("Step",  new JButton("Step"));

        this.buttons.get("Clear").addActionListener(_ -> GameOfLifeFrame.this.Clear());
        this.buttons.get("Reset").addActionListener(_ -> GameOfLifeFrame.this.Reset());
        this.buttons.get("Play").addActionListener(_ -> GameOfLifeFrame.this.TogglePlay());
        this.buttons.get("Step").addActionListener(_ -> GameOfLifeFrame.this.Step());

        for (JButton button : this.buttons.values()) {
            button.setFocusable(false);
        }

        // slider
        this.slider.addChangeListener(e -> GameOfLifeFrame.this.timeDelta = ((JSlider) e.getSource()).getValue());
        this.slider.setFocusable(false);
        
        // setting up layout
        this.bottomPanelLeft.add(this.buttons.get("Clear"));
        this.bottomPanelLeft.add(this.buttons.get("Reset"));
        this.bottomPanelLeft.add(this.buttons.get("Play"));
        this.bottomPanelLeft.add(this.buttons.get("Step"));

        this.bottomPanelRight.add(this.slider);

        this.bottomPanel.add(this.bottomPanelLeft, BorderLayout.WEST);
        this.bottomPanel.add(this.bottomPanelRight, BorderLayout.EAST);
        
        this.add(this.bottomPanel, BorderLayout.SOUTH);
        this.add(this.centralPanel, BorderLayout.CENTER);

        // starting update thread
        this.updateThread.setDaemon(true);
        this.updateThread.start();

        // frame visible
        this.setVisible(true);
    }

    /* Actions attached to buttons and keyboard */

    public void Clear() {
        this.game.clearField();
        this.centralPanel.repaint();
    }

    public void Reset() {
        this.game.setState(this.gameResetState);
        // this.centralPanel.resetGrid();
        this.centralPanel.repaint();
    }

    public void TogglePlay() {
        JButton play = this.buttons.get("Play");
        JButton step = this.buttons.get("Step");

        if (this.updating) {
            play.setText("Play");
            step.setEnabled(true);
        } else {
            play.setText("Stop");
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
    private static final int TIME_DELTA_MIN = 200;
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

    private final JSlider slider = new JSlider(JSlider.HORIZONTAL, TIME_DELTA_MIN, TIME_DELTA_MAX, timeDelta);

    private final Set<Integer> pressedKeys = new HashSet<>();

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
