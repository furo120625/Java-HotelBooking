package hotelbooking;

import hotelbooking.dao.DataStore;
import hotelbooking.service.HotelService;
import hotelbooking.ui.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                hotelbooking.ui.UiStyle.install();
                DataStore store = new DataStore();
                HotelService service = new HotelService(store);
                new LoginFrame(service).setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Không thể khởi động ứng dụng: " + ex.getMessage(),
                        "Lỗi khởi động", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}