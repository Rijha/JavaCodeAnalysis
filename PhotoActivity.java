package com.sarriaroman.PhotoViewer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import p009uk.p010co.senab.photoview.PhotoViewAttacher;

public class PhotoActivity extends Activity {
    public static JSONArray mArgs = null;
    private ImageButton closeBtn;
    private ProgressBar loadingBar;
    private PhotoViewAttacher mAttacher;
    /* access modifiers changed from: private */
    public String mImage;
    private boolean mShare;
    /* access modifiers changed from: private */
    public File mTempImage;
    private String mTitle;
    /* access modifiers changed from: private */
    public ImageView photo;
    private ImageButton shareBtn;
    private int shareBtnVisibility;
    private TextView titleTxt;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        int i = 0;
        super.onCreate(savedInstanceState);
        setContentView(getApplication().getResources().getIdentifier("activity_photo", "layout", getApplication().getPackageName()));
        findViews();
        try {
            this.mImage = mArgs.getString(0);
            this.mTitle = mArgs.getString(1);
            this.mShare = mArgs.getBoolean(2);
            if (!this.mShare) {
                i = 4;
            }
            this.shareBtnVisibility = i;
        } catch (JSONException e) {
            this.shareBtnVisibility = 4;
        }
        this.shareBtn.setVisibility(this.shareBtnVisibility);
        if (!this.mTitle.equals("")) {
            this.titleTxt.setText(this.mTitle);
        }
        loadImage();
        this.closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PhotoActivity.this.finish();
            }
        });
        this.shareBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 24) {
                    try {
                        StrictMode.class.getMethod("disableDeathOnFileUriExposure", new Class[0]).invoke((Object) null, new Object[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (PhotoActivity.this.mTempImage == null) {
                    File unused = PhotoActivity.this.mTempImage = PhotoActivity.this.getLocalBitmapFileFromView(PhotoActivity.this.photo);
                }
                Uri imageUri = Uri.fromFile(PhotoActivity.this.mTempImage);
                if (imageUri != null) {
                    Intent sharingIntent = new Intent("android.intent.action.SEND");
                    sharingIntent.setType("image/*");
                    sharingIntent.putExtra("android.intent.extra.STREAM", imageUri);
                    PhotoActivity.this.startActivity(Intent.createChooser(sharingIntent, "Share"));
                }
            }
        });
    }

    private void findViews() {
        this.closeBtn = (ImageButton) findViewById(getApplication().getResources().getIdentifier("closeBtn", "id", getApplication().getPackageName()));
        this.shareBtn = (ImageButton) findViewById(getApplication().getResources().getIdentifier("shareBtn", "id", getApplication().getPackageName()));
        this.loadingBar = (ProgressBar) findViewById(getApplication().getResources().getIdentifier("loadingBar", "id", getApplication().getPackageName()));
        this.photo = (ImageView) findViewById(getApplication().getResources().getIdentifier("photoView", "id", getApplication().getPackageName()));
        this.mAttacher = new PhotoViewAttacher(this.photo);
        this.titleTxt = (TextView) findViewById(getApplication().getResources().getIdentifier("titleTxt", "id", getApplication().getPackageName()));
    }

    /* access modifiers changed from: private */
    public Activity getActivity() {
        return this;
    }

    /* access modifiers changed from: private */
    public void hideLoadingAndUpdate() {
        this.photo.setVisibility(0);
        this.loadingBar.setVisibility(4);
        this.shareBtn.setVisibility(this.shareBtnVisibility);
        this.mAttacher.update();
    }

    private void loadImage() {
        if (this.mImage.startsWith("http") || this.mImage.startsWith("file")) {
            Picasso.with(this).load(this.mImage).fit().centerInside().into(this.photo, new Callback() {
                public void onSuccess() {
                    PhotoActivity.this.hideLoadingAndUpdate();
                }

                public void onError() {
                    Toast.makeText(PhotoActivity.this.getActivity(), "Error loading image.", 1).show();
                    PhotoActivity.this.finish();
                }
            });
        } else if (this.mImage.startsWith("data:image")) {
            new AsyncTask<Void, Void, File>() {
                /* access modifiers changed from: protected */
                public File doInBackground(Void... params) {
                    return PhotoActivity.this.getLocalBitmapFileFromString(PhotoActivity.this.mImage.substring(PhotoActivity.this.mImage.indexOf(",") + 1));
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(File file) {
                    File unused = PhotoActivity.this.mTempImage = file;
                    Picasso.with(PhotoActivity.this).load(PhotoActivity.this.mTempImage).fit().centerCrop().into(PhotoActivity.this.photo, new Callback() {
                        public void onSuccess() {
                            PhotoActivity.this.hideLoadingAndUpdate();
                        }

                        public void onError() {
                            Toast.makeText(PhotoActivity.this.getActivity(), "Error loading image.", 1).show();
                            PhotoActivity.this.finish();
                        }
                    });
                }
            }.execute(new Void[0]);
        } else {
            this.photo.setImageURI(Uri.parse(this.mImage));
            hideLoadingAndUpdate();
        }
    }

    public void onDestroy() {
        if (this.mTempImage != null) {
            this.mTempImage.delete();
        }
        super.onDestroy();
    }

    public File getLocalBitmapFileFromString(String base64) {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream output = new FileOutputStream(file);
            output.write(Base64.decode(base64, 0));
            output.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public File getLocalBitmapFileFromView(ImageView imageView) {
        if (!(imageView.getDrawable() instanceof BitmapDrawable)) {
            return null;
        }
        Bitmap bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
