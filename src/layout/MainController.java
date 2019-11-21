package layout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import application.Main;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainController {
	@FXML
	private AnchorPane mainRoot;

	@FXML
	private Button btnPlay;
	@FXML
	private ImageView currentImg;

	@FXML
	private Label lblNowTime;
	@FXML
	private Label lblFullTime;

	@FXML
	private Stage mainStage;
	@FXML
	private Button btnOpenDir;

	@FXML
	private VBox listPane;

	@FXML
	private Button btnNext;
	@FXML
	private Button btnPrev;

	@FXML
	private Slider volController;
	@FXML
	private Slider timeController;

	private List<ListController> musicList = new ArrayList<ListController>();

	private MediaPlayer mediaPlayer;
	private Media currentMedia;
	private String playing = "";
	private ObservableMap<String, Object> metaData;
	private Duration duration;
	// 음악이 재생 중 인가요?
	private boolean isPlaying = false;

	public void setMusicPlayer(Media sound) {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}
		this.currentMedia = sound;

		metaData = this.currentMedia.getMetadata();

		isPlaying = false;
		playMusic();
	}

	// 음악 재생 - 일시정지
	public void playMusic() {
		currentImg.setImage((Image) metaData.get("image"));
		// 재생 중 인지 검사
		if (isPlaying) {
			// 재생 중 이면 일시정지
			mediaPlayer.pause();
			isPlaying = false;
		} else {
			String compare = this.currentMedia.getSource();
			if (mediaPlayer == null || playing != compare) {
				playing = this.currentMedia.getSource();
				mediaPlayer = new MediaPlayer(this.currentMedia);
				mediaPlayer.setVolume(volController.getValue() / 100.0);
			}
			mediaPlayer.play();
			isPlaying = true;
		}

		// 프로그래스바
		mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
			DateFormat formatter = new SimpleDateFormat("mm:ss");
			lblNowTime.setText(formatter.format(newValue.toMillis()));
			lblFullTime.setText(formatter.format(this.currentMedia.getDuration().toMillis()));

			// 노래가 끝나면 다음노래
			if (newValue.toMillis() >= this.currentMedia.getDuration().toMillis()) {
				nextMusic();
			}

			double now = newValue.toMillis() / this.currentMedia.getDuration().toMillis() * 100;
			timeController.setValue(now);

			timeController.valueProperty().addListener(new InvalidationListener() {
				public void invalidated(Observable ov) {
					if (timeController.isValueChanging()) {
						duration = mediaPlayer.getMedia().getDuration();
						mediaPlayer.seek(duration.multiply(timeController.getValue() / 100.0));
					}
				}
			});
		});

		if (isPlaying)
			btnPlay.setText("정   지");
		else
			btnPlay.setText("재   생");
	}

	public void nextMusic() {
		for (int i = 0; i < musicList.size(); i++) {
			ListController lc = musicList.get(i);
			if (lc.isCurrentMusic(currentMedia)) {
				try {
					lc = musicList.get(i + 1);
					lc.selectMusic();
					return;
				} catch (IndexOutOfBoundsException e) {
					// 마지막파일일때
					lc = musicList.get(0);
					lc.selectMusic();
					return;
				}
			}
		}
	}

	public void prevMusic() {
		for (int i = 0; i < musicList.size(); i++) {
			ListController lc = musicList.get(i);
			if (lc.isCurrentMusic(currentMedia)) {
				try {
					lc = musicList.get(i - 1);
					lc.selectMusic();
					return;
				} catch (IndexOutOfBoundsException e) {
					// 마지막파일일때
					lc = musicList.get(musicList.size() - 1);
					lc.selectMusic();
					return;
				}
			}
		}
	}

	public void controllVolume() {
		volController.valueProperty().addListener(new InvalidationListener() {
			public void invalidated(Observable ov) {
				if (volController.isValueChanging()) {
					mediaPlayer.setVolume(volController.getValue() / 100.0);
				}
			}
		});
	}

	public void openDir() {
		DirectoryChooser dc = new DirectoryChooser();
		dc.setTitle("이미지 파일이 있는 폴더를 선택하세요");
		ArrayList<File> fileList = new ArrayList<File>();

		File dir = dc.showDialog(mainStage);

		// 선택한 디렉토리
		File[] selectMusic = dir.listFiles();
		// MP3형식의 파일만 찾아 fileList에 추가
		for (File file : selectMusic) {
			int dotIndex = file.toString().lastIndexOf('.');
			if (file.toString().substring(dotIndex + 1).equals("mp3")) {
				fileList.add(file);
			}
		}
		// MP3형식의 파일이 없을 때
		if (fileList.size() < 1)
			System.out.println("음악 파일을 찾지못했습니다. (mp3형식의 파일만 지원합니다.)");

		for (File f : fileList) {
			File musicFile = new File("C:\\myJavaMP3\\" + f.getName());

			InputStream is = null;
			OutputStream os = null;
			try {
				is = new FileInputStream(f);
				os = new FileOutputStream(musicFile);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("파일을 복사할 수 없수다");
			}
		}

		loadList();

	}

	// loadList
	public void loadList() {
		File musicFile = new File("C:\\myJavaMP3");

		if (musicFile.exists()) { // 경로가 존재하면
			File[] musicFileList = musicFile.listFiles();
			try {
				for (File f : musicFileList) {
					FXMLLoader xmlLoader = new FXMLLoader();
					xmlLoader.setLocation(Main.class.getResource("/layout/ListLayout.fxml"));
					AnchorPane ll = xmlLoader.load();
					ListController lc = xmlLoader.getController();
					lc.loadData(f, this);

					musicList.add(lc);
					listPane.getChildren().add(ll);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("오류");
			}
		} else { // 경로가 존재하지 않으면 경로생성
			musicFile.mkdirs();
		}
	}

	// drop
	public void listDropped(DragEvent event) {
		Dragboard db = event.getDragboard();
		boolean success = false;
		if (db.hasFiles()) {
			for (File f : db.getFiles()) {
				System.out.println(f.getAbsolutePath());
				File musicFile = new File("C:\\myJavaMP3\\" + f.getName());

				InputStream is = null;
				OutputStream os = null;
				try {
					is = new FileInputStream(f);
					os = new FileOutputStream(musicFile);
					byte[] buffer = new byte[1024];
					int length;
					while ((length = is.read(buffer)) > 0) {
						os.write(buffer, 0, length);
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("파일을 복사할 수 없수다");
				}
			}
			
			loadList();
			success = true;
		}

		event.setDropCompleted(success);
		event.consume();

	}

	public void listDragOver(DragEvent event) {
		if (event.getGestureSource() != listPane && event.getDragboard().hasFiles()) {
			event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
		}
		event.consume();
	}
}
