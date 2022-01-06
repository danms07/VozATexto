package com.dtse.demoandroid.vozatexto

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.huawei.hms.mlsdk.asr.MLAsrConstants
import com.huawei.hms.mlsdk.asr.MLAsrListener
import com.huawei.hms.mlsdk.asr.MLAsrRecognizer
import com.huawei.hms.mlsdk.common.MLApplication
import java.lang.StringBuilder
import java.util.jar.Manifest

class MainActivity : AppCompatActivity(), MLAsrListener {
    lateinit var textOutput:TextView
    lateinit var speakerBtn:Button
    val permissionLauncer=registerForActivityResult(ActivityResultContracts.RequestPermission()){granted->onMicPermission(granted)}
    var mSpeechRecognizer:MLAsrRecognizer?=null

    var dialog:AlertDialog?=null

    private fun onMicPermission(granted: Boolean) {
        if (granted) startRecording()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MLApplication.getInstance().apiKey = "CwEAAAAADyLM+ajvMt8gfeUCJMhQU7Be0deQyCp4ieeMiVOO4BG9VyDAx58wrhfNEPw2NLAfMa+5qCV8es9ASIvbdEJn6Xhquik="
        setContentView(R.layout.activity_main)
        textOutput=findViewById(R.id.output)
        speakerBtn=findViewById(R.id.speakBtn)
        speakerBtn.setOnClickListener{prepareVoiceCapture()}
    }

    private fun prepareVoiceCapture() {
        val micPermission=checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
        if(micPermission==PackageManager.PERMISSION_GRANTED){
            startRecording()
        } else permissionLauncer.launch(android.Manifest.permission.RECORD_AUDIO)
    }

    private fun startRecording() {
        //Desactivamos el botón
        speakerBtn.isEnabled=false
        //Preparamos ASR
        // context: app context.
        if(mSpeechRecognizer==null) mSpeechRecognizer = MLAsrRecognizer.createAsrRecognizer(this).apply {
            setAsrListener(this@MainActivity)
        }

        // Create an Intent to set parameters.
        val mSpeechRecognizerIntent = Intent(MLAsrConstants.ACTION_HMS_ASR_SPEECH).apply {
            putExtra(MLAsrConstants.LANGUAGE, "es-ES") // Set to return the recognition result along with the speech. If you ignore the setting, this mode is used by default. Options are as follows:
            // MLAsrConstants.FEATURE_WORDFLUX: Recognizes and returns texts through onRecognizingResults.
            // MLAsrConstants.FEATURE_ALLINONE: After the recognition is complete, texts are returned through onResults.
            putExtra(MLAsrConstants.FEATURE, MLAsrConstants.FEATURE_WORDFLUX) // Set the application scenario. MLAsrConstants.SCENES_SHOPPING indicates shopping, which is supported only for Chinese. Under this scenario, recognition for the name of Huawei products has been optimized.
            //putExtra(MLAsrConstants.SCENES, MLAsrConstants.SCENES_SHOPPING)
        }
        // Start speech recognition.
        mSpeechRecognizer?.startRecognizing(mSpeechRecognizerIntent)
    }

    private fun initDialog(){
        dialog=AlertDialog.Builder(this)
            .setTitle("ASR")
            .setCancelable(false)
            .create()
    }

    override fun onStartListening() {
        if(dialog==null) initDialog()
        dialog?.apply {
            setMessage("Habla ahora")
            show()
        }
    }

    override fun onStartingOfSpeech() {
        // The user starts to speak, that is, the speech recognizer detects that the user starts to speak.
        dialog?.setTitle("Reconociendo")
    }

    override fun onVoiceDataReceived(data: ByteArray, energy: Float, bundle: Bundle) {
        // Return the original PCM stream and audio power to the user. This API is not running in the main thread, and the return result is processed in the sub-thread.
    }

    override fun onRecognizingResults(partialResults: Bundle) {
        // Receive the recognized text from MLAsrRecognizer. This API is not running in the main thread, and the return result is processed in the sub-thread.

        dialog?.setMessage(parseResults(partialResults))
    }

    private fun parseResults(partialResults: Bundle):String{
        val sb=StringBuilder()
        for(key in partialResults.keySet()){
            sb.append(partialResults.getString(key))
        }
        return sb.toString()
    }

    override fun onResults(results: Bundle) {
        // Text data of ASR. This API is not running in the main thread, and the return result is processed in the sub-thread.
        dialog?.dismiss()
        textOutput.text=parseResults(results)
        speakerBtn.isEnabled=true
    }

    override fun onError(error: Int, errorMessage: String) {
        // Called when an error occurs in recognition. This API is not running in the main thread, and the return result is processed in the sub-thread.
        Log.e("ASR","Error Code:$error \tErrorMEssage:$errorMessage")
    }

    override fun onState(state: Int, params: Bundle) {
        // Notify the app status change. This API is not running in the main thread, and the return result is processed in the sub-thread.
    }
}