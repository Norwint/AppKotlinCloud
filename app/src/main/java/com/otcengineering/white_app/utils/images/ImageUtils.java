package com.otcengineering.white_app.utils.images;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.interfaces.Callback;
import com.otcengineering.white_app.tasks.GetImageTask;
import com.otcengineering.white_app.tasks.MyAsyncTask;
import com.otcengineering.white_app.utils.EncodingUtils;
import com.otcengineering.white_app.utils.PrefsManager;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

/**
 * Created by cenci7
 */

public class ImageUtils {

    private static final String TAG = "ImageRetriever";

    public static final String FILE_DIRECTORY = "file://";

    private static final int MAX_QUALITY = 100;
    private static final int RESIZE_TO = 200;
    private static final int RESIZE_MAX_WIDTH_OR_HEIGHT = 1000;

    public static final String IMAGE_FIELD_NAME = "image";

    public static String getPoiImageName(long poiPosition, Context context) {
        File folder = getFolderImagesTmp(context);
        if (folder != null) {
            File file = new File(folder, "poi" + poiPosition + ".jpg");
            return file.getAbsolutePath();
        } else {
            return null;
        }
    }

    public static boolean poiImageExists(long poiPosition, Context ctx) {
        File folder = getFolderImagesTmp(ctx);
        if (folder != null) {
            File file = new File(folder, "poi" + poiPosition + ".jpg");
            return file.exists();
        }
        return false;
    }

    public static String getPostImageName(Context context) {
        File folder = getFolderImagesTmp(context);
        if (folder != null) {
            return (new File(folder, "post.jpg")).getAbsolutePath();
        } else {
            return null;
        }
    }

    public static String getWalletImageName(Context context) {
        File folder = getFolderImagesTmp(context);
        if (folder != null) {
            return (new File(folder, "wallet.jpg")).getAbsolutePath();
        } else {
            return null;
        }
    }

    public static List<String> getPoiImages(long poiId, Context context) {
        List<String> poiImages = new ArrayList<>();
        try {
            File folder = getFolderImagesTmp(context);
            if (folder != null) {
                File[] files = folder.listFiles();
                if (files != null) {
                    Arrays.sort(files);
                    for (File file : files) {
                        String fileName = file.getName();
                        int filePoiId = getPoiId(fileName);
                        if (filePoiId == poiId) {
                            poiImages.add(file.getAbsolutePath());
                        }
                    }
                }
            }
        } catch (Exception e) {
            //Log.e(TAG, e.getMessage(), e);
        }
        return poiImages;
    }

    private static int getPoiId(String filename) {
        String[] split = filename.split("_");
        if (split.length > 0) {
            String serviceId = split[0];
            return Integer.parseInt(serviceId);
        }
        return -1;
    }

