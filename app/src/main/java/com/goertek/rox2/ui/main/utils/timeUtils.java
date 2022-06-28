package com.goertek.rox2.ui.main.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 创建时间：2021/7/20
 *  时间戳转换工具
 * @author michal.xu
 */
public class timeUtils {
    /**
     * 时间戳转换成日期格式字符串
     * @param seconds 精确到秒的字符串
     * @param format 需要转换的格式
     * @return
     */
    public static String timeStampToDate(long seconds,String format) {

        if(format == null || format.isEmpty()) format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds)));
    }
    /**
     * 日期格式字符串转换成时间戳
     * @param date 字符串日期
     * @param format 如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String date2TimeStamp(String date,String format){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return String.valueOf(sdf.parse(date).getTime()/1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    //获取某个小时整时对应的时间戳,输入的年月日是日历对应的值，月份需要比实际月份小1
    public static long getTimeStampForHour(int year,int month,int day,int hour){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,month,day);
        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        long mTime = calendar.getTimeInMillis();
        mTime = mTime/1000*1000;
        return  mTime;
    }

    /**
     * 取得当前时间戳（精确到秒）
     * @return
     */
    public static String timeStamp(){
        long time = System.currentTimeMillis();
        String t = String.valueOf(time/1000);
        return t;
    }

    /**
     * 获取当月的 天数
     */
    public static int getCurrentMonthDay()
    {
        Calendar a = Calendar.getInstance();
        a.set(Calendar.DATE, 1);
        a.roll(Calendar.DATE, -1);
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }

    /**
     * 根据 年、月 获取对应的月份 的 天数
     */
    public static int getDaysByYearMonth(int year, int month)
    {
        Calendar a = Calendar.getInstance();
        a.set(Calendar.YEAR, year);
        a.set(Calendar.MONTH, month);
        a.set(Calendar.DATE, 1);
        a.roll(Calendar.DATE, -1);
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }

    /**
     * 根据日期 找到对应日期的 星期几
     *
     * @param date 比如传参：2018-07-13 将返回“周五”
     */
    public static String getDayOfWeekByDate(String date)
    {
        String dayOfweek = "-1";
        try
        {
            SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
            Date myDate = myFormatter.parse(date);
            SimpleDateFormat formatter = new SimpleDateFormat("E");
            String str = formatter.format(myDate);
            dayOfweek = str;
        }
        catch (Exception e)
        {
            System.out.println("错误!");
        }
        return dayOfweek;
    }
    /**
     * 根据当前日期获得是星期几
     * time=yyyy-MM-dd
     * @return
     */
    public static String getWeek(String time) {
        String Week = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(format.parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int wek=c.get(Calendar.DAY_OF_WEEK);

        if (wek == 1) {
            Week += "星期日";
        }
        if (wek == 2) {
            Week += "星期一";
        }
        if (wek == 3) {
            Week += "星期二";
        }
        if (wek == 4) {
            Week += "星期三";
        }
        if (wek == 5) {
            Week += "星期四";
        }
        if (wek == 6) {
            Week += "星期五";
        }
        if (wek == 7) {
            Week += "星期六";
        }
        return Week;
    }

    public static String getTimeForMs(){
        Calendar Cld = Calendar.getInstance();
        int YY = Cld.get(Calendar.YEAR) ;
        int MM = Cld.get(Calendar.MONTH)+1;
        int DD = Cld.get(Calendar.DAY_OF_MONTH);
        int HH = Cld.get(Calendar.HOUR_OF_DAY);
        int mm = Cld.get(Calendar.MINUTE);
        int SS = Cld.get(Calendar.SECOND);
        int MI = Cld.get(Calendar.MILLISECOND);//毫秒
        return YY+"-"+MM+"-"+DD+"  "+HH+":"+mm+":"+SS+"."+MI;
    }
}
