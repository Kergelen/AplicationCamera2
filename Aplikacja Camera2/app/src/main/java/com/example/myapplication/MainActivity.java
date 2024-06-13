package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Range;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

//Screen capture
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = "myLogs";

    CameraService[] myCameras = null;

    private CameraManager mCameraManager = null;
    private final int CAMERA1 = 0;
    private final int CAMERA2 = 1;

    private int choiceCam = 0;
    public boolean autoea = false;
    private Button ButtonChange = null;
    private Button Button22 = null;
    private Button Buttontake = null;
    private Button  Buttonautoae  = null;
    private TextureView mImageView = null;
    private TextView textvie = null;
    private TextView textvieEx = null;
    private TextView textvieiso = null;

    private TextView textvie4 = null;

    public EditText editText = null;

    public ImageView ramka = null;

    private TextView textparam = null;
    private TextView textform = null;
    public SeekBar exposureSeekBar = null;
    public SeekBar eSeekBar = null;
    public SeekBar ISOSeekBar = null;


    public static int alfa = 0;
    public static int red = 0;
    public static int green = 0;
    public static int blue = 0;
    public static int xx = 0;
    public int high = 0;

    public int wigh = 0;

    public boolean evaluete = false;

    private ImageReader mImageReader;

    private Handler handler = new Handler();
    private Runnable updateRgbRunnable;

    private float touchX = -1;
    private float touchY = -1;

    public Runnable runnableCode;

    CameraCharacteristics[] characteristics = null;


    private CameraCaptureSession mCaptureSession;

    int screenWidth = 0;
    int screenHeight = 0;

    int ramkaWidth ;

    int exposureCompensation = 0;

    int ramkaHeight ;

    public CaptureRequest.Builder builder;

    String[] cameraIds = null;


    //Screen capture

    private ScreenCapture screenCapture;
    private static final int REQUEST_CODE_SCREEN_CAPTURE = 1;

    private static double estimateTemperature(int r, int g, int b) {
        // Step 1: Normalize RGB values
        double rn = r / 255.0;
        double gn = g / 255.0;
        double bn = b / 255.0;

        // Step 2: Convert RGB to XYZ
        double[] xyz = rgbToXyz(rn, gn, bn, D65_XYZ_TO_RGB);

        // Step 3: Calculate chromaticity coordinates (x, y)
        double x = xyz[0] / (xyz[0] + xyz[1] + xyz[2]);
        double y = xyz[1] / (xyz[0] + xyz[1] + xyz[2]);

        // Step 4: Estimate Correlated Color Temperature (CCT)
        double n = (x - 0.3366) / (y - 0.1735);
        double cct = 437 * Math.pow(n, 3) + 3601 * Math.pow(n, 2) + 6861 * n + 5517;
        return cct;
    }

    private static double[] rgbToXyz(double r, double g, double b, double[] matrix) {
        double[] xyz = new double[3];
        xyz[2] = matrix[0] * r + matrix[1] * g + matrix[2] * b;
        xyz[1] = matrix[3] * r + matrix[4] * g + matrix[5] * b;
        xyz[0] = matrix[6] * r + matrix[7] * g + matrix[8] * b;
        return xyz;
    }

    private static final double[] D65_XYZ_TO_RGB = {
            0.4124564, 0.3575761, 0.1804375,
            0.2126729, 0.7151522, 0.0721750,
            0.0193339, 0.1191920, 0.9503041
    };



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "Screen Capture Permission Denied", Toast.LENGTH_SHORT).show();
                return;
            }
            screenCapture.startScreenCapture(data, resultCode);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    screenCapture.captureScreenshotAndSendToServer("http://192.168.0.62:3000/upload");
                }
            }, 1000); // Delay of 1 second
        }
    }

    @SuppressLint("MissingInflatedId")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Find the button in your layout
        Button screenCaptureButton = findViewById(R.id.screenCaptureButton); // replace with your button's ID

