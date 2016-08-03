package com.fatty.puzzle;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    /*判断动画是否正在进行中*/
    private boolean isMoving = false;
    /* 利用二维数组创建若干个游戏小方块 */
    private ImageView[][] iv_game_arr = new ImageView[3][5];
    /*游戏主界面*/
    private GridLayout gl_main_game;
    /*空方块的实例*/
    private ImageView iv_null;

    private GestureDetector mDetector;


    @Override
    protected void onResume() {
        /**
         * 设置为横屏
         */
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onResume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        setContentView(R.layout.activity_main);
        mDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent motionEvent) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                int dir = getDirByGes(motionEvent.getX(),
                        motionEvent.getY(),
                        motionEvent1.getX(),
                        motionEvent1.getY());
                movePuzzleByGes(dir);
                return false;
            }
        });

        Bitmap original = ((BitmapDrawable) getResources().getDrawable(R.drawable.pic)).getBitmap();
        /*每一个小方块的宽和高*/
        int width = original.getWidth() / 5;
        int squareWidth = (int) (getWindowManager().getDefaultDisplay().getHeight() / 3.5);/*固定小方块的宽*/
       /*初始化游戏的若干个小方块*/
        for (int i = 0; i < iv_game_arr.length; i++) {
            for (int j = 0; j < iv_game_arr[0].length; j++) {
                /*切割图片*/
                Bitmap bitmap = Bitmap.createBitmap(original, j * width, i * width, width, width);
                iv_game_arr[i][j] = new ImageView(this);
                /*设置每一个小方块的图案*/
                iv_game_arr[i][j].setImageBitmap(bitmap);
                /*设置小方块固定的宽*/
                iv_game_arr[i][j].setLayoutParams(new RelativeLayout.LayoutParams(squareWidth, squareWidth));
                /*设置方块间的间距*/
                iv_game_arr[i][j].setPadding(2, 2, 2, 2);
                /*设置方块数据*/
                iv_game_arr[i][j].setTag(new GameData(bitmap, i, j));

                iv_game_arr[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean flag = isBeside((ImageView) view);
                        if (flag) {
                            animationAndChangeData((ImageView) view);
                        }
                    }
                });
            }
        }

        /*初始化游戏的主界面，并添加若干个小方块*/
        gl_main_game = (GridLayout) findViewById(R.id.gl_main_game);
        for (int i = 0; i < iv_game_arr.length; i++) {
            for (int j = 0; j < iv_game_arr[0].length; j++) {
                gl_main_game.addView(iv_game_arr[i][j]);
            }
        }

        /*设置最后一个方块是空的*/
        setNullImageView(iv_game_arr[2][4]);

        /*打乱顺序*/
        randomMove();
    }

    /*设置某个方块为空*/
    public void setNullImageView(ImageView mImageView) {
        mImageView.setImageBitmap(null);
        iv_null = mImageView;
    }

    /*判断点击方块是否在空方块旁边*/
    public boolean isBeside(ImageView mImageView) {
        GameData mNullGameData = (GameData) iv_null.getTag();
        GameData mGameData = (GameData) mImageView.getTag();
        /*分别判断点击方块是否在空方块的上下左右方*/
        if ((mGameData.y == mNullGameData.y) && ((mNullGameData.x - mGameData.x) == 1)) {
            return true;
        } else if ((mGameData.y == mNullGameData.y) && ((mGameData.x - mNullGameData.x) == 1)) {
            return true;
        } else if (((mNullGameData.y - mGameData.y) == 1) && (mNullGameData.x == mGameData.x)) {
            return true;
        } else if (((mGameData.y - mNullGameData.y) == 1) && (mNullGameData.x == mGameData.x)) {
            return true;
        }
        return false;
    }

    public void animationAndChangeData(final ImageView mImageView) {
        animationAndChangeData(mImageView, true);
    }


    /*创建交换动画，并在动画结束后交换两个方块的数据*/
    public void animationAndChangeData(final ImageView mImageView, boolean animated) {
        if (isMoving) {
            return;
        }
        if (!animated) {
            GameData mGameData = (GameData) mImageView.getTag();
            iv_null.setImageBitmap(mGameData.bitmap);
            GameData mNullGameData = (GameData) iv_null.getTag();
            mNullGameData.bitmap = mGameData.bitmap;
            mNullGameData.p_x = mGameData.p_x;
            mNullGameData.p_y = mGameData.p_y;
                /*设置点击方块为空*/
            setNullImageView(mImageView);
            return;
        }



        /*设置动画*/
        TranslateAnimation translateAnimation = null;
        if (mImageView.getX() > iv_null.getX()) {
            /*0.1f约等于0
            * 往左移动*/
            translateAnimation = new TranslateAnimation(0.1f, -mImageView.getWidth(), 0.1f, 0.1f);
        } else if (mImageView.getX() < iv_null.getX()) {
            /*往右移动*/
            translateAnimation = new TranslateAnimation(0.1f, mImageView.getWidth(), 0.1f, 0.1f);
        } else if (mImageView.getY() > iv_null.getY()) {
            /*往上移动*/
            translateAnimation = new TranslateAnimation(0.1f, 0.1f, 0.1f, -mImageView.getWidth());
        } else if (mImageView.getY() < iv_null.getY()) {
            /*往下移动*/
            translateAnimation = new TranslateAnimation(0.1f, 0.1f, 0.1f, mImageView.getWidth());
        }

        /*设置动画时长*/
        translateAnimation.setDuration(100);
        /*设置动画结束后停留*/
        translateAnimation.setFillAfter(true);
        /*设置动画结束后交换数据*/
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isMoving = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isMoving = false;
                mImageView.clearAnimation();
                GameData mGameData = (GameData) mImageView.getTag();
                iv_null.setImageBitmap(mGameData.bitmap);
                GameData mNullGameData = (GameData) iv_null.getTag();
                mNullGameData.bitmap = mGameData.bitmap;
                mNullGameData.p_x = mGameData.p_x;
                mNullGameData.p_y = mGameData.p_y;
                /*设置点击方块为空*/
                setNullImageView(mImageView);
                isGameOver();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        /*执行动画*/
        mImageView.startAnimation(translateAnimation);
    }

    /*手势判断，是向左右滑还是上下滑
    * 返回值类型为int
    * 返回1：上
    * 返回2：下
    * 返回3：左
    * 返回4：右
    * */
    public int getDirByGes(float start_x, float start_y, float end_x, float end_y) {
        boolean isHorizontal = Math.abs(start_x - end_x) > Math.abs(start_y - end_y);
        if (isHorizontal) {
            boolean isLeft = start_x - end_x > 0;
            if (isLeft) {
                return 3;
            } else {
                return 4;
            }
        } else {
            boolean isUp = start_y - end_y > 0;
            if (isUp) {
                return 1;
            } else {
                return 2;
            }
        }
    }


    /*下面两个方法，内容是一样的
    * 一个参数的方法主要给用户操作调用，默认有动画
    * 两个参数的方法主要给系统打乱拼图调用，可以选择没有动画*/
    public void movePuzzleByGes(int dir) {
        movePuzzleByGes(dir, true);
    }

    /*手势判断，是向左右滑还是上下滑
    * 返回值类型为int
    * 返回1：上
    * 返回2：下
    * 返回3：左
    * 返回4：右
    * */
    public void movePuzzleByGes(int dir, boolean animated) {
        /*获取当前空方块的位置*/
        GameData mNullGameData = (GameData) iv_null.getTag();

        int new_x = mNullGameData.x;
        int new_y = mNullGameData.y;

        if (dir == 1) {
            new_x++;
        } else if (dir == 2) {
            new_x--;
        } else if (dir == 3) {
            new_y++;
        } else if (dir == 4) {
            new_y--;
        }
        /*判断这个新坐标是否存在*/
        if (new_x >= 0 && new_x < iv_game_arr.length && new_y >= 0 && new_y < iv_game_arr[0].length) {
            if (animated) {
                animationAndChangeData(iv_game_arr[new_x][new_y]);
            } else {
                animationAndChangeData(iv_game_arr[new_x][new_y], animated);
            }
        }
    }

    /*随机打乱顺序*/
    public void randomMove() {
        /*打乱的次数*/
        for (int i = 0; i < 1000; i++) {
            /*根据手势开始交换，无动画*/
            int dir = (int) (Math.random() * 4) + 1;
            movePuzzleByGes(dir, false);
        }
    }

    /*判断游戏结束的方法*/
    public void isGameOver() {
        boolean isGameOver = true;
        /*遍历每个小方块*/
        for (int i = 0; i < iv_game_arr.length; i++) {
            for (int j = 0; j < iv_game_arr[0].length; j++) {
                /*如果当前的方块为空，则跳过，继续判断下一个方块*/
                if (iv_game_arr[i][j] == iv_null) {
                    continue;
                }
                GameData mGameData = (GameData) iv_game_arr[i][j].getTag();
                if (!mGameData.isTrue()) {
                    isGameOver = false;
                    break;
                }
            }
        }
        if (isGameOver) {
            Toast.makeText(this, "你赢了", Toast.LENGTH_LONG).show();
        }
    }


}
