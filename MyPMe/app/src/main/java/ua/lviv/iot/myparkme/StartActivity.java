package ua.lviv.iot.myparkme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        startButtonClick();
    }

    public void startButtonClick() {
        Button startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startLogin = new Intent(StartActivity.this, EmailPasswordActivity.class);
                startActivity(startLogin);
            }
        });
    }
}
