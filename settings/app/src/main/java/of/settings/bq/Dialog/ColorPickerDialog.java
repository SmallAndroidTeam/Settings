package of.settings.bq.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;

public class ColorPickerDialog extends Dialog {
    Context context;
    private String title;//标题
    private int mInitialColor;//初始颜色
    private OnColorChangedListener mListener;

    /**
     * 初始颜色黑色
     * @param context
     * @param title 对话框标题
     * @param listener 回调
     */
    public ColorPickerDialog(Context context, String title,
                             OnColorChangedListener listener) {
        this(context, Color.BLACK, title, listener);
    }

    /**
     *
     * @param context
     * @param initialColor 初始颜色
     * @param title 标题
     * @param listener 回调
     */
    public ColorPickerDialog(Context context, int initialColor,
                             String title, OnColorChangedListener listener) {
        super(context);
        this.context = context;
        mListener = listener;
        mInitialColor = initialColor;
        this.title = title;
    }










    /**
     * 回调接口
     * @author <a href="clarkamx@gmail.com">LynK</a>
     *
     * Create on 2012-1-6 上午8:21:05
     *
     */
    public interface OnColorChangedListener {
        /**
         * 回调函数
         * @param color 选中的颜色
         */
        void colorChanged(int color);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getmInitialColor() {
        return mInitialColor;
    }

    public void setmInitialColor(int mInitialColor) {
        this.mInitialColor = mInitialColor;
    }

    public OnColorChangedListener getmListener() {
        return mListener;
    }

    public void setmListener(OnColorChangedListener mListener) {
        this.mListener = mListener;
    }
}





