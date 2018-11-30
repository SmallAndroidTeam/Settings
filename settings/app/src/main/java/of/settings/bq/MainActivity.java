package of.settings.bq;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import of.settings.bq.fragment.AmbientLightFragment;
import of.settings.bq.fragment.AromatherapyFragment;
import of.settings.bq.fragment.BluetoothFragment;
import of.settings.bq.fragment.BrightnessFragment;
import of.settings.bq.fragment.VoiceFragment;
import of.settings.bq.fragment.WiFiFragment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView aromatherapyTv;
    private TextView ambientlightTv;
    private TextView bluetoothTv;
    private TextView brightnessTv;
    private TextView voiceTv;
    private TextView wifiTv;
    private TextView returnTv;
    private RelativeLayout aromatherapyLayout;
    private RelativeLayout ambientlightLayout;
    private RelativeLayout bluetoothLayout;
    private RelativeLayout brightnessLayout;
    private RelativeLayout voiceLayout;
    private RelativeLayout wifiLayout;
    private RelativeLayout returnRelativeLayout;
    private Fragment aromatherapyFragment;
    private Fragment ambientlightFragment;
    private Fragment bluetoothFragment;
    private Fragment brightnessFragment;
    private Fragment voiceFragment;
    private Fragment wifiFragment;
    private ImageView bgmenu;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionbar=getSupportActionBar();
        actionbar.hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvents();
        initShowFragment();

    }


    private void initView() {
        aromatherapyTv = this.findViewById(R.id.tv_aromatherapy);
        ambientlightTv=this.findViewById(R.id.tv_ambientlight);
        bluetoothTv=this.findViewById(R.id.tv_bluetooth);
        brightnessTv=this.findViewById(R.id.tv_brightness);
        voiceTv=this.findViewById(R.id.tv_voice);
        wifiTv=this.findViewById(R.id.tv_wifi);
        returnTv=this.findViewById(R.id.tv_return);
        aromatherapyLayout=this.findViewById(R.id.rl_aromatherapy);
        ambientlightLayout=this.findViewById(R.id.rl_ambientlight);
        bluetoothLayout=this.findViewById(R.id.rl_bluetooth);
        brightnessLayout=this.findViewById(R.id.rl_brightness);
        voiceLayout=this.findViewById(R.id.rl_voice);
        wifiLayout=this.findViewById(R.id.rl_wifi);
        returnRelativeLayout=this.findViewById(R.id.rl_return);

        bgmenu = this.findViewById(R.id.bgmenu);
    }


    private void initEvents() {
        aromatherapyTv.setOnClickListener(this);
        ambientlightTv.setOnClickListener(this);
        bluetoothTv.setOnClickListener(this);
        brightnessTv.setOnClickListener(this);
        voiceTv.setOnClickListener(this);
        wifiTv.setOnClickListener(this);
        returnTv.setOnClickListener(this);

    }

    private void initShowFragment() {
        wifiTv.callOnClick();
    }

    //设置view透明度变化
    private void setTextViewAlphaChange(View view){
        Animation animation=new AlphaAnimation(0.1f,1.0f);
        animation.setDuration(500);
        view.startAnimation(animation);
    }



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View v) {
        //淡入淡出的一个动画
        Fade fade=new Fade();
        fade.setDuration(500);
        TransitionSet transitionSet=new TransitionSet().addTransition(fade);
        //TransitionManager  用来为特定的场景指定自定义的Transition,并执行它。
        //第一个参数是他的父布局，第二个参数是
        TransitionManager.beginDelayedTransition((ViewGroup) bgmenu.getParent(),transitionSet);

        final FragmentManager fragmentManager=getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        hideAllFragment(fragmentTransaction);
        initTextViewColor();


        switch (v.getId()){
            case R.id.tv_aromatherapy:
                String text=aromatherapyTv.getText().toString();
                int i=text.length();
                //亮度的变化
                setTextViewAlphaChange(aromatherapyTv);
                //颜色的变化
                aromatherapyTv.setTextColor(getResources().getColor(R.color.textSelect));
                aromatherapyLayout.setVisibility(View.VISIBLE);
                ambientlightLayout.setVisibility(View.INVISIBLE);
                bluetoothLayout.setVisibility(View.INVISIBLE);
                brightnessLayout.setVisibility(View.INVISIBLE);
                voiceLayout.setVisibility(View.INVISIBLE);
                wifiLayout.setVisibility(View.INVISIBLE);
                returnRelativeLayout.setVisibility(View.INVISIBLE);
                if(aromatherapyFragment==null){
                    aromatherapyFragment=new AromatherapyFragment();
                    fragmentTransaction.add(R.id.mainFragment,aromatherapyFragment);
                }else{
                    fragmentTransaction.show(aromatherapyFragment);
                }
                break;
            case R.id.tv_ambientlight:
                setTextViewAlphaChange(ambientlightTv);
                ambientlightTv.setTextColor(getResources().getColor(R.color.textSelect));
                aromatherapyLayout.setVisibility(View.INVISIBLE);
                ambientlightLayout.setVisibility(View.VISIBLE);
                bluetoothLayout.setVisibility(View.INVISIBLE);
                brightnessLayout.setVisibility(View.INVISIBLE);
                voiceLayout.setVisibility(View.INVISIBLE);
                wifiLayout.setVisibility(View.INVISIBLE);
                returnRelativeLayout.setVisibility(View.INVISIBLE);
                if(ambientlightFragment==null){
                    ambientlightFragment=new AmbientLightFragment();
                    fragmentTransaction.add(R.id.mainFragment,ambientlightFragment);
                }else{
                    fragmentTransaction.show(ambientlightFragment);
                }
                break;
            case R.id.tv_bluetooth:
                setTextViewAlphaChange(bluetoothTv);
                bluetoothTv.setTextColor(getResources().getColor(R.color.textSelect));
                aromatherapyLayout.setVisibility(View.INVISIBLE);
                ambientlightLayout.setVisibility(View.INVISIBLE);
                bluetoothLayout.setVisibility(View.VISIBLE);
                brightnessLayout.setVisibility(View.INVISIBLE);
                voiceLayout.setVisibility(View.INVISIBLE);
                wifiLayout.setVisibility(View.INVISIBLE);
                returnRelativeLayout.setVisibility(View.INVISIBLE);
                if(bluetoothFragment==null){
                    bluetoothFragment=new BluetoothFragment();
                    fragmentTransaction.add(R.id.mainFragment,bluetoothFragment);
                }else{
                    fragmentTransaction.show(bluetoothFragment);
                }
                break;
            case R.id.tv_brightness:
                setTextViewAlphaChange(brightnessTv);
                brightnessTv.setTextColor(getResources().getColor(R.color.textSelect));
                aromatherapyLayout.setVisibility(View.INVISIBLE);
                ambientlightLayout.setVisibility(View.INVISIBLE);
                bluetoothLayout.setVisibility(View.INVISIBLE);
                brightnessLayout.setVisibility(View.VISIBLE);
                voiceLayout.setVisibility(View.INVISIBLE);
                wifiLayout.setVisibility(View.INVISIBLE);
                returnRelativeLayout.setVisibility(View.INVISIBLE);
                if(brightnessFragment==null){
                    brightnessFragment=new BrightnessFragment();
                    fragmentTransaction.add(R.id.mainFragment,brightnessFragment);
                }else{
                    fragmentTransaction.show(brightnessFragment);
                }
                break;
            case R.id.tv_voice:
                setTextViewAlphaChange(voiceTv);
                voiceTv.setTextColor(getResources().getColor(R.color.textSelect));
                aromatherapyLayout.setVisibility(View.INVISIBLE);
                ambientlightLayout.setVisibility(View.INVISIBLE);
                bluetoothLayout.setVisibility(View.INVISIBLE);
                brightnessLayout.setVisibility(View.INVISIBLE);
                voiceLayout.setVisibility(View.VISIBLE);
                wifiLayout.setVisibility(View.INVISIBLE);
                returnRelativeLayout.setVisibility(View.INVISIBLE);
                if(voiceFragment==null){
                    voiceFragment=new VoiceFragment();
                    fragmentTransaction.add(R.id.mainFragment,voiceFragment);
                }else{
                    fragmentTransaction.show(voiceFragment);
                }
                break;
            case R.id.tv_wifi:
                setTextViewAlphaChange(wifiTv);
                wifiTv.setTextColor(getResources().getColor(R.color.textSelect));
                aromatherapyLayout.setVisibility(View.INVISIBLE);
                ambientlightLayout.setVisibility(View.INVISIBLE);
                bluetoothLayout.setVisibility(View.INVISIBLE);
                brightnessLayout.setVisibility(View.INVISIBLE);
                voiceLayout.setVisibility(View.INVISIBLE);
                wifiLayout.setVisibility(View.VISIBLE);
                returnRelativeLayout.setVisibility(View.INVISIBLE);
                if(wifiFragment==null){
                    wifiFragment=new WiFiFragment();
                    fragmentTransaction.add(R.id.mainFragment,wifiFragment);
                }else{
                    fragmentTransaction.show(wifiFragment);
                }
                break;
            case R.id.tv_return:
                setTextViewAlphaChange(returnTv);
                returnTv.setTextColor(getResources().getColor(R.color.textSelect));
                aromatherapyLayout.setVisibility(View.INVISIBLE);
                ambientlightLayout.setVisibility(View.INVISIBLE);
                bluetoothLayout.setVisibility(View.INVISIBLE);
                brightnessLayout.setVisibility(View.INVISIBLE);
                voiceLayout.setVisibility(View.INVISIBLE);
                wifiLayout.setVisibility(View.INVISIBLE);
                returnRelativeLayout.setVisibility(View.VISIBLE);
                break;

            default:
                break;
        }
        fragmentTransaction.commit();

    }

    //隐藏所有的fragment
    private  void hideAllFragment(FragmentTransaction fragmentTransaction){
        if(aromatherapyFragment!=null){
            fragmentTransaction.hide(aromatherapyFragment);
        }
        if(ambientlightFragment!=null){
            fragmentTransaction.hide(ambientlightFragment);
        }
        if(bluetoothFragment!=null){
            fragmentTransaction.hide(bluetoothFragment);
        }
        if(brightnessFragment!=null){
            fragmentTransaction.hide(brightnessFragment);
        }
        if(voiceFragment!=null){
            fragmentTransaction.hide(voiceFragment);
        }
        if(wifiFragment!=null){
            fragmentTransaction.hide(wifiFragment);
        }
    }

    private void initTextViewColor(){
        aromatherapyTv.setTextColor(getResources().getColor(R.color.textNoSelect));
        ambientlightTv.setTextColor(getResources().getColor(R.color.textNoSelect));
        bluetoothTv.setTextColor(getResources().getColor(R.color.textNoSelect));
        brightnessTv.setTextColor(getResources().getColor(R.color.textNoSelect));
        voiceTv.setTextColor(getResources().getColor(R.color.textNoSelect));
        wifiTv.setTextColor(getResources().getColor(R.color.textNoSelect));
        returnTv.setTextColor(getResources().getColor(R.color.textNoSelect));
    }
}
