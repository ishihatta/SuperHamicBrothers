package com.hamee.hamic.super_hamic_bros

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.graphics.withScale
import java.util.*
import kotlin.math.absoluteValue

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    /**
     * ゲームの状態（初期化中, ゲーム中, ゲームオーバー）
     */
    private enum class State {
        INITIALIZING, PLAYING, GAME_OVER
    }
    private var state = State.INITIALIZING

    /**
     * 画面描画に使う Paint オブジェクト
     */
    private val paint = Paint().apply { isAntiAlias = true }

    /**
     * 自キャラの画像
     */
    private val ownBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.my_character)

    /**
     * 敵キャラの画像
     */
    private val enemyBitmaps = arrayOf(
        BitmapFactory.decodeResource(context.resources, R.drawable.enemy1),
        BitmapFactory.decodeResource(context.resources, R.drawable.enemy2),
        BitmapFactory.decodeResource(context.resources, R.drawable.enemy3)
    )

    /**
     * 画面右端のX座標
     */
    private val maxX: Float
        get() = width.toFloat() / height.toFloat()

    /**
     * 地面のY座標
     */
    private val groundY = 0.6f

    /**
     * 自キャラの状態
     */
    private val ownCharacter = OwnCharacter(groundY)

    /**
     * 敵キャラの状態のリスト
     */
    private val enemyList = mutableListOf<Enemy>()

    /**
     * 乱数ジェネレータ
     */
    private val random = Random()

    /**
     * 雲のX座標
     */
    private var cloudX = 0f

    /**
     * 画面をタッチされたことを表すフラグ
     */
    private var touchFlag = false

    /**
     * 点数
     */
    private var score = 0

    init {
        holder.addCallback(this)
    }

    /**
     * 画面描画処理
     */
    private fun drawGameScreen(canvas: Canvas) {
        // スケーリング
        val scale = height.toFloat()
        canvas.withScale(scale, scale) {
            // 背景を水色にする
            canvas.drawColor(Color.rgb(0, 200, 255))

            // 地面（赤い四角形）を描く
            paint.color = Color.rgb(160, 0, 0) // 色の指定
            paint.style = Paint.Style.FILL // スタイル（塗りつぶし）の指定
            canvas.drawRect(0f, groundY, maxX, 1f, paint)

            // 雲（白い楕円形）を描く
            paint.color = Color.WHITE
            canvas.drawOval(cloudX, 0.1f, cloudX + 0.5f, 0.2f, paint)

            // 自キャラを描く
            canvas.drawBitmap(
                ownBitmap,
                Rect(0, 0, ownBitmap.width, ownBitmap.height),
                RectF(
                    ownCharacter.positionX,
                    ownCharacter.positionY,
                    ownCharacter.positionX + ownCharacter.width,
                    ownCharacter.positionY + ownCharacter.height
                ),
                paint
            )

            // 敵キャラを描く
            enemyList.forEach {
                val bitmap = enemyBitmaps[it.type]
                canvas.drawBitmap(
                    bitmap,
                    Rect(0, 0, bitmap.width, bitmap.height),
                    RectF(
                        it.positionX,
                        it.positionY,
                        it.positionX + it.width,
                        it.positionY + it.height
                    ),
                    paint
                )
            }
        }

        // スコア（文字列）を描く
        paint.color = Color.BLACK
        paint.textSize = 0.05f * scale
        val fontMetrics = paint.fontMetrics
        canvas.drawText("SCORE $score", 0f, 0f - fontMetrics.ascent, paint)
    }

    /**
     * 画面を再描画する
     */
    private fun repaint() {
        // 再描画処理
        val surfaceHolder = holder
        val canvas = surfaceHolder.lockCanvas()
        drawGameScreen(canvas)
        surfaceHolder.unlockCanvasAndPost(canvas)
    }

    /**
     * ゲームを開始する
     */
    private fun startGame() {
        cloudX = maxX
        score = 0
        enemyList.clear()
        touchFlag = false
        ownCharacter.onStartGame()
        state = State.PLAYING
        onNextFrame.run()
    }

    /**
     * フレームごとに呼ばれ、自キャラや敵キャラの状態を変化させる
     */
    private val onNextFrame = object : Runnable {
        override fun run() {
            handler.postDelayed(this, 33)

            // スコアを1点増やす
            score++

            // 雲
            cloudX -= 0.006f
            if (cloudX < -0.5f) cloudX = maxX

            // 自キャラ
            ownCharacter.onNextFrame(touchFlag)
            touchFlag = false
            val ownCenterX = ownCharacter.positionX + ownCharacter.width / 2f
            val ownCenterY = ownCharacter.positionY + ownCharacter.height / 2f

            // 敵キャラ
            val iterator = enemyList.listIterator()
            iterator.forEach { enemy ->
                enemy.onNextFrame()

                // 左端まで行ったら消す
                if (enemy.positionX < -enemy.width) {
                    iterator.remove()
                }

                // 当たり判定
                if ((ownCenterX - (enemy.positionX + enemy.width / 2f)).absoluteValue < 0.06f &&
                    (ownCenterY - (enemy.positionY + enemy.height / 2f)).absoluteValue < 0.08f) {
                    if (ownCharacter.positionY < enemy.positionY) {
                        // 敵を倒した
                        iterator.remove()
                        ownCharacter.onDefeated()
                        score += 100
                    } else {
                        // 敵にぶつかった
                        handler.removeCallbacks(this)
                        state = State.GAME_OVER
                    }
                }
            }

            // 敵キャラの出現
            val randomNumber = random.nextInt(100)
            if (randomNumber < 3) {
                enemyList += Enemy.create(randomNumber, maxX, groundY)
            }

            // 状態の変化を画面に反映する
            repaint()
        }
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        // サーフェスが作られると呼ばれる
        if (state == State.PLAYING) onNextFrame.run()
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // 表示領域に変更があると呼ばれる
        // (最初にも1回呼ばれる)
        when (state) {
            State.INITIALIZING -> startGame()
            State.PLAYING -> Unit
            State.GAME_OVER -> repaint()
        }
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        // サーフェスがなくなると呼ばれる
        handler.removeCallbacks(onNextFrame)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            if (state == State.GAME_OVER) {
                // ゲームオーバー状態で画面タッチしたらゲーム開始する
                startGame()
            } else {
                // 「タッチした」フラグをセットする
                touchFlag = true
            }
        }
        return super.onTouchEvent(event)
    }
}