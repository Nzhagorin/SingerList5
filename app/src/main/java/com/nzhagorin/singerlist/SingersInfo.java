package com.nzhagorin.singerlist;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by 1 on 24.04.2016.
 */
public class SingersInfo extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.singers_info);
        //получаем информацию об исполнителе из MainActivity;
        String singerInfo=getIntent().getExtras().getString("SingerInfo");
        //объявляем переменные для второй активити;
        String name="";
        String genres="";
        String albums="";
        String tracks="";
        String descriprion="";
        String avatarURL="";
        Bitmap avatar=null;
        GetAvatar getAvatar;
        try {
            //переводим полученную строку в JSON-объект и далее вытаскиваем нужные значения;
            JSONObject item=new JSONObject(singerInfo);
            name=item.getString("name");
            albums="альбомов "+item.getString("albums");
            tracks="песен "+item.getString("tracks");
            descriprion="Биография\n"+item.getString("description");
            avatarURL=item.getJSONObject("cover").getString("big");
            JSONArray genresArray=item.getJSONArray("genres");
            //проверяем, указаны ли жанры у исполнителя, и если есть, то собираем в строку;
            if (genresArray.length()>0){
                genres = genresArray.getString(0);
                for (int y = 1; y < genresArray.length(); y++) {
                    genres = genres + ", " + genresArray.getString(y);
                }}
            //запускаем загрузку изображения;
            getAvatar=new GetAvatar();
            avatar=getAvatar.execute(avatarURL).get();


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        //указываем название активити;
        this.setTitle(name);
        //вставляем значения в соответствующие View;
        TextView description=(TextView)findViewById(R.id.description);
        TextView genres2=(TextView)findViewById(R.id.genres2);
        TextView albums2=(TextView)findViewById(R.id.albums2);
        TextView tracks2=(TextView)findViewById(R.id.tracks2);
        ImageView avatar2=(ImageView)findViewById(R.id.imageView2);
        avatar2.setImageBitmap(avatar);
        genres2.setText(genres);
        albums2.setText(albums);
        tracks2.setText(tracks);
        description.setText(descriprion);


    }
    class GetAvatar extends AsyncTask<String,Integer,Bitmap>{
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmapAvatar=null;
            try {
                URL avatarUrl=new URL(params[0]);
                HttpURLConnection avatarConnect=(HttpURLConnection)avatarUrl.openConnection();
                InputStream inputStream=new BufferedInputStream(avatarConnect.getInputStream());
                //измением парметры Bitmapfactory для получения параметров изображения;
                BitmapFactory.Options options=new BitmapFactory.Options();
                options.inJustDecodeBounds=true;
                //получаем параметры изображения
                Bitmap bitmap= BitmapFactory.decodeStream(inputStream,null,options);
                //получаем параметры экрана
                DisplayMetrics metrics=new DisplayMetrics();
                Display display=getWindowManager().getDefaultDisplay();
                display.getMetrics(metrics);
                int wight=metrics.widthPixels;
                int height=metrics.heightPixels;
                float w=options.outWidth;
                float h=options.outHeight;
                //проверяем ориентацию экрана
                if (getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE){
                    //вычисляем коэффициент масштабирования для ландшафтной ориентации и получаем изображение;
                    int scale=Math.round(h/height);
                    BitmapFactory.Options optionsScale=new BitmapFactory.Options();
                    optionsScale.inSampleSize=scale;
                    HttpURLConnection avatarConnectScale=(HttpURLConnection)avatarUrl.openConnection();
                    inputStream=new BufferedInputStream(avatarConnectScale.getInputStream());
                    bitmap=BitmapFactory.decodeStream(inputStream,null,optionsScale);
                    bitmapAvatar=bitmap;
                    avatarConnectScale.disconnect();
                }
                else {
                    int scale = Math.round(w / wight);
                    BitmapFactory.Options optionsScale = new BitmapFactory.Options();
                    optionsScale.inSampleSize = scale;
                    HttpURLConnection avatarConnectScale = (HttpURLConnection) avatarUrl.openConnection();
                    inputStream = new BufferedInputStream(avatarConnectScale.getInputStream());
                    bitmap = BitmapFactory.decodeStream(inputStream, null, optionsScale);
                    avatarConnectScale.disconnect();
                    bitmapAvatar = bitmap;
                    avatarConnectScale.disconnect();
                }
                inputStream.close();
                avatarConnect.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bitmapAvatar;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
        }
    }

}