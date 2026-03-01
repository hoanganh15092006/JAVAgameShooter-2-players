package SnapShooter.file_java;

import java.awt.*;
import java.util.ArrayList;

class Bullet {

    int x, y;
    int width = 8;
    int height = 15;
    int speed = 20;
    // vận tốc theo trục, dùng cho cả hướng xiên
    int dx = 0, dy = 0;
    boolean fromPlayer1;
    // Damage của đạn
    int damage = 20;
    // Loại đạn: 1 = Gojo theo mặc định
    // 2 = Sukuna thường
    // 3 = Gojo skill2 (red bounce)
    // 4 = Gojo skill3 (purple big)
    // 5 = Sukuna skill2 (white diagonal)
    // 6 = Sukuna skill3 (silver fast long)
    int bulletType = 1;
    // Số lần còn lại được bật tường (chỉ dùng cho skill2 Gojo)
    int bounceCount = 0;
    // Lịch sử vị trí để vẽ trail effect
    ArrayList<int[]> trailHistory = new ArrayList<>();
    // Animation tick
    int animTick = 0;
    // Kích thước panel
    int panelWidth = 800;
    int panelHeight = 600;

    public Bullet(int x, int y, boolean fromPlayer1, int bulletType) {
        this.x = x;
        this.y = y;
        this.fromPlayer1 = fromPlayer1;
        this.bulletType = bulletType;

        // thiết lập mặc định hướng lên/xuống theo bên bắn
        if (fromPlayer1) {
            dy = -speed;
            dx = 0;
        } else {
            dy = speed;
            dx = 0;
        }

        switch (bulletType) {
            case 1 -> { // Gojo bình thường
                this.width = 24*3;
                this.height = 24*3;
                this.damage = 20;
            }
            case 2 -> { // Sukuna bình thường
                this.width = 16*2;
                this.height = 24*2;
                this.damage = 25; // giảm xuống 25 theo yêu cầu
            }
            case 3 -> { // Gojo skill2: hai quả cầu đỏ tỏa hai bên, bật tường 3 lần
                this.width = 24;
                this.height = 24;
                this.damage = 20;
                this.bounceCount = 6; // bật lại 3 lần
                // hướng ban đầu sẽ được caller thiết lập (dx lệch trái/phải)
                dy = 0;
            }
            case 4 -> { // Gojo skill3: một quả cầu tím lớn
                this.width = 24 * 8;
                this.height = 24 * 8;
                this.damage = 80;
                // đường đạn vẫn bình thường (hướng lên/xuống)
            }
            case 5 -> { // Sukuna skill2: hình chữ nhật nằm ngang, bay thẳng xuống
                this.width = 90;            // bằng chiều dài chiêu thường
                this.height = 8;       // rộng 1/2 chiêu thường
                this.damage = 30;            
                speed = 40;           // gấp đôi tốc độ
            }
            case 6 -> { // Sukuna skill3: ngọn lửa truyền thẳng
                // kích thước lớn hơn, tầm chiêu khoảng 2 lần đạn thường
                this.width = 50;
                this.height = 120;
                this.damage = 80;          // theo yêu cầu
                speed = 70;               // gấp 10 lần như trước
            }
        }
    }

    public void update(int panelWidth, int panelHeight) {
        // Cập nhật kích thước panel
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        
        // Tăng animation tick
        animTick++;
        
        // Lưu lịch sử vị trí cho trail effect
        if (bulletType == 4 || bulletType == 6 || bulletType == 1 || bulletType == 2) {
            trailHistory.add(new int[]{x + width/2, y + height/2});
            // Giữ tối đa 15 vị trí trước đó để vẽ trail
            if (trailHistory.size() > 15) {
                trailHistory.remove(0);
            }
        }
        
        x += dx;
        y += dy;
        // xử lý bật tường cho chiêu 2 Gojo (type3) trên cả 4 cạnh
        if (bulletType == 3 && bounceCount > 0) {
            boolean bounced = false;
            if (x < 0 || x + width > panelWidth) {
                dx = -dx;
                bounced = true;
            }
            if (y < 0 || y + height > panelHeight) {
                dy = -dy;
                bounced = true;
            }
            if (bounced) bounceCount--;
        }
    }


    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Vẽ trail trước
        drawTrail(g2d);
        
