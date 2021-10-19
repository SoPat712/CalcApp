package com.jp12.calculatorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import org.mariuszgromada.math.mxparser.*;
import org.w3c.dom.Text;

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

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    private EditText editText;
    private TextView textView;
    private boolean equalPressedLast = false;

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
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
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
                    throw new RuntimeException("Unexpected: " + (char) ch);
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

    public void clearText(View view) {
        equalPressedLast = false;
        vibrate();
        editText.setText("");
        textView.setText("");
        TextView constText = findViewById(R.id.constTxt);
        constText.setText("");
    }

    public void vibrate() {
        getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    public void equalPressed(View view) {
        vibrate();
        /*try{
            ans = eval(display.getText().toString());
        }
        catch (Exception e){
            System.out.println("Solve error: "+e.toString());
        }*/

        try {
            String result = evaluate(editText.getText().toString());
            DecimalFormat format = new DecimalFormat("0.###############");
            result = format.format(Double.parseDouble(result));
            editText.setText(result);
            editText.setSelection(result.length());
            equalPressedLast = true;
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public String evaluate(String cleanedText) {
        try {
            cleanedText = cleanedText.replaceAll("×", "*");
            cleanedText = cleanedText.replaceAll("÷", "/");
            cleanedText = cleanedText.replaceAll("√", "sqrt");
            char[] charArray = cleanedText.toCharArray();
            cleanedText = cleanedText.replaceAll("π", "3.14159265358979");
            System.out.println("Cleaned Text: " + cleanedText);

            if (charArray[0] == '+' || charArray[0] == '*' || charArray[0] == '/') {
                return "NaN";
            } else if (charArray[charArray.length - 1] == '+' || charArray[charArray.length - 1] == '*' || charArray[charArray.length - 1] == '/' || charArray[charArray.length - 1] == '-') {
                return "NaN";
            } else if (cleanedText.contains("*/") || cleanedText.contains("*+") || cleanedText.contains("**") ||cleanedText.contains("+/") || cleanedText.contains("+*") ||  cleanedText.contains("++") || cleanedText.contains("/*") || cleanedText.contains("/+")||  cleanedText.contains("//") ||  cleanedText.contains("-/")||  cleanedText.contains("-+")||  cleanedText.contains("-*")) {
                return "NaN";
            } else {
            }
            while (cleanedText.contains("*") || cleanedText.contains("/") || cleanedText.contains("+") || cleanedText.contains("-")) {
                if (cleanedText.contains("*") || cleanedText.contains("/")) {
                    if (cleanedText.indexOf('*') < cleanedText.indexOf('/')) {
                        for (int a = 0; a < cleanedText.length(); a++) {
                            char[] tempArray = cleanedText.toCharArray();
                            if (tempArray[a] == '*') {
                                int lastOpIndex = lastOp(cleanedText, a);
                                int nextOpIndex = nextOp(cleanedText, a);
                                if (lastOpIndex == -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println("Answer: " + bef * aft);
                                    cleanedText = (String.valueOf(bef * aft));
                                    return cleanedText;
                                } else if (lastOpIndex == -1 && nextOpIndex != -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(bef * aft + cleanedText.substring(nextOpIndex));
                                    cleanedText = evaluate(bef * aft + cleanedText.substring(nextOpIndex));
                                } else if (lastOpIndex != -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef * aft)));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef * aft));
                                } else {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef * aft)) + cleanedText.substring(nextOpIndex));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef * aft)) + cleanedText.substring(nextOpIndex);
                                }
                            } else if (tempArray[a] == '/') {
                                int lastOpIndex = lastOp(cleanedText, a);
                                int nextOpIndex = nextOp(cleanedText, a);
                                if (lastOpIndex == -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println("Answer: " + bef / aft);
                                    cleanedText = (String.valueOf(bef / aft));
                                    return cleanedText;
                                } else if (lastOpIndex == -1 && nextOpIndex != -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(bef / aft + cleanedText.substring(nextOpIndex));
                                    cleanedText = evaluate(bef / aft + cleanedText.substring(nextOpIndex));
                                } else if (lastOpIndex != -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef / aft)));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef / aft));
                                } else {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef / aft)) + cleanedText.substring(nextOpIndex));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef / aft)) + cleanedText.substring(nextOpIndex);
                                }
                            }
                        }
                    } else if (cleanedText.indexOf('*') > cleanedText.indexOf('/')) {
                        for (int a = 0; a < cleanedText.length(); a++) {
                            char[] tempArray = cleanedText.toCharArray();
                            if (tempArray[a] == '/') {
                                int lastOpIndex = lastOp(cleanedText, a);
                                int nextOpIndex = nextOp(cleanedText, a);
                                if (lastOpIndex == -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println("Answer: " + bef / aft);
                                    cleanedText = (String.valueOf(bef / aft));
                                    return cleanedText;
                                } else if (lastOpIndex == -1 && nextOpIndex != -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(bef / aft + cleanedText.substring(nextOpIndex));
                                    cleanedText = evaluate(bef / aft + cleanedText.substring(nextOpIndex));
                                } else if (lastOpIndex != -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef / aft)));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef / aft));
                                } else {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef / aft)) + cleanedText.substring(nextOpIndex));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef / aft)) + cleanedText.substring(nextOpIndex);
                                }
                            } else if (tempArray[a] == '*') {
                                int lastOpIndex = lastOp(cleanedText, a);
                                int nextOpIndex = nextOp(cleanedText, a);
                                if (lastOpIndex == -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println("Answer: " + bef * aft);
                                    cleanedText = (String.valueOf(bef * aft));
                                    return cleanedText;
                                } else if (lastOpIndex == -1 && nextOpIndex != -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(bef * aft + cleanedText.substring(nextOpIndex));
                                    cleanedText = evaluate(bef * aft + cleanedText.substring(nextOpIndex));
                                } else if (lastOpIndex != -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef * aft)));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef * aft));
                                } else {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef * aft)) + cleanedText.substring(nextOpIndex));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef * aft)) + cleanedText.substring(nextOpIndex);
                                }
                            }
                        }
                    } else {
                        return "NaN";
                    }
                } else if (cleanedText.contains("+") || cleanedText.contains("-")) {
                    if (cleanedText.indexOf('+') < cleanedText.indexOf('-')) {
                        for (int a = 0; a < cleanedText.length(); a++) {
                            char[] tempArray = cleanedText.toCharArray();
                            if (tempArray[a] == '+') {
                                int lastOpIndex = lastOp(cleanedText, a);
                                int nextOpIndex = nextOp(cleanedText, a);
                                if (lastOpIndex == -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println("Answer: " + bef + aft);
                                    cleanedText = (String.valueOf(bef + aft));
                                    return cleanedText;
                                } else if (lastOpIndex == -1 && nextOpIndex != -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(bef + aft + cleanedText.substring(nextOpIndex));
                                    cleanedText = evaluate(bef + aft + cleanedText.substring(nextOpIndex));
                                } else if (lastOpIndex != -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef + aft)));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef + aft));
                                } else {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef + aft)) + cleanedText.substring(nextOpIndex));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef + aft)) + cleanedText.substring(nextOpIndex);
                                }
                            } else if (tempArray[a] == '-') {
                                int lastOpIndex = lastOp(cleanedText, a);
                                int nextOpIndex = nextOp(cleanedText, a);
                                if (lastOpIndex == -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println("Answer: " + (bef - aft));
                                    cleanedText = (String.valueOf(bef - aft));
                                    return cleanedText;
                                } else if (lastOpIndex == -1 && nextOpIndex != -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(bef - aft + cleanedText.substring(nextOpIndex));
                                    cleanedText = evaluate(bef - aft + cleanedText.substring(nextOpIndex));
                                } else if (lastOpIndex != -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef - aft)));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef - aft));
                                } else {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef - aft)) + cleanedText.substring(nextOpIndex));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef - aft)) + cleanedText.substring(nextOpIndex);
                                }
                            }
                        }
                    } else if (cleanedText.indexOf('+') > cleanedText.indexOf('-')) {
                        for (int a = 0; a < cleanedText.length(); a++) {
                            char[] tempArray = cleanedText.toCharArray();
                            if (tempArray[a] == '-') {
                                int lastOpIndex = lastOp(cleanedText, a);
                                int nextOpIndex = nextOp(cleanedText, a);
                                if (lastOpIndex == -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println("Answer: " + (bef - aft));
                                    cleanedText = (String.valueOf(bef - aft));
                                    return cleanedText;
                                } else if (lastOpIndex == -1 && nextOpIndex != -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(bef - aft + cleanedText.substring(nextOpIndex));
                                    cleanedText = evaluate(bef - aft + cleanedText.substring(nextOpIndex));
                                } else if (lastOpIndex != -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef - aft)));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef - aft));
                                } else {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef - aft)) + cleanedText.substring(nextOpIndex));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef - aft)) + cleanedText.substring(nextOpIndex);
                                }
                            } else if (tempArray[a] == '+') {
                                int lastOpIndex = lastOp(cleanedText, a);
                                int nextOpIndex = nextOp(cleanedText, a);
                                if (lastOpIndex == -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println("Answer: " + bef + aft);
                                    cleanedText = (String.valueOf(bef + aft));
                                    return cleanedText;
                                } else if (lastOpIndex == -1 && nextOpIndex != -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(bef + aft + cleanedText.substring(nextOpIndex));
                                    cleanedText = evaluate(bef + aft + cleanedText.substring(nextOpIndex));
                                } else if (lastOpIndex != -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef + aft)));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef + aft));
                                } else {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef + aft)) + cleanedText.substring(nextOpIndex));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef + aft)) + cleanedText.substring(nextOpIndex);
                                }
                            }
                        }
                    } else {
                        return "NaN";
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return cleanedText;
    }
    public String constEvaluate(String cleanedText) {
        try {
            cleanedText = cleanedText.replaceAll("×", "*");
            cleanedText = cleanedText.replaceAll("÷", "/");
            cleanedText = cleanedText.replaceAll("√", "sqrt");
            char[] charArray = cleanedText.toCharArray();
            cleanedText = cleanedText.replaceAll("π", "3.14159265358979");
            System.out.println("Cleaned Text: " + cleanedText);

            while (cleanedText.contains("*") || cleanedText.contains("/") || cleanedText.contains("+") || cleanedText.contains("-")) {
                if (cleanedText.contains("*") || cleanedText.contains("/")) {
                    if (cleanedText.indexOf('*') < cleanedText.indexOf('/')) {
                        for (int a = 0; a < cleanedText.length(); a++) {
                            char[] tempArray = cleanedText.toCharArray();
                            if (tempArray[a] == '*') {
                                int lastOpIndex = lastOp(cleanedText, a);
                                int nextOpIndex = nextOp(cleanedText, a);
                                if (lastOpIndex == -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println("Answer: " + bef * aft);
                                    cleanedText = (String.valueOf(bef * aft));
                                    return cleanedText;
                                } else if (lastOpIndex == -1 && nextOpIndex != -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(bef * aft + cleanedText.substring(nextOpIndex));
                                    cleanedText = evaluate(bef * aft + cleanedText.substring(nextOpIndex));
                                } else if (lastOpIndex != -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef * aft)));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef * aft));
                                } else {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef * aft)) + cleanedText.substring(nextOpIndex));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef * aft)) + cleanedText.substring(nextOpIndex);
                                }
                            } else if (tempArray[a] == '/') {
                                int lastOpIndex = lastOp(cleanedText, a);
                                int nextOpIndex = nextOp(cleanedText, a);
                                if (lastOpIndex == -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println("Answer: " + bef / aft);
                                    cleanedText = (String.valueOf(bef / aft));
                                    return cleanedText;
                                } else if (lastOpIndex == -1 && nextOpIndex != -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(bef / aft + cleanedText.substring(nextOpIndex));
                                    cleanedText = evaluate(bef / aft + cleanedText.substring(nextOpIndex));
                                } else if (lastOpIndex != -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef / aft)));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef / aft));
                                } else {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef / aft)) + cleanedText.substring(nextOpIndex));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef / aft)) + cleanedText.substring(nextOpIndex);
                                }
                            }
                        }
                    } else if (cleanedText.indexOf('*') > cleanedText.indexOf('/')) {
                        for (int a = 0; a < cleanedText.length(); a++) {
                            char[] tempArray = cleanedText.toCharArray();
                            if (tempArray[a] == '/') {
                                int lastOpIndex = lastOp(cleanedText, a);
                                int nextOpIndex = nextOp(cleanedText, a);
                                if (lastOpIndex == -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println("Answer: " + bef / aft);
                                    cleanedText = (String.valueOf(bef / aft));
                                    return cleanedText;
                                } else if (lastOpIndex == -1 && nextOpIndex != -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(bef / aft + cleanedText.substring(nextOpIndex));
                                    cleanedText = evaluate(bef / aft + cleanedText.substring(nextOpIndex));
                                } else if (lastOpIndex != -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef / aft)));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef / aft));
                                } else {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef / aft)) + cleanedText.substring(nextOpIndex));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef / aft)) + cleanedText.substring(nextOpIndex);
                                }
                            } else if (tempArray[a] == '*') {
                                int lastOpIndex = lastOp(cleanedText, a);
                                int nextOpIndex = nextOp(cleanedText, a);
                                if (lastOpIndex == -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println("Answer: " + bef * aft);
                                    cleanedText = (String.valueOf(bef * aft));
                                    return cleanedText;
                                } else if (lastOpIndex == -1 && nextOpIndex != -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(bef * aft + cleanedText.substring(nextOpIndex));
                                    cleanedText = evaluate(bef * aft + cleanedText.substring(nextOpIndex));
                                } else if (lastOpIndex != -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef * aft)));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef * aft));
                                } else {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef * aft)) + cleanedText.substring(nextOpIndex));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef * aft)) + cleanedText.substring(nextOpIndex);
                                }
                            }
                        }
                    } else {
                        return "NaN";
                    }
                } else if (cleanedText.contains("+") || cleanedText.contains("-")) {
                    if (cleanedText.indexOf('+') < cleanedText.indexOf('-')) {
                        for (int a = 0; a < cleanedText.length(); a++) {
                            char[] tempArray = cleanedText.toCharArray();
                            if (tempArray[a] == '+') {
                                int lastOpIndex = lastOp(cleanedText, a);
                                int nextOpIndex = nextOp(cleanedText, a);
                                if (lastOpIndex == -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println("Answer: " + bef + aft);
                                    cleanedText = (String.valueOf(bef + aft));
                                    return cleanedText;
                                } else if (lastOpIndex == -1 && nextOpIndex != -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(bef + aft + cleanedText.substring(nextOpIndex));
                                    cleanedText = evaluate(bef + aft + cleanedText.substring(nextOpIndex));
                                } else if (lastOpIndex != -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef + aft)));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef + aft));
                                } else {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef + aft)) + cleanedText.substring(nextOpIndex));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef + aft)) + cleanedText.substring(nextOpIndex);
                                }
                            } else if (tempArray[a] == '-') {
                                int lastOpIndex = lastOp(cleanedText, a);
                                int nextOpIndex = nextOp(cleanedText, a);
                                if (lastOpIndex == -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println("Answer: " + (bef - aft));
                                    cleanedText = (String.valueOf(bef - aft));
                                    return cleanedText;
                                } else if (lastOpIndex == -1 && nextOpIndex != -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(bef - aft + cleanedText.substring(nextOpIndex));
                                    cleanedText = evaluate(bef - aft + cleanedText.substring(nextOpIndex));
                                } else if (lastOpIndex != -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef - aft)));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef - aft));
                                } else {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef - aft)) + cleanedText.substring(nextOpIndex));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef - aft)) + cleanedText.substring(nextOpIndex);
                                }
                            }
                        }
                    } else if (cleanedText.indexOf('+') > cleanedText.indexOf('-')) {
                        for (int a = 0; a < cleanedText.length(); a++) {
                            char[] tempArray = cleanedText.toCharArray();
                            if (tempArray[a] == '-') {
                                int lastOpIndex = lastOp(cleanedText, a);
                                int nextOpIndex = nextOp(cleanedText, a);
                                if (lastOpIndex == -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println("Answer: " + (bef - aft));
                                    cleanedText = (String.valueOf(bef - aft));
                                    return cleanedText;
                                } else if (lastOpIndex == -1 && nextOpIndex != -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(bef - aft + cleanedText.substring(nextOpIndex));
                                    cleanedText = evaluate(bef - aft + cleanedText.substring(nextOpIndex));
                                } else if (lastOpIndex != -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef - aft)));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef - aft));
                                } else {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef - aft)) + cleanedText.substring(nextOpIndex));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef - aft)) + cleanedText.substring(nextOpIndex);
                                }
                            } else if (tempArray[a] == '+') {
                                int lastOpIndex = lastOp(cleanedText, a);
                                int nextOpIndex = nextOp(cleanedText, a);
                                if (lastOpIndex == -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println("Answer: " + bef + aft);
                                    cleanedText = (String.valueOf(bef + aft));
                                    return cleanedText;
                                } else if (lastOpIndex == -1 && nextOpIndex != -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(0, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(bef + aft + cleanedText.substring(nextOpIndex));
                                    cleanedText = evaluate(bef + aft + cleanedText.substring(nextOpIndex));
                                } else if (lastOpIndex != -1 && nextOpIndex == -1) {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef + aft)));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef + aft));
                                } else {
                                    double bef = Double.parseDouble(cleanedText.substring(lastOpIndex + 1, a));
                                    double aft = Double.parseDouble(cleanedText.substring(a + 1, nextOpIndex));
                                    System.out.println(cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef + aft)) + cleanedText.substring(nextOpIndex));
                                    cleanedText = cleanedText.substring(0, lastOpIndex + 1) + evaluate(String.valueOf(bef + aft)) + cleanedText.substring(nextOpIndex);
                                }
                            }
                        }
                    } else {
                        return "";
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return cleanedText;
    }

    private int lastOp(String text, int index) {
        char[] chars = text.toCharArray();
        for (int i = chars.length - 1; i >= 0; i--) {
            if ((chars[i] == '*' || chars[i] == '/' || chars[i] == '+' || chars[i] == '-') && (i < index)) {
                return i;
            }
        }
        return -1;
    }

    private int nextOp(String text, int index) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if ((chars[i] == '*' || chars[i] == '/' || chars[i] == '+' || chars[i] == '-') && (i > index) && (i-1 != index)){
                return i;
            }
        }
        return -1;
    }

    public void perenPressed(View view) {
        vibrate();
        int cursor = editText.getSelectionStart();
        int openPer = 0;
        int closedPer = 0;
        int textLen = editText.getText().length();

        for (int i = 0; i < cursor; i++) {
            if (editText.getText().toString().charAt(i) == '(') {
                openPer += 1;
            }
            if (editText.getText().toString().charAt(i) == ')') {
                closedPer += 1;
            }
        }
        if (openPer == closedPer || editText.getText().toString().charAt(textLen - 1) == '(') {
            updateText("(");
        } else if (closedPer < openPer || editText.getText().toString().charAt(textLen - 1) != '(') {
            updateText(")");
        }
        editText.setSelection(cursor + 1);
    }

    public void sqrtPressed(View view) {
        vibrate();
        updateText("√");
        constUpdate();
    }

    public void piPressed(View view) {
        vibrate();
        updateText("π");
        constUpdate();
    }

    public void expPressed(View view) {
        vibrate();
        updateText("^");
        constUpdate();
    }

    public void factPressed(View view) {
        vibrate();
        updateText("!");
        constUpdate();
    }


    public void opPressed(View view) {
        Button button = findViewById(view.getId());
        vibrate();
        updateText(button.getText().toString());
        equalPressedLast = false;
        constUpdate();
    }

    public void percentPressed(View view) {
        Button button = findViewById(view.getId());
        vibrate();
        updateText(button.getText().toString());
        constUpdate();
    }

    private void updateText(String string) {
        String oldStr = editText.getText().toString();
        int cursorPos = editText.getSelectionStart();
        String leftStr = oldStr.substring(0, cursorPos);
        String rightStr = oldStr.substring(cursorPos);
        editText.setText(String.format("%s%s%s", leftStr, string, rightStr));
        editText.setSelection(cursorPos + 1);
        constUpdate();
    }

    private void updateReplaceText(String string) {
        editText.setText(string);
        editText.setSelection(string.length());
        constUpdate();
    }

    public void addNum(View view) {
        Button button = findViewById(view.getId());
        vibrate();
        if (equalPressedLast) {
            updateReplaceText(button.getText().toString());
        } else {
            updateText(button.getText().toString());
        }
        constUpdate();
    }
    public void constUpdate(){
        TextView constText = findViewById(R.id.constTxt);
        try {
            String result = constEvaluate(editText.getText().toString());
            DecimalFormat format = new DecimalFormat("0.###############");
            result = format.format(Double.parseDouble(result));
            constText.setText(result);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public void addDec(View view) {
        Button button = findViewById(view.getId());
        vibrate();
        updateText(button.getText().toString());
        constUpdate();
    }

    public void removeNum(View view) {
        vibrate();
        int cursor = editText.getSelectionStart();
        int len = editText.getText().length();

        if (cursor != 0 && len != 0) {
            String a = editText.getText().toString();
            editText.setText(a.substring(0, cursor - 1) + a.substring(cursor));
            editText.setSelection(cursor - 1);
        }
        constUpdate();
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
