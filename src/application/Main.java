package application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import layout.MainController;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		
		try {
			FXMLLoader mainLoader = new FXMLLoader();
			mainLoader.setLocation(getClass().getResource("/layout/MainLayout.fxml"));
			
			AnchorPane ap = mainLoader.load();
			Scene scene = new Scene(ap);
			primaryStage.setScene(scene);
			primaryStage.show();
			
			MainController mc = mainLoader.getController();
			mc.loadList();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
