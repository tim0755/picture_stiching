package com.example.tim_hu.picture_stiching;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        calcRGB();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public int pictureNumber = 2;
    //public String pictureDir = "1";
    final static String inputFile1 = "/go6d/stiching/input/1/image1.jpg";
    final static String inputFile2 = "/go6d/stiching/input/1/image2.jpg";
    final static String outputFile = "/go6d/stiching/input/1/result.jpg";
    Bitmap srcBitmap1;
    Bitmap srcBitmap2;
    long totalR1 = 0;
    long totalG1 = 0;
    long totalB1 = 0;
    long totalR2 = 0;
    long totalG2 = 0;
    long totalB2 = 0;
    public byte[] dataPicture1;
    public byte[] dataPicture2;
    public byte[] dataResult;
    final int overlapWidth_ = 440;

    public void calcRGB() {

        //File sdDir = Environment.getExternalStorageDirectory().getPath();
        //Log.e("tim_hu", "sdDir:"+sdDir);
        //File file1 = new File(sdDir + inputFile1);
        //File file2 = new File(sdDir + inputFile2);

        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Log.e("tim_hu", "sdDir:"+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + inputFile1);
        srcBitmap1 = BitmapFactory.decodeFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + inputFile1, op);
        srcBitmap2 = BitmapFactory.decodeFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + inputFile2, op);
        int overlapWidth = srcBitmap1.getWidth() - overlapWidth_;
        int rgbPixel1 = 0;
        int rgbPixel2 = 0;
        int rgbPixelResult1 = 0;
        int rgbPixelResult2 = 0;
        long time = System.currentTimeMillis();
        for (int indexHeight=0; indexHeight<srcBitmap1.getHeight(); indexHeight++) {
            for (int indexWidth = 0; indexWidth < overlapWidth; indexWidth++) {
                rgbPixel1 = srcBitmap1.getPixel(srcBitmap1.getWidth() - overlapWidth + indexWidth, indexHeight);
                rgbPixel2 = srcBitmap2.getPixel(indexWidth, indexHeight);
                totalR1 += Color.red(rgbPixel1);
                totalG1 += Color.green(rgbPixel1);
                totalB1 += Color.blue(rgbPixel1);
                totalR2 += Color.red(rgbPixel2);
                totalG2 += Color.green(rgbPixel2);
                totalB2 += Color.blue(rgbPixel2);
            }
        }
        long time_calc = System.currentTimeMillis() - time;
        Log.e("tim_hu", "result@width*height: "+srcBitmap1.getWidth()+"*"+srcBitmap1.getHeight());
        Log.e("tim_hu", "result@calc time:"+time_calc);

        double ratioR1 = Math.pow(((float)totalR2/totalR1), 1.0/2);
        double ratioG1 = Math.pow(((float)totalG2/totalG1), 1.0/2);
        double ratioB1 = Math.pow(((float)totalB2/totalB1), 1.0/2);
        double ratioR2 = Math.pow(((float)totalR1/totalR2), 1.0/2);
        double ratioG2 = Math.pow(((float)totalG1/totalG2), 1.0/2);
        double ratioB2 = Math.pow(((float)totalB1/totalB2), 1.0/2);
        Log.e("tim_hu", "result@RGB ratio,r1:"+ratioR1+" g1:"+ratioG1+" b1:"+ratioB1+" r2:"+ratioR2+" g2:"+ratioG2+" b2:"+ratioB2);

        int [] desData = new int[(srcBitmap1.getWidth() + srcBitmap2.getWidth() - overlapWidth) * srcBitmap1.getHeight()];
        double deltaR1 = 2 * (1 - ratioR1) / srcBitmap1.getWidth();
        double deltaG1 = 2 * (1 - ratioG1) / srcBitmap1.getWidth();
        double deltaB1 = 2 * (1 - ratioB1) / srcBitmap1.getWidth();
        double deltaR2 = 2 * (1 - ratioR2) / srcBitmap2.getWidth();
        double deltaG2 = 2 * (1 - ratioG2) / srcBitmap2.getWidth();
        double deltaB2 = 2 * (1 - ratioB2) / srcBitmap2.getWidth();

        time = System.currentTimeMillis();
        int desIndex=0;
        for (int indexHeight = 0; indexHeight < srcBitmap1.getHeight(); indexHeight++) {

            for (int indexWidth = 0; indexWidth < overlapWidth; indexWidth++) {
                rgbPixel1 = srcBitmap1.getPixel(indexWidth, indexHeight);
                rgbPixel2 = srcBitmap2.getPixel(srcBitmap2.getWidth() - overlapWidth + indexWidth, indexHeight);
                desData[desIndex++] = Color.rgb((int) ( Color.red(rgbPixel1) * (ratioR1 + deltaR1 * indexWidth) * (indexWidth/(double)overlapWidth)
                                                        + Color.red(rgbPixel2) * (1 - deltaR2 * ( srcBitmap1.getWidth()/2 + indexWidth)) * (1 - indexWidth/(double)overlapWidth)),
                                                (int) ( Color.green(rgbPixel1) * (ratioG1 + deltaG1 * indexWidth) * (indexWidth/(double)overlapWidth)
                                                        + Color.green(rgbPixel2) * (1 - deltaG2 * ( srcBitmap1.getWidth()/2 + indexWidth)) * (1 - indexWidth/(double)overlapWidth)),
                                                (int) ( Color.blue(rgbPixel1) * (ratioG1 + deltaG1 * indexWidth) * (indexWidth/(double)overlapWidth)
                                                        + Color.blue(rgbPixel2) * (1 - deltaB2 * ( srcBitmap1.getWidth()/2 + indexWidth)) * (1 - indexWidth/(double)overlapWidth)));
            }

            for (int indexWidth = overlapWidth; indexWidth < srcBitmap1.getWidth()/2; indexWidth++) {
                rgbPixel1 = srcBitmap1.getPixel(indexWidth, indexHeight);
                desData[desIndex++] = Color.rgb((int) (Color.red(rgbPixel1)   * (ratioR1 + deltaR1 * indexWidth)),
                                                (int) (Color.green(rgbPixel1) * (ratioG1 + deltaG1 * indexWidth)),
                                                (int) (Color.blue(rgbPixel1)  * (ratioB1 + deltaB1 * indexWidth)));
            }

            for (int indexWidth = srcBitmap1.getWidth()/2; indexWidth < srcBitmap1.getWidth() - overlapWidth; indexWidth++) {
                rgbPixel1 = srcBitmap1.getPixel(indexWidth, indexHeight);
                desData[desIndex++] = Color.rgb((int) (Color.red(rgbPixel1)   * (1 - deltaR1 * (indexWidth - srcBitmap1.getWidth()/2))),
                                                (int) (Color.green(rgbPixel1) * (1 - deltaG1 * (indexWidth - srcBitmap1.getWidth()/2))),
                                                (int) (Color.blue(rgbPixel1)  * (1 - deltaB1 * (indexWidth - srcBitmap1.getWidth()/2))));
            }

            int offsetSrc = srcBitmap1.getWidth() - overlapWidth;
            for(int indexWidth = offsetSrc; indexWidth < srcBitmap1.getWidth(); indexWidth++) {
                rgbPixel1 = srcBitmap1.getPixel(indexWidth, indexHeight);
                rgbPixel2 = srcBitmap2.getPixel(indexWidth - offsetSrc, indexHeight);
                desData[desIndex++] = Color.rgb( (int) ( Color.red(rgbPixel1) * (1 - deltaR1 * (indexWidth - srcBitmap1.getWidth()/2)) * (1.0 - (indexWidth - offsetSrc)/(double)overlapWidth)
                                                       + Color.red(rgbPixel2) * (ratioR2 + deltaR2*(indexWidth- offsetSrc)) * ((indexWidth- offsetSrc)/(double)overlapWidth)),
                                                 (int) ( Color.green(rgbPixel1) * (1 - deltaG1 * (indexWidth - srcBitmap1.getWidth()/2)) * (1.0 - (indexWidth - offsetSrc)/(double)overlapWidth)
                                                       + Color.green(rgbPixel2) * (ratioG2 + deltaG2*(indexWidth- offsetSrc)) * ((indexWidth- offsetSrc)/(double)overlapWidth)),
                                                 (int) ( Color.blue(rgbPixel1) * (1 - deltaB1 * (indexWidth - srcBitmap1.getWidth()/2)) * (1.0 - (indexWidth - offsetSrc)/(double)overlapWidth)
                                                       + Color.blue(rgbPixel2) * (ratioB2 + deltaB2*(indexWidth- offsetSrc)) * ((indexWidth- offsetSrc)/(double)overlapWidth)));
            }

            for (int indexWidth = overlapWidth; indexWidth < srcBitmap2.getWidth()/2; indexWidth++) {
                rgbPixel2 = srcBitmap2.getPixel(indexWidth, indexHeight);
                desData[desIndex++] = Color.rgb((int) (Color.red(rgbPixel2)   * (ratioR2 + deltaR2 * indexWidth)),
                                                (int) (Color.green(rgbPixel2) * (ratioG2 + deltaG2 * indexWidth)),
                                                (int) (Color.blue(rgbPixel2)  * (ratioB2 + deltaB2 * indexWidth)));
            }

            for (int indexWidth = srcBitmap2.getWidth()/2; indexWidth < srcBitmap2.getWidth() - overlapWidth; indexWidth++) {
                rgbPixel2 = srcBitmap2.getPixel(indexWidth, indexHeight);
                desData[desIndex++] = Color.rgb((int) (Color.red(rgbPixel2)   * (1 - deltaR2 * (indexWidth - srcBitmap2.getWidth()/2))),
                                                (int) (Color.green(rgbPixel2) * (1 - deltaG2 * (indexWidth - srcBitmap2.getWidth()/2))),
                                                (int) (Color.blue(rgbPixel2)  * (1 - deltaB2 * (indexWidth - srcBitmap2.getWidth()/2))));
            }

            offsetSrc = srcBitmap2.getWidth() - overlapWidth;
            for(int indexWidth = offsetSrc; indexWidth < srcBitmap2.getWidth(); indexWidth++) {
                rgbPixel1 = srcBitmap1.getPixel(indexWidth - offsetSrc, indexHeight);
                rgbPixel2 = srcBitmap2.getPixel(indexWidth, indexHeight);
                desData[desIndex++] = Color.rgb( (int) ( Color.red(rgbPixel1) * (ratioR1 + deltaR1 * (indexWidth - offsetSrc)) * ((indexWidth - offsetSrc)/(double)overlapWidth)
                                                        + Color.red(rgbPixel2) * (1 - deltaR2*(indexWidth - srcBitmap2.getWidth()/2)) * (1 - (indexWidth - offsetSrc)/(double)overlapWidth)),
                                                (int) (Color.green(rgbPixel1) * (ratioG1 + deltaB1 * (indexWidth - offsetSrc)) * ((indexWidth - offsetSrc)/(double)overlapWidth)
                                                        + Color.green(rgbPixel2) * (1 - deltaG2*(indexWidth - srcBitmap2.getWidth()/2)) * (1 - (indexWidth - offsetSrc)/(double)overlapWidth)),
                                                (int) (Color.blue(rgbPixel1) * (ratioB1 + deltaB1 * (indexWidth - offsetSrc)) * ((indexWidth - offsetSrc)/(double)overlapWidth)
                                                        + Color.blue(rgbPixel2) * (1 - deltaB2*(indexWidth - srcBitmap2.getWidth()/2)) * (1 - (indexWidth - offsetSrc)/(double)overlapWidth)));
            }
        }
        time_calc = System.currentTimeMillis() - time;

        Log.e("tim_hu", "result@fill time:"+time_calc);
        Bitmap bitmap = Bitmap.createBitmap(desData, srcBitmap1.getWidth() + srcBitmap2.getWidth() - overlapWidth, srcBitmap1.getHeight(), Bitmap.Config.ARGB_8888);

        //Bitmap bmp = BitmapFactory.decodeByteArray((byte [])desData, 0, desData.length*4);
        storeImage(bitmap);




        /*File fileOutpot = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + outputFile);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.outWidth = srcBitmap1.getWidth() + srcBitmap2.getWidth() - overlapWidth;
        bmOptions.outHeight = srcBitmap1.getHeight();
        //bmOptions.outMimeType = ;
        Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmapOutput = BitmapFactory.decodeFile(fileOutpot.getAbsolutePath(),bmOptions);
        //Bitmap bitmapOutput = Bitmap.createBitmap(srcBitmap1.getWidth() + srcBitmap2.getWidth() - overlapWidth, srcBitmap1.getHeight(),bitmapConfig);
        //imageView.setImageBitmap(bitmap);

        for (int indexHeight=0; indexHeight<srcBitmap1.getHeight(); indexHeight++) {
            for (int indexWidth = 0; indexWidth < srcBitmap1.getWidth(); indexWidth++) {
                rgbPixel1 = srcBitmap1.getPixel(indexWidth, indexHeight);
                bitmapOutput.setPixel(indexWidth, indexHeight, Color.rgb((int) (Color.red(rgbPixel1) * ratioR1), (int) (Color.green(rgbPixel1) * ratioG1), (int) (Color.blue(rgbPixel1) * ratioB1)));
            }
            for(int indexWidth = overlapWidth; indexWidth < srcBitmap2.getWidth(); indexWidth++) {
                rgbPixel2 = srcBitmap2.getPixel(indexWidth, indexHeight);
                bitmapOutput.setPixel(srcBitmap1.getWidth() + indexWidth, indexHeight, Color.rgb((int) (Color.red(rgbPixel2) * ratioR2), (int) (Color.green(rgbPixel2) * ratioG2), (int) (Color.blue(rgbPixel2) * ratioB2)));
            }
        }*/
    }

    private boolean storeImage(Bitmap imageData/*, String filename*/) {
        /*//get path to external storage (SD card)
        String iconsStoragePath = Environment.getExternalStorageDirectory() + "/myAppDir/myImages/"
        File sdIconStorageDir = new File(iconsStoragePath);

        //create storage directories, if they don't exist
        sdIconStorageDir.mkdirs();*/

        try {
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + outputFile;
            Log.w("tim_hu", "saving image file to: " + filePath);
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);

            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);

            //choose another format if PNG doesn't suit you
            imageData.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            Log.w("tim_hu", "done image file to: " + filePath);

            bos.flush();
            bos.close();
            Log.w("tim_hu", "close image file to: " + filePath);

        } catch (FileNotFoundException e) {
            Log.w("TAG", "Error saving image file: " + e.getMessage());
            return false;
        } catch (IOException e) {
            Log.w("TAG", "Error saving image file: " + e.getMessage());
            return false;
        }

        return true;
    }
}
