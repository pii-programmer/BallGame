package com.example.ballgame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import com.example.ballgame.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    // プリミティブ型はlateinit使えない。nullable || non-null by lazy{ 初期値をキャッシュに残す }。初期値をキャッシュに残すから初期値の変更ができない。
    // FrameLayoutの高さ
    var frameHeight by Delegates.notNull<Int>()
    // 種のサイズ
    var seedSize by Delegates.notNull<Int>()
    // 種のY座標
    var seedY by Delegates.notNull<Float>()

    // タイマー
    val timerTask = MakeTimerTask()
    val timer = Timer()
    // ステータス
    var touch_flg = false
    var start_flg = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 雨と晴れを画面外へ
        binding.rain.apply {
            x = -100.0f
            y = -100.0f
        }
        binding.sun.apply {
            x = -100.0f
            y = -100.0f
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
        seedSize = binding.seed.height
        seedY = binding.seed.y

        when( start_flg ){
            // ゲーム開始前
            false -> {
                // timer実行
                GlobalScope.launch{
                    withContext(Dispatchers.IO){
                        timer.schedule(timerTask, 0, 50)
                    }
                }
                // 画面タップでゲーム開始
                if( event?.action == MotionEvent.ACTION_DOWN ){
                    start_flg = true
                }
            }
            // ゲーム開始したら
            true -> {
                if( event?.action == MotionEvent.ACTION_DOWN ){
                    touch_flg = true // タップしたら上に

                } else if( event?.action == MotionEvent.ACTION_UP ){
                    touch_flg = false// 離したら下に

                }
            }
        }
        return true
    }

    // UIの変更はメインスレッドでしかできない  // TimerTaskクラスを継承したクラス内で run
    inner class MakeTimerTask : TimerTask(){
        override fun run() {
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
        }
    }
}
