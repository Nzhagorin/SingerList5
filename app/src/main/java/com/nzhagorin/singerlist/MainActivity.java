package com.nzhagorin.singerlist;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    //отображать список исполнителей будем через RecycleView;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    //задаем строку с адресом страницы, откуда получаем данные;
    public static final String sourceJSON ="http://cache-default01d.cdn.yandex.net/download.cdn.yandex.net/mobilization-2016/artists.json";
    //объявдяем экзмпляры AsyncTask'ов
    DownloadJSON downloadJSON;
    GetContentToDevice getContentToDevice;
    //в данную строку мы положим содержимое страницы с данными;
    public String sourceContentJSON;
    //данную переменную используем для отображения ошибок;
    String error;
    //сюда запишем длину массива, полученого из контента;
    private int lenght;
    private int doInBackGroundFinished;
    //getItemSinger используется для подсчета загруженных
    public int getItemSinger=0;
    //объявлемя массив типа Singer;
    public Singer[] storage=null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //запускаем AsyncTask для получения контента с сервера;
        downloadJSON=new DownloadJSON();
        downloadJSON.execute(sourceJSON);
        try {
            sourceContentJSON=downloadJSON.get();
            //перводим полученную строку в JSON массив;
            JSONArray whole=new JSONArray(sourceContentJSON);
            //вычисляем длинну полученного массива;
            lenght=whole.length();
            //создаем массив типа Singer нужной длинны;
            storage=new Singer[lenght];

        } catch (InterruptedException e) {
            error=e.getMessage();
        } catch (ExecutionException e) {
            error=e.getMessage();
        } catch (JSONException e) {
            error=e.getMessage();
        }
        if (error!=null){
            Toast.makeText(getApplicationContext(),"Обнаружена ошибка"+error,Toast.LENGTH_LONG).show();
        }
        //запускаем Asynctask для получения данных из JSON массива, загрузки изображений
        // и записи их в наш массив storage. Для загрузки картинок можно было бы использовать Picasso, но так не интересно:);
        if(doInBackGroundFinished!=1){
        getContentToDevice=new GetContentToDevice();
        getContentToDevice.execute(sourceContentJSON);
        }
        //привязываем RecycleView,LayoutManager и adapter.
        mRecyclerView=(RecyclerView)findViewById(R.id.RecyclerView);
        //используем стандартный LinearLayoutManager;
        mLayoutManager=new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter=new MyAdapter(storage);
        if (error!=null){
            Toast.makeText(getApplicationContext(),"Обнаружена ошибка"+error,Toast.LENGTH_LONG).show();
        }
        mRecyclerView.setAdapter(mAdapter);
    }



    //=================================================================
    //наследуемся от AsyncTask'а для загрузки контента с сервера;
    class DownloadJSON extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... params) {
            //подаем на вход строку с адресом страницы с контентом;
            try{
                sourceContentJSON=getContentJSON(params[0]);
            }catch (IOException e){
                error=e.getMessage();
            }
            //и возвращаем контент страницы в строке;
            return sourceContentJSON;
        }
        private String getContentJSON(String address) throws IOException {
            BufferedReader reader=null;
            //подсоединяемся к странице с контентом, читаем входящий поток, преобразем его в строку;
            try {
                URL sourseJSON=new URL(address);
                HttpURLConnection connectionJSON=(HttpURLConnection)sourseJSON.openConnection();
                reader=new BufferedReader(new InputStreamReader(connectionJSON.getInputStream()));
                return (reader.readLine().toString());

            }
            finally {
                if (reader !=null){
                    reader.close();
                }
            }

        }
    }
    //=================================================================
    //наследуемся от AsyncTask'а для загрзки изображений, предварительно парсим JSON,
    //получаем нужные значения и записываем по готовности в элемент массива storage;
    class GetContentToDevice extends AsyncTask<String,Integer,Integer>{
        @Override
        protected Integer doInBackground(String... params) {
            //устанавливаем параметры BimapFactory, большая картинка в списке не нужна;
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inSampleSize=2;
            //далее парсим JSON массив, полученный из контента с сервера,
            //вытаскиваем имя, количество альбомов и песен, адрес маленькой картинки
            //проверяем жанры на существование и собираем их в строку;
            try {
                JSONArray whole=new JSONArray(params[0]);
                //запускаем цикл длинной, равной количеству элементов в массиве;
                //перебираем за одну итерацию цикла один жлемент массива;

                for (int i=getItemSinger;i<whole.length();i++){
                    JSONObject itemList = whole.getJSONObject(i);
                    String name = itemList.getString("name");
                    JSONArray genresArray = itemList.getJSONArray("genres");
                    String tracks = "песен "+itemList.getString("tracks");
                    String aldums = "альбомов "+itemList.getString("albums");
                    String smallPicURL = itemList.getJSONObject("cover").getString("small");
                    String genres="";
                    if (genresArray.length()>0){
                        genres = genresArray.getString(0);
                        for (int y = 1; y < genresArray.length(); y++) {
                            genres = genres + ", " + genresArray.getString(y);
                        }}
                    //получив адрес для маленькой картинки запускаем процесс загрузки;
                    URL pictureUrl=new URL(smallPicURL);
                    HttpURLConnection picConnect=(HttpURLConnection)pictureUrl.openConnection();
                    InputStream in =new BufferedInputStream(picConnect.getInputStream());
                    Bitmap bitmap=BitmapFactory.decodeStream(in,null,options);
                    //собрав нужные данные, записываем их в элемент массива нашего типа Singer;
                    storage[i]=new Singer(bitmap, name, genres, aldums, tracks);
                    //разрываем соединение
                    in.close();
                    picConnect.disconnect();
                    publishProgress(i);
                    //увеличиваем счетчик загруженных исполнителей на 1;
                    getItemSinger=getItemSinger+1;
                    if (isCancelled()){
                        return doInBackGroundFinished=0;}
                }
                doInBackGroundFinished=1;
            } catch (JSONException e) {
                error=e.getMessage();
            } catch (MalformedURLException e) {
                error=e.getMessage();
            } catch (IOException e) {
                error=e.getMessage();
            }
            return doInBackGroundFinished;
        }

    }

    //==============================================================
    //создаем наш класс Singer, для отображения в MainActivity нам нужно изображение и 4 текстовых поля;
    public class Singer{
        public Bitmap smallAvatar;
        public String name;
        public String genres;
        public String albums;
        public String tracks;
        public Singer(Bitmap smallAvatar,String name,String genres, String albums,String tracks){
            this.smallAvatar=smallAvatar;
            this.name=name;
            this.genres=genres;
            this.albums=albums;
            this.tracks=tracks;
        }
    }
