package SnapShooter.file_java;

import java.awt.*;
import java.awt.BasicStroke;

public class Player2 {
    
    // Vị trí của nhân vật
    public int x, y;
    // Kích thước nhân vật
    public int width = 60;
    public int height = 80;
    // Tốc độ di chuyển
    public int speed = 5;
    // Loại nhân vật: 1 = Gojo, 2 = Sukuna
    public int characterType = 1;
    // Máu (Health) – tăng lên 1000
    public int maxHealth = 1000;
    public int health = 1000;
    // Tên nhân vật (để vẽ phía trên thanh máu)
    public String name;
    // Chiêu đặc biệt (0 = không, 255 = đang chiêu)
    public int attackCharge = 0;
    
    // Skill counters & unlock flags
    public int normalHitCount = 0;      // số lần bắn thường trúng kẻ địch
    public int skill2HitCount = 0;      // số lần chiêu 2 trúng kẻ địch
    public boolean skill2Unlocked = false;
    public boolean skill3Unlocked = false;
    // Trạng thái sẵn sàng của chiêu 1 (cooldown) – được GamePanel cập nhật mỗi tick
    public boolean normalReady = true;

    public Player2(int x, int y, int characterType) {
        this.x = x;
        this.y = y;
        this.characterType = characterType;
        this.health = maxHealth;
        // khởi tạo tên theo loại
        if (characterType == 1) name = "GOJO";
        else name = "SUKUNA";
    }

