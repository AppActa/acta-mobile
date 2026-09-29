package br.com.acta.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;

import com.cloudinary.android.preprocess.Preprocess;
import com.cloudinary.android.preprocess.PreprocessException;

import java.io.InputStream;

public class AutoRotation implements Preprocess<Bitmap> {
    private Context context;
    private Uri imageUri;

    public AutoRotation(Context context, Uri imageUri){
        this.context = context;
        this.imageUri = imageUri;
    }

    private int getRotationAngleFromExif(Uri imageUri){
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null){
                return 0;
            }
            ExifInterface exifInterface;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                exifInterface = new ExifInterface(inputStream);
            } else {
                String path = imageUri.getPath();
                if (path == null) return 0;
                exifInterface = new ExifInterface(path);
            }

            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public Bitmap execute(Context context, Bitmap resource) throws PreprocessException {
        int rotationAngle = getRotationAngleFromExif(imageUri);
        if (rotationAngle == 0){
            return resource;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotationAngle);

        return Bitmap.createBitmap(resource, 0, 0, resource.getWidth(), resource.getHeight(), matrix, true);
    }
}