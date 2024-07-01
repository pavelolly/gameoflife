package gameoflife;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GameOfLifeFrame extends JFrame {
    public GameOfLifeFrame(GameOfLifeModel game) {
        // game
        mGame = game;

        // frame
        this.setTitle("GoL");
        this.setSize(M_PANEL_INIT_WIDTH, M_PANEL_INIT_HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());

        // panels
        mCentralPanel = new GameOfLifeCentralPanel();
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
        mClearButton = new JButton("Clear");
        mResetButton = new JButton("Reset");
        mPlayButton  = new JButton("Play");
        mStepButton  = new JButton("Step");
        
        // binding
        mBottomLeftPanel.add(mClearButton);
        mBottomLeftPanel.add(mResetButton);

        mBottomRightPanel.add(mPlayButton);
        mBottomRightPanel.add(mStepButton);

        mBottomPanel.add(mBottomLeftPanel, BorderLayout.EAST);
        mBottomPanel.add(mBottomRightPanel, BorderLayout.WEST);
        
        this.add(mBottomPanel, BorderLayout.SOUTH);
        this.add(mCentralPanel, BorderLayout.CENTER);

        // visible
        this.setVisible(true);
    }

    private final int M_PANEL_INIT_WIDTH = 800;
    private final int M_PANEL_INIT_HEIGHT = 600;

    private GameOfLifeCentralPanel mCentralPanel;
    private JPanel mBottomPanel;
    private JPanel mBottomLeftPanel;
    private JPanel mBottomRightPanel;

    private JButton mClearButton;
    private JButton mResetButton;
    private JButton mPlayButton;
    private JButton mStepButton;

    private GameOfLifeModel mGame;

    private class GameOfLifeCentralPanel extends JPanel {
        @Override
        public void paint(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics;
    
            graphics2D.setBackground(mBackgroundColor);
            graphics2D.clearRect(this.getBounds().x, this.getBounds().y, this.getWidth(), this.getHeight());

            GameOfLifeModel.State state = mGame.getState();

            // draw grid
            for (int y = 0; y < state.field.length; ++y) {
                for (int x = 0; x < state.field[y].length; ++x) {
                    if (state.field[x][y] > 0) {
                        graphics2D.setPaint(mAliveCellColor);
                    } else {
                        graphics2D.setPaint(mDeadCellColor);
                    }

                    graphics2D.fillRect(mGridX + y * mCellWidth, mGridY + x * mCellHeight, mCellWidth, mCellHeight);

                    graphics2D.setPaint(Color.BLACK);
                    graphics2D.drawRect(mGridX + y * mCellWidth, mGridY + x * mCellHeight, mCellWidth, mCellHeight);
                }
            }
        }

        private int mCellWidth  = 20;
        private int mCellHeight = 20;
        private int mGridX = 100;
        private int mGridY = 100;

        private Color mBackgroundColor = new Color(0xff505050);
        private Color mDeadCellColor = new Color(0xff181818);
        private Color mAliveCellColor = new Color(0xffFFB60B);
    }
}
