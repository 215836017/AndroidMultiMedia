package com.cakes.democamera2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/*
Test1Activity:
// https://www.cnblogs.com/guanxinjing/p/10940049.html
// https://cloud.tencent.com/developer/article/1650043

Test2Activity:
「Android」 Surface分析: https://www.cnblogs.com/1996swg/p/9834892.html
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /*
    需手动在手机设置里面开启权限。
     */
    public void btnClick(View view) {
        if (view.getId() == R.id.main_test_01) {
            startActivity(new Intent(this, Test1Activity.class));

        } else if (view.getId() == R.id.main_test_02) {
            startActivity(new Intent(this, Test2Activity.class));

        } else if (view.getId() == R.id.main_test_03) {
            startActivity(new Intent(this, Test3Activity.class));
        }
    }
}