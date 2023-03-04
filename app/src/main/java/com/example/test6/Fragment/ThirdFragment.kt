package com.example.test6.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.test6.R
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import coil.load
import com.example.test6.FoodApiService
import com.example.test6.food.Posts
import com.google.android.gms.maps.SupportMapFragment
import com.google.gson.Gson
import food_ID
import food_KEY
import kotlinx.android.synthetic.main.fragment_third.*
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.QueryMap
import java.io.IOException


class ThirdFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_third, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var url:String?=null


        var aaa:String=""

        val data: MutableMap<String, String> = HashMap()
        data["app_id"] = food_ID
        data["app_key"] = food_KEY
        data["nutrition-type"] ="cooking"
        data["ingr"]

        btn_query.setOnClickListener {
            data["ingr"] =ed.text.toString()
            Log.d("Huang",data.toString())


            val apiService = AppClientManager.client.create(FoodApiService::class.java)
            apiService.index(data).enqueue(object : Callback<Posts> {
                override fun onResponse(call: Call<Posts>, response: Response<Posts>) {
                    val list = response.body()
//                    list?.let {
//                        for (p2 in it) {
//                            sb.append(p.body)
//                            sb.append("\n")
//                            sb.append("---------------------\n")
//                        }
//                    }
                    tv.text =list!!.hints.get(0).food.toString()
                    Log.d("Huang", " get teams  success "+list.hints.get(0).food)

                    url=list.hints.get(0).food.image
                    image.load(url)



                }

                override fun onFailure(call: retrofit2.Call<Posts>, t: Throwable) {

                }
            })


        }



    }


}

class AppClientManager private constructor() {
    val baseURL="https://api.edamam.com"


    private val retrofit: Retrofit
    private val okHttpClient = OkHttpClient()

    init {
        retrofit = Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    companion object {
        private val manager = AppClientManager()
        val client: Retrofit
            get() = manager.retrofit
    }
}