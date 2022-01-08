package com.example.ballgame

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool

class SoundPlayer(context: Context) {

    // 最大同時再生数,音声ファイルの種類,再生品質(デフォルトは0)
    private val soundPool = SoundPool(2,AudioManager.STREAM_MUSIC,0)//privateにした理由：このクラスを呼び出した時、このsoundPoolにアクセスできてしまい、変更されそうで危なかったから。

    // loadで音声ファイルを読み込む。コンテキスト,音声ファイル,再生品質(デフォルトは1)
    private val hitSound = soundPool.load(context,R.raw.hit,1)//privateにした理由：上記と同じ
    private val overSound = soundPool.load(context,R.raw.over,1)//privateにした理由：上記と同じ

    fun hitSoundPlay(){
        // サウンドID,ステレオ左と右の音量(0.0f~1.0fで指定),優先度(最小値は0),ループ回数(0は無し、-1で繰り返し),再生速度(0.5f~2.0fで指定)
        soundPool.play(hitSound,1.0f,1.0f,1,0,1.0f)
    }

    fun overSoundPlay(){
        soundPool.play(overSound,1.0f,1.0f,1,0,1.0f)
    }
}