    // Phương thức giảm máu
    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
        // Kích hoạt hiệu ứng chiêu khi bị damage
        attackCharge = 0;
    }

    // Phương thức vẽ nhân vật
    public void draw(Graphics g) {
        if (characterType == 1) {
            drawGojo(g);
        } else if (characterType == 2) {
            drawSukuna(g);
        }
        // Vẽ tên phía dưới chân để không chen lên chiêu
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fmName = g.getFontMetrics();
        int nameX = x + (width - fmName.stringWidth(name)) / 2;
        int nameY = y + height + 18;
        g.drawString(name, nameX, nameY);

        // Vẽ thanh máu ở trên đầu nhân vật
        drawHealthBar(g);
        // Vẽ icon chiêu thức phía trên (U/I/P hoặc 1/2/3)
        drawSkillIcons(g);
    }

    // Vẽ nhân vật Gojo - chi tiết hơn và đẹp hơn
    private void drawGojo(Graphics g) {
        int centerX = x + width / 2;
        int headY = y + 10;
        int headRadius = 22;
        int bodyY = headY + headRadius + 12;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ tóc trắng (phần trên - tạo hình spiky)
        g.setColor(new Color(250, 250, 255));
        g.fillOval(centerX - headRadius - 2, headY - 8, headRadius * 2 + 4, headRadius + 8);

        // Vẽ chi tiết tóc spike
        g.setColor(new Color(240, 245, 250));
        g2d.setStroke(new BasicStroke(1.5f));
        int[] spikeX = {centerX - 10, centerX - 5, centerX, centerX + 5, centerX + 10};
        for (int sx : spikeX) {
            g2d.drawLine(sx, headY - 5, sx + 2, headY - 12);
        }

        // Vẽ khuôn mặt (da)
        g.setColor(new Color(255, 220, 177));
        g.fillOval(centerX - headRadius, headY, headRadius * 2, headRadius * 2);

        // Vẽ dây lụa phủ mắt (màu đen tuyền đặc)
        g.setColor(Color.BLACK);
        g.fillRect(centerX - headRadius - 5, headY + headRadius / 2 - 4, headRadius * 2 + 10, 10);

        // Vẽ cằm
        g.setColor(new Color(255, 200, 160));
        int[] chinX = {centerX - 10, centerX, centerX + 10};
        int[] chinY = {headY + headRadius + 3, headY + headRadius + 12, headY + headRadius + 3};
        g.fillPolygon(chinX, chinY, 3);

        // Vẽ cổ
        g.setColor(new Color(255, 220, 177));
        g.fillRect(centerX - 8, bodyY - 8, 16, 10);

        // Vẽ cơ thể (áo trắng)
        g.setColor(new Color(200, 220, 255));
        g.fillRect(centerX - 20, bodyY, 40, 45);

        // Vẽ giai đoạn cơ (trong áo)
        g.setColor(new Color(180, 200, 240));
        g.fillRect(centerX - 18, bodyY + 2, 36, 43);

        // Vẽ nút áo
        g.setColor(new Color(100, 100, 100));
        for (int i = 0; i < 3; i++) {
            g.fillOval(centerX - 2, bodyY + 10 + i * 10, 4, 4);
        }

        // Vẽ viền áo (xanh tím nhạt)
        g.setColor(new Color(150, 120, 200));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(centerX - 20, bodyY, 40, 45);

        // Vẽ tay trái
        g.setColor(new Color(255, 220, 177));
        g.fillRect(centerX - 22, bodyY + 5, 8, 30);
        g.fillOval(centerX - 22, bodyY + 32, 8, 8);  // Tay

        // Vẽ tay phải
        g.fillRect(centerX + 14, bodyY + 5, 8, 30);
        g.fillOval(centerX + 14, bodyY + 32, 8, 8);  // Tay

        // Vẽ chân trái (quần)
        g.setColor(new Color(50, 50, 50));
        g.fillRect(centerX - 10, bodyY + 45, 8, 20);
        g.setColor(new Color(255, 220, 177));
        g.fillOval(centerX - 10, bodyY + 62, 8, 8);  // Chân

        // Vẽ chân phải
        g.setColor(new Color(50, 50, 50));
        g.fillRect(centerX + 2, bodyY + 45, 8, 20);
        g.setColor(new Color(255, 220, 177));
        g.fillOval(centerX + 2, bodyY + 62, 8, 8);  // Chân
    }

    // Vẽ nhân vật Sukuna - chi tiết hơn và đẹp hơn
    private void drawSukuna(Graphics g) {
        int centerX = x + width / 2;
        int headY = y + 10;
        int headRadius = 22;
        int bodyY = headY + headRadius + 12;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ tóc đen dài
        g.setColor(new Color(20, 20, 30));
        g.fillOval(centerX - headRadius - 3, headY - 12, headRadius * 2 + 6, headRadius + 18);

        // Vẽ chi tiết tóc (sợi tóc)
        g.setColor(new Color(10, 10, 20));
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i < 5; i++) {
            int offsetX = centerX - 15 + i * 7;
            g2d.drawLine(offsetX, headY - 5, offsetX - 3, headY - 15);
        }

        // Vẽ khuôn mặt (da tím nhạt)
        g.setColor(new Color(245, 200, 200));
        g.fillOval(centerX - headRadius, headY, headRadius * 2, headRadius * 2);

        // Vẽ vệt hình bóng tím (ma quỷ)
        g.setColor(new Color(150, 80, 140));
        g.fillOval(centerX - headRadius + 2, headY + 3, 12, 10);    // Mắt trái
        g.fillOval(centerX + headRadius - 14, headY + 3, 12, 10);   // Mắt phải
        g.fillRect(centerX - 14, headY + headRadius - 12, 28, 14);  // Cằm

        // Vẽ mắt (trắng)
        g.setColor(Color.WHITE);
        g.fillOval(centerX - headRadius + 4, headY + 4, 8, 8);
        g.fillOval(centerX + headRadius - 12, headY + 4, 8, 8);

        // Vẽ con ng瞳 (tím sáng)
        g.setColor(new Color(200, 50, 200));
        g.fillOval(centerX - headRadius + 6, headY + 6, 4, 4);
        g.fillOval(centerX + headRadius - 10, headY + 6, 4, 4);

        // Vẽ miệng (cười nguy hiểm)
        g.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawArc(centerX - 12, headY + headRadius - 10, 24, 14, 180, 180);

        // Vẽ cổ
        g.setColor(new Color(245, 200, 200));
        g.fillRect(centerX - 8, bodyY - 8, 16, 10);

        // Vẽ cơ thể (áo đỏ/cam)
        g.setColor(new Color(220, 80, 80));
        g.fillRect(centerX - 20, bodyY, 40, 45);

        // Vẽ giai đoạn cơ (gradient tối hơn)
        g.setColor(new Color(180, 40, 40));
        g.fillRect(centerX - 18, bodyY + 2, 36, 43);

        // Vẽ nút/khuy chặt
        g.setColor(new Color(100, 20, 20));
        for (int i = 0; i < 3; i++) {
            g.fillRect(centerX - 3, bodyY + 10 + i * 10, 6, 6);
        }

        // Vẽ viền áo (đen đậm)
        g.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(centerX - 20, bodyY, 40, 45);

        // Vẽ tay trái
        g.setColor(new Color(245, 200, 200));
        g.fillRect(centerX - 22, bodyY + 5, 8, 30);
        g.fillOval(centerX - 22, bodyY + 32, 8, 8);

        // Vẽ tay phải
        g.fillRect(centerX + 14, bodyY + 5, 8, 30);
        g.fillOval(centerX + 14, bodyY + 32, 8, 8);

        // Vẽ chân trái (quần đen)
        g.setColor(Color.BLACK);
        g.fillRect(centerX - 10, bodyY + 45, 8, 20);
        g.setColor(new Color(245, 200, 200));
        g.fillOval(centerX - 10, bodyY + 62, 8, 8);

        // Vẽ chân phải
        g.setColor(Color.BLACK);
        g.fillRect(centerX + 2, bodyY + 45, 8, 20);
        g.setColor(new Color(245, 200, 200));
        g.fillOval(centerX + 2, bodyY + 62, 8, 8);

        // Vẽ chi tiết áo (dây buộc)
        g.setColor(new Color(80, 80, 80));
        g.drawLine(centerX - 20, bodyY + 10, centerX + 20, bodyY + 10);
        g.drawLine(centerX - 20, bodyY + 25, centerX + 20, bodyY + 25);
    }

    // Phương thức lấy vùng va chạm
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    // Phương thức vẽ thanh máu ở trên đầu nhân vật
    private void drawHealthBar(Graphics g) {
        int barWidth = 120;
        int barHeight = 16;
        int barX = x + (width - barWidth) / 2;
        int barY = y - 20;

        // Vẽ nền thanh máu (đen)
        g.setColor(Color.BLACK);
        g.fillRect(barX - 1, barY - 1, barWidth + 2, barHeight + 2);

        // Vẽ máu xanh (hiện tại)
        if (health > 50) {
            g.setColor(new Color(0, 200, 0));  // Xanh lá
        } else if (health > 25) {
            g.setColor(new Color(255, 200, 0));  // Vàng
        } else {
            g.setColor(new Color(255, 0, 0));  // Đỏ
        }

        int filledWidth = (int) ((health / (double) maxHealth) * barWidth);
        g.fillRect(barX, barY, filledWidth, barHeight);

        // Vẽ viền thanh máu
        g.setColor(Color.WHITE);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(barX, barY, barWidth, barHeight);

        // Vẽ text máu bên trong thanh (căn giữa)
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        String hpText = health + "/" + maxHealth;
        FontMetrics fmHp = g.getFontMetrics();
        int hpX = barX + (barWidth - fmHp.stringWidth(hpText)) / 2;
        int hpY = barY + (barHeight + fmHp.getAscent()) / 2 - 2;
        g.drawString(hpText, hpX, hpY);

    }

    // Vẽ các icon chiêu thức bên trên nhân vật
    private void drawSkillIcons(Graphics g) {
        int iconSize = 20;
        int gap = 8;
        int startX = x + (width - (iconSize*3 + gap*2)) / 2;
        int iconY = y - 60;

        if (characterType == 1) {
            // chữ U, I, O
            char[] keys = {'U','I','O'};
            boolean[] ready = {normalReady, skill2Unlocked, skill3Unlocked};
            for (int i = 0; i < 3; i++) {
                g.setColor(ready[i] ? Color.GREEN : Color.DARK_GRAY);
                g.fillRect(startX + i*(iconSize+gap), iconY, iconSize, iconSize);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 16));
                g.drawString(String.valueOf(keys[i]), startX + i*(iconSize+gap)+6, iconY+15);
            }
        } else {
            // Sukuna: số 1,2,3
            char[] keys = {'1','2','3'};
            boolean[] ready = {normalReady, skill2Unlocked, skill3Unlocked};
            for (int i = 0; i < 3; i++) {
                g.setColor(ready[i] ? Color.GREEN : Color.DARK_GRAY);
                g.fillRect(startX + i*(iconSize+gap), iconY, iconSize, iconSize);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 16));
                g.drawString(String.valueOf(keys[i]), startX + i*(iconSize+gap)+6, iconY+15);
            }
        }
    }

    // Phương thức vẽ chiêu đặc biệt
    public void drawAttack(Graphics g) {
        if (characterType == 1) {
            drawGojoAttack(g);
        } else if (characterType == 2) {
            drawSukunaAttack(g);
        }
    }

    // Vẽ chiêu của Gojo (màu trắng bạc)
    private void drawGojoAttack(Graphics g) {
        if (attackCharge <= 0) return;

        int centerX = x + width / 2;
        int centerY = y + height / 2;
        float alpha = attackCharge / 255f;

        // Tạo hiệu ứng ánh sáng trắng bạc
        g.setColor(new Color(200, 220, 255, (int) (150 * alpha)));
        
        // Vẽ các vòng tròn chiêu
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));
        
        // Vòng 1
        int radius1 = (int) (20 + attackCharge / 5);
        g.setColor(new Color(240, 248, 255, (int) (200 * alpha)));
        g2d.drawOval(centerX - radius1, centerY - radius1, radius1 * 2, radius1 * 2);
        
        // Vòng 2
        int radius2 = (int) (40 + attackCharge / 3);
        g.setColor(new Color(200, 220, 255, (int) (150 * alpha)));
        g2d.drawOval(centerX - radius2, centerY - radius2, radius2 * 2, radius2 * 2);
    }

    // Vẽ chiêu của Sukuna (màu đỏ)
    private void drawSukunaAttack(Graphics g) {
        if (attackCharge <= 0) return;

        int centerX = x + width / 2;
        int centerY = y + height / 2;
        float alpha = attackCharge / 255f;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(3));

        // Vẽ năng lượng tím đỏ
        int[] explosionRadius = {30, 50, 70};
        Color[] colors = {
            new Color(255, 100, 100, (int) (255 * alpha)),
            new Color(200, 0, 0, (int) (200 * alpha)),
            new Color(100, 0, 50, (int) (150 * alpha))
        };

        for (int i = 0; i < explosionRadius.length; i++) {
            int radius = (int) (explosionRadius[i] + attackCharge / 2);
            g.setColor(colors[i]);
            g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        }
    }
}
