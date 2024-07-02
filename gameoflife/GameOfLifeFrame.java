package gameoflife;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GameOfLifeFrame extends JFrame {
    public GameOfLifeFrame(GameOfLifeModel game) {
        if (game == null) {
            throw new IllegalArgumentException("game is null");
        }

        // game
        mGame = game;
        mGameInitialState = game.getState();

        // time delta
        mUpdateThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (!mUpdating) {
                        continue;
                    }

                    long before = System.currentTimeMillis();

                    nextStep();
                    mCentralPanel.repaint();

                    while (System.currentTimeMillis() - before <= mTimeDelta) {}
                }
            }
        };
        mUpdateThread.setDaemon(true);
        mUpdateThread.start();

        mUpdating = false;
        mTimeDelta = (M_TIME_DELTA_MIN + M_TIME_DELTA_MAX) / 2;

        // frame
        this.setTitle("GoL");
        this.setSize(M_PANEL_INIT_WIDTH, M_PANEL_INIT_HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());

        // panels
        mCentralPanel = new CentralPanel();
        mCentralPanel.setLayout(null);

        mBottomPanel = new JPanel();
        mBottomPanel.setLayout(new BorderLayout());
        mBottomPanel.setBackground(Color.BLUE);

        mBottomLeftPanel = new JPanel();
        mBottomLeftPanel.setLayout(new FlowLayout());
        mBottomLeftPanel.setBackground(Color.RED);
        
        mBottomRightPanel = new JPanel();
        mBottomRightPanel.setLayout(new FlowLayout());
        mBottomRightPanel.setBackground(Color.GREEN);

        // buttons
        mButtons = new HashMap<>();
        mButtons.put("Clear", new JButton("Clear"));
        mButtons.put("Reset", new JButton("Reset"));
        mButtons.put("Play",  new JButton("Play"));
        mButtons.put("Step",  new JButton("Step"));

        for (JButton button : mButtons.values()) {
            button.setFocusable(false);
            button.addActionListener(new ActionListenerButtons());
        }

        // slider
        mSlider = new JSlider(JSlider.HORIZONTAL, M_TIME_DELTA_MIN, M_TIME_DELTA_MAX, mTimeDelta);
        mSlider.addChangeListener(new ChangeListenerSlider());
        
        // binding
        mBottomLeftPanel.add(mButtons.get("Clear"));
        mBottomLeftPanel.add(mButtons.get("Reset"));
        mBottomLeftPanel.add(mButtons.get("Play"));
        mBottomLeftPanel.add(mButtons.get("Step"));

        mBottomRightPanel.add(mSlider);

        mBottomPanel.add(mBottomLeftPanel, BorderLayout.WEST);
        mBottomPanel.add(mBottomRightPanel, BorderLayout.EAST);
        
        this.add(mBottomPanel, BorderLayout.SOUTH);
        this.add(mCentralPanel, BorderLayout.CENTER);

        // visible
        this.setVisible(true);
    }

    private void nextStep() {
        mGame.nextStep();
        mCentralPanel.repaint();
    }

    private GameOfLifeModel mGame;
    private GameOfLifeModel.State mGameInitialState;

    private Thread mUpdateThread;
    private volatile boolean mUpdating;
    private int mTimeDelta;

    private final int M_TIME_DELTA_MIN = 200;
    private final int M_TIME_DELTA_MAX = 1000;

    private final int M_PANEL_INIT_WIDTH = 800;
    private final int M_PANEL_INIT_HEIGHT = 600;

    private CentralPanel mCentralPanel;
    private JPanel mBottomPanel;
    private JPanel mBottomLeftPanel;
    private JPanel mBottomRightPanel;

    private HashMap<String, JButton> mButtons;
    private JSlider mSlider;

    private class ActionListenerButtons implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            Object source = event.getSource();

            if (source == mButtons.get("Clear")) {
                mGame.clearField();
                mCentralPanel.repaint();
            } else if (source == mButtons.get("Reset")) {
                mGame.setState(mGameInitialState);
                mCentralPanel.repaint();
            } else if (source == mButtons.get("Play")) {
                var button = (JButton)source;
                
                if (mUpdating) {
                    button.setText("Play");
                    mButtons.get("Step").setEnabled(true);
                    mUpdating = false;
                } else {
                    button.setText("Stop");
                    mButtons.get("Step").setEnabled(false);
                    mUpdating = true;
                }

                // mUpdating = !mUpdating;
                
            } else if (source == mButtons.get("Step")) {
                nextStep();
            }
        }
    }

    public class ChangeListenerSlider implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            var slider = (JSlider) e.getSource();

            mTimeDelta = slider.getValue();
        }
    }

    private class CentralPanel extends JPanel {
        @Override
        public void paint(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics;
    
            graphics2D.setBackground(mBackgroundColor);
            graphics2D.clearRect(this.getBounds().x, this.getBounds().y, this.getWidth(), this.getHeight());

            GameOfLifeModel.State state = mGame.getState();

            // draw field
            for (int y = 0; y < state.field.length; ++y) {
                for (int x = 0; x < state.field[y].length; ++x) {
                    graphics2D.setPaint(state.field[y][x] > 0 ? mAliveCellColor : mDeadCellColor);
                    graphics2D.fillRect(mGridX + x * mCellWidth, mGridY + y * mCellHeight, mCellWidth, mCellHeight);
                }
            }

            // draw grid
            graphics2D.setPaint(Color.BLACK);
            for (int i = 0; i < state.field.length; ++i) {
                graphics2D.drawLine(mGridX, mGridY + i * mCellHeight, mGridX + state.field[0].length * mCellWidth, mGridY + i * mCellHeight);
            }
            for (int i = 0; i < state.field[0].length; ++i) {
                graphics2D.drawLine(mGridX + i * mCellWidth, mGridY, mGridX + i * mCellWidth, mGridY + state.field[0].length * mCellHeight);
            }
        }

        private int mCellWidth  = 20;
        private int mCellHeight = 20;
        private int mGridX = 100;
        private int mGridY = 100;

        private Color mBackgroundColor = new Color(0xff505050); // grey
        private Color mDeadCellColor   = new Color(0xff252525); // dark grey
        private Color mAliveCellColor  = new Color(0xffFFB60B); // yellow-ish
    }
}