    private static File getFolderRoot(Context context) {
        try {
            File folder = new File(context.getExternalFilesDir(null), "");
            //Log.d("Folder", folder.getAbsolutePath());
            if (!folder.exists()) {
                folder.mkdirs();
            } else if (folder.isFile()) {
                folder.delete();
                folder.mkdirs();
            }
            return folder;
        } catch (Exception e) {
            //Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    private static File getFolderImagesTmp(Context context) {
        try {
            File folder = new File(getFolderRoot(context), "tmp");
            if (!folder.exists()) {
                folder.mkdirs();
            } else if (folder.isFile()) {
                folder.delete();
                folder.mkdirs();
            }
            return folder;
        } catch (Exception e) {
            //Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public static File getFolderImagesCache(Context context) {
        try {
            File folder = new File(getFolderRoot(context), "cache");
            if (!folder.exists()) {
                folder.mkdirs();
            } else if (folder.isFile()) {
                folder.delete();
                folder.mkdirs();
            }
            return folder;
        } catch (Exception e) {
            //Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public static void saveImageInImmediateCache(@NonNull final Context ctx, @NonNull final byte[] bytes, final String name) {
        try {
            File folder = getFolderImagesCache(ctx);
            File file = new File(folder, name);

            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fos = new FileOutputStream(file.getPath());
            fos.write(bytes);
            fos.close();
        } catch (IOException | NullPointerException e) {
            //Log.e(TAG, "IO problems copying image to file", e);
        }
    }

    public static boolean saveImageFileInCache(@NonNull final Context context, @NonNull final byte[] bytes, final String name) {
        try {
            File folder = getFolderImagesCache(context);
            File file = new File(folder, name + ".jpg");

            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fos = new FileOutputStream(file.getPath());
            fos.write(EncodingUtils.encryptImage(bytes));
            fos.close();
        } catch (IOException | NullPointerException e) {
            //Log.e(TAG, "IO problems copying image to file", e);
            return false;
        }

        return true;
    }

    public static boolean saveImageFileInCache(@NonNull final Context context, @NonNull final byte[] bytes, final long fileId) {
        return saveImageFileInCache(context, bytes, String.format(Locale.US, "%d", fileId));
    }

    public static boolean existsImageFileInCache(@Nonnull final Context ctx, final long routeId) {
        File folder = getFolderImagesCache(ctx);
        File file = new File(folder, "rouImg" + routeId + ".jpg");
        return file.exists();
    }

    public static boolean saveImageFileInCache(@NonNull final Context ctx, @NonNull final Bitmap bmp, final long routeId) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);

        try {
            File folder = getFolderImagesCache(ctx);
            File file = new File(folder, "rouImg" + routeId + ".jpg");

            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fos = new FileOutputStream(file.getPath());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                fos.write(EncodingUtils.encryptImage(baos.toByteArray()));
            } else {
                fos.write(baos.toByteArray());
            }
            fos.close();
        } catch (IOException | NullPointerException e) {
            //Log.e(TAG, "IO problems copying image to file", e);
            return false;
        }

        return true;
    }

    public static @Nullable Bitmap getImageRoute(Context context, long rouId) {
        File folder = getFolderImagesCache(context);
        File file = new File(folder, "rouImg" + rouId + ".jpg");
        byte[] bs = getImageFromCache(context, file.getAbsolutePath());
        if (bs != null) {
            return BitmapFactory.decodeByteArray(bs, 0, bs.length);
        } else {
            return null;
        }
    }

    public static boolean existsImage(Context ctx, long fileId) {
        File folder = getFolderImagesCache(ctx);
        File file = new File(folder, fileId + ".jpg");
        return file.exists();
    }

    public static byte[] getImageFromCache(Context context, long fileId) {
        File folder = getFolderImagesCache(context);
        File file = new File(folder, fileId + ".jpg");
        return getImageFromCache(context, file.getAbsolutePath());
    }

