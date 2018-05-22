package ua.lviv.iot.myparkme;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class PhotoParkingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_parking);

        ImageView imageView = findViewById(R.id.image);

        String url = "https://firebasestorage.googleapis.com/v0/b/take-photo.appspot.com/o/MyCameraApp%2FIMG_Sofiia1.jpg?alt=media&token=e93b3f35-1b46-444f-9390-f93f7a0ea3c1";

        Glide.with(getApplicationContext()).load(url).into(imageView);
    }
}