        // Vẽ đạn Gojo (tròn, màu xanh sáng)
        if (bulletType == 1) {
            // Vẽ multiple glow rings
            for (int i = 5; i >= 1; i--) {
                g2d.setColor(new Color(80, 180, 255, 50 - i * 8));
                g2d.fillOval(x - i * 2, y - i * 2, width + i * 4, height + i * 4);
            }
            
            // Vẽ quả cầu chính
            GradientPaint gp = new GradientPaint(x, y, new Color(180, 230, 255),
                    x + width, y + height, new Color(30, 120, 220));
            g2d.setPaint(gp);
            g2d.fillOval(x, y, width, height);
            
            // Vẽ tia năng lượng xung quanh
            drawEnergySpikes(g2d, x + width/2, y + height/2, width/2);
            
            // Viền sáng chớp
            float pulse = 2 + (float)Math.sin(animTick * 0.15) * 1.5f;
            g2d.setColor(new Color(200, 255, 255, 200));
            g2d.setStroke(new BasicStroke(pulse + 2));
            g2d.drawOval(x, y, width, height);
        }
        // Vẽ đạn Sukuna (chữ nhật, màu đỏ)
        else if (bulletType == 2) {
            // Vẽ aura cháy
            for (int i = 3; i >= 1; i--) {
                g2d.setColor(new Color(255, 80, 20, 40 - i * 13));
                g2d.fillRect(x - i, y - i, width + i * 2, height + i * 2);
            }
            
            // Vẽ gradient đỏ
            GradientPaint gp = new GradientPaint(x, y, new Color(255, 180, 80),
                    x + width, y + height, new Color(200, 20, 0));
            g2d.setPaint(gp);
            g2d.fillRect(x, y, width, height);
            
            // Lửa xung quanh
            drawFlameBurst(g2d, x + width/2, y + height/2);
            
            // Viền đỏ sáng
            float pulse = 2 + (float)Math.sin(animTick * 0.12) * 1f;
            g2d.setColor(new Color(255, 150, 50));
            g2d.setStroke(new BasicStroke(pulse + 1.5f));
            g2d.drawRect(x, y, width, height);
        }
        // Gojo skill2 (type 3) – các quả cầu đỏ với sét
        else if (bulletType == 3) {
            // Vẽ halo xung quanh với sét
            for (int i = 3; i >= 1; i--) {
                g2d.setColor(new Color(255, 100, 100, 50 - i * 15));
                g2d.fillOval(x - i * 3, y - i * 3, width + i * 6, height + i * 6);
            }
            
            // Vẽ quả cầu
            GradientPaint gp = new GradientPaint(x, y, new Color(255, 140, 140),
                    x + width, y + height, new Color(150, 0, 0));
            g2d.setPaint(gp);
            g2d.fillOval(x, y, width, height);
            
            // Vẽ sét giật nhỏ xung quanh
            drawMiniStormLightning(g2d, x + width/2, y + height/2, width/2 + 5);
            
            // Viền sáng
            float pulse = 2 + (float)Math.sin(animTick * 0.1) * 1.2f;
            g2d.setColor(new Color(255, 180, 180));
            g2d.setStroke(new BasicStroke(pulse + 1));
            g2d.drawOval(x, y, width, height);
        }
        // Gojo skill3 (type4) – quả cầu tím lớn với sét giật cực mạnh
        else if (bulletType == 4) {
            // Vẽ massive glow bên ngoài
            for (int i = 8; i >= 1; i--) {
                g2d.setColor(new Color(150, 80, 220, 80 - i * 10));
                g2d.fillOval(x - i * 3, y - i * 3, width + i * 6, height + i * 6);
            }
            
            // Vẽ quả cầu chính với gradient
            GradientPaint gp = new GradientPaint(x, y, new Color(220, 120, 255),
                    x + width, y + height, new Color(80, 0, 150));
            g2d.setPaint(gp);
            g2d.fillOval(x, y, width, height);
            
            // Vẽ sét giật xung quanh quả cầu - RẤT MẠNH
            drawStormLightning(g2d);
            
            // Vẽ thêm sparkles
            drawSparks(g2d, x + width/2, y + height/2, width/2 + 20, Color.CYAN);
            
            // Outline sáng chớp
            float pulse = 3 + (float)Math.sin(animTick * 0.2) * 2f;
            g2d.setColor(new Color(200, 150, 255));
            g2d.setStroke(new BasicStroke(pulse + 3));
            g2d.drawOval(x, y, width, height);
            
            // Outline thứ hai
            g2d.setColor(new Color(150, 200, 255, 150));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(x, y, width, height);
            
            // Hiệu ứng FULLSCREEN SÉT GIẬT
            drawFullscreenLightning(g2d);
        }
        // Sukuna skill2 (type5) – đường chéo với lửa
        else if (bulletType == 5) {
            Graphics2D g2 = (Graphics2D) g;

            // Vẽ glow lửa
            for (int i = 3; i >= 1; i--) {
                g2.setColor(new Color(255, 100, 0, 50 - i * 12));
                g2.fillRect(x - i, y - i, width + i * 2, height + i * 2);
            }
            
            // Tạo hiệu ứng gradient
            GradientPaint silver = new GradientPaint(
                    x, y, new Color(255, 150, 50),
                    x, y + height * 3, new Color(255, 200, 100)
            );

            g2.setPaint(silver);
            g2.setStroke(new BasicStroke(Math.max(2, height), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x, y - 30, 180 + x, y + height*2);
            
            // Vẽ lửa xung quanh đường
            drawFlameTrail(g2, x, y, 180 + x, y + height*2);
            
            // Viền sáng
            g2.setColor(new Color(255, 200, 150));
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(x, y - 30, 180 + x, y + height*2);
        }
        // Sukuna skill3 (type6) – ngọn lửa lớn với cháy
        else if (bulletType == 6) {
            // Vẽ massive glow lửa
            for (int i = 4; i >= 1; i--) {
                int[] glowX = {x + width/2, x - i * 4, x + width + i * 4};
                int[] glowY = {y - i * 2, y + height + i * 3, y + height + i * 3};
                g2d.setColor(new Color(255, 100, 0, 60 - i * 12));
                g2d.fillPolygon(glowX, glowY, 3);
            }
            
            // Vẽ hình ngọn lửa chính
            int[] flameX = {x + width/2, x, x + width};
            int[] flameY = {y, y + height, y + height};
            
            GradientPaint gp = new GradientPaint(x, y, new Color(255, 180, 0),
                    x, y + height, new Color(255, 100, 0));
            g2d.setPaint(gp);
            g2d.fillPolygon(flameX, flameY, 3);
            
            // Vẽ lửa ngoài
            drawFlameOuter(g2d, flameX, flameY);
            
            // Vẽ hạt tro và lửa rơi
            drawAshParticles(g2d, x + width/2, y + height);
            
            // Viền sáng chớp
            float pulse = 2 + (float)Math.sin(animTick * 0.15) * 1.5f;
            g2d.setColor(new Color(255, 220, 100));
            g2d.setStroke(new BasicStroke(pulse + 2));
            g2d.drawPolygon(flameX, flameY, 3);
            
            // Outline màu cam
            g2d.setColor(new Color(255, 150, 0));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawPolygon(flameX, flameY, 3);
            
            // Hiệu ứng FULLSCREEN LỬA
            drawFullscreenFlames(g2d);
        }
    }
    
