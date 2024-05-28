package com.example.karaokekotlin.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.navArgs
import com.example.karaokekotlin.R
import com.example.karaokekotlin.data.database.entities.FavoriteSongEntity
import com.example.karaokekotlin.databinding.ActivityDetailBinding
import com.example.karaokekotlin.recorder.AndroidAudioRecorder
import com.example.karaokekotlin.service.AudioCaptureService
import com.example.karaokekotlin.stream.StreamObject
import com.example.karaokekotlin.util.Constants.Companion.ACTION_START
import com.example.karaokekotlin.util.Constants.Companion.ACTION_STOP
import com.example.karaokekotlin.util.Constants.Companion.DES_PATH_KEY
import com.example.karaokekotlin.util.Constants.Companion.EXTRA_RESULT_DATA
import com.example.karaokekotlin.util.Constants.Companion.MEDIA_PROJECTION_REQUEST_CODE
import com.example.karaokekotlin.viewmodel.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class DetailActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {
    private var _binding: ActivityDetailBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<DetailActivityArgs>()
    private val mainViewModel: MainViewModel by viewModels()
    private var songSaved = false
    private var savedSongId = 0
    private lateinit var menuItem: MenuItem
    private var actionBar: ActionBar? = null

    private var hasAllPermissions = false
    private lateinit var streamObject: StreamObject
    var isFullScreen = false
    private var isHeadphoneConnected = false
    private val audioRecorder: AndroidAudioRecorder by lazy {
        AndroidAudioRecorder(
            applicationContext
        )
    }
    private var audioFile: File? = null
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var isStreaming = false
    private var isCapturing = false
    private var isBack = false

    //view
    private lateinit var scrollSettingView: ScrollView
    private lateinit var tvVideoTitle: TextView
    private lateinit var youtubePlayer: YouTubePlayer
    private lateinit var youTubePlayerView: YouTubePlayerView
    private lateinit var btSing: Button
    private lateinit var btSingnRecord: Button
    private lateinit var swAutoTune: SwitchCompat
    private lateinit var swEffect: SwitchCompat
    private lateinit var viewMicVolume: View
    private lateinit var viewEffect: View
    private lateinit var sbMicVolume: SeekBar
    private lateinit var sbEcho: SeekBar
    private lateinit var sbReverb: SeekBar

    private val TAG = "TAGggg"

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "detail onCreate")
        super.onCreate(savedInstanceState)
        _binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Checking permissions.
        hasAllPermissions = checkPermission()
        //if (!hasAllPermissions) return
        //turn on/off mic
