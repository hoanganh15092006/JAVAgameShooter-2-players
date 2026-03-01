package SnapShooter.file_java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

// Lớp GamePanel: Panel chính chứa logic trò chơi Space Shooter 2 người chơi
// Quản lý vị trí người chơi, đạn và va chạm
class GamePanel extends JPanel implements ActionListener, KeyListener {

    // Timer điều khiển tốc độ cập nhật game (mỗi 16ms = ~60 FPS)
    Timer timer;

    // Người chơi 1 (Gojo - bên trái)
    Player2 player1;
    // Người chơi 2 (Sukuna - bên phải)
    Player2 player2;

    // Danh sách các background từ menu
    private ArrayList<Image> backgrounds = new ArrayList<>();
    // Index của background hiện tại
    private int currentBackgroundIndex = 0;

    // Danh sách các đạn hiện đang bay trên màn hình
    ArrayList<Bullet> bullets = new ArrayList<>();

    // Phím điều khiển người chơi 1: W, A, S, D
    boolean w, a, s, d;
    // Phím điều khiển người chơi 2: mũi tên UP, DOWN, LEFT, RIGHT
    boolean up, down, left, right;

    // Cooldown bắn thường (số tick ~60fps)
    int gojoNormalCooldown = 0;
    int sukunaNormalCooldown = 0;

    // Khoảng cách tối thiểu tới vạch giữa (pixel)
    int barrierMargin = 60;

    // Tọa độ Y của background (dùng cho hiệu ứng chuyển động)
    int bgY = 0;
    
    // Trạng thái game over
    private boolean gameOver = false;
    // Người thắng (1 = Gojo, 2 = Sukuna)
    private int winner = 0;
    // Tham chiếu đến cửa sổ chính
    private Main mainFrame;