    // Vẽ hiệu ứng SÉT GIẬT FULLSCREEN cho Gojo skill3
    private void drawFullscreenLightning(Graphics2D g2d) {
        // Vẽ các tia sét ngẫu nhiên khắp màn hình
        for (int i = 0; i < 8; i++) {
            int x1 = (int)(panelWidth * (i / 8.0 + animTick * 0.01) % 1.0);
            int y1 = (int)(panelHeight * (i / 8.0 + animTick * 0.015) % 1.0);
            
            // Tia sét chính
            double angle = (animTick * 15 + i * 45) * Math.PI / 180;
            int length = 80 + (int)(20 * Math.sin(animTick * 0.1));
            int x2 = x1 + (int)(Math.cos(angle) * length);
            int y2 = y1 + (int)(Math.sin(angle) * length);
            
            // Đảm bảo nằm trong màn hình
            x2 = Math.max(0, Math.min(x2, panelWidth));
            y2 = Math.max(0, Math.min(y2, panelHeight));
            
            g2d.setColor(new Color(100, 180, 255, 100 + (int)(100 * Math.sin(animTick * 0.1))));
            g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(x1, y1, x2, y2);
            
            // Chi nhánh nhỏ
            for (int j = 0; j < 2; j++) {
                double branchAngle = angle + (j == 0 ? 0.5 : -0.5);
                int bx = x1 + (int)(Math.cos(branchAngle) * length * 0.6);
                int by = y1 + (int)(Math.sin(branchAngle) * length * 0.6);
                
                bx = Math.max(0, Math.min(bx, panelWidth));
                by = Math.max(0, Math.min(by, panelHeight));
                
                g2d.setColor(new Color(80, 150, 255, 60 + (int)(80 * Math.sin(animTick * 0.1))));
                g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine(x2, y2, bx, by);
            }
        }
    }
    
