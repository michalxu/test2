package com.goertek.db.port;

/**
 * 创建时间：2021/7/6
 *
 * @author michal.xu
 */
public class RoxLitePal {

    private static RoxLitePal roxLitePal;
    public RoxLitePalAdd roxLitePalAdd;
    public RoxLitePalCheck roxLitePalCheck;
    public RoxLitePalModify roxLitePalModify;
    public RoxLitePalDelete roxLitePalDelete;
    public RoxLitePal(){
        roxLitePalAdd = new RoxLitePalAdd();
        roxLitePalCheck = new RoxLitePalCheck();
        roxLitePalModify = new RoxLitePalModify();
        roxLitePalDelete = new RoxLitePalDelete();

    }
    public static RoxLitePal getInstance(){
        if (roxLitePal ==null){
            synchronized (RoxLitePal.class){
                if (roxLitePal == null){
                    roxLitePal = new RoxLitePal();
                }
            }
        }
        return roxLitePal;
    }
}