//==============================================================
    //наследуемся от RecycleView.Adapter и создаем свой класс адаптера

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{
        //создаем массив типа Singer;
        public Singer[] mDataset=null;
        //переписываем ViewHolder для нашей дочерней View;
        public class ViewHolder extends RecyclerView.ViewHolder{
            public ImageView smallAvatar;
            public TextView name;
            public TextView genres;
            public TextView albums;
            public TextView tracks;

            public ViewHolder(View view) {
                super(view);
                smallAvatar=(ImageView)view.findViewById(R.id.imageView);
                name=(TextView)view.findViewById(R.id.textView);
                genres=(TextView)view.findViewById(R.id.textView2);
                albums=(TextView)view.findViewById(R.id.textView3);
                tracks=(TextView)view.findViewById(R.id.textView4);
            }
        }
        //переписываем массив значениями массива storage, который заполняется doInbackGround'е(GetContentToDevice);
        public MyAdapter(Singer[] myDataset){
            mDataset=myDataset;
        }
        //переписываем метод onCreateViewHolder;
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewHolder vh;
            //проверяем подгрузились данные о первых исполнителях которых нужно вывести на экран;
            while(getItemSinger<=mRecyclerView.getChildCount()){
                mRecyclerView.stopScroll();
                Toast.makeText(getApplicationContext(),"Загружаю данные, подождите",Toast.LENGTH_SHORT).show();
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    error=e.getMessage();
                }
            }
            //наполняем view
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mylayout, parent, false);
            vh = new ViewHolder(view);
            return vh;
        }
        //переписываем метод onBindViewHolder;
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            //вешаем сюда обработчика нажатий;
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //останавливаем AsyncTask
                    getContentToDevice.cancel(true);
                    //формируем интент для перехода во вторую активити;
                    Intent intent=new Intent(MainActivity.this,SingersInfo.class);
                    //к интенту добавляем ключ; getSingerInfo возвращает по позиции
                    // строку исполнителя из всего контента;
                    intent.putExtra("SingerInfo", getSingerInfo(sourceContentJSON, position));
                    //запускаем новыую активити;
                    startActivity(intent);
                }
            });
            //проверяем подгрузились ли исполнители, которых хотим вывести на экран;
            while(getItemSinger!=lenght&&getItemSinger<=position){
                mRecyclerView.stopScroll();
                Toast.makeText(getApplicationContext(),"Подождите, загружаю",Toast.LENGTH_SHORT).show();
                //проверяем отработал ли до конца предыдущий AsyncTask, загрузил ли он всех исполнителей
                //если нет, то запускаем новый с того момента где закончил предыдущий;
                if(doInBackGroundFinished!=1){
                    getContentToDevice=new GetContentToDevice();
                    getContentToDevice.execute(sourceContentJSON);
                }

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    error=e.getMessage();
                }
            }
            if (error!=null){
                Toast.makeText(getApplicationContext(),"Обнаружена ошибка"+error,Toast.LENGTH_LONG).show();
            }
            holder.smallAvatar.setImageBitmap(mDataset[position].smallAvatar);
            holder.name.setText(mDataset[position].name);
            holder.genres.setText(mDataset[position].genres);
            holder.albums.setText(mDataset[position].albums);
            holder.tracks.setText(mDataset[position].tracks);
        }
        private String getSingerInfo(String whole,int position){
            JSONArray array= null;
            String singerInfo=null;
            //переводим контент с исполнителями в JSON массив;
            //находим в массиве объект по позиции и перводим в строку;
            try {
                array = new JSONArray(whole);
                singerInfo=array.getJSONObject(position).toString();
            } catch (JSONException e) {
                error=e.getMessage();
            }
            //возвращаем строку, содержащую всю информацию об исполнителе;
            return singerInfo;
        }
        //переписываем getItemCount();
        @Override
        public int getItemCount() {
            return mDataset.length;
        }
    }
}


