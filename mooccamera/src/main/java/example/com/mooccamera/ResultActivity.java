package example.com.mooccamera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class ResultActivity extends Activity {

    private static final String TAG = ResultActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        String path = getIntent().getStringExtra("picPath");
        Log.d(TAG,"path="+path);
        ImageView imageView = (ImageView)findViewById(R.id.pic);

        Bitmap bitmap = BitmapFactory.decodeFile(path);
        Matrix matrix = new Matrix();
        matrix.setRotate(90);
        bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);

        imageView.setImageBitmap(bitmap);

    }

}
