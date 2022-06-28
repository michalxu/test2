package com.goertek.rox2.ui.main.pop_dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.goertek.rox2.R;
import com.goertek.rox2.ui.main.LogUtils;
import com.goertek.rox2.ui.main.utils.calendar.DayItem;
import com.goertek.rox2.ui.main.utils.calendar.DayView;
import com.goertek.rox2.ui.main.utils.calendar.MonthUtils;
import com.goertek.rox2.ui.main.utils.calendar.MonthView;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * 创建时间：2021/7/8
 *
 * @author michal.xu
 */
public class BottomPopCalendarDialog extends Dialog {
    private static final String TAG = "BottomPopCalendarDialog";
    private static SetListener setListener;
    public static int selectedDay;
    public static int selectedMonth;
    private DayItem selectedDayItem;
    private int preMonthDays;
    private int nextMonthDays;
    MonthView monthView;
    public int year;
    public int isCurrentMonth;
    public int month;//
    public String showMonth[] = new String[]{"January","February","March","April","May","Jun","July","August","September","October","November","December"};
    public int day;
    private float downXPisition = 0f;
    private float upXPisition = 0f;
    private float downYPosition = 0f;
    private boolean isRight = false;//判断用户是向左还是向右滑动屏幕
    private EditText mEditText;
    private Button mButton;

    private SetListener listener;
    private ImageView canlendarPre;
    private ImageView canlendarNext;
    private TextView calendarTitleMonth;
    public BottomPopCalendarDialog(@NonNull Context context) {
        super(context, R.style.bottom_dialog);
        setContentView(R.layout.calendar_layout);

        Window window = getWindow();
        if (window != null) {
            // 设置弹出位置
            window.setGravity(Gravity.BOTTOM);
            // 宽度全屏
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            window.setWindowAnimations(R.style.bottom_dialog_out);
        }
        initView();

        setCanceled(true);
    }

    /**
     * 构造方法
     *
     * @param context Context
     * @return BottomPopCalendarDialog
     */
    public static BottomPopCalendarDialog with(Context context,int month, int day) {
        selectedDay = day;
        selectedMonth = month;
        return new BottomPopCalendarDialog(context);
    }

    /**
     * 设置是否可以取消
     *
     * @param cancel 是否可以取消
     * @return BottomPopCalendarDialog
     */
    public BottomPopCalendarDialog setCanceled(boolean cancel) {
        setCanceledOnTouchOutside(cancel);
        setCancelable(cancel);
        return this;
    }


    public void initView() {
        calendarTitleMonth = findViewById(R.id.calendar_title_month);
        canlendarPre = findViewById(R.id.calendar_pre);
        canlendarPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                month --;
                if (month <= 0){
                    month = 11;
                    year --;
                }
                calendarTitleMonth.setText(showMonth[month]+" "+year+"");
//                    tvYearMonth.setText(year + "-" + month);
                initMonthView(year, month);
            }
        });
        canlendarNext = findViewById(R.id.calendar_next);
        canlendarNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                month ++;
                if (month > 11){
                    month = 0;
                    year ++;
                }
