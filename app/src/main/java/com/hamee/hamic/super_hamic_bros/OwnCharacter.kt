package com.hamee.hamic.super_hamic_bros

/**
 * 自キャラの状態を表すクラス
 *
 * @property groundY 地面のY座標
 */
class OwnCharacter(private val groundY: Float) {
    val width = 0.1f
    val height = 0.1f
    val positionX = 0.25f
    var positionY = 0f

    private var isJumping = false
    private var vy = 0f

    /**
     * ゲーム開始時に呼ばれる
     */
    fun onStartGame() {
        positionY = groundY - height
        isJumping = false
    }

    /**
     * フレームごとに呼ばれる
     */
    fun onNextFrame(touchFlag: Boolean) {
        if (isJumping) {
            // ジャンプ中はY座標を変化させる
            vy += 0.003f
            positionY += vy
            if (positionY >= groundY - height) {
                // 着地した
                positionY = groundY - height
                isJumping = false
            }
        } else {
            if (touchFlag) {
                // ジャンプ開始
                isJumping = true
                vy = -0.04f
                positionY += vy
            }
        }
    }

    /**
     * 敵を倒すと呼ばれる
     */
    fun onDefeated() {
        isJumping = true
        vy = -0.02f
    }
}