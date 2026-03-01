package SnapShooter.file_java;

import javax.swing.*;
import java.awt.*;

// Lớp Main: Cửa sổ chính của ứng dụng game Space Shooter
// Quản lý hai màn hình chính: Menu và Game bằng CardLayout
public class Main extends JFrame {

    // Bộ quản lý layout cho việc chuyển đổi giữa các panel
    CardLayout cardLayout;
    // Container chứa các panel (Menu và Game)
    JPanel container;
    // Tham chiếu đến GamePanel để quản lý
    private GamePanel gamePanel;
    // Tham chiếu đến MenuPanel
    private MenuPanel menuPanel;

    // Phương thức khởi tạo: Cấu hình cửa sổ chính và tính năng giao diện
    public Main() {

        // Đặt tiêu đề cửa sổ
        setTitle("Snapshooter 2 Players");
        // Đóng ứng dụng khi đóng cửa sổ
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Khởi tạo CardLayout để quản lý việc chuyển đổi giữa các panel
        cardLayout = new CardLayout();
        // Khởi tạo container với CardLayout
        container = new JPanel(cardLayout);

        // Tạo panel Menu với tham chiếu đến cửa sổ chính
        menuPanel = new MenuPanel(this);
        // Tạo panel Game với tham chiếu đến cửa sổ chính
        gamePanel = new GamePanel(this);

        // Thêm menu panel vào container với tên "Menu"
        container.add(menuPanel, "Menu");
        // Thêm game panel vào container với tên "Game"
        container.add(gamePanel, "Game");

        // Thêm container vào cửa sổ chính
        add(container);

        // Đặt kích thước cửa sổ: 800 pixel chiều rộng, 600 pixel chiều cao
        setSize(800, 600);
        // Căn giữa cửa sổ trên màn hình
        setLocationRelativeTo(null);
        // Cho phép thay đổi kích thước cửa sổ để có thể phóng to/thu nhỏ
        setResizable(true);
        // Hiển thị cửa sổ
        setVisible(true);
    }

    // Phương thức chuyển đổi sang màn hình Game
    // Hiển thị Game panel bằng cách gọi cardLayout.show()
    // Phương thức hiển thị game panel với background được chọn
    public void showGame(int backgroundIndex) {
        // Truyền backgrounds từ menu sang game
        gamePanel.setBackgrounds(menuPanel.getBackgrounds(), backgroundIndex);
        
        // Hiển thị GamePanel và đặt tiêu điểm để nhận bàn phím ngay lập tức
        cardLayout.show(container, "Game");
        gamePanel.requestFocusInWindow();
    }

    // Phương thức overload (dùng cho menu)
    public void showGame() {
        showGame(0);
    }
    
    // Phương thức chuyển đổi sang màn hình Menu
    public void showMenu() {
        cardLayout.show(container, "Menu");
    }

    // Phương thức main: Điểm bắt đầu của ứng dụng
    // SwingUtilities.invokeLater đảm bảo giao diện được tạo trên Event Dispatch Thread
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}