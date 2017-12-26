package org.linccy.graffiti_sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_color;
    private GraffitiFragment fragment;

    private String url = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1488533478235&di=11c5a9a69813e5ae6f05b48ffd06f701&imgtype=0&src=http%3A%2F%2Fwww.005.tv%2Fuploads%2Fallimg%2F160725%2F22-160H51JR1110.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_color = (TextView) findViewById(R.id.color_selector);
        tv_color.setOnClickListener(this);
        findViewById(R.id.undo).setOnClickListener(this);
        findViewById(R.id.redo).setOnClickListener(this);
        findViewById(R.id.clear).setOnClickListener(this);
        findViewById(R.id.round).setOnClickListener(this);
        findViewById(R.id.save).setOnClickListener(this);
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        fragment = new GraffitiFragment();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.graffiti_content, fragment).commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.undo:
                fragment.action(GraffitiFragment.UNDO);
                break;
            case R.id.redo:
                fragment.action(GraffitiFragment.REDO);
                break;
            case R.id.clear:
                fragment.action(GraffitiFragment.CLEAR);
                break;
            case R.id.round:
                fragment.action(GraffitiFragment.ROTATE);
                break;
            case R.id.color_selector:
                fragment.action(GraffitiFragment.UNDO);
                break;
            case R.id.save:
                String local = fragment.saveAndClear();
                Toast.makeText(this, "文件保存在：" + local, Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }
}
