package com.example.raftrumble;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.view.View;
import android.view.MotionEvent;

public class GameView extends View {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private MainActivity activity;

    private float playerX = 250;
    private float playerY = 250;

    private float remoteX = 500;
    private float remoteY = 250;

    private float targetX = 250;
    private float targetY = 250;

    public GameView(Context context, MainActivity activity) {
        super(context);
        this.activity = activity;
        setFocusable(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();

        canvas.drawColor(Color.rgb(15, 35, 55));

        // Water
        paint.setColor(Color.rgb(20, 110, 150));
        canvas.drawRect(0, h * 0.65f, w, h, paint);

        // Game title
        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        canvas.drawText("FART TOGETHER", 30, 50, paint);

        // Player raft
        paint.setColor(Color.rgb(220, 160, 70));
        canvas.drawRoundRect(
            playerX - 65, playerY - 35,
            playerX + 65, playerY + 35,
            15, 15, paint
        );

        // Player character
        paint.setColor(Color.GREEN);
        canvas.drawCircle(playerX, playerY - 15, 22, paint);

        // Remote player
        paint.setColor(Color.rgb(255, 100, 100));
        canvas.drawRoundRect(
            remoteX - 65, remoteY - 35,
            remoteX + 65, remoteY + 35,
            15, 15, paint
        );

        paint.setColor(Color.YELLOW);
        canvas.drawCircle(remoteX, remoteY - 15, 22, paint);

        // Instructions
        paint.setColor(Color.WHITE);
        paint.setTextSize(25);
        canvas.drawText("TOUCH AND DRAG TO MOVE", 30, h - 25, paint);

        // Smooth movement
        playerX += (targetX - playerX) * 0.08f;
        playerY += (targetY - playerY) * 0.08f;

        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN ||
            event.getAction() == MotionEvent.ACTION_MOVE) {

            targetX = event.getX();
            targetY = event.getY();

            targetX = Math.max(70,
                Math.min(getWidth() - 70, targetX));

            targetY = Math.max(100,
                Math.min(getHeight() - 70, targetY));

            activity.sendThrust(targetX, targetY);

            return true;
        }

        return true;
    }

    public void addRemote(float x, float y) {
        remoteX = x;
        remoteY = y;
        postInvalidate();
    }

    public void setRemoteState(float x, float y) {
        remoteX = x;
        remoteY = y;
        postInvalidate();
    }
}
