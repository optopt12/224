package com.example.test6.Fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test6.OpenAI.Msg
import com.example.test6.OpenAI.OpenAI
import com.example.test6.OpenAI.OpenaiService
import com.example.test6.R
import com.example.test6.databinding.FragmentFirstBinding
import kotlinx.android.synthetic.main.fragment_first.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
//import OPENAI_KEY
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.widget.TextView
import android.widget.Toast
import com.example.test6.BuildConfig
import java.util.*
import kotlin.collections.ArrayList


private var _binding : FragmentFirstBinding? = null
private val binding get() = _binding!!
private lateinit var msgAdapter: MsgAdapter
private val SPEECH_REQUEST_CODE = 0
private var answer:String ="發送訊息以獲得回覆"
private var msgList: MutableList<Msg> = ArrayList()//建立可改變的list
private var tts: TextToSpeech? = null

class FirstFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null;
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMsg()   //初始化
        initRv() //RecyclerView初始化
        displaySpeechRecognizer()//語音辨識
        setListener()//發送訊息與ai對話
        textToSpeech() //文字轉語音

    }


    private fun setListener() {
        binding.run {
            sendButton.setOnClickListener {
                val content: String = editText.text.toString()   //获取输入框的文本
                if (content.isNotEmpty()) {
                    msgList.add(Msg(content, Msg.RIGHT))    //将输入的消息及其类型添加进消息数据列表中
                    aichat()
                    editText.hideKeyboard() //收起鍵盤
                    editText.setText("")    //清空输入框文本
                }
            }
        }
    }

    private fun initRv() {
        binding.recyclerView.apply {
            msgAdapter = MsgAdapter(msgList)   //建立适配器实例
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )  //布局为线性垂直
            adapter = msgAdapter
        }
    }

    private fun initMsg() {
        msgList.add(Msg(answer, Msg.LEFT))
    }

    private fun aichat() {
        val API_KEY = "Bearer ${BuildConfig.OPENAI_KEY}" //獲得api-key
        val openAI = OpenAI(API_KEY)
        var prompt = editText.text.toString()//要和ai說的話
        val message = binding.editText.text.toString()

        progressBar.progress=0
        ll_progress.visibility=View.VISIBLE //讀取條跳出


        CoroutineScope(Dispatchers.IO).launch {
            prompt += "\n\nHuman: $message \nAI:"
            try {
                val response = openAI.createCompletion(
                    model = "text-davinci-003",
                    prompt = prompt,
                    temperature = 0.3,
                    max_tokens = 200,
                    top_p = 1,
                    frequency_penalty = 0.0,
                    presence_penalty = 0.0,
                    stop = listOf(" Human:", " AI:")
                )//ai相關參數
                if (response.isSuccessful) {
                    answer = response.body()?.choices?.first()?.text.toString()//ai回答的話
                    CoroutineScope(Dispatchers.Main).launch {

                        ll_progress.visibility=View.GONE //讀取完成，讀取條消失


                        msgList.add(Msg(answer, Msg.LEFT))//讓ai說的話顯示出來
                        msgAdapter.notifyDataSetChanged()   //为RecyclerView添加末尾子项
                    }
                } else {
                    Log.d("RESPONSE", "Error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.d("RESPONSE", "Error: $e")
            }
        }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }//打完字自動把鍵盤收起來

    private fun displaySpeechRecognizer() {
        voice_button.setOnClickListener{
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            }
            // This starts the activity and populates the intent with the speech text.
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val spokenText: String = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { results ->
                results[0]
            }.toString()
            binding.editText.setText(spokenText)
            // Do something with spokenText.
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun textToSpeech() {

        tts = TextToSpeech(context, OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val ttsLang = tts!!.setLanguage(Locale.TRADITIONAL_CHINESE)
                if (ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "The Language is not supported!")
                } else {
                    Log.i("TTS", "Language Supported.")
                }
                Log.i("TTS", "Initialization success.")
            } else {
                Toast.makeText(context, "TTS Initialization failed!", Toast.LENGTH_SHORT).show()
            }
        })
        speech_button!!.setOnClickListener {
            val data = answer
            Log.i("TTS", "button clicked: $data")
            tts!!.setPitch(1F)    // 語調(1 為正常；0.5 為低一倍；2 為高一倍)
            tts!!.setSpeechRate(1F)    // 速度(1 為正常；0.5 為慢一倍；2 為快一倍)
            val speechStatus = tts!!.speak(data, TextToSpeech.QUEUE_FLUSH, null)
            if (speechStatus == TextToSpeech.ERROR) {
                Log.e("TTS", "Error in converting Text to Speech!")
            }
        }
        tts!!.shutdown()   //釋放資源?

          fun onDestroy() {
            super.onDestroy()
            if (tts != null) {
                tts!!.stop()
                tts!!.shutdown()
            }
        }

    }





}


