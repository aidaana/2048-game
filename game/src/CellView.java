import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;
public class CellView {
    public static final Color[] colors = new Color[] {new Color(255, 255, 255),
            new Color(238, 228, 218), new Color(237, 224, 200), //2 4
            new Color(242, 177, 121), new Color(245, 149, 99),  //8 16
            new Color(246, 124, 95), new Color(246, 94, 59),    //32 64
            new Color(237, 207, 114), new Color(237, 204, 97),  //128 256
            new Color(236, 200, 80), new Color(237, 204, 97),   //512 1024
            new Color(237, 194, 46), new Color(60, 58, 50),     //2048 4096
    };
    private static final Color STRING_DARK_COLOR = new Color(119, 110, 101);
    private static final Color STRING_LIGHT_COLOR = new Color(249, 246, 242);
    private static final String STRING_FONT = "Arial";
    private static final float SIZE_ANIM_SPEED = 300.0f;
    private static final float MOVEMENT_ANIM_SPEED = 1200.0f;

    private int x, y;
    private final Queue<Board.Point> queue;
    private Integer nextX = null, nextY = null;
    private float pixelX = 0.0f, pixelY = 0.0f;
    private int number;
    private final int goal;
    private float initialAnimSize = 20.0f;
    private float animSize = 20.0f;
    private boolean appearing = true;
    private boolean disappearing = false;

    public CellView(int x, int y, int number, int goal) {
        queue = new LinkedList<>();
        this.x = x;
        this.y = y;
        this.number = number;
        this.goal = goal;
    }

    public void queueMovement(int x, int y) {
        queue.add(new Board.Point(x, y));
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setDisappearing(boolean e) {
        disappearing = e;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void draw(Graphics2D g2, float dt, int cellSize, int ctrShiftX, int ctrShiftY, int padding, int arcSize) {
        if (appearing) {
            animSize += SIZE_ANIM_SPEED * dt;
            if(Math.abs(animSize - cellSize) < 5) {
                animSize = cellSize;
                appearing = false;
            }
        } else if (!disappearing) {
            animSize = cellSize;
        } else if(disappearing) {
            animSize -= SIZE_ANIM_SPEED*dt;
            if(Math.abs(animSize - cellSize) > cellSize/2.0f) {
                animSize = initialAnimSize;
                disappearing = false;
                appearing = true;
            }
        }


        if(nextX == null && nextY == null && !queue.isEmpty()){
            var next = queue.remove();
            nextX = next.getX();
            nextY = next.getY();
            pixelX = ctrShiftX + x * cellSize;
            pixelY = ctrShiftY + y * cellSize;
        }

        if(nextX != null && nextY != null) {
            float nextPixelX = ctrShiftX + nextX * cellSize;
            float nextPixelY = ctrShiftY + nextY * cellSize;
            float dx = nextPixelX - pixelX;
            float dy = nextPixelY - pixelY;
            float length = (float) Math.sqrt(dx*dx + dy*dy);
            if (length < 6) {
                pixelX = nextPixelX;
                pixelY = nextPixelY;
                x = nextX;
                y = nextY;
                nextX = null;
                nextY = null;
            } else {
                dx /= length;
                dy /= length;
                pixelX += dx*MOVEMENT_ANIM_SPEED*dt;
                pixelY += dy*MOVEMENT_ANIM_SPEED*dt;
            }

        } else {
            pixelX = ctrShiftX + x * cellSize;
            pixelY = ctrShiftY + y * cellSize;
        }

        float cellCtrShift = Math.abs(cellSize-animSize)/2f;

        for(int i = 1; i <= colors.length; i++) {
            if(number == (int) Math.pow(2, i)) {
                g2.setColor(colors[i]);
                break;
            }
        }
        g2.fillRoundRect(
                (int) (pixelX + padding + cellCtrShift), (int) (pixelY + padding + cellCtrShift),
                (int) (animSize - 2 * padding), (int) (animSize - 2 * padding),
                arcSize, arcSize);

        String numString = String.valueOf(number);
        String goalString = String.valueOf(goal);
        int numLength = numString.length();
        int goalLength = goalString.length();

        float fontScale = (float) (numLength + 9)/(goalLength + 9);
        fontScale = (float) Math.pow(fontScale - 1.5f, 2) * 0.59f + 0.45f;
        int fontSize = (int) (animSize * 0.7 * fontScale);

        if(number <= 4) {
            g2.setColor(STRING_DARK_COLOR);
        } else {
            g2.setColor(STRING_LIGHT_COLOR);
        }
        g2.setFont(new Font(STRING_FONT, Font.BOLD, fontSize));
        FontMetrics metrics = g2.getFontMetrics();
        int stringWidth = metrics.stringWidth(numString);
        int stringHeight = metrics.getHeight();

        g2.drawString(
                numString,
                pixelX + padding + cellCtrShift + (animSize - 2 * padding) / 2 - stringWidth / 1.95f,
                pixelY + padding + cellCtrShift + (animSize - 2 * padding) / 2 + stringHeight / 3.25f);
    }
}
