package com.yiann0s.retronebefits

import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yiann0s.retronebefits.model.Car
import com.yiann0s.retronebefits.utils.Service
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.ExecutionException

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var BASE_URL = "https://navneet7k.github.io/"
    private var TAG = "TEST.MainActivity"
    private var startTime : Long? = null
    private var endTime : Long? = null
    private var progressBar : ProgressBar? = null
    private var retrofitResultText : TextView? = null
    private var asyncTaskResultText : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        (findViewById<Button>(R.id.start_button)).setOnClickListener(this)

        progressBar = findViewById(R.id.progress_bar)

        retrofitResultText = findViewById(R.id.retrofit_result)
        asyncTaskResultText = findViewById(R.id.asynctask_result)
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.start_button){
            retrofit(this)
            asyncTask()
            retrofitResultText?.visibility =View.VISIBLE
            asyncTaskResultText?.visibility = View.VISIBLE
        } else {
            Toast.makeText(this,getString(R.string.error),Toast.LENGTH_SHORT).show()
        }
    }

    fun asyncTask(){
        val task = MyAsyncTask(this)

        var rr = ""
        try {
            rr = task.execute().get()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        try {
            JSONObject(rr)
        } catch (e : JSONException) {
            e.printStackTrace()
            Log.i(TAG, "asyncTask: exception " + e.message)
        }

    }

    companion object{
        class MyAsyncTask internal constructor( context: MainActivity? ) : AsyncTask<String, Void, String>() {

            private var response : String? = null
            //weak reference in the class scope
            private val activityWeakReference : WeakReference<MainActivity> = WeakReference(context)

            @Override
            override fun onPreExecute() {
                super.onPreExecute()

                //strong reference in method scope
                val context = activityWeakReference.get()
                if ((context == null) || (context.isFinishing)) {
                    return
                }

                context.startTime = SystemClock.elapsedRealtime()

                context.progressBar?.visibility = View.VISIBLE
            }

            override fun doInBackground(vararg p0 : String?):String {
                //strong reference in method scope
                val context = activityWeakReference?.get()
                if ((context == null) || (context.isFinishing)) {
                    return ""
                }

                var urlConnection : HttpURLConnection? = null

                try {
                    val url = URL("https://navneet7k.github.io/sample_array.json")
                    urlConnection = url.openConnection() as HttpURLConnection
                    val code: Int = urlConnection.responseCode
                    if (code == 200) {
                        val inputStreamReader = BufferedInputStream(urlConnection.inputStream)
                        val bufferedReader = BufferedReader(InputStreamReader(inputStreamReader))
                        var line: String?

                        if (App.HAS_DELAY){
                            context.addDelay(4000)
                        }

                        while (true){
                            line = bufferedReader.readLine()
                            if (line == null){
                                break
                            }
                            response += line
                        }
                        inputStreamReader.close()
                    }
                    return response!!
                } catch ( e : MalformedURLException) {
                    e.printStackTrace()
                } catch (e : IOException) {
                    e.printStackTrace()
                } finally {
                    urlConnection?.disconnect()
                }
                return response!!
            }

            override fun onPostExecute(s : String) {
                super.onPostExecute(s)

                //strong reference in method scope
                val context = activityWeakReference?.get()
                if ((context == null) || (context.isFinishing)) {
                    return
                }

                context.endTime = SystemClock.elapsedRealtime()
                val totalTimeLong = context.endTime!!- context.startTime!!
                val totalTime = "$totalTimeLong"
                context.asyncTaskResultText?.text = context.resources?.getString(R.string.time,  "AsyncTask", totalTime)
                context.progressBar?.visibility = View.INVISIBLE
            }
        }
    }

    fun addDelay(delay: Long){
        try {
            Thread.sleep(delay)
        } catch ( e : InterruptedException) {
            e.printStackTrace()
            Log.e(TAG, "addDelay: exception " + e.message)
        }
    }

    fun retrofit( context : MainActivity?){
        //weak reference in the class scope
        val activityWeakReference = WeakReference<MainActivity>(context)

        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        //is this good ???
        activityWeakReference.get()?.startTime= SystemClock.elapsedRealtime()
        val service = retrofit.create(Service::class.java)
        val carList : Call<MutableList<Car>> = service.getJson()
        carList.enqueue( object : Callback<MutableList<Car>> {
            override fun onFailure(call: Call<MutableList<Car>>, t: Throwable) {
                val context: MainActivity? = activityWeakReference.get()
                if ((context == null) || (context.isFinishing)) {
                    return
                }

                Toast.makeText(context,"Exception: " + t.message,Toast.LENGTH_SHORT).show()
                if ( t is IOException) {
                    Log.d(TAG, "this is an actual network failure :( inform the user and possibly retry")
                } else {
                    Log.d(TAG, "conversion issue! big problems :(")
                }
            }

            override fun onResponse(
                    call: Call<MutableList<Car>>,
                    response: Response<MutableList<Car>>
            ) {
                val context: MainActivity? = activityWeakReference.get()
                if ((context == null) || (context.isFinishing)) {
                    return
                }

                if (response.isSuccessful && response.body() != null ){

                    context.endTime = SystemClock.elapsedRealtime()
                    val totalTimeLong = context.endTime!!- context.startTime!!
                    val totalTime = "$totalTimeLong"
                    context.retrofitResultText?.text = context.resources.getString(R.string.time, "Retrofit",totalTime)
                } else {
                    Log.d(TAG, "onResponse: reponse body isEmpty :" + (TextUtils.isEmpty(response.toString())))
                    Toast.makeText(context,"Retrofiit:"+context.resources.getString(R.string.error),Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

}