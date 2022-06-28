import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicReference;

public class Main extends JFrame {
    public static Board board = new Board();

    public Main() {
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setTitle("2048");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        AtomicReference<BoardView> boardView = new AtomicReference<>(new BoardView(board));
        add(boardView.get());

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if(!boardView.get().isGameOver() && !boardView.get().isWin()) {
                    switch(e.getKeyCode()) {
                        case KeyEvent.VK_UP -> {
                            board.moveUp();
                            repaint();
                        }
                        case KeyEvent.VK_DOWN -> {
                            board.moveDown();
                            repaint();
                        }
                        case KeyEvent.VK_LEFT -> {
                            board.moveLeft();
                            repaint();
                        }
                        case KeyEvent.VK_RIGHT -> {
                            board.moveRight();
                            repaint();
                        }
                    }
                }
            }
        });

    }

    public static void main(String[] args) {
        new Main().setVisible(true);
    }

}