    // Vẽ hiệu ứng LỬA RỰC FULLSCREEN cho Sukuna skill3
    private void drawFullscreenFlames(Graphics2D g2d) {
        // Vẽ hàng chục ngọn lửa khắp màn hình
        for (int i = 0; i < 20; i++) {
            // Vị trí ngẫu nhiên nhưng điều khiển bởi animation
            int fx = (int)(panelWidth * (0.05 + 0.9 * ((i + animTick * 2) % 20) / 20.0));
            int fy = (int)(panelHeight * (0.1 + 0.8 * ((i * 7 + animTick * 3) % 20) / 20.0));
            
            // Kích thước lửa
            int flameSize = 30 + (int)(15 * Math.sin(animTick * 0.1 + i));
            
            // Hình nón lửa
            int[] flameX = {fx, fx - flameSize/2, fx + flameSize/2};
            int[] flameY = {fy - flameSize, fy, fy};
            
            // Gradient màu lửa
            GradientPaint gp = new GradientPaint(fx, fy - flameSize, new Color(255, 200, 0),
                    fx, fy, new Color(255, 100, 0));
            g2d.setPaint(gp);
            
            // Độ trong suốt
            int alpha = 100 + (int)(50 * Math.sin(animTick * 0.15 + i * 0.5));
            g2d.setColor(new Color(255, 120, 0, alpha));
            g2d.fillPolygon(flameX, flameY, 3);
            
            // Viền sáng vàng
            g2d.setColor(new Color(255, 200, 100, alpha));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawPolygon(flameX, flameY, 3);
        }
        
        // Thêm hiệu ứng flash sáng ngẫu nhiên
        if (animTick % 10 < 3) {
            g2d.setColor(new Color(255, 150, 0, 30));
            g2d.fillRect(0, 0, panelWidth, panelHeight);
        }
    }
    
    // Vẽ tia năng lượng cho bắn thường Gojo
    private void drawEnergySpikes(Graphics2D g2d, int centerX, int centerY, int radius) {
        g2d.setColor(new Color(100, 220, 255, 180));
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        int numSpikes = 8;
        for (int i = 0; i < numSpikes; i++) {
            double angle = (i * 360.0 / numSpikes + animTick * 5) * Math.PI / 180;
            int endX = centerX + (int)(Math.cos(angle) * radius);
            int endY = centerY + (int)(Math.sin(angle) * radius);
            g2d.drawLine(centerX, centerY, endX, endY);
        }
    }
    
    // Vẽ lửa xung quanh viên đạn
    private void drawFlameBurst(Graphics2D g2d, int centerX, int centerY) {
        for (int i = 0; i < 6; i++) {
            double angle = (i * 60 + animTick * 8) * Math.PI / 180;
            int x1 = centerX + (int)(Math.cos(angle) * 20);
            int y1 = centerY + (int)(Math.sin(angle) * 20);
            int x2 = centerX + (int)(Math.cos(angle) * 35);
            int y2 = centerY + (int)(Math.sin(angle) * 35);
            
            g2d.setColor(new Color(255, 150, 0, 150));
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(x1, y1, x2, y2);
        }
    }
    
    // Vẽ sét giật nhỏ cho skill2 Gojo
    private void drawMiniStormLightning(Graphics2D g2d, int centerX, int centerY, int radius) {
        for (int bolt = 0; bolt < 4; bolt++) {
            double angle = (bolt * 90 + animTick * 6) * Math.PI / 180;
            int boltX = centerX + (int)(Math.cos(angle) * radius);
            int boltY = centerY + (int)(Math.sin(angle) * radius);
            
            g2d.setColor(new Color(100, 200, 255, 200));
            g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(centerX, centerY, boltX, boltY);
        }
    }
    
