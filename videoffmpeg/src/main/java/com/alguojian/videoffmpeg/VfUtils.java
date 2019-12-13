package com.alguojian.videoffmpeg;

import android.util.DisplayMetrics;

public class VfUtils {

    /**
     * 获得屏幕宽度
     */
    public static int getDeviceWidth() {
        return getDisplayMetrics().widthPixels;
    }

    public static DisplayMetrics getDisplayMetrics() {
        return VfApp.getMContext().getResources().getDisplayMetrics();
    }


    public static int dpToPx(float dp) {
        return (int) (dp * getDisplayMetrics().density + 0.5f);
    }

    /**
     * second to HH:MM:ss
     */
    public static String convertSecondsToTime(long seconds) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (seconds <= 0)
            return "00:00";
        else {
            minute = (int) seconds / 60;
            if (minute < 60) {
                second = (int) seconds % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = (int) (seconds - hour * 3600 - minute * 60);
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    private static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }
}