    public static byte[] getImageFromCache(Context context, String fileName) {
        try {
            File file = new File(fileName);

            if (!file.exists()) {
                file = new File(getFolderImagesCache(context), fileName);
                if (!file.exists()) {
                    return null;
                }
            }

            FileInputStream fis = new FileInputStream(file);
            byte[] read = new byte[fis.available()];
            fis.read(read);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return EncodingUtils.decryptImage(read);
            } else {
                return read;
            }
        }
        catch (IOException e) {
            return null;
        }
    }

    public static String getImageFilePathInCache(Context context, long fileId) {
        try {
            File folder = getFolderImagesCache(context);
            String filename = fileId + ".jpg";

            File[] files = folder.listFiles();
            for (File file : files) {
                if (file.getName().equalsIgnoreCase(filename)) {
                    return file.getAbsolutePath();
                }
            }
        } catch (Exception e) {
            //Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public static void deleteImageFile(Context context, String filePathToDelete) {
        if (context == null || filePathToDelete == null || filePathToDelete.isEmpty()) return;

        try {
            File folder = getFolderImagesTmp(context);
            File[] files = folder.listFiles();
            for (File file : files) {
                String filePath = file.getAbsolutePath();
                if (filePath.equalsIgnoreCase(filePathToDelete)) {
                    file.delete();
                    return;
                }
            }
        } catch (Exception e) {
            //Log.e(TAG, e.getMessage(), e);
        }
    }

    public static boolean deleteDirectory(@Nullable File dir) {
        if (dir != null) {
            for (File f : dir.listFiles()) {
                if (f.isFile()) {
                    if (!f.delete()) {
                        return false;
                    }
                } else {
                    if (!deleteDirectory(f)) {
                        return false;
                    }
                }
            }
            return dir.delete();
        } else {
            return false;
        }
    }

    public static void deleteImagesDirectory(Context context) {
        if (context == null) return;

        try {
            File folder = getFolderImagesTmp(context);
            File[] files = folder.listFiles();
            if (files != null) { // if the folder has files android doesn't allow to delete the folder
                for (File file : files) {
                    file.delete();
                }
            }
            folder.delete();
        } catch (Exception e) {
            //Log.e(TAG, e.getMessage(), e);
        }
    }

    public static File getManualFileName(Context context) {
        try {
            File folder = getFolderRoot(context);
            int manualVersion = PrefsManager.getInstance().getManualVersion(context);
            String filename = manualVersion + ".pdf";

            File[] files = folder.listFiles();
            for (File file : files) {
                if (file.getName().equalsIgnoreCase(filename)) {
                    return file;
                }
            }
            return null;
        } catch (Exception e) {
            //Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public static boolean savePdfFile(Context context, byte[] bytes) {
        try {
            int manualVersion = PrefsManager.getInstance().getManualVersion(context);
            String filename = String.valueOf(manualVersion) + ".pdf";

            File folder = getFolderRoot(context);
            File file = new File(folder, filename);

            if (file.exists()) {
                file.delete();
            }

            FileOutputStream fos = new FileOutputStream(file.getPath());
            fos.write(bytes);
            fos.close();
        } catch (Exception e) {
            //Log.e(TAG, "IO problems copying image to file", e);
            return false;
        }//Log.e(TAG, "Problems copying image to file", e);


        return true;
    }

    public static boolean saveBitmapIntoFile(String imageDst, Bitmap bitmap, final Bitmap.CompressFormat format) {
        FileOutputStream fo = null;
        try {
            File f = new File(imageDst);
            if (f.exists()) {
                // file is going to change, we delete the file now and we will create a new one later
                f.delete();
            }

            f.createNewFile();
            fo = new FileOutputStream(f);
            // Resize bitmap resolution (width max 1000 px)
            bitmap = resizeBitmap(bitmap);
            // Compress bitmap rotated to the file
            bitmap.compress(format, MAX_QUALITY, fo);
        } catch (IOException e) {
            //Log.e(TAG, "IO problems copying image to file", e);
        } catch (Exception e) {
            //Log.e(TAG, "Problems copying image to file", e);
        } finally {
            try {
                if (fo != null) {
                    fo.close();
                }
                return true;
            } catch (IOException e) {
                //Log.e(TAG, e.getMessage(), e);
            }
        }
        return false;
    }

    /*
     * Horizontal con alto > 1000px -> Se reduce el alto a 1000px y el ancho a lo correspondiente
     * Vertical con ancho > 1000px -> Se reduce el ancho a 1000px y el alto a lo correspondiente
     */
    private static Bitmap resizeBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        boolean isLandscape = width > height;
        float scaleFactor;
        if (isLandscape && height > RESIZE_MAX_WIDTH_OR_HEIGHT) {
            scaleFactor = ((float) RESIZE_MAX_WIDTH_OR_HEIGHT) / height;
        } else if (!isLandscape && width > RESIZE_MAX_WIDTH_OR_HEIGHT) {
            scaleFactor = ((float) RESIZE_MAX_WIDTH_OR_HEIGHT) / width;
        } else {
            return bitmap; // no resize
        }
        // do resize
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bitmap
        matrix.postScale(scaleFactor, scaleFactor);
        // "recreate" the new bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        bitmap.recycle();
        return resizedBitmap;
    }

    public static void saveImageRotated(Bitmap bitmap, String imageDst) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        // Compress bitmap rotated to a byte array
        bitmap.compress(Bitmap.CompressFormat.JPEG, MAX_QUALITY, bytes);

        File f = new File(imageDst);
        if (f.exists()) {
            // file is going to change, we delete the file now and we will create a new one later
            f.delete();
        }
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (IOException e) {
            //Log.e(TAG, "IO problems copying image to file", e);
        } catch (Exception e) {
            //Log.e(TAG, "Problems copying image to file", e);
        }
    }

    private static Bitmap decodeSampledBitmapFromResource(String pathFile, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathFile, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathFile, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static String getRealPathFromURI(Uri contentUri, Context context) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor;
        try {
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String str = cursor.getString(column_index);
            cursor.close();
            return str;
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap tintBitmapWithColor(Bitmap bitmap, int color) {
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        Bitmap bitmapResult = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapResult);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return bitmapResult;
    }

    private static final int MAX_SIZE = 10485760;

    public static Bitmap CheckServerSize(final Bitmap bmp) {
        if (bmp != null) {
            Bitmap img = bmp;
            while (img.getByteCount() > MAX_SIZE) {
                img = Bitmap.createScaledBitmap(img, (int) (img.getWidth() * 0.9), (int) (img.getHeight() * 0.9), false);
            }
            return img;
        } else {
            return null;
        }
    }

    public static Bitmap getImageCorrectedFromRotationOfEXIF(final String imageLocation) {
        File f = new File(imageLocation);
        byte[] bytes = new byte[(int)f.length()];

        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(imageLocation));
            bis.read(bytes, 0, (int)f.length());
            bis.close();
            ExifInterface exif = new ExifInterface(imageLocation);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            return rotateBitmap(bmp, orientation);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();

            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getFolderByType(int type) {
        switch (type) {
            case 0: return "MmcPub";
            case 1: return "MmcPri";
            case 2: return "DeaPub";
            case 3: return "DeaPri";
        }
        return "";
    }

    public static List<String> getKeysFromType(int type) {
        File fp = getFolderImagesCache(MyApp.getContext());
        File folder = new File(fp, getFolderByType(type));
        List<String> list = new ArrayList<>();
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) {
                        list.add(f.getName().split("\\.")[0]);
                    }
                }
            }
        }
        return list;
    }

    public static void putPost(int type, String key, String value) {
        File folder = new File(getFolderImagesCache(MyApp.getContext()), getFolderByType(type));
        if (!folder.exists()) {
            folder.mkdir();
        }
        try {
            File newFile = new File(folder.getAbsolutePath() + "/" + key + ".bin");
            newFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(newFile.getAbsolutePath());
            fos.write(EncodingUtils.encryptImage(value.getBytes()));
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPost(int type, String key) {
        File folder = new File(getFolderImagesCache(MyApp.getContext()), getFolderByType(type));
        try {
            File newFile = new File(folder.getAbsolutePath() + "/" + key + ".bin");
            FileInputStream fis = new FileInputStream(newFile.getAbsolutePath());
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream sb = new ByteArrayOutputStream();
            int read = 0;
            while ((read = fis.read(buffer)) >= 0) {
                sb.write(buffer, 0, read);
            }
            return new String(EncodingUtils.decryptImage(sb.toByteArray()));
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void getPostAsync(int type, String key, Callback<String> onResponse) {
        MyAsyncTask mat = new MyAsyncTask();
        mat.setOnBackground(() -> mat.putValue("result", getPost(type, key)));
        mat.setOnPostExecute(() -> {
            String result = mat.getValue("result");
            onResponse.onSuccess(result);
        });
        mat.run();
    }

    public static boolean deletePost(int type, String key) {
        File folder = new File(getFolderImagesCache(MyApp.getContext()), getFolderByType(type));
        try {
            File newFile = new File(folder.getAbsolutePath() + "/" + key + ".bin");
            if (newFile.exists()) {
                return newFile.delete();
            } else {
                return false;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void deletePosts(int type) {
        // Elimina'ls tots
        if (type == 271828183) {
            for (int i = 0; i < 4; ++i) {
                List<String> list = getKeysFromType(i);
                for (String key : list) {
                    deletePost(i, key);
                }
            }
        } else {
            List<String> list = getKeysFromType(type);
            for (String key : list) {
                deletePost(type, key);
            }
        }
    }

    public static void getImage(Context ctx, long imageId, Callback<String> onResponse) {
        if (!existsImage(ctx, imageId)) {
            GetImageTask task = new GetImageTask(imageId) {
                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    if (s != null) {
                        onResponse.onSuccess(s);
                    } else {
                        onResponse.onError(Shared.OTCStatus.SERVER_ERROR);
                    }
                }
            };
            task.execute(ctx);
        } else {
            String imagePath = getImageFilePathInCache(ctx, imageId);
            onResponse.onSuccess(imagePath);
        }
    }

    public static CharSequence getFileName(@NonNull final Context ctx, String name) {
        return getFolderImagesCache(ctx) + "res/" + name;
    }
}
