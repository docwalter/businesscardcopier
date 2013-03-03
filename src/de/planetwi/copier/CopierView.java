package de.planetwi.copier;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Philipp Walter
 */
public class CopierView extends SurfaceView implements SurfaceHolder.Callback {

    private CopierThread thread = null;

    public CopierView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // TODO message "touch screen to start copying"
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        if (thread == null || !thread.isAlive()) return;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (thread == null || !thread.isAlive()) {
            thread = new CopierThread(getHolder(), getContext(), null);
            thread.start();
            Logger.getLogger(CopierView.class.getName()).log(Level.INFO, "thread started");
        }
        return true;
    }

    class CopierThread extends Thread {

        final SurfaceHolder holder;
        final Context context;
        final Handler handler;
        boolean running;
        long starttime;

        public CopierThread(SurfaceHolder holder, Context context, Handler handler) {
            this.holder = holder;
            this.context = context;
            this.handler = handler;
            this.running = false;
            this.starttime = System.currentTimeMillis();
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            while (running) {
                Canvas c = null;
                try {
                    c = holder.lockCanvas(null);
                    synchronized (holder) {
                        draw(c);
                    }
                } finally {
                    if (c != null) holder.unlockCanvasAndPost(c);
                }
            }
        }
        int count = 0;

        public void draw(Canvas c) {
            long ms = System.currentTimeMillis() - starttime;
            final long forwardDuration = 2000; // ms
            final long backwardDuration = 1000; // ms
            int w = c.getWidth();
            int h = c.getHeight();
            int p = 0;
            if (ms < forwardDuration)
                p = (int) (h * ms / forwardDuration);
            else if (ms < forwardDuration + backwardDuration)
                p = h - (int) (h * (ms - forwardDuration) / backwardDuration);
            else running = false;

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);

            c.drawColor(Color.BLACK);
            paint.setColor(Color.WHITE);
            c.drawRect(0, p - 2, w, p + 2, paint);
            c.drawText(String.valueOf(count++), w / 2, h / 2, paint);
        }
    }
}
