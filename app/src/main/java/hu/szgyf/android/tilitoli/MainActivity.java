package hu.szgyf.android.tilitoli;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    final int PICK_PHOTO_FROM_GALLERY_REQUEST_CODE = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.drawableB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(getUriStringToDrawable(R.drawable.duck));
            }
        });

        findViewById(R.id.galleryB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(i.createChooser(i, "Select Picture"), PICK_PHOTO_FROM_GALLERY_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK)
            return;

        switch (requestCode) {
            case PICK_PHOTO_FROM_GALLERY_REQUEST_CODE:
                startActivity(data.getData().toString());
                break;
        }
    }

    void startActivity(String uri) {

        Intent i = new Intent(this, GameActivity.class);
        i.putExtra("pictureURI", uri);
        startActivity(i);
    }

    String getUriStringToDrawable(int ResourceID) {
        return ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(ResourceID)
                + '/' + getResources().getResourceTypeName(ResourceID)
                + '/' + getResources().getResourceEntryName(ResourceID);
    }
}