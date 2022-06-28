package com.goertek.rox2.ui.main.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.goertek.rox2.ui.main.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class CustomNumberPicker extends View {

    public static final int SMALL_FONT_COLOR = 0x2f000000;
    public static final int SMALL_FONT_SIZE = 39;
    public static final int FONT_COLOR = 0x4f000000;
    public static final int FONT_SIZE = 45;
    public static final int SELECT_FONT_COLOR = 0xFF2E94F9;
    public static final int SELECT_FONT_SIZE = 60;
    public static final int TIP_FONT_COLOR = 0xFF000000;
    public static final int TIP_FONT_SIZE = 30;
    public static final int PADDING = 30;
    public static final int SHOW_COUNT = 4;
    public static final int SELECT = 0;
    private int itemHeight;
    private int itemWidth;
    //需要显示的行数
    private int showCount = SHOW_COUNT;
    //当前默认选择的位置
    private int select = SELECT;
    //字体颜色、大小、补白
    private int smallfontColor = SMALL_FONT_COLOR;
    private int smallfontSize = SMALL_FONT_SIZE;
    private int fontColor = FONT_COLOR;
    private int fontSize = FONT_SIZE;
    private int selectfontColor = SELECT_FONT_COLOR;
    private int selectfontSize = SELECT_FONT_SIZE;
    private int tipfontColor = TIP_FONT_COLOR;
    private int tipfontSize = TIP_FONT_SIZE;
    private int padding = PADDING;
    private int marginLeft = 30;
    private int tipPadding = 0;

    //文本列表
    private List<String> lists;
    //选中项的辅助文本，可为空
    private String selectTip;
    //每一项Item和选中项
    private List<WheelItem> wheelItems = new ArrayList<>();
    private WheelSelect wheelSelect = null;
    //手点击的X坐标
    private float mTouchX;
    //监听器
    private OnWheelViewItemSelectListener listener;

    private float selectedLineHeight;
    private float selectedLinePaddingTop;
    private float scale;
    private int selectItemWidth;
    private int screenWidth;
    public CustomNumberPicker(Context context) {
        super(context);
        scale = context.getResources().getDisplayMetrics().density;
    }

    public CustomNumberPicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        scale = context.getResources().getDisplayMetrics().density;
    }

    public CustomNumberPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        scale = context.getResources().getDisplayMetrics().density;
    }

    /**
     * 设置字体的颜色，不设置的话默认为黑色
     * @param fontColor
     * @return
     */
    public CustomNumberPicker smallfontColor(int fontColor){
        this.smallfontColor = fontColor;
        return this;
    }

    public CustomNumberPicker fontColor(int fontColor){
        this.fontColor = fontColor;
        return this;
    }

    public CustomNumberPicker selectfontColor(int fontColor){
        this.selectfontColor = fontColor;
        return this;
    }

    public CustomNumberPicker tipfontColor(int fontColor){
        this.tipfontColor = fontColor;
        return this;
    }

    /**
     * 设置字体的大小，不设置的话默认为30
     * @param fontSize
     * @return
     */
    public CustomNumberPicker smallfontSize(int fontSize){
        this.smallfontSize = (int)(fontSize * scale + 0.5f);
        return this;
    }

    public CustomNumberPicker fontSize(int fontSize){
        this.fontSize = (int)(fontSize * scale + 0.5f);
        return this;
    }

    public CustomNumberPicker selectfontSize(int fontSize){
        this.selectfontSize = (int)(fontSize * scale + 0.5f);
        return this;
    }

    public CustomNumberPicker tipfontSize(int fontSize){
        this.tipfontSize = (int)(fontSize * scale + 0.5f);
        return this;
    }

    /**
     * 设置文本到上下两边的补白，不合适的话默认为10
     * @param padding
     * @return
     */
    public CustomNumberPicker padding(int padding){
        this.padding = (int)(padding * scale + 0.5f);
        return this;
    }

    public CustomNumberPicker setTipPadding(int padding){
        this.tipPadding = (int)(padding * scale + 0.5f);
        return this;
    }

    /**
     * 设置选中项的复制文本，可以不设置
     * @param selectTip
     * @return
     */
    public CustomNumberPicker selectTip(String selectTip){
        this.selectTip = selectTip;
        return this;
    }

    /**
     * 设置文本列表，必须且必须在build方法之前设置
     * @param lists
     * @return
     */
    public CustomNumberPicker lists(List<String> lists){
        this.lists = lists;
        return this;
    }

    /**
     * 设置显示行数，不设置的话默认为3
     * @param showCount
     * @return
     */
    public CustomNumberPicker showCount(int showCount){
        if(showCount % 2 == 0){
            throw new IllegalStateException("the showCount must be odd");
        }
        this.showCount = showCount;
        return this;
    }

    /**
     * 设置默认选中的文本的索引，不设置默认为0
     * @param select
     * @return
     */
    public CustomNumberPicker select(int select){
        this.select = select;
        return this;
    }

    /**
     * 最后调用的方法，判断是否有必要函数没有被调用
     * @return
     */
    public CustomNumberPicker build(){
        if(lists == null){
            throw new IllegalStateException("this method must invoke after the method [lists]");
        }
        return this;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //得到总体宽度
        //总体宽度、高度、Item的高度
        screenWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        // 得到每一个Item的高度
        Paint mSelectPaint = new Paint();
        mSelectPaint.setTextSize(selectfontSize);
        Paint.FontMetrics metrics1 =  mSelectPaint.getFontMetrics();
        selectItemWidth = (int)mSelectPaint.measureText("1975")+2*marginLeft;

        Paint mPaint = new Paint();
        mPaint.setTextSize(fontSize);
        Paint.FontMetrics metrics =  mPaint.getFontMetrics();
        itemHeight = (int) (metrics.bottom - metrics.top) + 2 * padding;
        itemWidth = (int)mPaint.measureText("1975")+2*marginLeft;
        //初始化每一个WheelItem
        initWheelItems(itemWidth, itemHeight);
        //初始化WheelSelect

        wheelSelect = new WheelSelect(showCount / 2 * itemWidth, itemWidth, itemHeight, selectTip, tipfontColor, tipfontSize, padding, tipPadding);
        //得到所有的高度
        LogUtils.d("screenWidth="+screenWidth);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 创建显示个数+2个WheelItem
     * @param width
     * @param itemHeight
     */
    private void initWheelItems(int width, int itemHeight) {
        wheelItems.clear();
        int centerX = screenWidth/2;
        for (int i = 0; i < showCount + 1; i++) {
            int startX;
            if (i<=(showCount+1)/2){
                startX = centerX - ((showCount+1)/2-i)*itemWidth - selectItemWidth/2;
            }else {
                startX = centerX +(i-(showCount+1)/2-1)*itemWidth + selectItemWidth/2;
            }
            int stringIndex = select - showCount / 2 - 1 + i;
            if (lists!=null){
                if (stringIndex < 0) {
                    stringIndex = lists.size() + stringIndex;
                }
                if (stringIndex > lists.size() - 1) {
                    stringIndex = stringIndex - lists.size();
                }
                if (stringIndex < lists.size()) {
                    wheelItems.add(new WheelItem(startX, width, itemHeight, smallfontColor, smallfontSize, fontColor, fontSize, selectfontColor, selectfontSize, lists.get(stringIndex)));
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mTouchX = event.getX();
                return true;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - mTouchX;
                mTouchX = event.getX();
                handleMove(dx);
                LogUtils.d("dx="+dx);
                break;
            case MotionEvent.ACTION_UP:
                LogUtils.d("action up");
                handleUp();
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 处理移动操作
     * @param dx
     */
    private void handleMove(float dx) {
        //调整坐标
        for(WheelItem item : wheelItems){
            item.adjust(dx);
        }
        invalidate();
        //调整
        adjust();
    }

    /**
     * 处理抬起操作
     */
    private void handleUp(){
        int index = -1;
        LogUtils.d("wheelItems.get(0).getStartX()="+wheelItems.get(0).getStartX());
        //得到应该选择的那一项
        for(int i = 0; i < wheelItems.size(); i++){
            WheelItem item = wheelItems.get(i);
            //如果startX在selectItem的中点左边，则将该项作为选择项
            LogUtils.d("item.getStartX()="+item.getStartX()+";wheelSelect.getStartX()="+wheelSelect.getStartX()+";itemWidth="+itemWidth/2);
            if(item.getStartX() > wheelSelect.getStartX()- itemWidth/2  && item.getStartX() < (wheelSelect.getStartX() + itemWidth / 2)){
                index = i;
                LogUtils.d("index="+index);
                break;
            }
        }
        //如果没找到或者其他因素，直接返回
        if(index == -1){
            return;
        }
        //得到偏移的位移
        float dx = wheelSelect.getStartX() - wheelItems.get(index).getStartX();
        //调整坐标
        for(WheelItem item : wheelItems){
            item.adjust(dx);
        }
        invalidate();
        // 调整
        adjust();
        //设置选择项
        int stringIndex = lists.indexOf(wheelItems.get(index).getText());
        if(stringIndex != -1){
            select = stringIndex;
            if(listener != null){
                listener.onItemSelect(select);
            }
        }
    }

    /**
     * 调整Item移动和循环显示
     */
    private void adjust(){
        //如果向右滑动超出半个Item的宽度，则调整容器
        if(wheelItems.get(0).getStartX() >= itemWidth /2){
            //移除最后一个Item重用
            WheelItem item = wheelItems.remove(wheelItems.size() - 1);
            //设置起点X坐标
            item.setStartX(wheelItems.get(0).getStartX()- itemWidth );//
            //得到文本在容器中的索引
            int index = lists.indexOf(wheelItems.get(0).getText());
            if(index == -1){
                return;
            }
            index -= 1;
            if(index < 0){
                index = lists.size() + index;
            }
            //设置文本
            item.setText(lists.get(index));
            //添加到最开始
            wheelItems.add(0, item);
            WheelItem item1 = wheelItems.get((showCount+1)/2+1);
            item1.adjust(70);
            invalidate();
            return;
        }
        //如果向上滑超出半个Item的高度，则调整容器
        if(wheelItems.get(0).getStartX() <= (-itemWidth / 2 - itemHeight)){
            //移除第一个Item重用
            WheelItem item = wheelItems.remove(0);
            //设置起点Y坐标
            item.setStartX(wheelItems.get(wheelItems.size() - 1).getStartX() + itemWidth);
            //得到文本在容器中的索引
            int index = lists.indexOf(wheelItems.get(wheelItems.size() - 1).getText());
            if(index == -1){
                return;
            }
            index += 1;
            if(index >= lists.size()){
                index = 0;
            }
            //设置文本
            item.setText(lists.get(index));
            //添加到最后面
            wheelItems.add(item);
            invalidate();
            return;
        }
    }

    /**
     * 得到当前的选择项
     */
    public int getSelectItem(){
        return select;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawColor(Color.WHITE);

        //绘制每一项Item
        for (int i = 0; i < wheelItems.size(); i++) {
            int middle = (showCount + 1) / 2;
            if (i == middle ) {
                wheelItems.get(i).onDraw(canvas, 0);
            } else {
                wheelItems.get(i).onDraw(canvas, 1);
            }
        }

        //绘制阴影
        if(wheelSelect != null){
            wheelSelect.onDraw(canvas);
        }
    }

    /**
     * 设置监听器
     * @param listener
     * @return
     */
    public CustomNumberPicker listener(OnWheelViewItemSelectListener listener){
        this.listener = listener;
        return this;
    }

    public interface OnWheelViewItemSelectListener{
        void onItemSelect(int index);
    }

    public class WheelItem {
        // 起点Y坐标、宽度、高度
        private float startX;
        private int width;
        private int height;
        //四点坐标
        private RectF rect = new RectF();
        //字体大小、颜色
        private int smallfontColor;
        private int smallfontSize;
        private int fontColor;
        private int fontSize;
        private int selectfontColor;
        private int selectfontSize;
        private String text;
        private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public WheelItem(float startX, int width, int height, int smallfontColor, int smallfontSize, int fontColor, int fontSize, int selectfontColor, int selectfontSize, String text) {
            this.startX = startX;
            this.width = width;
            this.height = height;
            this.smallfontColor = smallfontColor;
            this.smallfontSize = smallfontSize;
            this.fontColor = fontColor;
            this.fontSize = fontSize;
            this.selectfontColor = selectfontColor;
            this.selectfontSize = selectfontSize;
            this.text = text;
            adjust(0);
        }

        /**
         * 根据Y坐标的变化值，调整四点坐标值
         * @param dx
         */
        public void adjust(float dx){
            startX += dx;
            rect.left = startX;
            rect.top = 0;
            rect.right = width+startX;
            rect.bottom = height;
        }

        public float getStartX() {
            return startX;
        }

        /**
         * 直接设置Y坐标属性，调整四点坐标属性
         * @param startX
         */
        public void setStartX(float startX) {
            this.startX = startX;
            rect.left = startX;
            rect.top = 0;
            rect.right = width+startX;
            rect.bottom =  height;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void onDraw(Canvas mCanvas, int itemIndex){
            //设置钢笔属性
            if (itemIndex == 0) {
                mPaint.setTextSize(selectfontSize);
                mPaint.setColor(selectfontColor);
            } else if (itemIndex == 1) {
                mPaint.setTextSize(fontSize);
                mPaint.setColor(fontColor);
            } else {
                mPaint.setTextSize(smallfontSize);
                mPaint.setColor(smallfontColor);
            }
            //得到字体的宽度
            int textWidth = (int)mPaint.measureText(text);
            //drawText的绘制起点是左下角,y轴起点为baseLine
            Paint.FontMetrics metrics =  mPaint.getFontMetrics();
            int baseLine = (int)(rect.centerY() + (metrics.bottom - metrics.top) / 2 - metrics.bottom);
            //居中绘制
            LogUtils.d("rect.left+padding="+rect.left+";baseLine = "+baseLine);
            mCanvas.drawText(text, rect.left+padding, baseLine, mPaint);
        }
    }

    public class WheelSelect {
        //黑框背景颜色
        public static final int COLOR_BACKGROUND = 0x10000000;
        //黑框的Y坐标起点、宽度、高度
        private int startX;
        //四点坐标
        private Rect rect = new Rect();
        //需要选择文本的颜色、大小、补白
        private String selectText;
        private int fontColor;
        private int fontSize;
        private int padding;
        private int tippadding;
        private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public WheelSelect(int startX, int width, int height, String selectText, int fontColor, int fontSize, int padding, int tippadding) {
            this.startX = startX;
            this.selectText = selectText;
            this.fontColor = fontColor;
            this.fontSize = fontSize;
            this.padding = padding;
            this.tippadding = tippadding;
            rect.left = startX;
            rect.top = 0;
            rect.right = width+startX;
            rect.bottom =  height;
        }

        public int getStartX() {
            return startX;
        }

        public void setStartX(int startY) {
            this.startX = startX;
        }

        public void onDraw(Canvas mCanvas) {
            //绘制背景
            mPaint.setStyle(Paint.Style.FILL);
//            mPaint.setColor(COLOR_BACKGROUND);
//            mCanvas.drawRect(rect, mPaint);
            //绘制提醒文字
            int baseLine = 0;
            if(selectText != null){
                //设置钢笔属性
                mPaint.setTextSize(fontSize);
                mPaint.setColor(fontColor);
                //得到字体的宽度
                int selectedLine = Integer.parseInt(selectText)%5;
                if (selectedLine==0){
                    selectedLineHeight = 64*scale+0.5f;
                    selectedLinePaddingTop = 12*scale+0.5f;
                }else {
                    selectedLineHeight = 36*scale+0.5f;
                    selectedLinePaddingTop = 26*scale+0.5f;
                }
                int textWidth = (int)mPaint.measureText(selectText);
                //drawText的绘制起点是左下角,y轴起点为baseLine
                Paint.FontMetrics metrics =  mPaint.getFontMetrics();
                baseLine = (int)(rect.centerY() + (metrics.bottom - metrics.top) / 2 - metrics.bottom + 5);
                //在靠右边绘制文本
                mCanvas.drawText(selectText, rect.centerX() + padding + textWidth + tippadding, baseLine, mPaint);
            }
            drawLines(startX,baseLine+selectedLinePaddingTop,baseLine+selectedLinePaddingTop+selectedLineHeight,mCanvas,mPaint);
        }
    }

    private void drawLines(int xPosition, float startY, float endY, Canvas canvas, Paint paint){
        canvas.drawLine(xPosition,startY,xPosition,endY,paint);

    }
}
