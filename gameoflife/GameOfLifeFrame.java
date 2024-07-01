package gameoflife;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GameOfLifeFrame extends JFrame {
    public GameOfLifeFrame(GameOfLifeModel game) {
        if (game == null) {
            throw new IllegalArgumentException("game is null");
        }

        // game
        mGame = game;
        mGameInitialState = game.getState();

        // timer
        mUpdateTimer = new Timer();
        mTimerIsScheduled = false;

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
        
        // binding
        mBottomLeftPanel.add(mButtons.get("Clear"));
        mBottomLeftPanel.add(mButtons.get("Reset"));
        mBottomLeftPanel.add(mButtons.get("Play"));
        mBottomLeftPanel.add(mButtons.get("Step"));

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
    private Timer mUpdateTimer;
    private boolean mTimerIsScheduled;

    private final int M_PANEL_INIT_WIDTH = 800;
    private final int M_PANEL_INIT_HEIGHT = 600;

    private CentralPanel mCentralPanel;
    private JPanel mBottomPanel;
    private JPanel mBottomLeftPanel;
    private JPanel mBottomRightPanel;

    private HashMap<String, JButton> mButtons;

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
                
                if (mTimerIsScheduled) {
                    mUpdateTimer.cancel();
                    mUpdateTimer = new Timer();

                    button.setText("Play");
                    mButtons.get("Step").setEnabled(true);
                } else {
                    mUpdateTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            nextStep();
                        }
                    }, 1000, 1000);

                    button.setText("Stop");
                    mButtons.get("Step").setEnabled(false);
                }
                
                mTimerIsScheduled = !mTimerIsScheduled;
            } else if (source == mButtons.get("Step")) {
                nextStep();
            }
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
