package jp.isobe.tomohiro.accball;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends AppCompatActivity implements SensorEventListener, SurfaceHolder.Callback {

    SensorManager mSensorManager;
    Sensor mAccSensor;

    SurfaceHolder mHolder;
    int mSurfaceWidth;
    int mSurfaceHeight;

    static final float RADIUS = 150.0f; // ボール描画する時の半径
    static final int DIA = (int)RADIUS * 2; // ボールの直径
    static final float COEF = 1000.0f; // ボールの移動量を調整する係数

    float mBallX; // ボールの現在のｘ座標
    float mBallY; // ボールの現在のｙ座標
    float mVX;   // ボールのx軸方向への加速度
    float mVY;   // ボールのy軸方向への加速度

    long mT0;   // 前回センサーから加速度を取得した時間

    Bitmap mBallBitmap;  // ボールの画像

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 画面を縦方向にロック
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mHolder = surfaceView.getHolder();

        mHolder.addCallback(this);

        // SurfaceViewを透明にする
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        surfaceView.setZOrderOnTop(true);

        // ボールの画像を用意する
        Bitmap ball = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
        // リサイズ
        mBallBitmap = Bitmap.createScaledBitmap(ball, DIA, DIA, false);
    }

    // 加速度センサーの値に変化が合った時に呼ばれる
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = -sensorEvent.values[0];
            float y = sensorEvent.values[1];





            // 時間ｔを求める
            if (mT0 == 0) {
                mT0 = sensorEvent.timestamp;
                return;
            }
            float t = sensorEvent.timestamp -mT0;
            mT0 = sensorEvent.timestamp;
            t = t/ 1000000000.0f; // ナノ秒を秒に変換

            // 移動距離を求める
            //　加速度の公式　d = v0t + a*t*t / 2
            float dx = (mVX * t) + (x * t * t / 2.0f);
            float dy = (mVY * t) + (y * t * t /2.0f);

            // 移動距離からボールの現在位置を更新
            mBallX = mBallX + dx * COEF;
            mBallY = mBallY + dy * COEF;

            // 現在のボールの移動速度を更新
            mVX = mVX + (x * t);
            mVY = mVY + (y * t);

            // ボールが画面外に出ないようにする処理
            // 左端
            if (mBallX - RADIUS < 0 && mVX < 0) {
                mVX = -mVX / 1.5f;
                mBallX = RADIUS;
                // 右端
            } else if (mBallX + RADIUS > mSurfaceWidth && mVX > 0) {
                mVX = -mVX / 1.5f;
                mBallX = mSurfaceWidth - RADIUS;
            }

            // 上端
            if (mBallY - RADIUS < 0 && mVY < 0) {
                mVY = -mVY / 1.5f;
                mBallY = RADIUS;
            } else if (mBallY + RADIUS > mSurfaceHeight && mVY > 0) {
                mVY = -mVY / 1.5f;
                mBallY = mSurfaceHeight - RADIUS;
            }

            // 加速度から算出したボールの現在位置で、ボールをキャンバスに描画
            drawCanvas();
        }
    }

    private void drawCanvas() {

        // 画面にボールを表示する処理
        // キャンバスを取得
        Canvas c = mHolder.lockCanvas();
        c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        Paint paint = new Paint();
        c.drawBitmap(mBallBitmap, mBallX - RADIUS, mBallY - RADIUS, paint);

        mHolder.unlockCanvasAndPost(c);
    }

    // 加速度センサーの精度が変更された時に呼ばれる
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    // 画面が表示された時に呼ばれる
    @Override
    protected void onResume() {
        super.onResume();


    }

    // 画面が閉じられた時
    @Override
    protected void onPause() {
        super.onPause();


    }

    // Surfaceが作られた時の処理
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        mSensorManager.registerListener(this, mAccSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    // Surfaceに変更があった時の処理
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        mSurfaceWidth = i1;
        mSurfaceHeight = i2;

        // 画面の中央をボールの初期位置とする
        mBallX = mSurfaceWidth / 2;
        mBallY = mSurfaceHeight / 2;

        // 最初の速度、時間を初期化
        mVX = 0;
        mVY = 0;
        mT0 = 0;
    }

    // Surfaceが削除される時の処理
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        // センサーの監視を終了
        mSensorManager.unregisterListener(this);

    }
}
