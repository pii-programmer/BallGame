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

    // プリミティブ型はlateinit使えない。nullable || non-null by lazy{ 初期値をキャッシュに残す }。初期値をキャッシュに残すから初期値の変更ができない。
    // FrameLayoutの高さ
    var frameHeight by Delegates.notNull<Int>()
    // FrameLayoutの横
    var frameWidth by Delegates.notNull<Int>()
    // 種のサイズ
    var seedSize by Delegates.notNull<Int>()
    // 種のY座標
    var seedY by Delegates.notNull<Float>()
    // 雨のサイズ
    var rainSize by Delegates.notNull<Int>()
    // 雨の座標
    var rainY by Delegates.notNull<Float>()
    var rainX by Delegates.notNull<Float>()
    // 晴れのサイズ
    var sunSize by Delegates.notNull<Int>()
    // 晴れの座標
    var sunY by Delegates.notNull<Float>()
    var sunX by Delegates.notNull<Float>()

    // スコアの初期値
    var score = 0

    // タイマーインスタンス
    val timerTask = MakeTimerTask()
    val timer = Timer()

    // フラグの初期値
    var touch_flg = false
    var start_flg = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 雨と晴れの初期位置
        binding.rain.apply{
            x = 900.0f
            y = 1000.0f
        }
        binding.sun.apply{
            x = 900.0f
            y = 700.0f
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

        when( start_flg ){
            // ゲーム開始前
            false -> {
                // timer実行
                GlobalScope.launch{
                    withContext(Dispatchers.IO){
                        timer.schedule(timerTask, 0, 50)
                    }
                    withContext(Dispatchers.Main){
                        hitCheck()
                    }
                }
                // 画面タップでゲーム開始
                if( event?.action == MotionEvent.ACTION_DOWN ){
                    start_flg = true
                }
            }
            // ゲーム開始したら
            true -> {
                hitCheck()

                if( event?.action == MotionEvent.ACTION_DOWN ){
                    touch_flg = true // タップしたら上に

                } else if( event?.action == MotionEvent.ACTION_UP ){
                    touch_flg = false// 離したら下に

                }
            }
        }
        return true
    }

    // UI変更はメインスレッドでのみ可能
    fun hitCheck(){
        // 衝突してる状態は？種のXY座標の中に雨が入っていること
        if( 0 <= rainX && rainX <= seedSize && seedY <= rainY && rainY <= seedY + seedSize ){
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
        }else if( 0 <= sunX && sunX <= seedSize && seedY <= sunY && sunY <= seedY + seedSize ){
            sunX -= 1.0f // 画面外（左）に出して画面右へ戻す

            score += 10
            binding.score.text = "Score : $score"

            binding.seed.setImageResource(R.drawable.smile_tanpopo)
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
            // 種のY座標がFrameLayoutの外だったら現在のY座標を代入
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
}
