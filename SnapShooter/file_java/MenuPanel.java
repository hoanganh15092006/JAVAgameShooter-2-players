package SnapShooter.file_java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import java.util.ArrayList;

class MenuPanel extends JPanel implements ActionListener {

    private ArrayList<Image> backgrounds = new ArrayList<>();
    private int currentBackground = 0;

    private JButton startButton;
    private JButton changeBgButton;

    private Main mainFrame;

    public MenuPanel(Main frame) {

        this.mainFrame = frame;

        setLayout(null);
        loadBackgrounds();

        startButton = new JButton("START GAME");
        startButton.setBounds(300, 250, 200, 50);
        startButton.addActionListener(this);
        add(startButton);

        changeBgButton = new JButton("CHANGE BACKGROUND");
        changeBgButton.setBounds(300, 320, 200, 50);
        changeBgButton.addActionListener(this);
        add(changeBgButton);

        // Thêm listener để cập nhật vị trí nút khi cửa sổ thay đổi kích thước
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateButtonPositions();
            }
        });
    }

    // Phương thức cập nhật vị trí nút dựa trên kích thước panel
    private void updateButtonPositions() {
        int buttonWidth = 200;
        int buttonHeight = 50;
        int buttonX = (getWidth() - buttonWidth) / 2;

        // Đặt nút START GAME ở vị trí giữa chiều rộng, trên nút CHANGE BACKGROUND
        startButton.setBounds(buttonX, getHeight() / 2 - 80, buttonWidth, buttonHeight);
        
        // Đặt nút CHANGE BACKGROUND dưới nút START GAME
        changeBgButton.setBounds(buttonX, getHeight() / 2, buttonWidth, buttonHeight);
    }

    private void loadBackgrounds() {
        try {
            backgrounds.add(ImageIO.read(
                    getClass().getResource("../src/images/background1.png")
            ));
            backgrounds.add(ImageIO.read(
                    getClass().getResource("../src/images/background2.png")
            ));
            backgrounds.add(ImageIO.read(
                    getClass().getResource("../src/images/background3.png")
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!backgrounds.isEmpty()) {
            g.drawImage(backgrounds.get(currentBackground),
                    0, 0, getWidth(), getHeight(), this);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        
        // Lấy thông tin về kích thước của văn bản để căn giữa
        String title = "SPACE SHOOTER";
        FontMetrics fm = g.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        int titleX = (getWidth() - titleWidth) / 2;  // Căn giữa theo chiều ngang
        int titleY = (int) (getHeight() * 0.25);     // Đặt ở 1/4 chiều cao
        
        g.drawString(title, titleX, titleY);
    }

    // Phương thức trả về backgroundIndex hiện tại
    public int getBackgroundIndex() {
        return currentBackground;
    }

    // Phương thức trả về danh sách backgrounds
    public ArrayList<Image> getBackgrounds() {
        return backgrounds;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == startButton) {
            mainFrame.showGame(currentBackground); // chuyển sang GamePanel và truyền background index
        }

        if (e.getSource() == changeBgButton) {
            currentBackground++;
            if (currentBackground >= backgrounds.size()) {
                currentBackground = 0;
            }
            repaint();
        }
    }
}