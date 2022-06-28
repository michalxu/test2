package com.goertek.rox2.ui.main.utils.calendar;

/**
 * 创建时间：2021/7/7
 *
 * @author michal.xu
 */
public class DayItem {
    private int key;

    private int day;
    private int background;

    private String label;
    private int labelTextColor;
    private float labelTextSize;

    private String subLabel;
    private int subLabelTextColor;
    private float subLabelTextSize;

    private long date;

    private boolean hasData = false; //判断当天是否有数据，是否画红点

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    private String familyName="";

    public DayItem() {
        setBackground(-1);
        setLabelTextColor(0xFF364356);
        setLabelTextSize(16);
        setSubLabelTextColor(0xFF333333);
        setSubLabelTextSize(8);
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getLabelTextColor() {
        return labelTextColor;
    }

    public void setLabelTextColor(int labelTextColor) {
        this.labelTextColor = labelTextColor;
    }

    public float getLabelTextSize() {
        return labelTextSize;
    }

    /**
     * 单位是sp
     *
     * @param labelTextSize the label text size
     */
    public void setLabelTextSize(float labelTextSize) {
        this.labelTextSize = labelTextSize;
    }

    public String getSubLabel() {
        return subLabel;
    }

    public void setSubLabel(String subLabel) {
        this.subLabel = subLabel;
    }

    public int getSubLabelTextColor() {
        return subLabelTextColor;
    }

    public void setSubLabelTextColor(int subLabelTextColor) {
        this.subLabelTextColor = subLabelTextColor;
    }

    public float getSubLabelTextSize() {
        return subLabelTextSize;
    }

    /**
     * 单位是sp
     *
     * @param subLabelTextSize the sub label text size
     */
    public void setSubLabelTextSize(float subLabelTextSize) {
        this.subLabelTextSize = subLabelTextSize;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setHasData(boolean hasData) {
        this.hasData = hasData;
    }

    public boolean isHasData() {
        return hasData;
    }
    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getTypeface() {
        return familyName;
    }
}

