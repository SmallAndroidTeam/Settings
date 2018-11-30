package of.settings.bq.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import of.settings.bq.R;
import of.settings.bq.widget.KBubbleSeekBar;

public class BrightnessFragment extends Fragment implements KBubbleSeekBar.OnProgressChangedListener {
    private TextView tx;
    private KBubbleSeekBar seekbar;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_brightness, container, false);
        initView(view);
        initListener();
        return view;
    }

    public void initView(View view){
        tx=view.findViewById(R.id.value);
        seekbar=view.findViewById(R.id.seekbar);
    }

    public void initListener(){
        seekbar.setOnProgressChangedListener(this);
    }

    @Override
    public void onProgressChanged(KBubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
        tx.setText("" + progress);
    }

    @Override
    public void getProgressOnActionUp(KBubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

    }

    @Override
    public void getProgressOnFinally(KBubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

    }
}
