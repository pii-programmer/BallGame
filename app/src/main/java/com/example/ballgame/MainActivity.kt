package com.example.ballgame

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import com.example.ballgame.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.properties.Delegates
//TODO:ヒット効果音、スタート画面、ゲームオーバークリア画面
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    // クラスのメンバにする
    // プリミティブ型はlateinit使えない。nullable || non-null by lazy{ 初期値をキャッシュに残す }。初期値をキャッシュに残すから初期値の変更ができない。だからDelegates.notNull()
    var frameHeight by Delegates.notNull<Int>()
    var frameWidth by Delegates.notNull<Int>()

    var seedSize by Delegates.notNull<Int>()
    var seedY by Delegates.notNull<Float>()

    var rainSize by Delegates.notNull<Int>()
    var rainY by Delegates.notNull<Float>()
    var rainX by Delegates.notNull<Float>()

    var sunSize by Delegates.notNull<Int>()
    var sunY by Delegates.notNull<Float>()
    var sunX by Delegates.notNull<Float>()

//    var beeSize by Delegates.notNull<Int>()
//    var beeY by Delegates.notNull<Float>()
//    var beeX by Delegates.notNull<Float>()
//
//    var butterflySize by Delegates.notNull<Int>()
//    var butterflyY by Delegates.notNull<Float>()
//    var butterflyX by Delegates.notNull<Float>()

    // スコアの初期値
    var score = 0

    // フラグの初期値
    var touch_flg = false
    var start_flg = false

    // タイマーインスタンス
    val timerTask = MakeTimerTask()
//    val trickTimerTask = TrickTimerTask()
    val timer = Timer()

    // 効果音のインスタンス。初期化はcontextの生成が完了してるonCreateで
    lateinit var soundPlayer:SoundPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        soundPlayer = SoundPlayer(this)

        // 雨晴れ蜂蝶の初期位置
        binding.rain.apply{
            x = 900.0f
            y = 700.0f
        }
        binding.sun.apply{
            x = 900.0f
            y = 1000.0f
        }
        binding.bee.apply {
            x = 1200.0f
            y = 500.0f
        }
        binding.butterfly.apply {
            x = 1200.0f
            y = 1200.0f
        }

        // スタート文字をフェードアウト
        AlphaAnimation(1.0f,0.0f).apply {
            duration = 5000
            fillAfter = true
            binding.start.startAnimation(this)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // ビュー描画が完了してるタッチイベントで初期化
        frameHeight = binding.frame.height
        frameWidth = binding.frame.width

        seedSize = binding.seed.height
        seedY = binding.seed.y

        rainSize = binding.rain.height
        rainY = binding.rain.y
        rainX = binding.rain.x

        sunSize = binding.sun.height
        sunY = binding.sun.y
        sunX = binding.sun.x

//        beeSize = binding.bee.height
//        beeY = binding.sun.y
//        beeX = binding.sun.x
//
//        butterflySize = binding.butterfly.height
//        butterflyY = binding.sun.y
//        butterflyX = binding.sun.x

        when( start_flg ) {
            // ゲーム開始前
            false -> {
                GlobalScope.launch {
                    withContext(Dispatchers.IO) {
                        timer.schedule(timerTask, 0, 50)//待ち時間なし 50ミリ秒毎に実行
//                        timer.schedule(trickTimerTask,0,50)
                    }
                    withContext(Dispatchers.Main) {
                        hitCheck()
                    }
                }
                // 画面タップでゲーム開始
                if (event?.action == MotionEvent.ACTION_DOWN) {
                    start_flg = true
                }
            }
            // ゲーム開始したら
            true -> {
                hitCheck()

                if (event?.action == MotionEvent.ACTION_DOWN) {
                    touch_flg = true // タップしたら上に

                } else if (event?.action == MotionEvent.ACTION_UP) {
                    touch_flg = false// 離したら下に
                }
            }
        }
        return true
    }

    // UI変更はメインスレッドでのみ可能
    fun hitCheck(){
        // 衝突してる状態 = 種のXY座標の中に雨が入っていること
        if( 0 <= rainX && rainX <= seedSize && seedY <= rainY && rainY <= seedY + seedSize ){
            soundPlayer.hitSoundPlay()

            rainX = -1.0f // 画面外（左）に出して画面右へ戻す

            score += 10
            binding.score.text = "Score : $score"

            when(score){
                50 -> {
                    binding.seed.setImageResource(R.drawable.hutaba)
                }
                100 -> {
                    binding.seed.setImageResource(R.drawable.tanpopo)
                }
                150 -> {
                    binding.seed.setImageResource(R.drawable.smile_tanpopo)
                }
            }
        // 晴れの場合は
        }else if( 0 <= sunX && sunX <= seedSize && seedY <= sunY && sunY <= seedY + seedSize ){
            soundPlayer.hitSoundPlay()

            sunX -= 1.0f // 画面外（左）に出して画面右へ戻す

            score += 150
            binding.score.text = "Score : $score"

            when(score){
                150 -> {
                    binding.seed.setImageResource(R.drawable.smile_tanpopo)
                }
                300 -> {
                    binding.seed.setImageResource(R.drawable.tree)
                }
            }
        }
    }

    // Timerはワーカースレッドのみ実行可能 // UI変更はメインスレッドでのみ可能 // TimerTaskクラスを継承したクラス内で run
    //TODO: ランダムに蜂と蝶がでてgame overとgame clearになる
    inner class MakeTimerTask : TimerTask(){
        override fun run() {
            // 種の移動
            when( touch_flg ){
                true -> {
                    seedY -= 20.0f
                }
                false -> {
                    seedY += 20.0f
                }
            }
            // 種のY座標が無くなったら0.0fを代入
            if( seedY < 0.0f ){
                seedY = 0.0f
            }
            // 種のY座標がFrameHeightの外だったら現在のY座標を代入
            if( seedY > frameHeight - seedSize ){
                seedY = (frameHeight - seedSize).toFloat()
            }
            binding.seed.y = seedY

            // 雨
            rainX -= 10.0f
            // 雨が画面外へ行ったら画面右へ戻す。Yはランダムに生成する。
            if( rainX < 0.0f ){
                rainX = frameWidth + 10.0f
                // 乱数×(最大値-最小値)+最小値=範囲
                rainY = (Math.random() * (frameHeight - rainSize) + rainSize).toFloat()
            }
            binding.rain.x = rainX
            binding.rain.y = rainY

            // 晴れ
            sunX -= 10.0f
            if( sunX < 0.0f ){
                sunX = frameWidth + 10.0f
                sunY = (Math.random() * (frameHeight - sunSize) + sunSize).toFloat()
            }
            binding.sun.x = sunX
            binding.sun.y = sunY
        }
    }
//    inner class TrickTimerTask : TimerTask(){
//        override fun run() {
//            beeX -= 10.0f
//            if( beeX < 0.0f ){
//                beeX = frameWidth + 10.0f
//                beeY = (Math.random() * (frameHeight - beeSize) + beeSize).toFloat()
//            }
//            binding.bee.x = beeX
//            binding.bee.y = beeY
//
//            butterflyX -= 10.0f
//            if( butterflyX < 0.0f ){
//                butterflyX = frameWidth + 10.0f
//                butterflyY = (Math.random() * (frameHeight - butterflySize) + butterflySize).toFloat()
//            }
//            binding.butterfly.x = butterflyX
//            binding.butterfly.y = butterflyY
//        }
//    }
}
