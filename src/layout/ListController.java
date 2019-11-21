package layout;

import java.io.File;

import javafx.collections.MapChangeListener.Change;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;

public class ListController {
	@FXML
	private AnchorPane listRoot;
	@FXML
	private ImageView imgView;

	@FXML
	private Label lblRealTitle;
	@FXML
	private Label lblArtist;
	
	private MainController mc;
	private File musicFile;
	private Media sound;
	
	public boolean isCurrentMusic(Media sound) {
		return this.sound == sound;
	}
	
	
	public String getFilename() {
		return musicFile.getName();
	}
	//불러오는거
	public void loadData(File music, MainController mc) {
		this.musicFile = music;
		sound = new Media(musicFile.toURI().toString());
		sound.getMetadata().addListener((Change<? extends String, ? extends Object> c) -> {
	        if (c.wasAdded()) {
	        	
	        	//System.out.println(c.getKey() + " : " + c.getValueAdded());
	        	
	        	if(c.getKey().equals("title")) {
	        		lblRealTitle.setText((String)c.getValueAdded());
	        	}
	        	
	        	if(c.getKey().equals("artist")) {
	        		lblArtist.setText((String)c.getValueAdded());
	        	}
	        	
	        	if(c.getKey().equals("image")) {
	        		imgView.setImage((Image)c.getValueAdded());
	        	}
	        }
	    });
		this.mc = mc;
	}

	//클릭시 이벤트
	public void selectMusic() {
		this.mc.setMusicPlayer(sound);
	}
		
}