    // Vẽ sét giật rất mạnh cho skill3 Gojo
    private void drawStormLightning(Graphics2D g2d) {
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int radius = width / 2 + 15;
        
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Vẽ 8 tia sét lớn
        for (int i = 0; i < 8; i++) {
            double angle = (i * 45 + animTick * 10) * Math.PI / 180;
            int boltX = centerX + (int)(Math.cos(angle) * radius);
            int boltY = centerY + (int)(Math.sin(angle) * radius);
            
            // Tia chính (xanh sáng)
            g2d.setColor(new Color(150, 220, 255, 240));
            g2d.drawLine(centerX, centerY, boltX, boltY);
            
            // Tia phụ chi nhánh
            for (int j = 0; j < 2; j++) {
                double branchAngle = angle + (j == 0 ? 0.4 : -0.4);
                int branchX = centerX + (int)(Math.cos(branchAngle) * (radius * 0.6));
                int branchY = centerY + (int)(Math.sin(branchAngle) * (radius * 0.6));
                g2d.setColor(new Color(100, 200, 255, 160));
                g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine(boltX, boltY, branchX, branchY);
            }
        }
    }
    
    // Vẽ các hạt lửa rơi xung quanh
    private void drawAshParticles(Graphics2D g2d, int centerX, int centerY) {
        for (int i = 0; i < 6; i++) {
            double angle = (i * 60 + animTick * 5) * Math.PI / 180;
            int px = centerX + (int)(Math.cos(angle) * 20);
            int py = centerY + (int)(Math.sin(angle) * 20) + (animTick % 30);
            
            int size = 3 + (int)(2 * Math.sin(animTick * 0.1));
            g2d.setColor(new Color(255, 150, 0, 200 - (animTick % 30) * 6));
            g2d.fillOval(px - size/2, py - size/2, size, size);
        }
    }
    
    // Vẽ lửa ngoài phần chính
    private void drawFlameOuter(Graphics2D g2d, int[] flameX, int[] flameY) {
        int[] outerX = {flameX[0], flameX[1] - 10, flameX[2] + 10};
        int[] outerY = {flameY[0] - 10, flameY[1] + 5, flameY[2] + 5};
        
        g2d.setColor(new Color(255, 200, 100, 100));
        g2d.fillPolygon(outerX, outerY, 3);
    }
    
    // Vẽ vết lửa theo đường
    private void drawFlameTrail(Graphics2D g2, int x1, int y1, int x2, int y2) {
        int pointCount = 8;
        for (int i = 0; i < pointCount; i++) {
            double t = (double) i / pointCount;
            int px = (int)(x1 + (x2 - x1) * t);
            int py = (int)(y1 + (y2 - y1) * t);
            
            double offset = Math.sin((animTick * 0.1 + i) * 0.5) * 10;
            int offsetX = (int)(offset * Math.cos(Math.atan2(y2 - y1, x2 - x1) + Math.PI/2));
            int offsetY = (int)(offset * Math.sin(Math.atan2(y2 - y1, x2 - x1) + Math.PI/2));
            
            g2.setColor(new Color(255, 150, 0, 150 - i * 20));
            g2.fillOval(px + offsetX - 3, py + offsetY - 3, 6, 6);
        }
    }
    