// Set an OnClickListener
        screenCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("MOJE", "screenCaptureButton clicked");
                // Start the screen capture when the button is clicked
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(new Intent(MainActivity.this, ScreenCaptureService.class));
                }
                screenCapture = new ScreenCapture(MainActivity.this);
                Intent captureIntent = screenCapture.getProjectionManager().createScreenCaptureIntent();
                startActivityForResult(captureIntent, REQUEST_CODE_SCREEN_CAPTURE);
            }
        });




        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        wigh = screenWidth - 10;
        high = screenWidth - 10;

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        mImageReader = ImageReader.newInstance(wigh, high, ImageFormat.JPEG, 1);
        mImageReader.setOnImageAvailableListener(mImageAvailableListener, null);

        editText = findViewById(R.id.editText);
        String formula = editText.getText().toString();
        double odp = 0;
        double x = 0;




        ButtonChange = findViewById(R.id.button4);
        Button22 = findViewById(R.id.button6);
        textvie = findViewById(R.id.textView2);
        textparam = findViewById(R.id.textView3);
        mImageView = findViewById(R.id.textureView);
        Buttontake = findViewById(R.id.make_photo);
        mImageView.setSurfaceTextureListener(textureListener);
        exposureSeekBar = findViewById(R.id.sbar1);
        eSeekBar = findViewById(R.id.seekBar);
        eSeekBar.setMax(450);

        textform = findViewById(R.id.textView5);

        ISOSeekBar = findViewById(R.id.isoseek);

        textvieEx = findViewById(R.id.textView4);

        textvieiso = findViewById(R.id.textiso);

        textvie4 = findViewById(R.id.textView);

        ramka = findViewById(R.id.imageView);

        ramkaWidth =  ramka.getLayoutParams().width;
        ramkaHeight = ramka.getLayoutParams().height;

        Buttonautoae = findViewById(R.id.buttonauto);

        mImageView.setLayoutParams(new ConstraintLayout.LayoutParams(wigh, high));



        mImageView.setOnTouchListener(new View.OnTouchListener() {
            private static final int NONE = 0;
            private static final int ZOOM = 1;
            private int mode = NONE;
            private float oldDist = 1f;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    handleTouch(event.getX(), event.getY());
//                    //return true;
//                }

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                        oldDist = getFingerSpacing(event);
                        mode = ZOOM;
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == ZOOM) {
                            float newDist = getFingerSpacing(event);
                            if (newDist > oldDist) {
                                // Зум вперед
                                xx++; // Увеличиваем значение xx
                            } else if (newDist < oldDist) {
                                // Зум назад
                                if (xx > 0) {
                                    xx--; // Уменьшаем значение xx, но не допускаем отрицательных значений
                                }
                            }
                            oldDist = newDist;
                        }
                        break;
                }
                return true;
            }

            private float getFingerSpacing(MotionEvent event) {
                float x = event.getX(0) - event.getX(1);
                float y = event.getY(0) - event.getY(1);
                return (float) Math.sqrt(x * x + y * y);
            }
        });



        // Add touch listener to TextureView
        mImageView.setOnTouchListener(new View.OnTouchListener() {



            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    handleTouch(event.getX(), event.getY());
                    return true;
                }
                return false;
            }
        });

        ButtonChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!myCameras[0].isOpen()) {
                    myCameras[1].closeCamera();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            myCameras[0].openCamera();
                        }
                    }, 500); // Add delay to ensure the camera is properly closed
                    choiceCam = 0;
                } else {
                    myCameras[0].closeCamera();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            myCameras[1].openCamera();
                        }
                    }, 500); // Add delay to ensure the camera is properly closed
                    choiceCam = 1;
                }
            }
        });

        Button22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //double x = 6;

                //String formula = editText.getText().toString();
                evaluete = !evaluete;

                if (evaluete){
                    Button22.setBackgroundColor(Color.GREEN);

                }else{

                    Button22.setBackgroundColor(Color.BLUE);
                };

            }
        });


        Buttonautoae.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoea = !autoea;

                if (autoea){
                    Buttonautoae.setBackgroundColor(Color.GREEN);

                }else{

                    Buttonautoae.setBackgroundColor(Color.BLUE);
                };

            }
        });


        Buttontake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myCameras[choiceCam].takePicture();
            }
        });


        exposureSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                     exposureCompensation = progress - exposureSeekBar.getMax() / 2;
                    try {
                        builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                        builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, exposureCompensation);
                        mCaptureSession.setRepeatingRequest(builder.build(), null, null);
                        textvieEx.setText(" "+ exposureCompensation);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Necessary actions when tracking the seek bar starts
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Necessary actions when tracking the seek bar stops
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
            }
        });




        ISOSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int iso = progress ;
                    try {

                        builder.set(CaptureRequest.SENSOR_SENSITIVITY, iso);
                        mCaptureSession.setRepeatingRequest(builder.build(), null, null);
                        textvieiso.setText(String.valueOf(iso));
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Necessary actions when tracking the seek bar starts
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Necessary actions when tracking the seek bar stops

            }
        });




        eSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    //int exposureCompensation = progress - eSeekBar.getMax() / 2;
                    textvie4.setText(String.valueOf(xx));
                    xx = progress;

                    if (xx>5){

                    ramka.getLayoutParams().width = xx+xx;
                    ramka.getLayoutParams().height = xx+xx;

                    drawTouchRectangle();

                    ramka.requestLayout();
                    }
                    else {
                        ramka.getLayoutParams().width = 15;
                        ramka.getLayoutParams().height = 15;

                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Necessary actions when tracking the seek bar starts
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Necessary actions when tracking the seek bar stops
            }
        });


        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            myCameras = new CameraService[mCameraManager.getCameraIdList().length];
            for (String cameraID : mCameraManager.getCameraIdList()) {
                Log.i(LOG_TAG, "cameraID: " + cameraID);
                int id = Integer.parseInt(cameraID);
                myCameras[id] = new CameraService(mCameraManager, cameraID);
            }
        } catch (CameraAccessException e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }

        // Schedule the RGB update task
        startRgbUpdateTask();
    }

    private static double evaluateFormula(String formula) {
        try{
        Expression expression = null;
        Log.w(LOG_TAG, String.valueOf(formula));
        double[] params= {red, blue, green, alfa};
        String[] paramName = {"red", "blue", "green", "alfa"};

        ExpressionBuilder expressionBuilder = new ExpressionBuilder(formula)
                .variables(paramName);

        expression = expressionBuilder.build();

        for (int i = 0; i < paramName.length; i++) {
            expression.setVariable(paramName[i], params[i]);
        }

        double result = expression.evaluate();
        Log.w(LOG_TAG, String.valueOf(result));

        return result;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    private void handleTouch(float x, float y) {
        touchX = x;
        touchY = y;
        updateRgbValues();
        mImageView.invalidate();

        drawTouchRectangle();
    }

    private void updateRgbValues() {
        if (mImageView.getBitmap() != null && touchX >= 0 && touchY >= 0) {
            Bitmap bitmap = mImageView.getBitmap();

            int pixelX = (int) (touchX * bitmap.getWidth() / mImageView.getWidth());
            int pixelY = (int) (touchY * bitmap.getHeight() / mImageView.getHeight());

            int startX = Math.max(0, pixelX - xx);
            int endX = Math.min(bitmap.getWidth() - 1, pixelX + xx);
            int startY = Math.max(0, pixelY - xx);
            int endY = Math.min(bitmap.getHeight() - 1, pixelY + xx);

            int redSum = 0, greenSum = 0, blueSum = 0;
            int pixelCount = 0;
            int exposureAdjustmentNeeded = 0;

            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    int pixelColor = bitmap.getPixel(x, y);
                    int pixelRed = Color.red(pixelColor);
                    int pixelGreen = Color.green(pixelColor);
                    int pixelBlue = Color.blue(pixelColor);

                    if (pixelRed > 252 || pixelGreen > 252 || pixelBlue > 252) {
                        exposureAdjustmentNeeded = 1;
                    }
                    if (pixelRed < 2 || pixelGreen < 2 || pixelBlue < 2) {
                        exposureAdjustmentNeeded = 2;
                    }

                    redSum += pixelRed;
                    greenSum += pixelGreen;
                    blueSum += pixelBlue;
                    pixelCount++;
                }
            }

            int avgRed = redSum / pixelCount;
            int avgGreen = greenSum / pixelCount;
            int avgBlue = blueSum / pixelCount;

            alfa = 255;
            red = avgRed;
            green = avgGreen;
            blue = avgBlue;

            textparam.setText("rgb : " + red + " " + green + " " + blue);


            double estimatedTemperature = estimateTemperature(red, green, blue);
            textvie.setText(String.format("Temperature: %.2f K", estimatedTemperature));

            try {
                if (autoea) {
                    if (exposureAdjustmentNeeded == 1){
                        adjustExposure(-1);}
                    if (exposureAdjustmentNeeded == 2){
                        adjustExposure(+1);}
                    exposureAdjustmentNeeded = 0;
                }


                if (evaluete) {
                    String formula = editText.getText().toString();
                    double odp = (evaluateFormula(formula));
                    textform.setText(String.valueOf(odp));}
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }
    }



    private void adjustExposure(int adjustment) {
        exposureCompensation += adjustment;
        exposureCompensation = Math.max(Math.min(exposureCompensation, exposureSeekBar.getMax() / 2), -exposureSeekBar.getMax() / 2);

        try {
            builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, exposureCompensation);
            mCaptureSession.setRepeatingRequest(builder.build(), null, null);
            textvieEx.setText(" " + exposureCompensation);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startRgbUpdateTask() {
        updateRgbRunnable = new Runnable() {
            @Override
            public void run() {
                updateRgbValues();
                handler.postDelayed(updateRgbRunnable, 200);
            }
        };
        handler.post(updateRgbRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRgbRunnable);
    }

    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            myCameras[choiceCam].openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private void drawTouchRectangle() {
        if (touchX >= 0 && touchY >= 0) {
            ramka.setX(touchX - ramka.getWidth() / 2);
            ramka.setY(touchY - ramka.getHeight() / 2);
        }
    }

    private final ImageReader.OnImageAvailableListener mImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = null;
            try {
                image = reader.acquireLatestImage();
                if (image != null) {
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.capacity()];
                    buffer.get(bytes);
                    saveImage(bytes);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (image != null) {
                    image.close();
                }
            }
        }
    };

    private void saveImage(byte[] bytes) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.e(LOG_TAG, "Error creating media file, check storage permissions");
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(bytes);
            fos.close();
            Log.i(LOG_TAG, "Image saved: " + pictureFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error  file: " + e.getMessage());
        }
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(getExternalFilesDir(null), "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(LOG_TAG, "Failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
    }

    public class CameraService {
        private String mCameraID;
        private CameraDevice mCameraDevice = null;


        public CameraService(CameraManager cameraManager, String cameraID) {
            mCameraManager = cameraManager;
            mCameraID = cameraID;
        }

        private void takePicture() {
            if (mCameraDevice == null) {
                Log.e(LOG_TAG, "Camera is not initialized.");
                return;
            }

            if (mImageReader == null) {
                Log.e(LOG_TAG, "ImageReader is not initialized.");
                return;
            }

            try {
                CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureBuilder.addTarget(mImageReader.getSurface());

                Log.w(LOG_TAG, "zrobione");


                mCaptureSession.stopRepeating();
                mCaptureSession.capture(captureBuilder.build(), null, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        private CameraDevice.StateCallback mCameraCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice camera) {
                mCameraDevice = camera;
                Log.i(LOG_TAG, "Open camera:" + mCameraDevice.getId());
                createCameraPreviewSession();
            }

            @Override
            public void onDisconnected(CameraDevice camera) {
                mCameraDevice.close();
                mCameraDevice = null;
            }

            @Override
            public void onError(CameraDevice camera, int error) {
                Log.i(LOG_TAG, " " + error);
            }
        };

        private void createCameraPreviewSession() {
            SurfaceTexture texture = mImageView.getSurfaceTexture();
            texture.setDefaultBufferSize(wigh, high);
            Surface surface = new Surface(texture);

            try {
                builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                builder.addTarget(surface);

                builder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF);


                  builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);

               // builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, exposureCompensation);


                builder.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_OFF);

                builder.set(CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest.CONTROL_SCENE_MODE_DISABLED); //hdr





                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraID);
                Range<Integer> exposureRange = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
                int minExposure = exposureRange.getLower();
                int maxExposure = exposureRange.getUpper();



                exposureSeekBar.setMax(maxExposure - minExposure);

                exposureSeekBar.setProgress(0);

                Range<Integer> isoRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
                int maxISO = isoRange.getUpper() ;
                int minISO = isoRange.getLower() ;

                Log.w(LOG_TAG,""+ maxISO + minISO);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ISOSeekBar.setMin(minISO);
                }
                ISOSeekBar.setMax(maxISO);





                mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                        new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(CameraCaptureSession session) {
                                mCaptureSession = session;
                                try {
                                    Log.d(LOG_TAG,"Start");
                                    mCaptureSession.setRepeatingRequest(builder.build(), null, null);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onConfigureFailed(CameraCaptureSession session) {
                                Log.e(LOG_TAG, "ce ce ");
                            }
                        }, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        public boolean isOpen() {
            return mCameraDevice != null;
        }

        public void openCamera() {
            try {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    mCameraManager.openCamera(mCameraID, mCameraCallback, null);
                }
            } catch (CameraAccessException e) {
                Log.i(LOG_TAG, e.getMessage());
            }
        }

        public void closeCamera() {
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }
    }

    private void stopRepeatingTask() {
        handler.removeCallbacks(runnableCode);
    }


}


