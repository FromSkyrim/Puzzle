package com.fatty.puzzle;

import android.graphics.Bitmap;

/**
 * Created by 17255 on 2016/8/2.
 */
public class GameData {

    /**/
    public int x;
    /**/
    public int y;
    /*小方块所绑定的图像bitmap*/
    public Bitmap bitmap;
    /**/
    public int p_x;
    /**/
    public int p_y;

    public GameData(Bitmap bitmap, int x, int y) {
        this.bitmap = bitmap;
        this.x = x;
        this.y = y;
    }

    /*判断方块是否在正确的位置
    * true:位置正确  false:位置不正确*/
    public boolean isTrue() {
        if (x == p_x && y == p_y) {
            return true;
        }
        return false;
    }
}
