package of.settings.bq.view;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import of.settings.bq.R;

import java.util.List;

public class KeyboardUtil {

    final private static String TAG = "WiFi.KeyboardUtil";

    private KeyboardView keyboardView;
    private Keyboard alphabetKeyboard;
    private Keyboard characterKeyboard;

    public boolean isupper = false;

    private EditText inputText;
    private KeyboardListener keyboardListener;

    public interface KeyboardListener {
        void onAction();
    }

    public void setKeyboardListener(KeyboardListener keyboardListener) {
        this.keyboardListener = keyboardListener;
    }

    public KeyboardUtil(View parent, int resId, EditText edit) {
        inputText   = edit;
        alphabetKeyboard  = new Keyboard(parent.getContext(), R.xml.symbols_alphabet);
        characterKeyboard = new Keyboard(parent.getContext(), R.xml.symbols_character);
        keyboardView      =  parent.findViewById(resId);
        try {
            keyboardView.setKeyboard(alphabetKeyboard);
            keyboardView.setEnabled(true);
            keyboardView.setPreviewEnabled(true);
            OnKeyboardActionListener listener = new OnKeyboardActionListener() {
                @Override
                public void swipeUp() { }

                @Override
                public void swipeRight() { }

                @Override
                public void swipeLeft() { }

                @Override
                public void swipeDown() { }

                @Override
                public void onText(CharSequence text) {
                    Log.e(TAG, text.toString());
                }

                @Override
                public void onRelease(int primaryCode) { }

                @Override
                public void onPress(int primaryCode) { }

                @Override
                public void onKey(int primaryCode, int[] keyCodes) {
                    Editable editable = inputText.getText();
                    int start = inputText.getSelectionStart();
                    if (primaryCode == Keyboard.KEYCODE_DONE) {
                        // Done, do next action
                        keyboardListener.onAction();
                    } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
                        // Delete
                        if (editable != null && editable.length() > 0) {
                            if (start > 0) {
                                editable.delete(start - 1, start);
                            }
                        }
                    } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
                        // Keyboard Case switch
                        changeKey();
                        keyboardView.setKeyboard(alphabetKeyboard);
                    } else if (primaryCode == 456456) {
                        // Change to alphabet Keyboard
                        keyboardView.setKeyboard(alphabetKeyboard);
                    } else if (primaryCode == 789789) {
                        // Change to character Keyboard
                        keyboardView.setKeyboard(characterKeyboard);
                    } else if (primaryCode == 57419) {
                        // go left
                        if (start > 0) {
                            inputText.setSelection(start - 1);
                        }
                    } else if (primaryCode == 57421) {
                        // go right
                        if (start < inputText.length()) {
                            inputText.setSelection(start + 1);
                        }
                    } else {
                        editable.insert(start, Character.toString((char)primaryCode));
                    }
                }
            };
            keyboardView.setOnKeyboardActionListener(listener);
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    /**
     * Keyboard Case switch
     */
    private void changeKey() {
        List<Key> keylist = alphabetKeyboard.getKeys();
        if (isupper) {
            //大写切换小写
            isupper = false;
            for (Key key : keylist) {
                if (key.label != null && isword(key.label.toString())) {
                    key.label = key.label.toString().toLowerCase();
                    key.codes[0] = key.codes[0] + 32;
                }
            }
        } else {
            //小写切换大写
            isupper = true;
            for (Key key : keylist) {
                if (key.label != null && isword(key.label.toString())) {
                    key.label = key.label.toString().toUpperCase();
                    key.codes[0] = key.codes[0] - 32;
                }
            }
        }
    }

    private boolean isword(String str) {
        String wordStr = "abcdefghijklmnopqrstuvwxyz";
        if (wordStr.contains(str.toLowerCase())) {
            return true;
        }
        return false;
    }

}