//        audioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        var isMicOn = !(audioManager.isMicrophoneMute)
//        binding.btMic.setOnClickListener {
//            audioManager.isMicrophoneMute = isMicOn
//            isMicOn = !isMicOn
//        }

        // Got all permissions
        if (hasAllPermissions) {
          firstInit()
        }
    }

    private fun firstInit()
    {
        actionBar = supportActionBar
        setUpView()
        setYoutubePlayer()
        initStreamObject()
        setSingButtonClick()
        setSingnRecordButtonClick()
        setEffectSwitchClick()
        setAutotuneSwitchClick()
        setSeekbarListener()
        binding.viewStart.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        if (isFullScreen) {
            Log.d(TAG + "backPress", "exit fullscreen")
            youtubePlayer.toggleFullscreen()
        } else {
            if (isCapturing) {
                Log.d("$TAG save", "dialog exit")
                val tittle = getString(R.string.exit_dialog_tittle)
                MaterialAlertDialogBuilder(this)
                    .setTitle(tittle)
                    .setNegativeButton(resources.getString(R.string.xoa)) { _, _ ->
                        finish()
                    }
                    .setPositiveButton(resources.getString(R.string.save)) { _, _ ->
                        btSingnRecord.performClick()
                    }
                    .show()
            } else {
                Log.d(TAG + "backPress", "finish activity")
                finish()
            }
        }
    }

    private fun checkPermission(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                   Manifest.permission.FOREGROUND_SERVICE
               )
        } else {
            arrayOf(
                Manifest.permission.RECORD_AUDIO
            )
        }
        for (s in permissions) {
            if (ContextCompat.checkSelfPermission(this, s) != PackageManager.PERMISSION_GRANTED) {
                // Some permissions are not granted, ask the user.
                ActivityCompat.requestPermissions(this, permissions, 0)
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Called when the user answers to the permission dialogs.
        if (requestCode != 0 || grantResults.isEmpty() || grantResults.size != permissions.size) return
        hasAllPermissions = true
        for (grantResult in grantResults)
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                hasAllPermissions = false
                Toast.makeText(
                    applicationContext,
                    getString(R.string.request_permission_toast),
                    Toast.LENGTH_SHORT
                ).show()
        }

        if (hasAllPermissions)
            firstInit()
    }

    private fun setUpView() {
        scrollSettingView = binding.scrollSettingView
        tvVideoTitle = binding.tvVideoTitle
        btSing = binding.btStart
        btSingnRecord = binding.btStartAndRecord
        swEffect = binding.swEffect
        swAutoTune = binding.swAutotune
        viewMicVolume = binding.viewMicVolume
        viewEffect = binding.viewEffect
        sbMicVolume = binding.sbMicVolume
        sbEcho = binding.sbEcho
        sbReverb = binding.sbReverb
        youTubePlayerView = binding.youtubePlayerView
    }

    private fun setYoutubePlayer() {
        val videoId = args.item.id.videoId
        val videoTitle = args.item.snippet.title
        val fullscreenViewContainer = binding.fullScreenViewContainer
        lifecycle.addObserver(youTubePlayerView)
        val listener: YouTubePlayerListener = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youtubePlayer = youTubePlayer
//                val defaultPlayerUiController =
//                   DefaultPlayerUiController(youTubePlayerView, youTubePlayer)
//                youTubePlayerView.setCustomPlayerUi(defaultPlayerUiController.rootView)
                youTubePlayer.loadVideo(videoId, 0f)
            }
        }
        val options: IFramePlayerOptions = IFramePlayerOptions.Builder().controls(1).fullscreen(1).build()
        youTubePlayerView.initialize(listener, options)
        youTubePlayerView.addFullscreenListener(object : FullscreenListener {
            override fun onEnterFullscreen(fullscreenView: View, function0: Function0<Unit>) {
                isFullScreen = true
                Log.d(TAG, "onEnterFull + $isFullScreen")
                actionBar?.hide()
                youTubePlayerView.visibility = View.INVISIBLE
                tvVideoTitle.visibility = View.INVISIBLE
                scrollSettingView.visibility = View.INVISIBLE
                fullscreenViewContainer.visibility = View.VISIBLE
                fullscreenViewContainer.addView(fullscreenView)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }

            override fun onExitFullscreen() {
                isFullScreen = false
                Log.d(TAG, "onExitFull + $isFullScreen")
                actionBar?.show()
                // the video will continue playing in the player
                youTubePlayerView.visibility = View.VISIBLE
                tvVideoTitle.visibility = View.VISIBLE
                scrollSettingView.visibility = View.VISIBLE
                fullscreenViewContainer.visibility = View.INVISIBLE
                fullscreenViewContainer.removeAllViews()
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        })
        tvVideoTitle.text = videoTitle
    }

