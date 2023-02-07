package com.otcengineering.white_app.activities;

import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;

import com.google.protobuf.ByteString;
import com.otc.alice.api.model.General;
import com.otc.alice.api.model.ProfileAndSettings;
import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.network.utils.ApiCaller;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.network.Endpoints;
import com.otcengineering.white_app.utils.CloudErrorHandler;
import com.otcengineering.white_app.utils.ConnectionUtils;
import com.otcengineering.white_app.utils.MySharedPreferences;
import com.otcengineering.white_app.utils.Utils;
import com.otcengineering.white_app.utils.images.ImageUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.media.MediaRecorder.VideoSource.CAMERA;
import static com.otcengineering.white_app.utils.Utils.runOnUiThreadLock;

public class EditBasicProfileActivity extends EventActivity {

    private ScrollView scrollViewMyProfile;
    private FrameLayout btnScrollUp;
    private Button save;
    private Button imageChange;
    private CircleImageView img;
    private TextView txtUser, txtMail, txtPass, txtRetyoe, txtPhone;
    private boolean imageProfile = false;
    public static final int PICK_IMAGE = 0;
    private EditText nick, email, tlf, pass, retype;
    private ProfileAndSettings.UserDataResponse udr;

    private String m_filename;
    private byte[] m_bytes;

    private String emptyField;
    private String originalPhone;

    public EditBasicProfileActivity() {
        super("EditBasicProfileActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_edit_basic_profile);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.comunications_15);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#001e67'>" + getResources().getString(R.string.title_my_profile) + "</font>"));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        retrieveViews();

        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");
        Typeface face1 = Typeface.createFromAsset(getAssets(), "fonts/Montserrat-Regular.ttf");

        save.setTypeface(face1);
        imageChange.setTypeface(face1);
        nick.setTypeface(face);
        email.setTypeface(face);
        tlf.setTypeface(face);
        txtUser.setTypeface(face);
        txtMail.setTypeface(face);
        txtPass.setTypeface(face);
        pass.setTypeface(face);
        txtRetyoe.setTypeface(face);
        retype.setTypeface(face);
        txtPhone.setTypeface(face);


