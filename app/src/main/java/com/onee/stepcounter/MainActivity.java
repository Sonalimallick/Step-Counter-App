package com.onee.stepcounter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.maps.model.LatLng;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener
{
    private TextView textView, max, power, distance_in_metre, calories, appname, speed_textView,greet;
    private double magnitudePrevious = 0;
    private static final String CHANNEL_ID = "My Channel";
    private static final int NOTIFICATION_ID = 100;
    private Integer stepCount = 0;
    public ImageView imageView;
    private CircularProgressBar circularProgressBar;
    private TextView address_disp,degree;
    private Integer steps = 1000;
    private NotificationManager nm;
    private String address,city,country;
    private Notification notification;
    private String lastMode, modeToBeDisplayed;
    private Sensor magnetometersensor;
    LottieAnimationView lottie, lottieburn, lottiewalk, lottiespeed;
    Switch sw;
    SensorManager sensorManager;
    Sensor sensor,accelerometersensor;
    boolean flag = false;
    boolean isLastAccelerometerArrayCopied=false;
    boolean isLastMagnetometerArrayCopied=false;

    private float[] lastAccelerometer=new float[3];
    private float[] lastMagnetometer=new float[3];
    private float[] rotationMatrix=new float[9];
    private float[] orientation=new float[3];

    long lastUpdatedTime=0;
    float currentDegree=0f;

    // Hello, just updating.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        textView=findViewById(R.id.tv_stepsTaken);
        imageView=findViewById(R.id.compassimage);
        greet=findViewById(R.id.greet);
        degree=findViewById(R.id.degree);
        address_disp=findViewById(R.id.locationdisp);
        speed_textView=findViewById(R.id.speed_textView);
        appname=findViewById(R.id.appnamedisp);
        max=findViewById(R.id.tv_totalMax);
        calories=findViewById(R.id.TV_CALORIES);
        sw=findViewById(R.id.switch1);
        lottie=findViewById(R.id.lottieeee);
        lottiewalk=findViewById(R.id.lottieswalk);
        lottieburn=findViewById(R.id.lottiesburn);
        lottiespeed=findViewById(R.id.lottiesspeed);
        distance_in_metre=findViewById(R.id.TV_DISTANCE);
        circularProgressBar=findViewById(R.id.progg);

        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        
        lottie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,Info.class);
                startActivity(intent);
                finish();
                System.gc();
            }
        });


        lottie.setRepeatCount(Animation.INFINITE);
        lottiespeed.setRepeatCount(Animation.INFINITE);
        lottieburn.setRepeatCount(Animation.INFINITE);
        lottiewalk.setRepeatCount(Animation.INFINITE);

        String greeting="";
        Date date=new Date();


        if(date.getHours()>=0 && date.getHours()<=4)
            greeting="Good Evening !";
        else if(date.getHours()>4 && date.getHours()<12)
            greeting="A very Good Morning to you !";
        else if(date.getHours()>=12 && date.getHours()<=17)
            greeting="Good afternoon ! Stay healthy and happy";
        else
            greeting="Good evening ! Get a evening stroll";

        greet.setText(greeting);


        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            Toast.makeText(this, "Please grant permission for speedometer", Toast.LENGTH_SHORT).show();
        }
        else
        {
            doStuff();
        }

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    stepCount-=1;
                    lastMode="night";
                }
                else
                {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    stepCount-=1;
                    lastMode="day";
                };
            }
        });



        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.step_counter_icon)
                    .setContentTitle("Step Counter")
                    .setContentText("Congratulation on achieving your target !")
                    .setSubText(new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date()))
                    .setChannelId(CHANNEL_ID)
                    .setColor(Color.CYAN)
                    .build();

            nm.createNotificationChannel(new NotificationChannel(CHANNEL_ID,"New Channel",NotificationManager.IMPORTANCE_HIGH));
        }
        else
        {
            notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.step_counter_icon)
                    .setContentTitle("Step Counter")
                    .setContentText("Congratulation on achieving your target !")
                    .setSubText(new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date()))
                    .setColor(Color.CYAN)
                    .build();
        }


        //power.setText("Power consumed by accelerometer sensor : "+String.valueOf(sensor.getPower())+" µA");

        circularProgressBar.setProgressMax(steps);

        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view)
            {
                Toast.makeText(MainActivity.this, "Steps set to 0", Toast.LENGTH_SHORT).show();
                stepCount=0;
                circularProgressBar.setProgressWithAnimation(stepCount);
                steps=1000;
                textView.setText(String.valueOf(stepCount));
                circularProgressBar.setProgressMax(steps);
                max.setText(String.valueOf(steps));
                return false;
            }
        });

        max.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view)
            {
                Toast.makeText(MainActivity.this, "Steps Limit increased by 1000", Toast.LENGTH_SHORT).show();
                steps=steps+1000;
                max.setText("/ "+String.valueOf(steps));
                circularProgressBar.setProgressWithAnimation(stepCount);
                circularProgressBar.setProgressMax(steps);
                flag=false;
                return false;
            }
        });

        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometersensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometersensor=sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        ACC ob=new ACC();
        MAG ob2=new MAG();
        ob.start();
        ob2.start();

    }

    class MAG extends Thread {
        public void run() {
            SensorEventListener m=new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {

                    if (sensorEvent.sensor == accelerometersensor) {
                        System.arraycopy(sensorEvent.values, 0, lastAccelerometer, 0, sensorEvent.values.length);
                        isLastAccelerometerArrayCopied = true;
                    } else if (sensorEvent.sensor == magnetometersensor) {
                        System.arraycopy(sensorEvent.values, 0, lastMagnetometer, 0, sensorEvent.values.length);
                        isLastMagnetometerArrayCopied = true;

                        float azimuth = Math.round(sensorEvent.values[0]);
                        float pitch = Math.round(sensorEvent.values[1]);
                        float roll = Math.round(sensorEvent.values[2]);

                        double tesla = Math.sqrt((azimuth * azimuth) + (pitch * pitch) + (roll * roll));

                        String s = String.format("%.1f", tesla);
                    }

                    if (isLastAccelerometerArrayCopied && isLastMagnetometerArrayCopied && System.currentTimeMillis() - lastUpdatedTime > 900) {
                        SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer);
                        SensorManager.getOrientation(rotationMatrix, orientation);

                        float azimuthInRadian = orientation[0];
                        float azimuthInDegree = (float) Math.toDegrees(azimuthInRadian);

                        RotateAnimation rotateAnimation = new RotateAnimation(currentDegree, -azimuthInDegree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        rotateAnimation.setDuration(900);
                        rotateAnimation.setFillAfter(true);
                        imageView.startAnimation(rotateAnimation);

                        currentDegree = -azimuthInDegree;
                        lastUpdatedTime = System.currentTimeMillis();

                        int x=(int)azimuthInDegree;
                        String direction="";

                        if(x>=-22 && x<=22)
                            direction="N";
                        else if(x>=23 && x<=67)
                            direction="NE";
                        else if(x>=68 && x<=112)
                            direction="E";
                        else if(x>=113 && x<=157)
                            direction="SE";
                        else if(x>=158 || x<=-158)
                            direction="S";
                        else if(x>=-157 && x<=-113)
                            direction="SW";
                        else if(x>=-112 && x<=-68)
                            direction="W";
                        else if(x>=-67 && x<=-23)
                            direction="NW";

                        degree.setText(x+"° "+direction);
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {

                }
            };
            sensorManager.registerListener(m,accelerometersensor,SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(m,magnetometersensor,SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    class ACC extends Thread
    {
        public void run()
        {
            SensorEventListener stepDetector=new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent)
                {

                    if(sensorEvent!=null)
                    {
                        if(stepCount>=steps && !flag)
                        {
                            nm.notify(NOTIFICATION_ID, notification);
                            flag=true;
                            Toast.makeText(MainActivity.this, "Congratulations on achieving your target", Toast.LENGTH_SHORT).show();
                        }

                        float x_acceleration=sensorEvent.values[0];
                        float y_acceleration=sensorEvent.values[1];
                        float z_acceleration=sensorEvent.values[2];

                        double Magnitude=Math.sqrt(x_acceleration*x_acceleration + y_acceleration*y_acceleration + z_acceleration*z_acceleration);
                        double magnitudeDelta=Magnitude-magnitudePrevious;
                        magnitudePrevious=Magnitude;

                        if(magnitudeDelta>4)
                        {
                            stepCount++;
                            circularProgressBar.setProgressWithAnimation(stepCount);
                        }
                        textView.setText(stepCount.toString());

                        String cal=String.format("%.1f",stepCount*0.037);
                        calories.setText(cal+" cal");

                        int feet=(int)(stepCount*2.5);
                        double d=feet/3.281;
                        String suffix=" m";
                        if(d>=1000)
                        {
                            d /= 1000;
                            suffix = " km";
                        }


                        String dist=String.format("%.2f",d);
                        distance_in_metre.setText(dist+suffix);
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {
                }
            };
            sensorManager.registerListener(stepDetector,sensor,SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences sharedPreferences=getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.clear();
        editor.putInt("stepCount",stepCount);
        editor.putInt("max",steps);
        editor.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences sharedPreferences=getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.clear();
        editor.putInt("stepCount",stepCount);
        editor.putInt("max",steps);
        editor.putString("lastMode",lastMode);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

       SharedPreferences sharedPreferences=getPreferences(MODE_PRIVATE);
       stepCount=sharedPreferences.getInt("stepCount",0);
       steps=sharedPreferences.getInt("max",1000);
       modeToBeDisplayed=sharedPreferences.getString("lastMode","day");
       if(modeToBeDisplayed.equals("night"))
       {
           AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
           sw.setChecked(true);
       }
       else
       {
           AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
       }
       max.setText("/ "+String.valueOf(steps));
       circularProgressBar.setProgressMax(steps);
       speed_textView.setText("Fetching...");
       address_disp.setText("Finding where you are...");
    }



    @Override
    public void onLocationChanged(@NonNull Location location)
    {
        if(location!=null)
        {
            CLocation myLocation=new CLocation(location,this.useMetricUnits());
            this.updateSpeed(myLocation);

            try {
                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                address=addresses.get(0).getAddressLine(0);
                city=addresses.get(0).getLocality();
                country=addresses.get(0).getCountryName();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    @SuppressLint("MissingPermission")
    private void doStuff()
    {
        LocationManager locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if(locationManager!=null)
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
        }
    }

    private void updateSpeed(CLocation location)
    {
        float nCurrentSpeed=0;
        if(location==null)
        {
            address_disp.setText("Searching your current location... ");
        }
        if(location != null)
        {
            location.setUserMetricUnits(this.useMetricUnits());
            nCurrentSpeed=location.getSpeed();
            address_disp.setText("You are at "+address);
        }

        Formatter fmt=new Formatter(new StringBuilder());
        fmt.format(Locale.US,"%.6f",nCurrentSpeed);
        String strCurrentSpeed=fmt.toString();
        strCurrentSpeed=strCurrentSpeed.replace(" ","0");

        if(this.useMetricUnits())
        {
            speed_textView.setText(strCurrentSpeed+" km/h");
        }
        else
        {
            speed_textView.setText("Calculating speed...");
        }
    }

    private boolean useMetricUnits()
    {
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doStuff();
            }
        }
    }
}