//    private fun setYoutubePlayer() {
//        val videoId = args.item.id.videoId
//        val videoTitle = args.item.snippet.title
//        youTubePlayerView = binding.youtubePlayer
//        val youTubePlayerListener = object : AbstractYouTubePlayerListener() {
//            override fun onReady(youTubePlayer: YouTubePlayer) {
//                val defaultPlayerUiController =
//                    DefaultPlayerUiController(youTubePlayerView, youTubePlayer)
//                youTubePlayerView.setCustomPlayerUi(defaultPlayerUiController.rootView)
//                youTubePlayer.cueVideo(videoId, 0f)
//            }
//        }
//        val options: IFramePlayerOptions = IFramePlayerOptions.Builder().controls(0).build()
//        youTubePlayerView.initialize(youTubePlayerListener, options);
//        binding.tvVideoTitle.text = videoTitle
//    }

    private fun setSingButtonClick() {
        btSing.setOnClickListener {
            if (!isStreaming) {
                if (!checkHeadphoneConnected()) {
                    Toast.makeText(
                        applicationContext,
                        "Please plug in headphones for a better experience.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                btSingnRecord.visibility = View.GONE
                btSing.text = getString(R.string.stop)
                viewOnStatStreaming()

                isStreaming = true
                streamObject.startStreaming()
                streamObject.setMicVolume(sbMicVolume.progress.toFloat())
                Log.d(TAG + "sing", "value is ${sbMicVolume.progress.toFloat()}")
            } else {
                btSingnRecord.visibility = View.VISIBLE
                btSing.text = getString(R.string.start)
                viewOnStopStreaming()

                isStreaming = false
                streamObject.stopStreaming()
            }
        }
    }

    private fun setSingnRecordButtonClick() {
        btSingnRecord.setOnClickListener {
            if (!checkHeadphoneConnected() && !isStreaming) {
                Toast.makeText(
                    applicationContext,
                    "Please plug in headphones for a better experience.",
                    Toast.LENGTH_LONG
                ).show()
            }
            if (!isStreaming) {
                // stream and record start
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Log.d("TAGggg sing&record", "wait")
                    startCapturing()
                    Log.d(TAG + " sing&record", "before call isStreaming is ${isStreaming}")
                } else {
                    btSing.visibility = View.GONE
                    viewOnStatStreaming()
                    btSingnRecord.text = getString(R.string.stop)

                    streamObject.startStreaming()
                    streamObject.setMicVolume(sbMicVolume.progress.toFloat())
                    val fp = createAudioFile().path.replace(".pcm", ".mp3")
                    Log.d("tagggg crt", File(fp).exists().toString())
                    audioFile = File(fp)
                    audioRecorder.start(audioFile!!)
                    isStreaming = true
                    isCapturing = true
                }
//                streamObject.setMicVolume(sbMicVolume.progress.toFloat())
//                Log.d(TAG + "sing&record", "value is ${sbMicVolume.progress.toFloat()}")
            } else {
                //stream and record stop
                btSing.visibility = View.VISIBLE
                viewOnStopStreaming()
                btSingnRecord.text = getString(R.string.start_and_record)
                streamObject.stopStreaming()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Log.d("TAGggg stop sing&capture", "call stop + isStreaming is ${isStreaming}")
                    stopCapturing()
                } else {
                    audioRecorder.stop()
                    isStreaming = false
                    isCapturing = false
                }
                //save to database
                saveRecord(getString(R.string.song_saved))
            }
        }
    }

    private fun saveRecord(tittle: String) {
        val view: View = layoutInflater.inflate(R.layout.dialog_layout, null)
        val editText: EditText = view.findViewById(R.id.etSongName)
        view.findViewById<TextView>(R.id.dialog_title).text = tittle
        editText.setText(audioFile!!.name.replace(".pcm", ""))
        MaterialAlertDialogBuilder(this).apply {
            setView(view)
            setNegativeButton(getString(R.string.xoa)) { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            setPositiveButton(getString(R.string.save)) { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    mainViewModel.saveRecordedSongToDatabase(audioFile!!.path,
                        editText.text.toString()
                    )
                }
            }
            show()
        }
    }

    private fun viewOnStopStreaming() {
        swEffect.isChecked = false
        swAutoTune.isChecked = false
        swEffect.visibility = View.INVISIBLE
        viewEffect.visibility = View.INVISIBLE
        viewMicVolume.visibility = View.INVISIBLE
        swAutoTune.visibility = View.INVISIBLE
    }

    private fun viewOnStatStreaming() {
        swAutoTune.visibility = View.VISIBLE
        swEffect.visibility = View.VISIBLE
        viewMicVolume.visibility = View.VISIBLE
    }

    private fun setEffectSwitchClick() {
        binding.swEffect.setOnClickListener {
            streamObject.setEffectEnable(binding.swEffect.isChecked)
            if (swEffect.isChecked) {
                viewEffect.visibility = View.VISIBLE
                setEffectValue()
            } else {
                viewEffect.visibility = View.INVISIBLE
            }
        }
    }

    private fun setEffectValue() {
        streamObject.setEffectValue(1, binding.sbEcho.progress)
        streamObject.setEffectValue(2, binding.sbReverb.progress)
    }

    private fun setAutotuneSwitchClick() {
        swAutoTune.setOnClickListener(View.OnClickListener {
            streamObject.setAutoTuneEnable(
                swAutoTune.isChecked
            )
        })
    }

    private fun setSeekbarListener() {
        binding.sbMicVolume.setOnSeekBarChangeListener(this)
        binding.sbEcho.setOnSeekBarChangeListener(this)
        binding.sbReverb.setOnSeekBarChangeListener(this)
    }

    private fun checkHeadphoneConnected(): Boolean {
        isHeadphoneConnected = false
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        for (d in devices) {
            if (d.type == AudioDeviceInfo.TYPE_USB_HEADSET || d.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES || d.type == AudioDeviceInfo.TYPE_WIRED_HEADSET || d.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP) {
                return true
            }
        }
        return false
    }

    private fun initStreamObject() {
        // Get the device's sample rate and buffer size to enable
        // low-latency Android audio output, if available.
        val audioManager = this.getSystemService(AUDIO_SERVICE) as AudioManager
        val sampleRateString =
            audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE) ?: "48000"
        val bufferSizeString =
            audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER) ?: "480"

        val sampleRate = sampleRateString.toInt()
        val bufferSize = bufferSizeString.toInt()
        streamObject = StreamObject(sampleRate, bufferSize)
    }

    private fun startCapturing() {
        //if (!isRecordAudioPermissionGranted()) {
        //    requestRecordAudioPermission()
        //} else {
        startMediaProjectionRequest()
        //}
    }

    private fun stopCapturing() {
        //setButtonsEnabled(isCapturingAudio = false)
        isStreaming = false
        isCapturing = false
        startService(Intent(this, AudioCaptureService::class.java).apply {
            action = ACTION_STOP
        })
    }

    /**
     * Before a capture session can be started, the capturing app must
     * call MediaProjectionManager.createScreenCaptureIntent().
     * This will display a dialog to the user, who must tap "Start now" in order for a
     * capturing session to be started. This will allow both video and audio to be captured.
     */
    private fun startMediaProjectionRequest() {
        // use applicationContext to avoid memory leak on Android 10.
        // see: https://partnerissuetracker.corp.google.com/issues/139732252
        mediaProjectionManager =
            applicationContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(
            mediaProjectionManager.createScreenCaptureIntent(),
            MEDIA_PROJECTION_REQUEST_CODE
        )
    }

    @Deprecated("Deprecated in Java")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MEDIA_PROJECTION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                isStreaming = true
                isCapturing = true
                btSing.visibility = View.GONE
                audioFile = createAudioFile()
                btSingnRecord.text = getString(R.string.stop)
                viewOnStatStreaming()

                streamObject.startStreaming()
                streamObject.setMicVolume(sbMicVolume.progress.toFloat())
                val audioCaptureIntent = Intent(this, AudioCaptureService::class.java).apply {
                    action = ACTION_START
                    putExtra(EXTRA_RESULT_DATA, data!!)
                    putExtra(DES_PATH_KEY, audioFile!!.path)
                }
                startForegroundService(audioCaptureIntent)
            } else {
                Toast.makeText(
                    this, getString(R.string.request_permission_toast),
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(TAG + " s&r", "if deny isStreammung = ${isStreaming}")
            }
        }
    }

    private fun createAudioFile(): File {
        val audioCapturesDirectory = File(filesDir, "/AudioCaptures")
        if (!audioCapturesDirectory.exists()) {
            audioCapturesDirectory.mkdirs()
        }
        val timestamp = SimpleDateFormat("dd-MM-yyyy-hh-mm-ss", Locale.US).format(Date())
        val fileName = "Capture-$timestamp-${System.currentTimeMillis()}.pcm"
        return File(audioCapturesDirectory.absolutePath + "/" + fileName)
    }

    //menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        menuItem = menu!!.findItem(R.id.save_to_favorites_menu)
        checkSavedRecipes(menuItem)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        } else if (item.itemId == R.id.save_to_favorites_menu && !songSaved) {
            saveToFavorites(item)
        } else if (item.itemId == R.id.save_to_favorites_menu && songSaved) {
            removeFromFavorites(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkSavedRecipes(menuItem: MenuItem) {
        mainViewModel.readFavoriteSongs.observe(this) { favoritesEntity ->
            try {
                for (savedSong in favoritesEntity) {
                    if (savedSong.item.id.videoId == args.item.id.videoId) {
                        changeMenuItemColor(menuItem, R.color.yellow)
                        savedSongId = savedSong.id
                        songSaved = true
                    }
                }
            } catch (e: Exception) {
                Log.d("DetailsActivity", e.message.toString())
            }
        }
    }

    private fun saveToFavorites(item: MenuItem) {
        val favoriteSong =
            FavoriteSongEntity(
                0,
                args.item
            )
        mainViewModel.insertFavoriteSong(favoriteSong)
        changeMenuItemColor(item, R.color.yellow)
        showSnackBar(ContextCompat.getString(this, R.string.songs_saved))
        songSaved = true
    }

    private fun removeFromFavorites(item: MenuItem) {
        val favoritesEntity =
            FavoriteSongEntity(
                savedSongId,
                args.item
            )
        mainViewModel.deleteFavoriteSong(favoritesEntity)
        changeMenuItemColor(item, R.color.white)
        showSnackBar(ContextCompat.getString(this, R.string.song_removed_from_favorite))
        songSaved = false
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(
            binding.mainLayout,
            message,
            Snackbar.LENGTH_SHORT
        ).setAction("OK") {}
            .show()
    }

    private fun changeMenuItemColor(item: MenuItem, color: Int) {
        item.icon?.setTint(ContextCompat.getColor(this, color))
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (seekBar!! == sbMicVolume) {
            streamObject.setMicVolume(progress.toFloat())
            Log.d(TAG + "onChanged", "value is ${progress.toFloat()}")
        } else if (seekBar == sbEcho) {
            streamObject.setEffectValue(1, progress)
        } else if (seekBar == sbReverb) {
            streamObject.setEffectValue(2, progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

//    override fun onPause() {
//        Log.d(TAG + "  detail", "onPause")
//        super.onPause()
//        if (isStreaming) {
//            streamObject.stopStreaming()
//            isStreaming = false
//        }
//        isBack = true
//    }
//
//    override fun onResume() {
//        Log.d(TAG + "  detail", "onResume")
//        super.onResume()
//        if (!isStreaming && isBack) {
//            streamObject.startStreaming()
//            isStreaming = true
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "ondestroy")
        changeMenuItemColor(menuItem, R.color.white)
        _binding = null
        if (isStreaming) {
            streamObject.stopStreaming()
        }
        if (isCapturing) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                stopCapturing()
            } else {
                audioRecorder.stop()
            }
        }
    }
}