        nick.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (nick.getRight() - nick.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // your action here
                    nick.setText("");
                    return true;
                }
            }
            return false;
        });

        email.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (email.getRight() - email.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // your action here
                    email.setText("");
                    return true;
                }
            }
            return false;
        });

        tlf.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (tlf.getRight() - tlf.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // your action here
                    tlf.setText("");
                    return true;
                }
            }
            return false;
        });

        pass.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (pass.getRight() - pass.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // your action here
                    pass.setText("");
                    return true;
                }
            }
            return false;
        });

        retype.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (retype.getRight() - retype.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    // your action here
                    retype.setText("");
                    return true;
                }
            }
            return false;
        });

        save.setOnClickListener(v -> {
            String resultado = stripAccents(pass.getText().toString());

            if (checkEmptysFields()){
                showError(emptyField + " " + getResources().getString(R.string.error_default), getResources().getString(R.string.field) + " " + emptyField + " " + getResources().getString(R.string.is_empty));
                return;
            }
            if (!pass.getText().toString().equals(retype.getText().toString())) {
                showError(getString(R.string.password_error), getString(R.string.retype_pass_error));
                return;
            }
            if (!tlf.getText().toString().substring(0, 1).equals("+")) {
                tlf.setText(String.format("+%s", tlf.getText().toString()));
            }
            if (tlf.getText().toString().length() < 12) {
                showError(getString(R.string.error), getString(R.string.lower_than_11));
            } else {
                if (resultado.equals("error")) {
                    showError(getString(R.string.error), getResources().getString(R.string.invalid_userorpass));
                } else {
                    if (ConnectionUtils.isOnline(getApplicationContext())) {
                        new Thread(() -> {
                            try {
                                if (udr.getVin() == null) {
                                    fillFields();
                                }
                                updateProfile();
                            }
                            catch (ApiCaller.OTCException e) {
                                //Log.e("EditBasicProfile", "OTCException", e);
                            }
                        }, "EditBasicProfileThread").start();
                    } else {
                        ConnectionUtils.showOfflineToast();
                    }
                    MySharedPreferences.createLogin(getApplicationContext()).putString("Nick", nick.getText().toString());
                }
            }
        });

        img.setOnClickListener(v -> showPictureDialog());

        imageChange.setOnClickListener(v -> {
            if (imageProfile) {
                showPictureDialog();
            } else {
                imageProfile = true;
                imageChange.setText(getResources().getString(R.string.add));
                img.setImageResource(R.drawable.user_placeholder_correct);
                Bitmap bitmap = ((BitmapDrawable) img.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                baos = null;
                bitmap = null;
                Glide.with(getBaseContext()).load(R.drawable.user_placeholder_correct).into(img);

                imageChange.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });
        if (ConnectionUtils.isOnline(getApplicationContext())) {
            new Thread(this::fillFields, "FillThread").start();
        } else {
            ConnectionUtils.showOfflineToast();
        }

        btnScrollUp.setOnClickListener(view -> scrollViewMyProfile.smoothScrollTo(0, 0));

        scrollViewMyProfile.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = scrollViewMyProfile.getScrollY();
            btnScrollUp.setVisibility(scrollY > 0 ? View.VISIBLE : View.GONE);
        });
    }

    public boolean checkEmptysFields() {
        boolean isEmpty = false;
        if (nick.getText().length() == 0) {
            emptyField = getResources().getString(R.string.nickname);
            isEmpty = true;
        } else if (email.getText().length() == 0) {
            emptyField = getResources().getString(R.string.email);
            isEmpty = true;
        }
        else if (tlf.getText().length() == 0) {
            emptyField = getResources().getString(R.string.mobile_phone);
            isEmpty = true;
        }

        return isEmpty;
    }

    private void retrieveViews() {
        scrollViewMyProfile = findViewById(R.id.profile_edit_scrollView);
        btnScrollUp = findViewById(R.id.profile_edit_btnScrollUp);
        save = findViewById(R.id.btnSave);
        imageChange = findViewById(R.id.btnImgChange);
        img = findViewById(R.id.imagPerfil);
        nick = findViewById(R.id.etNickName);
        email = findViewById(R.id.etEmailAddress);
        pass = findViewById(R.id.etSignupPass);
        retype = findViewById(R.id.etRetype);
        tlf = findViewById(R.id.etPhoneNumber);
        txtUser = findViewById(R.id.txtUser);
        txtMail = findViewById(R.id.txtMail);
        txtPass = findViewById(R.id.txtPass);
        txtRetyoe = findViewById(R.id.txtRetype);
        txtPhone = findViewById(R.id.txtPhone);
    }

    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle(R.string.select_action);
        String[] pictureDialogItems = {
                getString(R.string.select_from_gallery),
                getString(R.string.capture_from_camera)};
        pictureDialog.setItems(pictureDialogItems,
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            choosePhotoFromGallary();
                            break;
                        case 1:
                            takePhotoFromCamera();
                            break;
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (galleryIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(galleryIntent, PICK_IMAGE);
        } else {
            Intent filesIntent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(filesIntent, PICK_IMAGE);
        }
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    runOnUiThread(() -> {
                        imageChange.setText(getResources().getString(R.string.delete));
                        imageChange.setTextColor(getResources().getColor(R.color.textButton));
                        imageProfile = false;
                    });

                    Uri uri = data.getData();

                    img.setImageURI(uri);

                    String path = getPath(uri);

                    File file = new File(path);
                    int size = (int)file.length();
                    m_bytes = new byte[size];
                    m_filename = file.getName();
                    try {
                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                        bis.read(m_bytes, 0, size);
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Bitmap bitmap2 = ((BitmapDrawable) img.getDrawable()).getBitmap();
                    Bitmap bitmap = Bitmap.createScaledBitmap(bitmap2, 150, 150, false);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);

                    String path2 = MediaStore.Images.Media.insertImage(getBaseContext().getContentResolver(), bitmap, "Title", null);

                    Glide.with(getBaseContext()).load(Uri.parse(path2)).into(img);
                    break;
                }
            case CAMERA:
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                img.setImageBitmap(thumbnail);

                runOnUiThread(() -> {
                    imageChange.setText(getResources().getString(R.string.delete));
                    imageChange.setTextColor(getResources().getColor(R.color.textButton));
                    imageProfile = false;
                });

                String path = MediaStore.Images.Media.insertImage(getBaseContext().getContentResolver(), thumbnail, "Title", null);
                File file = new File(getPath(Uri.parse(path)));
                int size = (int)file.length();
                m_filename = file.getName();
                m_bytes = new byte[size];
                try {
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                    bis.read(m_bytes, 0, size);
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Glide.with(getBaseContext()).load(Uri.parse(path)).into(img);
                break;
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;

    }

    private static final String REPLACEMENT
            = "AaEeIiOoUuNnUu";

    public String stripAccents(String str) {
        String original = getString(R.string.letras_especiales);
        if (str == null) {
            return null;
        }
        char[] array = str.toCharArray();
        for (int index = 0; index < array.length; index++) {
            int pos = original.indexOf(array[index]);
            if (pos > -1) {
                return "error";
            }
        }
        return new String(array);
    }

    void updateProfile() throws ApiCaller.OTCException {
        AtomicReference<ProgressDialog> customProgressDialog = new AtomicReference<>();
        Utils.runOnMainThread(() -> {
            customProgressDialog.set(new ProgressDialog(this));
            customProgressDialog.get().setTitle(getResources().getString(R.string.updating_profile));
            customProgressDialog.get().show();
        });
        General.UserProfile.Builder userProfile = General.UserProfile.newBuilder();
        userProfile.setVin(udr.getVin());
        userProfile.setMac(udr.getMac());
        userProfile.setImei(Utils.getImei());
        userProfile.setAddress(udr.getAddress());
        userProfile.setInstallationNumber(udr.getInstallationNumber());
        userProfile.setName(udr.getName());
        userProfile.setCountryId(udr.getCountryId());
        userProfile.setRegion(udr.getRegion());
        userProfile.setCity(udr.getCity());
        userProfile.setSexType(udr.getSexType());
        userProfile.setPostalCode(udr.getPostalCode());
        Stream.of(udr.getTermsList()).forEach(userProfile::addTerms);
        userProfile.setBirthdayDate(udr.getBirthdayDate());
        userProfile.setDrivingLicenseDate(udr.getDrivingLicenseDate());
        userProfile.setDealershipId(udr.getDealershipId());
        userProfile.setDongleSerialNumber(udr.getDongleSerialNumber());
        userProfile.setPlate(udr.getPlate());
        userProfile.setLanguage(udr.getLanguage());
        userProfile.setInsuranceTermDateEnd(udr.getInsuranceTermDateEnd());
        userProfile.setInsuranceTermDateStart(udr.getInsuranceTermDateStart());
        userProfile.setFinanceTermDateEnd(udr.getFinanceTermDateEnd());
        userProfile.setFinanceTermDateStart(udr.getFinanceTermDateStart());
        userProfile.setCarOwner(udr.getCarOwner());
        userProfile.setBloodType(udr.getBloodType());
        userProfile.setCarRegistrationDate(udr.getCarRegistrationDate());

        MySharedPreferences.createLogin(getApplicationContext()).putString("DealerShipName", "");
        ProfileAndSettings.UserUpdate.Builder userUpdate = ProfileAndSettings.UserUpdate.newBuilder();

        runOnUiThreadLock(() -> {
            userUpdate.setUsername(nick.getText().toString());
            userUpdate.setPhone(tlf.getText().toString());
            userUpdate.setEmail(email.getText().toString());

            if (pass.length() > 0) {
                userUpdate.setPassword(pass.getText().toString());
            }
        });

        userUpdate.setProfile(userProfile);

        MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());

        Shared.OTCResponse updateUserResponse = ApiCaller.doCall(Endpoints.USER_UPDATE, msp.getBytes("token"), userUpdate.build(), Shared.OTCResponse.class);
        final int statusValue = updateUserResponse.getStatusValue();

        if (statusValue != 1) {
            Utils.runOnMainThread(() -> customProgressDialog.get().dismiss());
            showError("Server error", CloudErrorHandler.handleError(statusValue));
            return;
        }

        ProfileAndSettings.UserImage.Builder userImage = ProfileAndSettings.UserImage.newBuilder();

        if (m_bytes != null) {
            userImage.setName(m_filename);
            Bitmap bmp = BitmapFactory.decodeByteArray(m_bytes, 0, m_bytes.length);
            bmp = ImageUtils.CheckServerSize(bmp);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            userImage.setData(ByteString.copyFrom(baos.toByteArray()));

            Shared.OTCResponse updateUserImage = ApiCaller.doCall(Endpoints.USER_IMAGE, msp.getBytes("token"), userImage.build(), Shared.OTCResponse.class);

            if (updateUserImage.getStatusValue() != 1) {
                Utils.runOnMainThread(() -> customProgressDialog.get().dismiss());

                showError("Server error", CloudErrorHandler.handleError(updateUserImage.getStatusValue()));
                return;
            } else {
                Shared.OTCResponse responseProfile = ApiCaller.doCall(Endpoints.USER_INFO, true, null, Shared.OTCResponse.class);
                if (responseProfile.getStatus() == Shared.OTCStatus.SUCCESS) {
                    try {
                        ProfileAndSettings.UserDataResponse udr = responseProfile.getData().unpack(ProfileAndSettings.UserDataResponse.class);
                        MySharedPreferences.createLogin(this).putLong("UserImageId", udr.getImageId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        } else if (imageProfile) {
            Shared.OTCResponse response = ApiCaller.doCall(Endpoints.USER_IMAGE_DELETE, true, null, Shared.OTCResponse.class);
            Utils.runOnMainThread(() -> customProgressDialog.get().dismiss());

            if (response.getStatus() != Shared.OTCStatus.SUCCESS) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), CloudErrorHandler.handleError(response.getStatusValue()), Toast.LENGTH_LONG).show());
            }
        }
        Utils.runOnMainThread(() -> customProgressDialog.get().dismiss());

        runOnUiThread(() -> {
            if (!pass.getText().toString().isEmpty()) {
                msp.putString("Pass", pass.getText().toString());
            }
            if (!originalPhone.equals(tlf.getText().toString())) {
                Intent intent = new Intent(EditBasicProfileActivity.this, ChangePhone.class);
                Bundle bund = new Bundle();
                bund.putString("phone", tlf.getText().toString());
                bund.putString("nick", nick.getText().toString());
                intent.putExtras(bund);
                startActivity(intent);
            }
            m_bytes = null;
            System.gc();
            finish();
        });
    }

    String getPath(final Uri uri) {
        String[] data = {MediaStore.MediaColumns.DATA};
        CursorLoader loader = new CursorLoader(this);
        loader.setUri(uri);
        loader.setProjection(data);
        Cursor cursor = loader.loadInBackground();
        int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(index);
    }

    void fillFields() {
        MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
        try {
            udr = ApiCaller.doCall(Endpoints.USER_INFO, msp.getBytes("token"),
                    null, ProfileAndSettings.UserDataResponse.class);
            if (udr != null) {
                runOnUiThread(() -> {
                    if (email.getText().toString().isEmpty())
                    email.setText(udr.getEmail());
                    if (tlf.getText().toString().isEmpty())
                    tlf.setText(udr.getPhone());
                    if (nick.getText().toString().isEmpty())
                    nick.setText(udr.getUsername());
                });
                originalPhone = udr.getPhone();

                long imageID = udr.getImageId();
                if (imageID != 0) {
                    getImage(imageID);
                }
            } else {
                runOnUiThread(ConnectionUtils::showOfflineToast);
            }
        } catch (ApiCaller.OTCException e) {
            Log.e("EditBasicProfile", "OTCException", e);
        }
    }

    void getImage(long imageId)
    {
        MySharedPreferences msp = MySharedPreferences.createLogin(getApplicationContext());
        try {
            byte[] imageBytes = ApiCaller.getImage(Endpoints.FILE_GET + imageId, msp.getString("token"));
            if (imageBytes != null) {
                Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                runOnUiThread(() -> img.setImageBitmap(decodedImage));
                runOnUiThread(() -> imageChange.setText(getResources().getString(R.string.delete)));
            }
        } catch (ApiCaller.OTCException e) {
            //Log.e("EditBasicProfile", "OTCException", e);
        }
    }

    private void showError(String title, String message) {
        runOnUiThread(() -> new AlertDialog.Builder(EditBasicProfileActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.done), (dialog, which) -> {
                    // Whatever...
                }).show());
    }
}