    // Phương thức khởi tạo: Thiết lập game panel và khởi động trò chơi
    public GamePanel(Main frame) {
        this.mainFrame = frame;

        // Cho phép panel nhận input từ bàn phím
        setFocusable(true);
        // Ghi đăng ký nghe sự kiện phím
        addKeyListener(this);

        // Tạo người chơi 1 - Gojo (loại 1)
        player1 = new Player2(100, 400, 1);
        // Tạo người chơi 2 - Sukuna (loại 2)
        player2 = new Player2(600, 100, 2);
        
        // Khởi tạo backgrounds nếu chưa có
        if (backgrounds.isEmpty()) {
            // Tạo background mặc định là đen
            backgrounds.add(new java.awt.image.BufferedImage(800, 600, java.awt.image.BufferedImage.TYPE_INT_RGB));
        }

        // Tạo Timer với độ trễ 16ms (khoảng 60 FPS)
        timer = new Timer(16, this);
        
        // Thêm listener để cập nhật vị trí nhân vật khi cửa sổ thay đổi kích thước
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updatePlayerPositions();
            }
        });
        
        // Bắt đầu chạy game loop
        timer.start();
    }
    
    // Phương thức cập nhật vị trí nhân vật dựa trên kích thước panel
    private void updatePlayerPositions() {
        // Đặt người chơi 1 ở vị trí 1/4 chiều rộng, gần phía dưới
        player1.x = getWidth() / 4 - 15;
        player1.y = getHeight() - 100;
        int mid = getHeight() / 2;
        if (player1.y < mid + barrierMargin) player1.y = mid + barrierMargin;
        // Đặt người chơi 2 ở vị trí 3/4 chiều rộng, phía trên
        player2.x = getWidth() * 3 / 4 - 15;
        player2.y = getHeight() / 5;
        if (player2.y > mid - barrierMargin) player2.y = mid - barrierMargin;
    }

    // Phương thức đặt backgrounds từ menu
    public void setBackgrounds(ArrayList<Image> bgs, int bgIndex) {
        this.backgrounds = bgs;
        this.currentBackgroundIndex = bgIndex;
    }

    // Phương thức reset trò chơi
    public void resetGame() {
        // Reset trạng thái
        gameOver = false;
        winner = 0;
        bullets.clear();
        
        // Reset phím
        w = a = s = d = false;
        up = down = left = right = false;
        
        // Reset nhân vật
        player1.health = player1.maxHealth;
        player2.health = player2.maxHealth;
        player1.attackCharge = 0;
        player2.attackCharge = 0;
        // Reset skill trạng thái
        player1.normalHitCount = player1.skill2HitCount = 0;
        player1.skill2Unlocked = player1.skill3Unlocked = false;
        player2.normalHitCount = player2.skill2HitCount = 0;
        player2.skill2Unlocked = player2.skill3Unlocked = false;
        
        // Cập nhật vị trí nhân vật
        updatePlayerPositions();
    }

    // Phương thức vẽ: Vẽ tất cả các thành phần của trò chơi lên màn hình
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Vẽ nền background từ menu
        if (!backgrounds.isEmpty()) {
            g.drawImage(backgrounds.get(currentBackgroundIndex),
                    0, 0, getWidth(), getHeight(), this);
        } else {
            // Nếu không có background, vẽ nền đen
            g.setColor(Color.BLACK);
            g.fillRect(0,0,getWidth(),getHeight());
        }

        // Vẽ đường kẻ ngang ngăn cách hai nửa (mờ hơn)
        Graphics2D g2 = (Graphics2D) g;
        int midY = getHeight() / 2;
        g2.setColor(new Color(255, 255, 255, 110)); // alpha để mờ
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(0, midY, getWidth(), midY);
        // Vẽ viền tường xung quanh 4 bên
        g2.setColor(new Color(200, 200, 200));
        g2.setStroke(new BasicStroke(4f));
        g2.drawRect(2, 2, getWidth()-4, getHeight()-4);

        // Vẽ người chơi 1
        player1.draw(g);
        // Vẽ người chơi 2
        player2.draw(g);

        // Vẽ tất cả các đạn trên bảng trò chơi
        for (Bullet b : bullets)
            b.draw(g);
        
        // Vẽ chiêu đặc biệt của cả hai nhân vật
        player1.drawAttack(g);
        player2.drawAttack(g);
        
        // Nếu trò chơi kết thúc, hiển thị thông báo
        if (gameOver) {
            drawGameOverScreen(g);
        }
    }
    
    // Phương thức vẽ màn hình Game Over
    private void drawGameOverScreen(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        // Vẽ nền đậm (overlay)
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Vẽ thông báo kết quả trò chơi
        g.setColor(new Color(255, 200, 0));
        g.setFont(new Font("Arial", Font.BOLD, 50));
        
        String resultText = "";
        if (winner == 1) {
            resultText = "GOJO WINS!";
        } else if (winner == 2) {
            resultText = "SUKUNA WINS!";
        }
        
        FontMetrics fm = g.getFontMetrics();
        int resultX = (getWidth() - fm.stringWidth(resultText)) / 2;
        int resultY = getHeight() / 2 - 100;
        g.drawString(resultText, resultX, resultY);
        
        // Vẽ hướng dẫn
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 25));
        String hint = "Press Y to Play Again or N to Back to Menu";
        fm = g.getFontMetrics();
        int hintX = (getWidth() - fm.stringWidth(hint)) / 2;
        int hintY = getHeight() / 2 + 100;
        g.drawString(hint, hintX, hintY);
    }

    // Phương thức actionPerformed: Được gọi mỗi khi Timer kích hoạt (mỗi 16ms)
    // Cập nhật logic trò chơi và vẽ lại màn hình
    public void actionPerformed(ActionEvent e) {
        // Cập nhật chỉ khi trò chơi chưa kết thúc
        if (!gameOver) {
            // Cập nhật vị trí của cả hai người chơi dựa trên phím được nhấn
            movePlayers();
            // Cập nhật vị trí các đạn và xoá các đạn ra ngoài màn hình
            updateBullets();
            // giảm cooldown của bắn thường
            if (gojoNormalCooldown > 0) gojoNormalCooldown--;
            if (sukunaNormalCooldown > 0) sukunaNormalCooldown--;
            // cập nhật trạng thái sẵn sàng cho Player2
            player1.normalReady = (gojoNormalCooldown == 0);
            player2.normalReady = (sukunaNormalCooldown == 0);
            // Kiểm tra va chạm giữa đạn và người chơi
            checkCollision();
        }

        // Vẽ lại toàn bộ màn hình
        repaint();
    }

    // Phương thức movePlayers: Cập nhật vị trí của hai người chơi dựa trên input từ bàn phím
    private void movePlayers() {

        // Di chuyển người chơi 1 lên (khi nhấn W)
        if (w) player1.y -= player1.speed;
        // Di chuyển người chơi 1 xuống (khi nhấn S)
        if (s) player1.y += player1.speed;
        // Di chuyển người chơi 1 sang trái (khi nhấn A)
        if (a) player1.x -= player1.speed;
        // Di chuyển người chơi 1 sang phải (khi nhấn D)
        if (d) player1.x += player1.speed;

        // Di chuyển người chơi 2 lên (khi nhấn mũi tên UP)
        if (up) player2.y -= player2.speed;
        // Di chuyển người chơi 2 xuống (khi nhấn mũi tên DOWN)
        if (down) player2.y += player2.speed;
        // Di chuyển người chơi 2 sang trái (khi nhấn mũi tên LEFT)
        if (left) player2.x -= player2.speed;
        // Di chuyển người chơi 2 sang phải (khi nhấn mũi tên RIGHT)
        if (right) player2.x += player2.speed;

        // ngăn không cho vượt qua vạch giữa (giữ khoảng cách cố định)
        int barrier = getHeight() / 2;
        if (player1.y < barrier + barrierMargin) player1.y = barrier + barrierMargin;
        if (player2.y > barrier - barrierMargin) player2.y = barrier - barrierMargin;

        // Giữ nhân vật trong vùng hiển thị
        int wPanel = getWidth();
        int hPanel = getHeight();
        // khoảng trống nhất định để nhân vật không bị đứt nét (≈30px)
        int margin = 30;
        // hạn chế người chơi 1
        if (player1.x < 0) player1.x = 0;
        if (player1.x > wPanel - margin) player1.x = wPanel - margin;
        if (player1.y < 0) player1.y = 0;
        if (player1.y > hPanel - margin) player1.y = hPanel - margin;
        // hạn chế người chơi 2
        if (player2.x < 0) player2.x = 0;
        if (player2.x > wPanel - margin) player2.x = wPanel - margin;
        if (player2.y < 0) player2.y = 0;
        if (player2.y > hPanel - margin) player2.y = hPanel - margin;
    }

    // Phương thức updateBullets: Cập nhật vị trí các đạn và xoá các đạn ra ngoài màn hình
    private void updateBullets() {

        // Lặp qua danh sách các đạn
        for (int i = 0; i < bullets.size(); i++) {
            // Cập nhật vị trí của đạn tại index i
            bullets.get(i).update(getWidth(), getHeight());

            // Nếu đạn bay ra ngoài màn hình và không phải là chiêu 2 Gojo đang còn bật
            Bullet temp = bullets.get(i);
            if (!(temp.bulletType == 3 && temp.bounceCount > 0)) {
                if (temp.y < -50 || temp.y > getHeight()+50 || temp.x < -50 || temp.x > getWidth()+50) {
                    bullets.remove(i);
                }
            }
        }
    }

    // Phương thức checkCollision: Kiểm tra xem có đạn nào trúng người chơi hay không
    private void checkCollision() {

        // Lặp qua danh sách các đạn
        for (int i = 0; i < bullets.size(); i++) {
            // Lấy đạn tại vị trí i
            Bullet b = bullets.get(i);

            // Kiểm tra nếu đạn từ người chơi 1 trúng vào người chơi 2
            if (b.fromPlayer1 && b.getBounds().intersects(player2.getBounds())) {
                // Gây sát thương cho người chơi 2
                player2.takeDamage(b.damage);
                // Kích hoạt chiêu của Sukuna
                player2.attackCharge = 200;
                // Cập nhật counter Gojo (bắn thường)
                if (b.bulletType == 1) {
                    player1.normalHitCount++;
                    if (player1.normalHitCount >= 3) {
                        player1.skill2Unlocked = true;
                        player1.normalHitCount = 0;
                    }
                }
                // skill2 Gojo
                if (b.bulletType == 3) {
                    player1.skill2HitCount++;
                    if (player1.skill2HitCount >= 3) {
                        player1.skill3Unlocked = true;
                        player1.skill2HitCount = 0;
                    }
                }
                // Xoá đạn
                bullets.remove(i);
                
                // Kiểm tra nếu máu < 0 thì game over
                if (player2.health <= 0) {
                    endGame(1);  // Gojo thắng
                }
                return;
            }

            // Kiểm tra nếu đạn từ người chơi 2 trúng vào người chơi 1
            if (!b.fromPlayer1 && b.getBounds().intersects(player1.getBounds())) {
                // Gây sát thương cho người chơi 1
                player1.takeDamage(b.damage);
                // Kích hoạt chiêu của Gojo
                player1.attackCharge = 200;
                // Cập nhật counter Sukuna
                if (b.bulletType == 2) {
                    player2.normalHitCount++;
                    if (player2.normalHitCount >= 3) {
                        player2.skill2Unlocked = true;
                        player2.normalHitCount = 0;
                    }
                }
                if (b.bulletType == 5) {
                    player2.skill2HitCount++;
                    if (player2.skill2HitCount >= 4) {
                        player2.skill3Unlocked = true;
                        player2.skill2HitCount = 0;
                    }
                }
                // Xoá đạn
                bullets.remove(i);
                
                // Kiểm tra nếu máu < 0 thì game over
                if (player1.health <= 0) {
                    endGame(2);  // Sukuna thắng
                }
                return;
            }
        }
        
        // Giảm dần hiệu ứng chiêu
        if (player1.attackCharge > 0) {
            player1.attackCharge -= 5;
        }
        if (player2.attackCharge > 0) {
            player2.attackCharge -= 5;
        }
    }
    
    // Phương thức kết thúc trò chơi khi ai đó thua
    private void endGame(int winnerPlayer) {
        gameOver = true;
        winner = winnerPlayer;
        System.out.println("\n=== GAME OVER ===");
        if (winner == 1) {
            System.out.println("Player 1 (GOJO) THẮNG!");
            System.out.println("Player 2 (SUKUNA) THUA!");
        } else {
            System.out.println("Player 2 (SUKUNA) THẮNG!");
            System.out.println("Player 1 (GOJO) THUA!");
        }
        System.out.println("===================\n");
    }

    // Phương thức keyPressed: Xử lý khi một phím được nhấn
    public void keyPressed(KeyEvent e) {
        // Nếu trò chơi kết thúc, chỉ xử lý Y/N
        if (gameOver) {
            if (e.getKeyCode() == KeyEvent.VK_Y) {  // Y = Chơi lại
                resetGame();
            } else if (e.getKeyCode() == KeyEvent.VK_N) {  // N = Về menu
                mainFrame.showMenu();
            }
            return;
        }

        // Nếu trò chơi đang chơi, xử lý các phím điều khiển
        switch (e.getKeyCode()) {

            // Phím điều khiển người chơi 1
            case KeyEvent.VK_W -> w = true;  // Nhấn W: bật cờ w
            case KeyEvent.VK_S -> s = true;  // Nhấn S: bật cờ s
            case KeyEvent.VK_A -> a = true;  // Nhấn A: bật cờ a
            case KeyEvent.VK_D -> d = true;  // Nhấn D: bật cờ d

            // Phím điều khiển người chơi 2
            case KeyEvent.VK_UP -> up = true;        // Nhấn mũi tên UP: bật cờ up
            case KeyEvent.VK_DOWN -> down = true;    // Nhấn mũi tên DOWN: bật cờ down
            case KeyEvent.VK_LEFT -> left = true;    // Nhấn mũi tên LEFT: bật cờ left
            case KeyEvent.VK_RIGHT -> right = true;  // Nhấn mũi tên RIGHT: bật cờ right

            // ------------ SKILL KEYS ------------
            // Gojo normal shot (U) với cooldown 1s
            case KeyEvent.VK_U -> {
                if (gojoNormalCooldown == 0) {
                    bullets.add(new Bullet(player1.x + 15, player1.y - 12, true, 1));
                    gojoNormalCooldown = 62; // ~ 1 giây
                }
            }
            // Gojo skill2 (I)
            case KeyEvent.VK_I -> {
                if (player1.skill2Unlocked) {
                    // Tạo 2 quả cầu bắn về phía trước theo hình chữ V (lệch trái + lệch phải)
                    Bullet b1 = new Bullet(player1.x + 6, player1.y - 12, true, 3);
                    Bullet b2 = new Bullet(player1.x + 24, player1.y - 12, true, 3);
                    // Góc V: đi lên (dy negative) và lệch sang trái/phải (dx)
                    int vDx = Math.max(2, b1.speed * 4 / 7); // khoảng dx nhỏ để tạo V
                    b1.dx = -vDx; b1.dy = -b1.speed;
                    b2.dx = vDx;  b2.dy = -b2.speed;
                    bullets.add(b1);
                    bullets.add(b2);
                    player1.skill2Unlocked = false;
                    player1.normalHitCount = 0;
                }
            }
            // Gojo skill3 (P)
            case KeyEvent.VK_P -> {
                if (player1.skill3Unlocked) {
                    bullets.add(new Bullet(player1.x + 15, player1.y - 12, true, 4));
                    player1.skill3Unlocked = false;
                    player1.skill2HitCount = 0;
                }
            }

            // Sukuna normal shot (1) với cooldown
            case KeyEvent.VK_1 -> {
                if (sukunaNormalCooldown == 0) {
                    bullets.add(new Bullet(player2.x + 15, player2.y + 40, false, 2));
                    sukunaNormalCooldown = 62;
                }
            }
            // Sukuna skill2 (2)
            case KeyEvent.VK_2 -> {
                if (player2.skill2Unlocked) {
                    // Chiêu 2: viên lưỡi liềm bắn xuống; khởi tạo ở giữa Sukuna
                    Bullet b = new Bullet(player2.x + 15, player2.y + 40, false, 5);
                    // căn giữa theo chiều rộng mới của đạn
                    b.x = player2.x + player2.width / 2 - b.width / 2;
                    // constructor đã đặt dy = speed và dx = speed/4
                    bullets.add(b);
                    player2.skill2Unlocked = false;
                    player2.normalHitCount = 0;
                }
            }
            // Sukuna skill3 (3)
            case KeyEvent.VK_3 -> {
                if (player2.skill3Unlocked) {
                    Bullet b = new Bullet(player2.x + 15, player2.y + 40, false, 6);
                    bullets.add(b);
                    player2.skill3Unlocked = false;
                    player2.skill2HitCount = 0;
                }
            }
        }
    }

    // Phương thức keyReleased: Xử lý khi một phím được thả ra
    public void keyReleased(KeyEvent e) {

        // Kiểm tra phím nào được thả ra
        switch (e.getKeyCode()) {
            // Tắt các cờ của người chơi 1 khi phím được thả
            case KeyEvent.VK_W -> w = false;
            case KeyEvent.VK_S -> s = false;
            case KeyEvent.VK_A -> a = false;
            case KeyEvent.VK_D -> d = false;

            // Tắt các cờ của người chơi 2 khi phím được thả
            case KeyEvent.VK_UP -> up = false;
            case KeyEvent.VK_DOWN -> down = false;
            case KeyEvent.VK_LEFT -> left = false;
            case KeyEvent.VK_RIGHT -> right = false;
        }
    }

    // Phương thức keyTyped: Xử lý khi một ký tự được nhập (không cần xử lý)
    public void keyTyped(KeyEvent e) {}
}