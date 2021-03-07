package com.yiann0s.retronebefits;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yiann0s.retronebefits.model.Car;
import com.yiann0s.retronebefits.utils.Service;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Long startTime, endTime;
    private final String BASE_URL = "https://navneet7k.github.io/";

    private final String TAG = "TEST.MainActivity";

    private TextView retrofitResultText, asyncTaskResultText;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.start_button).setOnClickListener(this);

        progressBar = findViewById(R.id.progress_bar);

        retrofitResultText = findViewById(R.id.retrofit_result);
        asyncTaskResultText = findViewById(R.id.asynctask_result);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_button:
                retrofit(this);
                asyncTask();
                retrofitResultText.setVisibility(View.VISIBLE);
                asyncTaskResultText.setVisibility(View.VISIBLE);
                break;
            default:
                Toast.makeText(this,getString(R.string.error),Toast.LENGTH_SHORT).show();
        }
    }

    public void asyncTask(){
        MyAsyncTask task = new MyAsyncTask(this);

        String rr = "";
        try {
            rr = task.execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            JSONObject obj = new JSONObject(rr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private static class MyAsyncTask extends AsyncTask<String, Void, String> {

        //weak reference in the class scope
        private WeakReference<MainActivity> activityWeakReference;

        public MyAsyncTask(MainActivity activity) {
            activityWeakReference = new WeakReference<MainActivity>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //strong reference in method scope
            MainActivity activity = activityWeakReference.get();
            if ((activity == null) || (activity.isFinishing())) {
                return;
            }

            activity.startTime = SystemClock.elapsedRealtime();

            activity.progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            //strong reference in method scope
            MainActivity activity = activityWeakReference.get();
            if ((activity == null) || (activity.isFinishing())) {
                return "";
            }

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

                        activity.addDelay(4000);

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

            //strong reference in method scope
            MainActivity activity = activityWeakReference.get();
            if ((activity == null) || (activity.isFinishing())) {
                return;
            }

            activity.endTime = SystemClock.elapsedRealtime();
            activity.asyncTaskResultText.setText(activity.getResources().getString(R.string.time).replace("{x}",String.valueOf(activity.endTime-activity.startTime)).replace("{y}","AsyncTask"));
            activity.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void addDelay(long delay){
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, "addDelay: exception " + e.getMessage());
        }
    }

    public void retrofit(MainActivity activity){
        //weak reference in the class scope
        WeakReference<MainActivity> activityWeakReference = new WeakReference<MainActivity>(activity);

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

        //is this good ???
        activityWeakReference.get().startTime= SystemClock.elapsedRealtime();
        Service service = retrofit.create(Service.class);
        Call<List<Car>> carList = service.getJson();
        carList.enqueue(new Callback<List<Car>>() {
            @Override
            public void onResponse(Call<List<Car>> call, Response<List<Car>> response) {
                //strong reference in method scope
                MainActivity activity = activityWeakReference.get();
                if ((activity == null) || (activity.isFinishing())) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null ){

                    activity.endTime = SystemClock.elapsedRealtime();
                    activity.retrofitResultText.setText(activity.getResources().getString(R.string.time).replace("{x}",String.valueOf(activity.endTime-activity.startTime)).replace("{y}","Retrofit"));
                } else {
                    Log.d(TAG, "onResponse: reponse body isEmpty :" + (TextUtils.isEmpty(response.toString())));
                    Toast.makeText(activity,"Retrofiit:"+activity.getResources().getString(R.string.error),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Car>> call, Throwable t) {
                //strong reference in method scope
                MainActivity activity = activityWeakReference.get();
                if ((activity == null) || (activity.isFinishing())) {
                    return;
                }

                Toast.makeText(activity,"Exception: " + t.getMessage(),Toast.LENGTH_SHORT).show();
                if (t instanceof IOException) {
                    Log.d(TAG, "this is an actual network failure :( inform the user and possibly retry");
                } else {
                    Log.d(TAG, "conversion issue! big problems :(");
                }
            }
        });
    }

}
