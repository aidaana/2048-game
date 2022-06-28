import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class BoardView extends JPanel{

    private static final Color BOARD_COLOR = new Color(189, 172, 159);
    private static final Color EMPTY_CELL_COLOR = new Color(205, 190, 180);
    private static final Color MESSAGE_COLOR = new Color(119, 110, 101);
    private static final Color WHITE_COLOR = new Color(255, 255, 255);
    private static final Color YOU_WIN_BOARD_COLOR = new Color(255, 255, 155, 150);
    private static final Color GAME_OVER_BOARD_COLOR = new Color(255, 255, 255, 150);
    private static final String STRING_FONT = "Arial";

    private Board board;
    private CellView[][] cellViews;
    private long prevFrameTime = 0;
    private boolean gameOver = false;
    private boolean win = false;
    private RoundRectangle2D actionMessageRect;
    private RoundRectangle2D newGameRect;
    private int actionMsgRectHeight;

    public BoardView(Board board) {
        this.board = board;
        cellViews = new CellView[this.board.getSize()][this.board.getSize()];

        this.board.addCellCreatedListener((x, y, number) -> {
            cellViews[y][x] = new CellView(x, y, number, board.getGoal());
        });

        this.board.addCellMovedListener((fromX, fromY, toX, toY) -> {
            cellViews[toY][toX] = cellViews[fromY][fromX];
            cellViews[toY][toX].queueMovement(toX, toY);
            cellViews[fromY][fromX] = null;
        });

        this.board.addCellMergedFromListener((fromX, fromY, toX, toY, mergedValue) -> {
            cellViews[toY][toX].setNumber(mergedValue);
            cellViews[toY][toX].setDisappearing(true);
        });

        this.board.addCellMergedToListener(((toX, toY, fromX, fromY) -> {
            cellViews[fromY][fromX] =  null;
        }));

        this.board.addGameOverListener((()-> {
            gameOver = true;
        }));

        this.board.addWinListener((()-> {
            win = true;
        }));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(actionMessageRect != null && actionMessageRect.contains(e.getX(), e.getY())) {
                    if(gameOver) {
                        restart();
                    } else if(win) win = false;
                } else if(newGameRect != null && newGameRect.contains(e.getX(), e.getY())) {
                    restart();
                }
            }
        });


        for(int y = 0; y < this.board.getSize(); y++) {
            for(int x = 0; x < this.board.getSize(); x++) {
                int value = this.board.getValue(x, y);
                if(value != 0) {
                    cellViews[y][x] = new CellView(x, y, value, board.getGoal());
                }
            }
        }
    }


    private void restart() {
        gameOver = false;
        this.board = new Board();
        this.board.copyTo(Main.board);

        cellViews = new CellView[board.getSize()][board.getSize()];
        for(int y = 0; y < this.board.getSize(); y++) {
            for(int x = 0; x < this.board.getSize(); x++) {
                int value = this.board.getValue(x, y);
                if(value != 0) {
                    cellViews[y][x] = new CellView(x, y, value, board.getGoal());
                }
            }
        }
    }


    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isWin() {
        return win;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        long frameTime = System.currentTimeMillis();
        float dt = Math.min((frameTime-prevFrameTime)/1000.0f, 0.1f);
        prevFrameTime = frameTime;

        Graphics2D g2 = (Graphics2D) g;

        int cellSize = (int) (Math.min(getWidth(), getHeight()) / board.getSize() * 0.6);
        int boardScreenSize = cellSize * board.getSize();
        int ctrShiftX = (getWidth() - boardScreenSize) / 2;
        int ctrShiftY = (getHeight() - boardScreenSize) / 2;
        int padding = (int) (cellSize * 0.043);
        int arcSize = (int) (cellSize * 0.18);
        int actualCellSize = cellSize - 2 * padding;

        g2.setColor(MESSAGE_COLOR); //new game button
        newGameRect = new RoundRectangle2D.Double(
                padding + ctrShiftX + (board.getSize()-1) * cellSize,
                (getHeight() - boardScreenSize)/6.0,
                cellSize, (getHeight() - boardScreenSize)/6.0,
                arcSize/3.0, arcSize/3.0);
        g2.fill(newGameRect);

        drawCenteredString("New Game", WHITE_COLOR, g2,
                (float) newGameRect.getX() + cellSize / 2.0f,
                (float) newGameRect.getY() + (float)newGameRect.getHeight()/2.0f, cellSize);

        g2.setColor(BOARD_COLOR);
        g2.fillRoundRect(
                ctrShiftX - padding, ctrShiftY - padding,
                boardScreenSize + 2 * padding, boardScreenSize + 2 * padding,
                arcSize, arcSize);

        for(int y = 0; y < board.getSize(); y++) {
            for(int x = 0; x < board.getSize(); x++) {
                int pixelX = ctrShiftX + x * cellSize;
                int pixelY = ctrShiftY + y * cellSize;

                g2.setColor(EMPTY_CELL_COLOR);
                g2.fillRoundRect(
                        pixelX + padding, pixelY + padding,
                        actualCellSize, actualCellSize,
                        arcSize, arcSize);
            }
        }

        for(int y = 0; y < board.getSize(); y++) {
            for (int x = 0; x < board.getSize(); x++) {
                var cellView = cellViews[y][x];
                if(cellView != null) {
                    cellView.draw(g2, dt, cellSize, ctrShiftX, ctrShiftY, padding, arcSize);
                }
            }
        }

        if(gameOver) {
            displayMessage("Game over!", "Try again", g2, ctrShiftX, ctrShiftY, padding, boardScreenSize, arcSize);
        }
        else if(win) {
            displayMessage("You win!", "Keep going", g2, ctrShiftX, ctrShiftY, padding, boardScreenSize, arcSize);
        }
        repaint();
    }

    private void displayMessage(String message, String message2, Graphics2D g2, int ctrShiftX, int ctrShiftY, int padding, int boardScreenSize, int arcSize) {
        if(message.equals("You win!")) {
            g2.setColor(YOU_WIN_BOARD_COLOR);
        } else {
            g2.setColor(GAME_OVER_BOARD_COLOR);
        }
        g2.fillRoundRect( //creating larger transparent rect
                ctrShiftX - padding, ctrShiftY - padding,
                boardScreenSize + 2 * padding, boardScreenSize + 2 * padding,
                arcSize, arcSize);


        g2.setColor(MESSAGE_COLOR); //creating small rect for "try again" or "keep going"
        actionMessageRect = new RoundRectangle2D.Double(
                ctrShiftX + padding + boardScreenSize/3.0, ctrShiftY + padding + 2.0 * boardScreenSize/3.0,
                boardScreenSize/3.0, 2.0 * actionMsgRectHeight / 3.0,
                arcSize/3.0, arcSize/3.0);
        g2.fill(actionMessageRect);

        Color colorVerdict;
        if(message.equals("You win!")) colorVerdict = WHITE_COLOR;
        else colorVerdict = MESSAGE_COLOR;
        drawCenteredString(message, colorVerdict, g2,
                padding + ctrShiftX + (float) (boardScreenSize - padding) / 2.0f,
                padding + ctrShiftY + (float) (boardScreenSize - 2 * padding) / 2.0f,
                boardScreenSize);


        drawCenteredString(message2, WHITE_COLOR, g2,
                padding+ctrShiftX + (float) (3.0f * actionMessageRect.getWidth() / 2.0f),
                padding+ctrShiftY + 2.0f * (float) actionMessageRect.getWidth() + (float) actionMessageRect.getHeight() / 2.0f,
                actionMessageRect.getWidth());
    }

    private void drawCenteredString(String message, Color color, Graphics2D g2, float x, float y, double relativeTo) {
        int msgLength = message.length();
        float fontScale = (float) Math.pow(1.0f * msgLength/relativeTo - 1.5f, 2) * 0.13f + 0.45f;
        int fontSize = (int) (relativeTo * 0.18 * fontScale);
        g2.setFont(new Font(STRING_FONT, Font.BOLD, fontSize));
        FontMetrics metrics = g2.getFontMetrics();
        int stringWidth = metrics.stringWidth(message);
        int stringHeight = metrics.getHeight();

        g2.setColor(color);
        g2.drawString(
                message,
                x - stringWidth/1.95f,
                y + stringHeight/3.25f);
        if(message.equals("You win!") || message.equals("Game over!")) {
            actionMsgRectHeight = stringHeight;
        }
    }
}