import javax.swing.*;
import java.awt.*;

public class AdventureApp {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame mainFrame = new JFrame("Adventure Game");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(1200, 400);
            mainFrame.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

            AdventureGame game = new AdventureGame();
            System.out.println("Created Adventure Game");

            GamePanel mapPanel = new GamePanel(game.player, game.getGameMap());
            NavigationPanel sidePanel = new NavigationPanel(game, mapPanel);

            mainFrame.add(sidePanel);
            mainFrame.add(game);
            mainFrame.add(mapPanel);
            System.out.println("Created Adventure Game GUI");

            mainFrame.setVisible(true);
        });


    }



}