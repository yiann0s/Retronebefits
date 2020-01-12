package com.yiann0s.retronebefits;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.yiann0s.retronebefits.model.Car;
import com.yiann0s.retronebefits.model.Dog;
import com.yiann0s.retronebefits.utils.Service;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Long startTime, endTime;
    private final String BASE_URL = "https://navneet7k.github.io/";

    private final String TAG = "TEST.MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.async_button).setOnClickListener(this);
        findViewById(R.id.retro_button).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.async_button:
                asyncTask();
                break;
            case R.id.retro_button:
                retrofit();
                break;
            default:
                Toast.makeText(this,getString(R.string.error),Toast.LENGTH_SHORT).show();
        }
    }

    public void asyncTask(){
        String rr = "";
        try {
            rr = new MyAsyncTasks().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        JSONObject obj = null;
        try {
            obj = new JSONObject(rr);
            String error = obj.getString("status");
            String message = obj.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public class MyAsyncTasks extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startTime = SystemClock.elapsedRealtime();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            String result = "";
            try {
                URL url = new URL("https://navneet7k.github.io/sample_array.json");
                urlConnection = (HttpURLConnection) url.openConnection();
                int code = urlConnection.getResponseCode();
                if (code == 200) {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    if (in != null) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                        String line = "";

                        while ((line = bufferedReader.readLine()) != null)
                            result += line;
                    }
                    in.close();
                }
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            endTime = SystemClock.elapsedRealtime();
            showTime(startTime,endTime);
        }
    }

    public void retrofit(){

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();


        Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(
                                GsonConverterFactory.create()
                        );

        Retrofit retrofit =
                builder
                        .client(
                                httpClient.build()
                        )
                        .build();

        startTime= SystemClock.elapsedRealtime();
        Service service = retrofit.create(Service.class);
        Call<List<Car>> carList = service.getJson();
        carList.enqueue(new Callback<List<Car>>() {
            @Override
            public void onResponse(Call<List<Car>> call, Response<List<Car>> response) {
                if (response.isSuccessful() && response.body() != null ){
                    endTime = SystemClock.elapsedRealtime();
                    showTime(startTime,endTime);
                } else {
                    Log.d(TAG, "onResponse: reponse body isEmpty :" + (TextUtils.isEmpty(response.toString())));
                    Toast.makeText(MainActivity.this,"Retrofiit:"+getString(R.string.error),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Car>> call, Throwable t) {
                Toast.makeText(MainActivity.this,"Exception: " + t.getMessage(),Toast.LENGTH_SHORT).show();
                if (t instanceof IOException) {
                    Log.d(TAG, "this is an actual network failure :( inform the user and possibly retry");
                } else {
                    Log.d(TAG, "conversion issue! big problems :(");
                }
            }
        });
    }

    private void showTime(long t1, long t2){
        long diffTime = t2 - t1;
        Toast.makeText(MainActivity.this,getString(R.string.total_time).replace("{x}",String.valueOf(diffTime)),Toast.LENGTH_SHORT).show();
    }
}