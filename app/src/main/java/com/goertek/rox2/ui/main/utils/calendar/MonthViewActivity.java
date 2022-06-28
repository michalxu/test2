package com.goertek.rox2.ui.main.utils.calendar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.goertek.rox2.R;
import com.goertek.rox2.ui.main.LogUtils;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class MonthViewActivity extends AppCompatActivity {

    MonthView monthView;
    TextView tvYearMonth;
    int year;
    int month;
    private float downXPisition = 0f;
    private float upXPisition = 0f;
    private boolean isRight = false;//判断用户是向左还是向右滑动屏幕
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_view);
//        setTitleBarTitle(getClass().getSimpleName().replace("Activity", ""));

        initView();
    }

    private void initView() {
        monthView = findViewById(R.id.month_view);
//        tvYearMonth = findViewById(R.id.tv_year_month);
        monthView.setOnDayClickListener(new MonthView.OnDayClickListener() {
            @Override
            public void onDayClick(DayView dayView) {

            }
        });
        monthView.setOnDayLongClickListener(new MonthView.OnDayLongClickListener() {
            @Override
            public boolean onDayLongClick(DayView dayView) {
                return false;
            }
        });
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        tvYearMonth.setText(year + "-" + month);
        monthView.showSubLabel(true);
        initMonthView(year, month);
    }

    private void initMonthView(int year, int month){
        List<DayItem> dayItems = MonthUtils.getMonthDays(year, month);
        for (int i = 0; i < dayItems.size(); i++) {
            boolean enable = new Random().nextBoolean();
            DayItem item = dayItems.get(i);
            switch (item.getKey()){
                case -1://上个月的最后几天
                case 1://下个月的前几天
//                    item.setBackground(R.drawable.circle_gray_light_shape);
                    break;
                case 0://本月的所有天数
//                    item.setBackground(R.drawable.circle_theme_light_shape);
                    break;
            }
            if (i>15){
                item.setHasData(true);
            }
//            item.setSubLabel("可约");//enable ? "可约" : "不可约"
//            item.setSubLabelTextColor(Color.BLACK);//enable ? Color.YELLOW : 0xFF666666
        }
        monthView.setDays(dayItems);
    }

    public void onWidgetClick(View view){
        switch (view.getId()){
//            case R.id.btn_pre:
//                month --;
//                if (month <= 0){
//                    month = 12;
//                    year --;
//                }
//                break;
//            case R.id.btn_next:
//                month ++;
//                if (month > 12){
//                    month = 1;
//                    year ++;
//                }
//                break;
        }
        tvYearMonth.setText(year + "-" + month);
        initMonthView(year, month);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downXPisition = event.getX();
                upXPisition = downXPisition;
                LogUtils.d("down");
                break;
            case MotionEvent.ACTION_UP:
                upXPisition = event.getX();
                LogUtils.d("up");
                //断定是用户滑动屏幕，切换
                if ((upXPisition-downXPisition)>10){
                    month --;
                    if (month <= 0){
                        month = 12;
                        year --;
                    }
                    tvYearMonth.setText(year + "-" + month);
                    initMonthView(year, month);
                }else if ((upXPisition-downXPisition)<-10){
                    month ++;
                    if (month > 12){
                        month = 1;
                        year ++;
                    }
                    tvYearMonth.setText(year + "-" + month);
                    initMonthView(year, month);
                }
                break;
            default:
                break;
        }
        return false;
    }
}