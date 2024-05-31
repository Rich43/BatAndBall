package com.wardcrew.batandball;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    private static final String PREFS_NAME = "GamePrefs";
    private static final String KEY_AI_DIFFICULTY = "ai_difficulty";

    private Thread gameThread;
    private SurfaceHolder surfaceHolder;
    private boolean isPlaying;
    private Paint paint;
    private float screenX, screenY;
    private RectF ball, playerPaddle, aiPaddle;
    private float ballSpeedX = 10f, ballSpeedY = 10f;
    private float playerPaddleSpeed = 20f;
    private float aiPaddleSpeed;  // AI paddle speed limit

    public GameView(Context context) {
        super(context);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        paint = new Paint();
        ball = new RectF();
        playerPaddle = new RectF();
        aiPaddle = new RectF();

        loadPreferences(context);
    }

    public void loadPreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int aiDifficulty = preferences.getInt(KEY_AI_DIFFICULTY, 5);
        aiPaddleSpeed = aiDifficulty;  // Adjust this factor as needed to match desired difficulty
    }

    public void setAiPaddleSpeed(float speed) {
        this.aiPaddleSpeed = speed;
    }

    @Override
    public void run() {
        while (isPlaying) {
            if (!surfaceHolder.getSurface().isValid()) {
                continue;
            }
            update();
            draw();
            control();
        }
    }

    private void update() {
        ball.offset(ballSpeedX, ballSpeedY);

        if (ball.left < 0 || ball.right > screenX) {
            ballSpeedX = -ballSpeedX;
        }
        if (ball.top < 0 || ball.bottom > screenY) {
            ballSpeedY = -ballSpeedY;
        }

        // Check collision with player paddle
        if (RectF.intersects(ball, playerPaddle)) {
            ballSpeedY = -ballSpeedY;
            ball.offset(0, -Math.abs(ballSpeedY)); // Move ball out of paddle to prevent sticking
        }

        // Check collision with AI paddle
        if (RectF.intersects(ball, aiPaddle)) {
            ballSpeedY = -ballSpeedY;
            ball.offset(0, Math.abs(ballSpeedY)); // Move ball out of paddle to prevent sticking
        }

        // AI Paddle movement
        moveAIPaddle();
    }

    private void moveAIPaddle() {
        float aiTargetX = ball.centerX() - aiPaddle.centerX();
        float aiTargetY = ball.centerY() - aiPaddle.centerY();

        if (Math.abs(aiTargetX) > aiPaddleSpeed) {
            aiTargetX = aiPaddleSpeed * Math.signum(aiTargetX);
        }
        if (Math.abs(aiTargetY) > aiPaddleSpeed) {
            aiTargetY = aiPaddleSpeed * Math.signum(aiTargetY);
        }

        aiPaddle.offset(aiTargetX, aiTargetY);

        // Restrict AI paddle to the top quarter of the screen
        if (aiPaddle.top < 0) {
            aiPaddle.offsetTo(aiPaddle.left, 0);
        }
        if (aiPaddle.bottom > screenY / 4) {
            aiPaddle.offsetTo(aiPaddle.left, screenY / 4 - aiPaddle.height());
        }
        if (aiPaddle.left < 0) {
            aiPaddle.offsetTo(0, aiPaddle.top);
        }
        if (aiPaddle.right > screenX) {
            aiPaddle.offsetTo(screenX - aiPaddle.width(), aiPaddle.top);
        }
    }

    private void draw() {
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas != null) {
            canvas.drawColor(Color.BLACK);

            paint.setColor(Color.WHITE);
            canvas.drawRect(ball, paint);
            canvas.drawRect(playerPaddle, paint);
            canvas.drawRect(aiPaddle, paint);

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            Thread.sleep(10); // Increase frame rate by reducing sleep time
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause() {
        try {
            isPlaying = false;
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        resume();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        screenX = width;
        screenY = height;

        ball.set(screenX / 2 - 25, screenY / 2 - 25, screenX / 2 + 25, screenY / 2 + 25);
        playerPaddle.set(screenX / 2 - 100, screenY - 150, screenX / 2 + 100, screenY - 130);
        aiPaddle.set(screenX / 2 - 100, 50, screenX / 2 + 100, 70);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        pause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float touchX = event.getX();
                float touchY = event.getY();

                // Restrict paddle movement to the lower quarter of the screen
                if (touchY > screenY * 3 / 4) {
                    float halfPaddleWidth = 100;
                    playerPaddle.left = Math.max(0, Math.min(touchX - halfPaddleWidth, screenX - playerPaddle.width()));
                    playerPaddle.right = playerPaddle.left + 200;

                    float halfPaddleHeight = 10;
                    playerPaddle.top = Math.max(screenY * 3 / 4, Math.min(touchY - halfPaddleHeight, screenY - halfPaddleHeight * 2));
                    playerPaddle.bottom = playerPaddle.top + 20;
                }
                break;
        }
        return true;
    }
}
