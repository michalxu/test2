package com.goertek.bluetooth.protocol.tws.entity;
/**
 * 文件名：TWSBattery
 * 描述：电量
 * 创建时间：2020/9/2
 * @author jochen.zhang
 */
public class TWSBattery extends TWSBaseEntity {
    private int box;
    private int left;
    private int right;

    public int getBox() {
        return box;
    }

    public void setBox(int box) {
        this.box = box;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }
}
