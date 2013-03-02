package de.planetwi.copier;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 *
 * @author Philipp Walter
 */
public class CopierView extends SurfaceView implements SurfaceHolder.Callback {

    private CopierThread thread;

    public CopierView(Context context) {
        super(context);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        thread = new CopierThread(holder, context, null);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        thread.setSurfaceSize(width, height);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    class CopierThread extends Thread {

        final SurfaceHolder holder;
        final Context context;
        final Handler handler;
        int width, height;
        boolean running;

        public CopierThread(SurfaceHolder holder, Context context, Handler handler) {
            this.holder = holder;
            this.context = context;
            this.handler = handler;
            this.running = false;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        public void setSurfaceSize(int width, int height) {
            synchronized (holder) {
                this.width = width;
                this.height = height;
            }
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
            c.drawColor(Color.BLUE);

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);

            int w = c.getWidth();
            int h = c.getHeight();
            paint.setColor(Color.RED);
            c.drawLine(0,0, w,h, paint);
            c.drawLine(w,0, 0,h, paint);
            c.drawText(String.valueOf(count++), w/2, h/2, paint);
        }
    }
}
