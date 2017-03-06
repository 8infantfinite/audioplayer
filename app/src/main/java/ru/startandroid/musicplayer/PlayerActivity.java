package ru.startandroid.musicplayer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class PlayerActivity extends AppCompatActivity implements OnCompletionListener, SeekBar.OnSeekBarChangeListener {

	private ImageButton btnPlay;
	private ImageButton btnForward;
	private ImageButton btnBackward;
	private ImageButton btnNext;
	private ImageButton btnPrevious;
	private ImageButton btnPlaylist;
	private ImageButton btnRepeat;
	private ImageButton btnShuffle;

	private SeekBar songSeekBar;

	private TextView songTitleLabel;
	private TextView songCurrentDurationLabel;
	private TextView songTotalDurationLabel;
	// Media Player
	private  MediaPlayer mp;
	// обработчик для обновления таймера
	private Handler mHandler = new Handler();
	private ConvertTime utils;
	private int seekForwardTime = 5000; // 5000 миллисекунд
	private int seekBackwardTime = 5000; // 5000 миллисекунд
	public int currentSongIndex = 0;

	private boolean isShuffle = false;
	private boolean isRepeat = false;

	private String folderPath = "";
	private String author = "";
	private String title = "";

	private List<String> songsList;
	private List<String> AuthorList;

    boolean playlistMode; //определение режима выбора песни
	public int itemPosition = -1;
	private ImageView imageView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		imageView = (ImageView)findViewById(R.id.imageView);

		// All player buttons
		btnPlay = (ImageButton) findViewById(R.id.btnPlay);
		btnForward = (ImageButton) findViewById(R.id.btnForward);
		btnBackward = (ImageButton) findViewById(R.id.btnBackward);
		btnNext = (ImageButton) findViewById(R.id.btnNext);
		btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
		btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);
		btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
		btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
		songSeekBar = (SeekBar) findViewById(R.id.songSeekBar);
		songTitleLabel = (TextView) findViewById(R.id.songTitle);
		songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
		songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
		
		// Mediaplayer
		mp = new MediaPlayer();
		utils = new ConvertTime();
		
		// Listeners
		songSeekBar.setOnSeekBarChangeListener(this);
		mp.setOnCompletionListener(this);
		


		btnPlay.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				// check for already playing

				if(songTitleLabel.getText().toString().equals(getResources().getString(R.string.SongTitle))){
					Intent i = new Intent(getApplicationContext(), FileManagerActivity.class);
					i.putExtra("SongPosition", itemPosition);
					startActivityForResult(i, 100);
					return;
				}
				if(mp.isPlaying()){ //Остановка песни
					if(mp!=null){
						mp.pause();
						btnPlay.setImageResource(R.drawable.btn_play);
					}
				}else{
					if(mp!=null){ // Возобновление песни
						mp.start();
						btnPlay.setImageResource(R.drawable.btn_pause);
					}
				}
				
			}
		});
		
		/**
		 * Промотать песню на 5 сек вперёд
		 **/
		btnForward.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				if(songTitleLabel.getText().toString().equals(getResources().getString(R.string.SongTitle))){
					Intent i = new Intent(getApplicationContext(), FileManagerActivity.class);
					i.putExtra("SongPosition", itemPosition);
					startActivityForResult(i, 100);
					return;
				}
				// Получаем текущую позицию продолжительнности песни
				int currentPosition = mp.getCurrentPosition();
				// проверяем если текущее время меньше, чем продолжительность песни
				if(currentPosition + seekForwardTime <= mp.getDuration()){
					// перемотка на 5 сек
					mp.seekTo(currentPosition + seekForwardTime);
				}else{
					// перемотка в конец
					mp.seekTo(mp.getDuration());
				}
			}
		});
		
		/**
		 * Промотать песню на 5 секунд назад
		 **/
		btnBackward.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if(songTitleLabel.getText().toString().equals(getResources().getString(R.string.SongTitle))){
					Intent i = new Intent(getApplicationContext(), FileManagerActivity.class);
					i.putExtra("SongPosition", itemPosition);
					startActivityForResult(i, 100);
					return;
				}
				// Получаем текущую позицию продолжительнности песни
				int currentPosition = mp.getCurrentPosition();
				// проверяем если текущее время больше, чем 0 секунд
				if(currentPosition - seekBackwardTime >= 0){
					// перематываем назад
					mp.seekTo(currentPosition - seekBackwardTime);
				}else{
					// перемотка в начало
					mp.seekTo(0);
				}
				
			}
		});
		
		/**
		 *
		 * Следующая песня
		 * */
		btnNext.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if(songTitleLabel.getText().toString().equals(getResources().getString(R.string.SongTitle))){
					Intent i = new Intent(getApplicationContext(), FileManagerActivity.class);
					i.putExtra("SongPosition", itemPosition);
					startActivityForResult(i, 100);
					return;
				}
				// Проверяем есть ли следующая песня
				if(currentSongIndex < (songsList.size() - 1)){
					playSong(currentSongIndex + 1, playlistMode);
					currentSongIndex = currentSongIndex + 1;
					itemPosition = currentSongIndex;
				}else{
					// играм первую песню
					playSong(0, playlistMode);
					currentSongIndex = 0;
					itemPosition = currentSongIndex;
				}
				
			}
		});
		
		/**
		 * Предыдущая песня
		 * */
		btnPrevious.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if(songTitleLabel.getText().toString().equals(getResources().getString(R.string.SongTitle))){

					Intent i = new Intent(getApplicationContext(), FileManagerActivity.class);
					i.putExtra("SongPosition", itemPosition);
					startActivityForResult(i, 100);
					return;
				}
				//Проверяем есть ли предыдущая песня
				if(currentSongIndex > 0){
					playSong(currentSongIndex - 1, playlistMode);
					currentSongIndex = currentSongIndex - 1;
					itemPosition = currentSongIndex;
				}else{
					// играем последюю песню
					playSong(songsList.size() - 1, playlistMode);
					currentSongIndex = songsList.size() - 1;
					itemPosition = currentSongIndex;
				}
				
			}
		});
		
		/**
		 * Повторение песни
		 * */
		btnRepeat.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(isRepeat){
					isRepeat = false;
					Toast.makeText(getApplicationContext(), "Повторение выключено", Toast.LENGTH_SHORT).show();
					btnRepeat.setImageResource(R.drawable.btn_repeat);
				}else{

					isRepeat = true;
					Toast.makeText(getApplicationContext(), "Повторение включено", Toast.LENGTH_SHORT).show();

					isShuffle = false;
					btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
					btnShuffle.setImageResource(R.drawable.btn_shuffle);
				}	
			}
		});
		
		/**
		 * Случайный порядок
		 * */
		btnShuffle.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(isShuffle){
					isShuffle = false;
					Toast.makeText(getApplicationContext(), "Случайный порядок выключен", Toast.LENGTH_SHORT).show();
					btnShuffle.setImageResource(R.drawable.btn_shuffle);
				}else{
					isShuffle= true;
					Toast.makeText(getApplicationContext(), "Случайный порядок включён", Toast.LENGTH_SHORT).show();
					isRepeat = false;
					btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
					btnRepeat.setImageResource(R.drawable.btn_repeat);
				}	
			}
		});
		
		/**
		 *Вызов плэйлиста
		 * */
		btnPlaylist.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				Intent i = new Intent(getApplicationContext(), FileManagerActivity.class);

				if(folderPath.equals("") || folderPath.equals("sdcard")){
					i.putExtra("SongPosition", itemPosition);
					i.putExtra("playlistMode", playlistMode);
					startActivityForResult(i, 100);
				}else{
					i.putExtra("path", folderPath);
					i.putExtra("songTitle", songTitleLabel.getText().toString());
					i.putExtra("playlistMode", playlistMode);
					i.putExtra("SongPosition", itemPosition);
					startActivityForResult(i, 100);
				}

			}
		});
		
	}
	
	/**
	 *Получение индекса песни из плэйлиста
	 * и её воспроизведение
	 * */
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data); // зачем что такое это 1,2

		if(resultCode == RESULT_CANCELED){
			return;
		}

        if(resultCode == 100){
			itemPosition = -1;
			currentSongIndex = data.getExtras().getInt("songIndex");
			folderPath = data.getExtras().getString("folderPath");

			String imageJpg = "";
			// если в папке с музыкой есть картинка, то вставить её в ImageView
			if(data.hasExtra("jpg") && !data.getExtras().getString("jpg").equals("")){
				imageJpg = data.getExtras().getString("jpg");
				Bitmap bitmap = BitmapFactory.decodeFile(folderPath + "/" + imageJpg);
				imageView.setImageBitmap(bitmap);

			}else{
				imageView.setImageResource(R.drawable.default_image1);
			}

			songsList = Arrays.asList(data.getStringArrayExtra("songsArray"));
			author = "";

			playlistMode = true;
			playSong(currentSongIndex, playlistMode);

        }else{

			playlistMode = false;
			imageView.setImageResource(R.drawable.default_image1);
			songsList = data.getStringArrayListExtra("list");
			currentSongIndex = data.getExtras().getInt("index");
			author = data.getExtras().getString("author");
			AuthorList = data.getExtras().getStringArrayList("listAuthors");
			folderPath = "sdcard";

			itemPosition = data.getIntExtra("SongPosition",0);
			playSong(currentSongIndex, playlistMode);
		}
 
    }
	
	/**
	 * Метод для воспроизведения музыки
	 * */
	public void  playSong(int songIndex, boolean playlistMode){
		try {
        	mp.reset();
			String songTitle = "";
			if(playlistMode){
				mp.setDataSource(folderPath + "/" + songsList.get(songIndex));
				songTitle = songsList.get(songIndex);
			}else{
				mp.setDataSource(songsList.get(songIndex));
				songTitle = songsList.get(songIndex).substring(songsList.get(songIndex).lastIndexOf("/") + 1);
			}

			mp.prepare();
			mp.start();


			// Отображение названия песни после воспроизведения
			songTitleLabel.setText(songTitle);
			songTitleLabel.setSelected(true);

			//<Создание уведомления
			Intent notIntent = new Intent(this, PlayerActivity.class);
			notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			PendingIntent pendInt = PendingIntent.getActivity(this, 0,
					notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

			if(author.equals("")){
				title = "Playing";
			}else{
				title = AuthorList.get(songIndex);
			}

			builder.setContentIntent(pendInt)
					.setSmallIcon(R.drawable.play)
					.setTicker(songTitleLabel.getText().toString())
					.setOngoing(true)
					.setContentTitle(title)
					.setWhen(0)
					.setContentText(songTitleLabel.getText().toString());
			Notification not = builder.build();

			NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
			notificationManager.notify(0, not);
				// /Создание уведомления>


			btnPlay.setImageResource(R.drawable.btn_pause);
			
			// Установка значений SeekBar
			songSeekBar.setProgress(0);
			songSeekBar.setMax(100);

			// Обновления SeekBar
			updateSeekBar();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Метод для обновления времни
	 * */
	public void updateSeekBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);        
    }	
	
	/**
	 * Метод для фонового изменения времени
	 * */
	private Runnable mUpdateTimeTask = new Runnable() {
		   public void run() {
			   long totalDuration = mp.getDuration();
			   long currentDuration = mp.getCurrentPosition();
			  
			   // Показываем текущее время
			   songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
			   // Показываем конечное время
			   songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));
			   
			   // обновление SeekBar
			   int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
			   songSeekBar.setProgress(progress);

		       mHandler.postDelayed(this, 100);
		   }
		};
		
	/**
	 *
	 * */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
	}

	/**
	 * Метод для обработки перетягивания SeekBar
	 * */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		if(songTitleLabel.getText().toString().equals(getResources().getString(R.string.SongTitle))){
			return;
		}
		mHandler.removeCallbacks(mUpdateTimeTask);
    }
	
	/**
	 * Метод для обработки момента, когда отпускается SeekBar
	 * */
	@Override
    public void onStopTrackingTouch(SeekBar seekBar) {
		if(songTitleLabel.getText().toString().equals(getResources().getString(R.string.SongTitle))){
			return;
		}
		mHandler.removeCallbacks(mUpdateTimeTask);
		int totalDuration = mp.getDuration();
		int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
		
		// перемотка времени
		mp.seekTo(currentPosition);
		
		//обновление
		updateSeekBar();
    }

	/**
	 *Метод для работы повтора и шафла
	 * */
	@Override
	public void onCompletion(MediaPlayer arg0) {
		
		// Проверяем состояние повтора
		if(isRepeat){
			// песня играет по кругу
			playSong(currentSongIndex, playlistMode);
		} else if(isShuffle){
			// играет в случайном порядке
			Random rand = new Random();
			currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
			playSong(currentSongIndex, playlistMode);
		} else{
			//если не то и не другое играет следующую песню
			if(currentSongIndex < (songsList.size() - 1)){
				playSong(currentSongIndex + 1, playlistMode);
				currentSongIndex = currentSongIndex + 1;
				itemPosition = currentSongIndex;
			}else{
				// если нет следующуей играет первую
				playSong(0, playlistMode);
				currentSongIndex = 0;
				itemPosition = currentSongIndex;
			}
		}
	}

	@Override
	 public void onDestroy(){
		//удаление уведомления
		NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
	 super.onDestroy();
	    mp.release();
		mHandler.removeCallbacks(mUpdateTimeTask);
	 }
	
}