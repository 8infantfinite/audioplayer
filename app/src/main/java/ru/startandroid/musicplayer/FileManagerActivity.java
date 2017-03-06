package ru.startandroid.musicplayer;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileManagerActivity extends ListActivity {
    final String ATTRIBUTE_NAME_TEXT = "text";
    final String ATTRIBUTE_NAME_TEXT_AUTHOR = "text_author";
    final String ATTRIBUTE_NAME_IMAGE = "image";
    final String ATTRIBUTE_NAME_IMAGE_PLAY = "image_play";
    final String ATTRIBUTE_IMAGE_PLAY = "play";

    private String path;
    private String songTitle = "";

    List<String> values = new ArrayList<String>();

    private ImageButton btnBackFolder;
    private TextView editText;
    private SwitchCompat switchFolder;
    private int width;
    private int height;

    ArrayList<String> listOfSongs;
    ArrayList<String> listOfSongsPath;
    ArrayList<String> listOfSongsAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_manager_activity);
        btnBackFolder = (ImageButton) findViewById(R.id.btnBackFolder);
        editText = (TextView) findViewById(R.id.editText);
        switchFolder = (SwitchCompat)findViewById(R.id.switchFolder);
        // минимальные параметры для поиска альбомных картинок
        width = 150;
        height = 150;

        path = "sdcard";

        //Проверка для сохранения состояние после поворота экрана
        if(savedInstanceState == null){
            if (getIntent().hasExtra("path")) {
                path = getIntent().getStringExtra("path");
            }

        }else{
            path = savedInstanceState.getString("StatePath");
        }
        //обработка свитча
        switchFolder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {

                if (check) {
                    //обновляем ListView
                    updateListView(path);}

                else {
                    // с помощью ContentProvider достаём из телефона музыка и добавляем в общий список
                    editText.setText("");
                    Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    Cursor cursor = getContentResolver().query(uri, null, MediaStore.Audio.Media.IS_MUSIC + " != 0", null, null);
                    listOfSongs = new ArrayList<String>();
                    listOfSongsPath = new ArrayList<String>();
                    listOfSongsAuthor = new ArrayList<String>();
                    cursor.moveToFirst();

                    while (cursor.moveToNext()) {
                        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                        String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        listOfSongs.add(title);
                        listOfSongsPath.add(data);
                        listOfSongsAuthor.add(artist);
                    }
                    cursor.close();

                    ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(listOfSongs.size());
                    Map<String, Object> m;

                    int currentSong = R.drawable.play;
                    int empty = R.drawable.empty_picture;

                    for (int i = 0; i < listOfSongs.size(); i++) {
                        m = new HashMap<String, Object>();
                        if(i == getIntent().getIntExtra("SongPosition", 0)){
                            m.put(ATTRIBUTE_NAME_TEXT, listOfSongs.get(i));
                            m.put(ATTRIBUTE_NAME_TEXT_AUTHOR, listOfSongsAuthor.get(i));
                            m.put(ATTRIBUTE_IMAGE_PLAY, currentSong);
                        }else{
                            m.put(ATTRIBUTE_NAME_TEXT, listOfSongs.get(i));
                            m.put(ATTRIBUTE_NAME_TEXT_AUTHOR, listOfSongsAuthor.get(i));
                            m.put(ATTRIBUTE_IMAGE_PLAY, empty);
                        }
                        data.add(m);
                    }

                    // массив имен атрибутов, из которых будут читаться данные
                    String[] from = {ATTRIBUTE_NAME_TEXT, ATTRIBUTE_NAME_TEXT_AUTHOR, ATTRIBUTE_IMAGE_PLAY};
                    // массив ID View-компонентов, в которые будут вставлять данные
                    int[] to = {R.id.songTitle, R.id.songAuthor, R.id.empty};

                    // создаем адаптер
                    SimpleAdapter sAdapter = new SimpleAdapter(FileManagerActivity.this, data, R.layout.songs_playlist_item,
                            from, to);
                    setListAdapter(sAdapter);
                    setSelection(getIntent().getIntExtra("SongPosition", 0));

                }
            }
        });

        boolean check = false;
        switchFolder.setChecked(check);
        check = getIntent().getBooleanExtra("playlistMode", false);
        switchFolder.setChecked(check);

        /**
         *Кнопка назад
         * */
        btnBackFolder.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (switchFolder.isChecked()) {
                    if (path.equals("sdcard")) {
                        finish();
                        return;
                    }
                    int positionSlash = path.lastIndexOf("/");
                    path = path.substring(0, positionSlash);
                    updateListView(path);
                } else {
                    finish();
                }
            }

        });
    }
    /**
     *Метод для обновления ListView при переходе по папкам
     * */
    private void updateListView(String path){
        int folder = R.drawable.folder;
        int song = R.drawable.song1;
        int currentSong = R.drawable.play;
        int empty = R.drawable.empty_picture;

        if(switchFolder.isChecked()){
            editText.setText(path);
            editText.setSelected(true);
        }
        values.clear();
        String[] startList = new File(path).list();
        for(String startPath : startList){

            if (startPath.startsWith(".")) {
                continue;
            }
            if(new File(path + "/" + startPath).isDirectory()){
                if(checkMusicFile(path + "/" + startPath)){
                    values.add(startPath);
                }
            }else if(startPath.endsWith(".mp3") || startPath.endsWith(".MP3") || startPath.endsWith(".m4a")){
                values.add(startPath);
            }
        }
        Collections.sort(values);

        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(values.size());
        Map<String, Object> m;
        //получем название песни
        if(getIntent().hasExtra("songTitle")){
            songTitle = getIntent().getStringExtra("songTitle");
        }
        // расстановка ImageView
        for (int i = 0; i < values.size(); i++) {
            if(songTitle.equals(getResources().getString(R.string.SongTitle))){
                m = new HashMap<String, Object>();
                m.put(ATTRIBUTE_NAME_TEXT, values.get(i));

                if(!(values.get(i).endsWith(".mp3") || values.get(i).endsWith(".MP3") || values.get(i).endsWith(".m4a"))){
                    m.put(ATTRIBUTE_NAME_IMAGE, folder);
                    m.put(ATTRIBUTE_NAME_IMAGE_PLAY, empty);
                    data.add(m);
                }
                else{
                    m.put(ATTRIBUTE_NAME_IMAGE, song);
                    m.put(ATTRIBUTE_NAME_IMAGE_PLAY, empty);
                    data.add(m);
                }
            }else{
                m = new HashMap<String, Object>();
                m.put(ATTRIBUTE_NAME_TEXT, values.get(i));
                if((values.get(i).endsWith(".mp3") || values.get(i).endsWith(".MP3") || values.get(i).endsWith(".m4a")) &&
                        values.get(i).equals(songTitle)){
                    m.put(ATTRIBUTE_NAME_IMAGE, song);
                    m.put(ATTRIBUTE_NAME_IMAGE_PLAY, currentSong);
                    data.add(m);
                }else if(values.get(i).endsWith(".mp3") || values.get(i).endsWith(".MP3") || values.get(i).endsWith(".m4a")){
                    m.put(ATTRIBUTE_NAME_IMAGE, song);
                    m.put(ATTRIBUTE_NAME_IMAGE_PLAY, empty);
                    data.add(m);
                }else{
                    m.put(ATTRIBUTE_NAME_IMAGE, folder);
                    m.put(ATTRIBUTE_NAME_IMAGE_PLAY, empty);
                    data.add(m);
                }
            }

        }

        // массив имен атрибутов, из которых будут читаться данные
        String[] from = { ATTRIBUTE_NAME_TEXT, ATTRIBUTE_NAME_IMAGE, ATTRIBUTE_NAME_IMAGE_PLAY };
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = { R.id.songTitle,  R.id.ivImg, R.id.ivPlay };

        // создаем адаптер
        SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.folders_playlist_item,
                from, to);
        setListAdapter(sAdapter);
    }
    /**
     *Метод для поиска папок в которых находятся аудиофайлы
     * */
    private boolean checkMusicFile(String path){
        File dir = new File(path);
        String[] list = dir.list();
        if(list.length == 0){
            return false;
        }
        for (String pathFile : list) {
            if(new File(path + "/" + pathFile).isDirectory()){
                if(checkMusicFile(path + "/" + pathFile)){
                    return true;
                }else{
                    continue;
                }
            }else if(pathFile.endsWith(".mp3") || pathFile.endsWith(".MP3")|| pathFile.endsWith(".m4a")){
                return true;
            }else{
                continue;
            }
        }
        return false;
    }

    /**
     *Обработчик нажатия на элементы ListView
     * */

    @Override
    protected void onListItemClick (ListView l, View v, int position, long id){

        if(switchFolder.isChecked()){
            String filename = (String)((HashMap<String, Object>) getListAdapter().getItem(position)).get(ATTRIBUTE_NAME_TEXT);
            path = path + "/" + filename;

            if (new File(path).isDirectory()) {
                updateListView(path);

            }else{

                int positionSlash = path.lastIndexOf("/");
                String folderPath = path.substring(0, positionSlash);
                String[] songs = new File(folderPath).list();
                List<String> list = new ArrayList<String>();
                String imageJpg = "";
                //находим самую большую альбомную картинку в папке
                for(String file : songs){
                    if(file.endsWith(".jpg") ){
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(folderPath + "/" + file, options);

                        if(options.outWidth >= width && options.outHeight >= height){
                            width = options.outWidth;
                            height = options.outHeight;
                            imageJpg = file;
                        }
                    }
                    if(file.endsWith(".mp3") || file.endsWith(".MP3")|| file.endsWith(".m4a")){
                        list.add(file);
                    }

                }
                Collections.sort(list);
                String[] songsArray = new String[list.size()];
                list.toArray(songsArray);

                int songIndex = 0;
                for(int pos = 0; pos < songsArray.length; pos++){
                    if(songsArray[pos].equals(filename)){
                        songIndex = pos;
                    }
                }

                Intent intent = new Intent();
                intent.putExtra("songIndex", songIndex);


                if(!imageJpg.equals("")){
                    intent.putExtra("jpg", imageJpg);
                }
                intent.putExtra("folderPath", folderPath);
                intent.putExtra("songsArray", songsArray);

                setResult(100, intent);
                finish();
            }

        }else{

            Intent intent = new Intent();
            intent.putExtra("index", position);

            intent.putExtra("SongPosition", position);

            intent.putExtra("author", listOfSongsAuthor.get(position));
            intent.putStringArrayListExtra("listAuthors", listOfSongsAuthor);
            intent.putStringArrayListExtra("list", listOfSongsPath);
            setResult(200, intent);
            finish();

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(switchFolder.isChecked()){
            outState.putString("StatePath", path);
        }
        else{
            outState.putString("StatePath", "");
        }
    }


    @Override
    public void onBackPressed() {

        finish();
    }
}