package com.hamee.hamic.super_hamic_bros

/**
 * 敵キャラの状態を表す抽象クラス
 *
 * @property type 敵タイプ（0〜2）
 * @param maxX 最大X座標（画面の右端）
 */
abstract class Enemy(val type: Int, maxX: Float) {
    val width = 0.1f
    val height = 0.1f
    var positionX = maxX
    var positionY = 0f

    /**
     * フレームごとに呼ばれる
     */
    abstract fun onNextFrame()

    companion object {
        /**
         * ジェネレータメソッド
         *
         * @param type 敵タイプ（0〜2）
         * @param maxX 最大X座標（画面の右端）
         * @param groundY 地面のY座標
         */
        fun create(type: Int, maxX: Float, groundY: Float): Enemy {
            return when (type) {
                0 -> Enemy1(maxX, groundY)
                1 -> Enemy2(maxX, groundY)
                2 -> Enemy3(maxX)
                else -> throw IllegalArgumentException()
            }
        }
    }
}

class Enemy1(maxX: Float, groundY: Float) : Enemy(0, maxX) {
    init {
        positionY = groundY - height
    }

    override fun onNextFrame() {
        positionX -= 0.01f
    }
}

class Enemy2(maxX: Float, private val groundY: Float) : Enemy(1, maxX) {
    private var vy = -0.08f

    init {
        positionY = groundY - height
    }

    override fun onNextFrame() {
        positionX -= 0.007f
        vy += 0.01f
        positionY += vy
        if (positionY >= groundY - height) {
            // 着地したら即ジャンプする
            positionY = groundY - height
            vy = -0.08f
        }
    }
}

class Enemy3(maxX: Float) : Enemy(2, maxX) {
    private var vy = 0f

    init {
        positionY = 0.35f
    }

    override fun onNextFrame() {
        positionX -= 0.005f

        // Y座標はバネ運動
        vy += (0.3f - positionY) * 0.1f
        positionY += vy
    }
}