    // Vẽ hạt lửa spark
    private void drawSparks(Graphics2D g2d, int centerX, int centerY, int radius, Color color) {
        for (int i = 0; i < 8; i++) {
            double angle = (i * 45 + animTick * 12) * Math.PI / 180;
            int sx = centerX + (int)(Math.cos(angle) * radius);
            int sy = centerY + (int)(Math.sin(angle) * radius);
            
            int size = 2 + (int)(1.5f * Math.sin(animTick * 0.15));
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 200 - (animTick * 2) % 200));
            g2d.fillOval(sx - size, sy - size, size * 2, size * 2);
        }
    }
    
    // Vẽ trail effect cho skill3
    private void drawTrail(Graphics2D g2d) {
        if (trailHistory.isEmpty()) return;
        
        if (bulletType == 1) {
            // Bắn thường Gojo - trail xanh
            for (int i = 0; i < trailHistory.size(); i++) {
                int[] pos = trailHistory.get(i);
                float alpha = (float) i / trailHistory.size();
                int radius = (int) (width/4 * alpha);
                if (radius > 1) {
                    g2d.setColor(new Color(80, 180, 255, (int)(80 * alpha)));
                    g2d.fillOval(pos[0] - radius, pos[1] - radius, radius*2, radius*2);
                }
            }
        }
        else if (bulletType == 2) {
            // Bắn thường Sukuna - trail đỏ cam
            for (int i = 0; i < trailHistory.size(); i++) {
                int[] pos = trailHistory.get(i);
                float alpha = (float) i / trailHistory.size();
                int radius = (int) (width/4 * alpha);
                if (radius > 1) {
                    g2d.setColor(new Color(255, 100, 0, (int)(100 * alpha)));
                    g2d.fillOval(pos[0] - radius, pos[1] - radius, radius*2, radius*2);
                }
            }
        }
        else if (bulletType == 4) {
            // Gojo skill3 - hiệu ứng điện xanh tím
            for (int i = 0; i < trailHistory.size(); i++) {
                int[] pos = trailHistory.get(i);
                float alpha = (float) i / trailHistory.size();
                
                // Vẽ các quả cầu nhỏ hơn phía sau với độ trong suốt giảm
                int radius = (int) (width/2 * alpha * 0.5f);
                if (radius > 1) {
                    g2d.setColor(new Color(180, 120, 220, (int)(120 * alpha)));
                    g2d.fillOval(pos[0] - radius, pos[1] - radius, radius*2, radius*2);
                    
                    // Vẽ sét giật nhỏ
                    drawMiniLightning(g2d, pos[0], pos[1], radius, alpha);
                    
                    // Vẽ tia sáng nối các điểm
                    if (i > 0) {
                        int[] prevPos = trailHistory.get(i-1);
                        g2d.setColor(new Color(150, 200, 255, (int)(100 * alpha)));
                        g2d.setStroke(new BasicStroke(2 * alpha, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2d.drawLine(prevPos[0], prevPos[1], pos[0], pos[1]);
                    }
                }
            }
        }
        else if (bulletType == 6) {
            // Sukuna skill3 - hiệu ứng lửa cháy đi qua
            for (int i = 0; i < trailHistory.size(); i++) {
                int[] pos = trailHistory.get(i);
                float alpha = (float) i / trailHistory.size();
                
                // Vẽ hình lửa nhỏ hơn phía sau
                int trailWidth = (int)(width * 0.6f * alpha);
                int trailHeight = (int)(height * 0.4f * alpha);
                
                if (trailWidth > 2) {
                    // Hình nón lửa
                    int[] flameX = {pos[0], pos[0] - trailWidth/2, pos[0] + trailWidth/2};
                    int[] flameY = {pos[1] - trailHeight/2, pos[1] + trailHeight/2, pos[1] + trailHeight/2};
                    
                    // Gradient đỏ-cam mờ
                    g2d.setColor(new Color(255, 120, 0, (int)(160 * alpha)));
                    g2d.fillPolygon(flameX, flameY, 3);
                    
                    // Viền vàng sáng
                    g2d.setColor(new Color(255, 200, 100, (int)(120 * alpha)));
                    g2d.setStroke(new BasicStroke(1.5f));
                    g2d.drawPolygon(flameX, flameY, 3);
                }
            }
        }
    }
    
    // Vẽ sét giật nhỏ cho trail
    private void drawMiniLightning(Graphics2D g2d, int centerX, int centerY, int radius, float alpha) {
        g2d.setColor(new Color(150, 200, 255, (int)(150 * alpha)));
        g2d.setStroke(new BasicStroke(1.5f * alpha, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Vẽ 3 tia sét nhỏ
        for (int i = 0; i < 3; i++) {
            double angle = (i * 120 + animTick * 5) * Math.PI / 180;
            int boltX = centerX + (int)(Math.cos(angle) * radius);
            int boltY = centerY + (int)(Math.sin(angle) * radius);
            g2d.drawLine(centerX, centerY, boltX, boltY);
        }
    }


    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}