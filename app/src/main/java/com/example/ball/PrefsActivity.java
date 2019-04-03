package com.example.ball;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class PrefsActivity extends AppCompatActivity {

    SeekBar mSliderMinAcc;
    TextView mTxtMinAcc;
    Button mBtnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefs);

        mSliderMinAcc = findViewById(R.id.slider_min_acc);
        mTxtMinAcc = findViewById(R.id.txt_min_acc);
        mBtnSave = findViewById(R.id.btn_save);

        mSliderMinAcc.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTxtMinAcc.setText(
                    "Minimum acceleration" + mSliderMinAcc.getProgress()
                );
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("minAcc", (double)mSliderMinAcc.getProgress());
                setResult(0, intent);
                finish();
            }
        });
    }
}
