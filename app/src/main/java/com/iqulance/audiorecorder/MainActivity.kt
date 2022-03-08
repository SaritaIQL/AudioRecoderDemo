package com.iqulance.audiorecorder

import android.Manifest.permission
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.*


class MainActivity : AppCompatActivity() {
    private var startbtn: Button? = null
    private var stopbtn: Button? = null
    private var playbtn: Button? = null
    private var stopplay: Button? = null
    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null

    private var isStart : Boolean =false
    private  var folderName : String ="AudioTrackRecorder"
    private var  dir: File ?= null
    private var countFile : Int =0
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startbtn = findViewById<View>(R.id.btnRecord) as Button
        stopbtn = findViewById<View>(R.id.btnStop) as Button
        playbtn = findViewById<View>(R.id.btnPlay) as Button
        stopplay = findViewById<View>(R.id.btnStopPlay) as Button
        stopbtn!!.isEnabled = false
        playbtn!!.isEnabled = true
        stopplay!!.isEnabled = false

        createAudioFile()
//        mFileName = Environment.getExternalStorageDirectory().absolutePath
//        mParentFileName = Environment.getExternalStorageDirectory().absolutePath
//        mFileName += "/AudioRecording.wav"
//        mParentFileName += "/AudioParentRecording.wav"
        startbtn!!.setOnClickListener {
            if (CheckPermissions()) {
                stopbtn!!.isEnabled = true
                startbtn!!.isEnabled = false
                playbtn!!.isEnabled = true
                stopplay!!.isEnabled = false
                mRecorder = MediaRecorder()
                mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                val getFileName = fileCountUpdate()
                mRecorder!!.setOutputFile( getFileName)
//                mRecorder!!.setOutputFile(mParentFileName)

                isStart=true
                try {
                    mRecorder!!.prepare()
                } catch (e: IOException) {
                    Log.e(LOG_TAG, "prepare() failed")
                }
                mRecorder!!.start()
                Toast.makeText(applicationContext, "Recording Started", Toast.LENGTH_LONG).show()


//                if(isStart){
//                    mRecorder!!.resume()
//                    Toast.makeText(applicationContext, "Re-Recording Started", Toast.LENGTH_LONG).show()
//
//                }
//                else{
//                    isStart=true
//                    try {
//                        mRecorder!!.prepare()
//                    } catch (e: IOException) {
//                        Log.e(LOG_TAG, "prepare() failed")
//                    }
//                    mRecorder!!.start()
//                    Toast.makeText(applicationContext, "Recording Started", Toast.LENGTH_LONG).show()
//
//                }
            } else {
                RequestPermissions()
            }
        }
        stopbtn!!.setOnClickListener {
            stopbtn!!.isEnabled = false
            startbtn!!.isEnabled = true
            playbtn!!.isEnabled = true
            stopplay!!.isEnabled = true
            mRecorder!!.stop()
            mRecorder!!.release()
        //    mRecorder!!.pause()
//            mRecorder = null
            Toast.makeText(applicationContext, "Recording Stopped", Toast.LENGTH_LONG).show()
        }
        playbtn!!.setOnClickListener {
            stopbtn!!.isEnabled = false
            startbtn!!.isEnabled = true
            playbtn!!.isEnabled = false
            stopplay!!.isEnabled = true
          var mergeFileName=  appendAudioFileData()
            mPlayer = MediaPlayer()
            try {
//                mPlayer!!.setDataSource(mFileName)
                mPlayer!!.setDataSource(mergeFileName)
                mPlayer!!.prepare()
                mPlayer!!.start()
                Toast.makeText(applicationContext, "Recording Started Playing", Toast.LENGTH_LONG)
                    .show()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }
        stopplay!!.setOnClickListener {
            mPlayer!!.release()
            mPlayer = null
            stopbtn!!.isEnabled = false
            startbtn!!.isEnabled = true
            playbtn!!.isEnabled = true
            stopplay!!.isEnabled = false
            Toast.makeText(applicationContext, "Playing Audio Stopped", Toast.LENGTH_SHORT).show()
        }
    }

    private fun appendAudioFileData(): String {

        val  directoryPath=dir!!.absolutePath

        val directory: File = File(directoryPath)
        val files = directory.listFiles()
        val arrayAudioFile : ArrayList<FileInputStream> = arrayListOf()
        Log.d("Files", "Size: " + files.size.toString())


        var sistream : SequenceInputStream ?=  null
        for (i in 0 until files.size) {
            Log.d("Files", "FileName:" + files[i].name)
            arrayAudioFile.add(FileInputStream(files[i].path))

        }
        var fisToFinal: FileInputStream? = null
        var fos: FileOutputStream? = null
        val  mergedFile=dir!!.absolutePath + "/MergerAudio"+countFile+".mp3"

        fos =  FileOutputStream(mergedFile);
        fisToFinal =  FileInputStream(mergedFile);

        try{
            for (mp3File in files) {
                if (!mp3File.exists()) continue
                val fisSong = FileInputStream(mp3File)
                val sis = SequenceInputStream(fisToFinal, fisSong)
                val buf = ByteArray(1024)
                try {
                    var readNum: Int
                    while (fisSong!!.read(buf).also { readNum = it } != -1) {
                        fos.write(buf, 0, readNum)
                    }
                } finally {
                    if (fisSong != null) {
                        fisSong.close()
                    }
                    if (sis != null) {
                        sis.close()
                    }
                }
            }

        }
        catch (e:IOException) {
            e.printStackTrace();
        }
        finally{
            try {
                if(fos!=null){
                    fos.flush();
                    fos.close();
                }
                if(fisToFinal!=null){
                    fisToFinal.close();
                }
            } catch (e:IOException) {
                e.printStackTrace();
            }
        }

//        sistream = SequenceInputStream(fistream1, fistream2)


        return mergedFile
    }

    private fun createAudioFile() {

         dir = File(
            Environment.getExternalStorageDirectory().toString() +
                   "/"+ folderName
        )
        var success = true
        if (!dir!!.exists()) {
            success = dir!!.mkdirs()
        }
        if (success) {
            // Do something on success
        } else {
            // Do something else on failure
        }


    }

     fun fileCountUpdate(): String {
        //        mFileName = Environment.getExternalStorageDirectory().absolutePath
//        mParentFileName = Environment.getExternalStorageDirectory().absolutePath
//        mFileName += "/AudioRecording.wav"
//        mParentFileName += "/AudioParentRecording.wav"
        countFile = countFile+1
       val  fileNameUpdate=dir!!.absolutePath + "/AudioRecording_"+countFile+".mp3"

         return fileNameUpdate
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_AUDIO_PERMISSION_CODE -> if (grantResults.size > 0) {
                val permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (permissionToRecord && permissionToStore) {
                    Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_LONG)
                        .show()
                } else {
                    Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    fun CheckPermissions(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(applicationContext, permission.WRITE_EXTERNAL_STORAGE)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun RequestPermissions() {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(permission.RECORD_AUDIO, permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_AUDIO_PERMISSION_CODE
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mRecorder=null

        val dir = File(Environment.getExternalStorageDirectory().toString() + folderName)

        if(dir.isDirectory){
            val children = dir.list()
            for (i in children.indices) {
                File(dir, children[i]).delete()
            }
        }

    }

    companion object {
        private const val LOG_TAG = "AudioRecording"
        private var mFileName: String? = null
        private var mParentFileName : String?=null
        private  var audioFile : File ?= null
        private  var audioParentFile : File  ?= null
        const val REQUEST_AUDIO_PERMISSION_CODE = 1
    }
}