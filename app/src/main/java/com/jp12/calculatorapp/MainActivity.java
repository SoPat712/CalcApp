package com.jp12.calculatorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import org.mariuszgromada.math.mxparser.*;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private EditText editText;
    private TextView textView;

    //private int relHeightMain;
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        if (view.getId() == R.id.editText) {
            menu.removeItem(2);
            menu.removeItem(3);

        } else
            super.onCreateContextMenu(menu, view, menuInfo);
    }
    public static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        editText = findViewById(R.id.editText);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editText.setShowSoftInputOnFocus(false);
        }
        textView = findViewById(R.id.invisTextView);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                textView.setText(charSequence.toString());
                float size = textView.getTextSize();
                size = size / 3;
                editText.setTextSize(size);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {
                    String text = s.toString();

                    if (text.matches(".*[a-zA-Z].*")) {
                        editText.setTextColor(Color.RED);
                    } else {
                        editText.setTextColor(Color.BLACK);
                    }
                }
            }
        });
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        RelativeLayout relativeLayout = findViewById(R.id.editLayout);
        LinearLayout linearLayout = findViewById(R.id.linlayout1);
        LinearLayout secondLayout = findViewById(R.id.secondLayout);
        LinearLayout extraLayout = findViewById(R.id.extraLayout);
        secondLayout.setVisibility(View.INVISIBLE);
        ViewGroup.LayoutParams linParams = linearLayout.getLayoutParams();
        ViewGroup.LayoutParams secParams = secondLayout.getLayoutParams();
        ViewGroup.LayoutParams extraParams = extraLayout.getLayoutParams();
        ViewGroup.LayoutParams relParams = relativeLayout.getLayoutParams();
        //relHeightMain = height - linParams.height - extraParams.height;
        relParams.height = height - linParams.height - extraParams.height;
        relativeLayout.setLayoutParams(relParams);
    }

    /*public static double eval(String str) {
        str = str.replaceAll("×", "*");
        str = str.replaceAll("÷", "/");
        str = str.replaceAll("√", "sqrt");
        String finalStr = str;
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < finalStr.length()) ? finalStr.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < finalStr.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(finalStr.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = finalStr.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }*/

    public void clearText(View view) {
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        editText.setText("");
        textView.setText("");
    }
    public int nextOp(int preIndex, String ans){
        char[] charArray = ans.toCharArray();
        for(int i = preIndex; i <= ans.length(); i++){
            if(charArray[i] == '*' | charArray[i] == '+' | charArray[i] == '-'| charArray[i] == '/'| charArray[i] == '*'){
                return i;
            }
        }
    }
    public void equalPressed(View view) {
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        String cleanedText;
        String displayText = editText.getText().toString();
        /*try{
            ans = eval(display.getText().toString());
        }
        catch (Exception e){
            System.out.println("Solve error: "+e.toString());
        }*/
        cleanedText = displayText.replaceAll("×", "*");
        cleanedText = cleanedText.replaceAll("÷", "/");
        cleanedText = cleanedText.replaceAll("√", "*sqrt(");
        for(int i = 0; i < cleanedText.length(); i++){
            char[] arr = cleanedText.toCharArray();
            if(arr[i] == '√'){
                
            }
            int nextOpIndex = nextOp(i, cleanedText);
        }
        cleanedText = cleanedText.replaceAll("π", "*pi");
        Expression exp = new Expression(cleanedText);
        String result = String.valueOf(exp.calculate());

        editText.setText(result);
        editText.setSelection(result.length());
    }

    public void perenPressed(View view) {
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        int cursor = editText.getSelectionStart();
        int openPer = 0;
        int closedPer = 0;
        int textLen = editText.getText().length();

        for (int i = 0; i < cursor; i++) {
            if (editText.getText().toString().substring(i, i + 1).equals("(")) {
                openPer += 1;
            }
            if (editText.getText().toString().substring(i, i + 1).equals(")")) {
                closedPer += 1;
            }
        }
        if (openPer == closedPer || editText.getText().toString().substring(textLen - 1, textLen).equals("(")) {
            updateText("(");
        } else if (closedPer < openPer || !editText.getText().toString().substring(textLen - 1, textLen).equals("(")) {
            updateText(")");
        }
        editText.setSelection(cursor + 1);
    }

    public void sqrtPressed(View view) {
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        updateText("√");
    }

    public void piPressed(View view) {
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        updateText("π");
    }

    public void expPressed(View view) {
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        updateText("^");
    }

    public void factPressed(View view) {
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        updateText("!");
    }

    public void subPressed(View view) {
        Button button = findViewById(view.getId());
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        updateText(button.getText().toString());
    }

    public void addPressed(View view) {
        Button button = findViewById(view.getId());
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        updateText(button.getText().toString());
    }

    public void multPressed(View view) {
        Button button = findViewById(view.getId());
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        updateText(button.getText().toString());
    }

    public void divPressed(View view) {
        Button button = findViewById(view.getId());
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        updateText(button.getText().toString());
    }

    public void percentPressed(View view) {
        Button button = findViewById(view.getId());
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        updateText(button.getText().toString());
    }

    private void updateText(String string) {
        String oldStr = editText.getText().toString();
        int cursorPos = editText.getSelectionStart();
        String leftStr = oldStr.substring(0, cursorPos);
        String rightStr = oldStr.substring(cursorPos);
        editText.setText(String.format("%s%s%s", leftStr, string, rightStr));
        editText.setSelection(cursorPos + 1);
    }

    public void addNum(View view) {
        Button button = findViewById(view.getId());
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        updateText(button.getText().toString());
    }

    public void addDec(View view) {

    }

    public void removeNum(View view) {
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        int cursor = editText.getSelectionStart();
        int len = editText.getText().length();

        if (cursor != 0 && len != 0) {
            String a = editText.getText().toString();
            editText.setText(a.substring(0, cursor - 1) + a.substring(cursor));
            editText.setSelection(cursor - 1);
        }
    }
    /*
    public void resizeClick(View view) {
        final LinearLayout linearLayout = findViewById(R.id.linlayout1);
        final LinearLayout secondLayout = findViewById(R.id.secondLayout);
        final LinearLayout extraLayout = findViewById(R.id.extraLayout);
        ViewGroup.LayoutParams extraLayoutParams = extraLayout.getLayoutParams();
        ResizeAnimation resizeAnimation = null;
        TranslateAnimation translateAnimation = null;
        AlphaAnimation alphaAnimation = null;
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        if (linearLayout.getLayoutParams().height == 1275) {
            System.out.println(linearLayout.getLayoutParams().height);
            resizeAnimation = new ResizeAnimation(
                    linearLayout,
                    -250,
                    1275
            );
            resizeAnimation.setDuration(150);
            secondLayout.setVisibility(View.VISIBLE);
            translateAnimation = new TranslateAnimation(
                    0,                 // fromXDelta
                    0,                 // toXDelta
                    relHeightMain,  // fromYDelta
                    relHeightMain+extraLayoutParams.height-50);                // toYDelta
            translateAnimation.setDuration(500);
            translateAnimation.setFillAfter(true);
            alphaAnimation = new AlphaAnimation(0, 1);
            alphaAnimation.setInterpolator(new DecelerateInterpolator()); //add this
            alphaAnimation.setDuration(500);
            AnimationSet inner = new AnimationSet(false);
            inner.addAnimation(translateAnimation);
            inner.addAnimation(alphaAnimation);
            secondLayout.startAnimation(inner);
        } else {
            System.out.println(linearLayout.getLayoutParams().height);
            resizeAnimation = new ResizeAnimation(
                    linearLayout,
                    250,
                    1025
            );
            resizeAnimation.setDuration(250);
            translateAnimation = new TranslateAnimation(
                    0,                 // fromXDelta
                    0,                 // toXDelta
                    relHeightMain+extraLayoutParams.height-50,                 // fromYDelta
                    relHeightMain); // toYDelta
            translateAnimation.setDuration(500);
            translateAnimation.setFillAfter(true);
            alphaAnimation = new AlphaAnimation(1, 0);
            alphaAnimation.setInterpolator(new AccelerateInterpolator()); //and this
            alphaAnimation.setStartOffset(0);
            alphaAnimation.setDuration(500);
            AnimationSet inner = new AnimationSet(false);
            inner.addAnimation(translateAnimation);
            inner.addAnimation(alphaAnimation);
            secondLayout.startAnimation(inner);
            secondLayout.setVisibility(View.INVISIBLE);
        }
        AnimationSet s = new AnimationSet(false);
        s.addAnimation(resizeAnimation);
        view.startAnimation(s);
    }*/
}