//                    tvYearMonth.setText(year + "-" + month);
                calendarTitleMonth.setText(showMonth[month]+" "+year+"");
                initMonthView(year, month);
            }
        });
        monthView = findViewById(R.id.month_view);
        monthView.setOnDayLongClickListener(new MonthView.OnDayLongClickListener() {
            @Override
            public boolean onDayLongClick(DayView dayView) {
                return false;
            }
        });
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        LogUtils.d("month init = "+month);
        monthView.showSubLabel(false);
        calendarTitleMonth.setText(showMonth[month]+" "+year+"");
        initMonthView(year, month);
    }

    public void initMonthView(int year, int month){
        LogUtils.d("initMonthView = "+month);
        List<DayItem> dayItems = MonthUtils.getMonthDays(year, month);
        for (int i = 0; i < dayItems.size(); i++) {
            boolean enable = new Random().nextBoolean();
            DayItem item = dayItems.get(i);
            switch (item.getKey()){
                case -1://上个月的最后几天
                    preMonthDays = i+1;
                    item.setLabelTextColor(Color.parseColor("#80364356"));
                    break;
                case 1://下个月的前几天
//                    item.setBackground(R.drawable.circle_gray_light_shape);
                    item.setLabelTextColor(Color.parseColor("#80364356"));
                    break;
                case 0://本月的所有天数
                    nextMonthDays = dayItems.size()-i-1;
                    if (Integer.parseInt(item.getLabel())==selectedDay &&selectedMonth == month){
                        item.setBackground(R.drawable.calendar_selected_bg_blue);
                        item.setLabelTextColor(Color.WHITE);
                        selectedDayItem = item;
                        LogUtils.d("selectedItem");
                    }else {
                        item.setLabelTextColor(Color.parseColor("#FF364356"));
                        item.setBackground(R.drawable.gender_bg_white);
                    }

//                    item.setBackground(R.drawable.circle_theme_light_shape);
                    break;
            }
            item.setDay(i-preMonthDays+1);
//            item.setSubLabel("可约");//enable ? "可约" : "不可约"
//            item.setSubLabelTextColor(Color.BLACK);//enable ? Color.YELLOW : 0xFF666666
        }
        monthView.setDays(dayItems);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downXPisition = event.getX();
                upXPisition = downXPisition;
                downYPosition = event.getY();
                if (downYPosition<0){
                    setListener.onCancelClick();
                }
                break;
            case MotionEvent.ACTION_UP:
                upXPisition = event.getX();
                //断定是用户滑动屏幕，切换
                if ((upXPisition-downXPisition)>10){
                    month --;
                    if (month <= 0){
                        month = 12;
                        year --;
                    }
                    initMonthView(year, month);
                }else if ((upXPisition-downXPisition)<-10){
                    month ++;
                    if (month > 12){
                        month = 1;
                        year ++;
                    }
                    initMonthView(year, month);
                }
                break;
                case MotionEvent.ACTION_MOVE:
                    float tempYposition = event.getY();
                    if (downXPisition>400&&downXPisition<700&&downYPosition<100){
                        if (tempYposition-downYPosition>50){
                            setListener.onCancelClick();
                        }
                    }
            default:
                break;
        }
        return false;
    }
    public void setCancelClickListener(final SetListener listener){
        setListener = listener;
        monthView.setOnDayClickListener(new MonthView.OnDayClickListener() {

            @Override
            public void onDayClick(DayView dayView) {
                DayItem dayItem = dayView.getDayItem();
                selectedDayItem.setLabelTextColor(Color.parseColor("#FF364356"));
                selectedDayItem.setBackground(R.drawable.gender_bg_white);
                selectedDayItem = dayItem;
                dayItem.setBackground(R.drawable.calendar_selected_bg_blue);
                dayItem.setLabelTextColor(Color.WHITE);
                dayItem.setFamilyName("sans-serif-medium");
                selectedDay = Integer.parseInt(dayItem.getLabel());
                isCurrentMonth = dayItem.getKey();
                LogUtils.d("day = "+selectedDay+"isCurrentMonth = "+isCurrentMonth);
//                dayItem.setBackground(R.drawable.calendar_selected_bg_blue);
                listener.onDayClick();

            }
        });



    }

    public interface SetListener{
        public void onCancelClick();
        public void onDayClick();
    }
    public void setCurrentMonth(int month){
        selectedMonth = month;
    }
    public void setCurrentDay(int day){
        selectedDay = day;
    }
    public int getSelectedMonth() {
        return selectedMonth;
    }

    public void setSelectedMonth(int selectedMonth) {
        this.selectedMonth = selectedMonth;
    }

    public int getSelectedDay() {
        return selectedDay;
    }

    public void setSelectedDay(int selectedDay) {
        this.selectedDay = selectedDay;
    }
}
