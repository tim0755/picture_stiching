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

    private Bitmap srcBitmap1;
    private Bitmap srcBitmap2;
    private int rgbPixel1;
    private int rgbPixel2;
    private long totalR1 = 0, totalG1 = 0, totalB1 = 0;
    private long totalR2 = 0, totalG2 = 0, totalB2 = 0;
    private double ratioR1, ratioG1, ratioB1;
    private double ratioR2, ratioG2, ratioB2;
    private double deltaR1, deltaG1, deltaB1;
    private double deltaR2, deltaG2, deltaB2;
    private final int overlapWidth_ = 440;
    private int overlapWidth;
    private int desIndex;
    private int [] desData;
    private String inputDir;
    private final static String inputFile1 = "/go6d/stiching/input/1/image1.jpg";
    private final static String inputFile2 = "/go6d/stiching/input/1/image2.jpg";

    //support correctionMode:
    //1: all pixel adjust by coefficients for picture A, all pixel except overlay adjust by coefficients' for picture B
    //2: first,all pixel adjust by coefficients for picture A; all pixel adjust by coefficients' for picture B. then, for the overlay, blend pixel from A and pixel from B by weight
    //3: first, all pixel adjust by coefficients by weight for picture A; all pixel adjust by coefficients' by weight for picture B. then, for the overlay , blend pixel from A and pixel from B by weight
    //4: base on mode 3 above, consider the picture A and picture B have two overlay block. the left of A overlay to right of B; the right of A overlay to left of B.
    public void calcRGB() {
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inPreferredConfig = Bitmap.Config.ARGB_8888;
        inputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
        Log.e("tim_hu", "sdDir:" + inputDir + inputFile1);
        srcBitmap1 = BitmapFactory.decodeFile(inputDir + inputFile1, op);
        srcBitmap2 = BitmapFactory.decodeFile(inputDir + inputFile2, op);
        overlapWidth = srcBitmap1.getWidth() - overlapWidth_;

        desData = new int[(srcBitmap1.getWidth() + srcBitmap2.getWidth() - overlapWidth) * srcBitmap1.getHeight()];

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
        Log.e("tim_hu", "result@width*height: "+srcBitmap1.getWidth()+"*"+srcBitmap1.getHeight()+" calc rate time:"+(System.currentTimeMillis() - time));

        ratioR1 = Math.pow(((float)totalR2/totalR1), 1.0/2);
        ratioG1 = Math.pow(((float)totalG2/totalG1), 1.0/2);
        ratioB1 = Math.pow(((float)totalB2/totalB1), 1.0/2);
        ratioR2 = Math.pow(((float)totalR1/totalR2), 1.0/2);
        ratioG2 = Math.pow(((float)totalG1/totalG2), 1.0/2);
        ratioB2 = Math.pow(((float)totalB1/totalB2), 1.0/2);
        Log.e("tim_hu", "result@RGB ratio,r1:"+ratioR1+" g1:"+ratioG1+" b1:"+ratioB1+" r2:"+ratioR2+" g2:"+ratioG2+" b2:"+ratioB2);

        fillImageAdjust();
        fillImageAdjustOverlayBlend();
        fillImageAdjustShadingOverlayBlend();
        fillImageAdjustShadingTwoOverlayBlend();

        Log.e("tim_hu", "result@finish time:" + (System.currentTimeMillis() - time));
    }

    private boolean fillImageAdjust(){

        long time = System.currentTimeMillis();
        desIndex=0;
        for (int indexHeight = 0; indexHeight < srcBitmap1.getHeight(); indexHeight++) {
            for (int indexWidth = 0; indexWidth < srcBitmap1.getWidth(); indexWidth++) {
                rgbPixel1 = srcBitmap1.getPixel(indexWidth, indexHeight);
                desData[desIndex++] = Color.rgb((int) (Color.red(rgbPixel1) * ratioR1), (int) (Color.green(rgbPixel1) * ratioG1), (int) (Color.blue(rgbPixel1) * ratioB1));
            }
            for (int indexWidth = overlapWidth; indexWidth < srcBitmap2.getWidth(); indexWidth++) {
                rgbPixel2 = srcBitmap2.getPixel(indexWidth, indexHeight);
                desData[desIndex++] = Color.rgb((int) (Color.red(rgbPixel2) * ratioR2), (int) (Color.green(rgbPixel2) * ratioG2), (int) (Color.blue(rgbPixel2) * ratioB2));
            }
        }
        Log.e("tim_hu", "result@fillImageAdjust time:" + (System.currentTimeMillis() - time));

        Bitmap bitmap = Bitmap.createBitmap(desData, srcBitmap1.getWidth() + srcBitmap2.getWidth() - overlapWidth, srcBitmap1.getHeight(), Bitmap.Config.ARGB_8888);
        storeImage(bitmap, inputDir+"/go6d/stiching/input/1/resultAdjust.jpg");
        return true;
    }
    private boolean fillImageAdjustOverlayBlend(){

        desIndex=0;

        long time = System.currentTimeMillis();
        for (int indexHeight = 0; indexHeight < srcBitmap1.getHeight(); indexHeight++) {
            for (int indexWidth = 0; indexWidth < srcBitmap1.getWidth() - overlapWidth; indexWidth++) {
                rgbPixel1 = srcBitmap1.getPixel(indexWidth, indexHeight);
                desData[desIndex++] = Color.rgb((int) (Color.red(rgbPixel1) * ratioR1), (int) (Color.green(rgbPixel1) * ratioG1), (int) (Color.blue(rgbPixel1) * ratioB1));
            }
            for (int indexWidth = srcBitmap1.getWidth() - overlapWidth; indexWidth < srcBitmap1.getWidth(); indexWidth++) {
                rgbPixel1 = srcBitmap1.getPixel(indexWidth, indexHeight);
                rgbPixel2 = srcBitmap2.getPixel(indexWidth - (srcBitmap1.getWidth() - overlapWidth), indexHeight);
                desData[desIndex++] = Color.rgb(
                        (int) ((Color.red(rgbPixel1) * ratioR1   * (1 - (indexWidth - (srcBitmap1.getWidth() - overlapWidth))/(double)overlapWidth) + Color.red(rgbPixel2)   * ratioR2 * (indexWidth - (srcBitmap1.getWidth() - overlapWidth))/(double)overlapWidth)),
                        (int) ((Color.green(rgbPixel1) * ratioG1 * (1 - (indexWidth - (srcBitmap1.getWidth() - overlapWidth))/(double)overlapWidth) + Color.green(rgbPixel2) * ratioG2 * (indexWidth - (srcBitmap1.getWidth() - overlapWidth))/(double)overlapWidth)),
                        (int) ((Color.blue(rgbPixel1) * ratioB1  * (1 - (indexWidth - (srcBitmap1.getWidth() - overlapWidth))/(double)overlapWidth) + Color.blue(rgbPixel2)  * ratioB2 * (indexWidth - (srcBitmap1.getWidth() - overlapWidth))/(double)overlapWidth)));
            }
            for (int indexWidth = overlapWidth; indexWidth < srcBitmap2.getWidth(); indexWidth++) {
                rgbPixel2 = srcBitmap2.getPixel(indexWidth, indexHeight);
                desData[desIndex++] = Color.rgb((int) (Color.red(rgbPixel2) * ratioR2), (int) (Color.green(rgbPixel2) * ratioG2), (int) (Color.blue(rgbPixel2) * ratioB2));
            }
        }
        Log.e("tim_hu", "result@fillImageAdjustOverlayBlend time:" + (System.currentTimeMillis() - time));

        Bitmap bitmap = Bitmap.createBitmap(desData, srcBitmap1.getWidth() + srcBitmap2.getWidth() - overlapWidth, srcBitmap1.getHeight(), Bitmap.Config.ARGB_8888);
        storeImage(bitmap, inputDir+"/go6d/stiching/input/1/resultAdjustOverlayBlend.jpg");
        return true;
    }
    private boolean fillImageAdjustShadingOverlayBlend(){

        deltaR1 = 2 * (1 - ratioR1) / srcBitmap1.getWidth();
        deltaG1 = 2 * (1 - ratioG1) / srcBitmap1.getWidth();
        deltaB1 = 2 * (1 - ratioB1) / srcBitmap1.getWidth();
        deltaR2 = 2 * (1 - ratioR2) / srcBitmap2.getWidth();
        deltaG2 = 2 * (1 - ratioG2) / srcBitmap2.getWidth();
        deltaB2 = 2 * (1 - ratioB2) / srcBitmap2.getWidth();
        desIndex=0;

        long time = System.currentTimeMillis();
        for (int indexHeight = 0; indexHeight < srcBitmap1.getHeight(); indexHeight++) {
            for (int indexWidth = 0; indexWidth < srcBitmap1.getWidth() - overlapWidth; indexWidth++) {
                rgbPixel1 = srcBitmap1.getPixel(indexWidth, indexHeight);
                desData[desIndex++] = Color.rgb(
                        (int) (Color.red(rgbPixel1) * (1 - deltaR1 * indexWidth)),
                        (int) (Color.green(rgbPixel1) * (1 - deltaG1 * indexWidth)),
                        (int) (Color.blue(rgbPixel1) * (1 - deltaB1 * indexWidth)));
            }
            for (int indexWidth = 0; indexWidth < overlapWidth; indexWidth++) {
                rgbPixel1 = srcBitmap1.getPixel(srcBitmap1.getWidth() - overlapWidth + indexWidth, indexHeight);
                rgbPixel2 = srcBitmap2.getPixel(indexWidth, indexHeight);
                desData[desIndex++] = Color.rgb(
                        (int) (Color.red(rgbPixel1) * (1 - deltaR1 * (srcBitmap1.getWidth() - overlapWidth + indexWidth)) * (1.0 - indexWidth / (double) overlapWidth)
                                + Color.red(rgbPixel2) * (ratioR2 + deltaR2 * indexWidth) * (indexWidth / (double) overlapWidth)),
                        (int) (Color.green(rgbPixel1) * (1 - deltaG1 * (srcBitmap1.getWidth() - overlapWidth + indexWidth)) * (1.0 - indexWidth / (double) overlapWidth)
                                + Color.green(rgbPixel2) * (ratioG2 + deltaG2 * indexWidth) * (indexWidth / (double) overlapWidth)),
                        (int) (Color.blue(rgbPixel1) * (1 - deltaB1 * (srcBitmap1.getWidth() - overlapWidth + indexWidth)) * (1.0 - indexWidth / (double) overlapWidth)
                                + Color.blue(rgbPixel2) * (ratioB2 + deltaB2 * indexWidth) * (indexWidth / (double) overlapWidth)));
            }
            for (int indexWidth = overlapWidth; indexWidth < srcBitmap2.getWidth(); indexWidth++) {
                rgbPixel2 = srcBitmap2.getPixel(indexWidth, indexHeight);
                desData[desIndex++] = Color.rgb((int) (Color.red(rgbPixel2) * (ratioR2 + deltaR2 * indexWidth)),
                        (int) (Color.green(rgbPixel2) * (ratioG2 + deltaG2 * indexWidth)),
                        (int) (Color.blue(rgbPixel2) * (ratioB2 + deltaB2 * indexWidth)));
            }
        }
        Log.e("tim_hu", "result@fillImageAdjustShadingOverlayBlend time:" + (System.currentTimeMillis() - time));

        Bitmap bitmap = Bitmap.createBitmap(desData, srcBitmap1.getWidth() + srcBitmap2.getWidth() - overlapWidth, srcBitmap1.getHeight(), Bitmap.Config.ARGB_8888);
        storeImage(bitmap, inputDir+"/go6d/stiching/input/1/resultAdjustShadingOverlayBlend.jpg");
        return true;
    }
    private boolean fillImageAdjustShadingTwoOverlayBlend(){
        //1. 图片A和B的中间保持颜色值不变
        //2. 然后从中间往两边渐变，到最边缘渐变为Ca*Pa和Cb*Pb。其中Cn表示n图片某点的颜色值，Pn表示n图片的校正系数。Pa*Pb==1
        //3. 两个图片的重叠区取两个图片的加权平均值，其中权值取该点在重叠区内的偏移量，该点靠近A，则A的权值更大。两个权值的和为1
        //4. 对于双目的来说，A图的左边和B图的右边重叠，A图的右边和B图的左边重叠。需要计算两个重叠区

        deltaR1 = 2 * (1 - ratioR1) / srcBitmap1.getWidth();
        deltaG1 = 2 * (1 - ratioG1) / srcBitmap1.getWidth();
        deltaB1 = 2 * (1 - ratioB1) / srcBitmap1.getWidth();
        deltaR2 = 2 * (1 - ratioR2) / srcBitmap2.getWidth();
        deltaG2 = 2 * (1 - ratioG2) / srcBitmap2.getWidth();
        deltaB2 = 2 * (1 - ratioB2) / srcBitmap2.getWidth();
        desIndex=0;

        long time = System.currentTimeMillis();
        for (int indexHeight = 0; indexHeight < srcBitmap1.getHeight(); indexHeight++) {
            for (int indexWidth = 0; indexWidth < overlapWidth; indexWidth++) {
                rgbPixel1 = srcBitmap1.getPixel(indexWidth, indexHeight);
                rgbPixel2 = srcBitmap2.getPixel(srcBitmap2.getWidth() - overlapWidth + indexWidth, indexHeight);
                desData[desIndex++] = Color.rgb(
                        (int) ( Color.red(rgbPixel1) * (ratioR1 + deltaR1 * indexWidth) * (indexWidth/(double)overlapWidth)
                                + Color.red(rgbPixel2) * (1 - deltaR2 * ( srcBitmap1.getWidth()/2 + indexWidth)) * (1 - indexWidth/(double)overlapWidth)),
                        (int) ( Color.green(rgbPixel1) * (ratioG1 + deltaG1 * indexWidth) * (indexWidth/(double)overlapWidth)
                                + Color.green(rgbPixel2) * (1 - deltaG2 * ( srcBitmap1.getWidth()/2 + indexWidth)) * (1 - indexWidth/(double)overlapWidth)),
                        (int) ( Color.blue(rgbPixel1) * (ratioG1 + deltaG1 * indexWidth) * (indexWidth/(double)overlapWidth)
                                + Color.blue(rgbPixel2) * (1 - deltaB2 * ( srcBitmap1.getWidth()/2 + indexWidth)) * (1 - indexWidth/(double)overlapWidth)));
            }
            for (int indexWidth = overlapWidth; indexWidth < srcBitmap1.getWidth()/2; indexWidth++) {
                rgbPixel1 = srcBitmap1.getPixel(indexWidth, indexHeight);
                desData[desIndex++] = Color.rgb(
                        (int) (Color.red(rgbPixel1)   * (ratioR1 + deltaR1 * indexWidth)),
                        (int) (Color.green(rgbPixel1) * (ratioG1 + deltaG1 * indexWidth)),
                        (int) (Color.blue(rgbPixel1)  * (ratioB1 + deltaB1 * indexWidth)));
            }
            for (int indexWidth = srcBitmap1.getWidth()/2; indexWidth < srcBitmap1.getWidth() - overlapWidth; indexWidth++) {
                rgbPixel1 = srcBitmap1.getPixel(indexWidth, indexHeight);
                desData[desIndex++] = Color.rgb(
                        (int) (Color.red(rgbPixel1)   * (1 - deltaR1 * (indexWidth - srcBitmap1.getWidth()/2))),
                        (int) (Color.green(rgbPixel1) * (1 - deltaG1 * (indexWidth - srcBitmap1.getWidth()/2))),
                        (int) (Color.blue(rgbPixel1)  * (1 - deltaB1 * (indexWidth - srcBitmap1.getWidth()/2))));
            }
            int offsetSrc = srcBitmap1.getWidth() - overlapWidth;
            for(int indexWidth = offsetSrc; indexWidth < srcBitmap1.getWidth(); indexWidth++) {
                rgbPixel1 = srcBitmap1.getPixel(indexWidth, indexHeight);
                rgbPixel2 = srcBitmap2.getPixel(indexWidth - offsetSrc, indexHeight);
                desData[desIndex++] = Color.rgb(
                        (int) ( Color.red(rgbPixel1) * (1 - deltaR1 * (indexWidth - srcBitmap1.getWidth()/2)) * (1.0 - (indexWidth - offsetSrc)/(double)overlapWidth)
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
                desData[desIndex++] = Color.rgb(
                        (int) ( Color.red(rgbPixel1) * (ratioR1 + deltaR1 * (indexWidth - offsetSrc)) * ((indexWidth - offsetSrc)/(double)overlapWidth)
                                + Color.red(rgbPixel2) * (1 - deltaR2*(indexWidth - srcBitmap2.getWidth()/2)) * (1 - (indexWidth - offsetSrc)/(double)overlapWidth)),
                        (int) (Color.green(rgbPixel1) * (ratioG1 + deltaB1 * (indexWidth - offsetSrc)) * ((indexWidth - offsetSrc)/(double)overlapWidth)
                                + Color.green(rgbPixel2) * (1 - deltaG2*(indexWidth - srcBitmap2.getWidth()/2)) * (1 - (indexWidth - offsetSrc)/(double)overlapWidth)),
                        (int) (Color.blue(rgbPixel1) * (ratioB1 + deltaB1 * (indexWidth - offsetSrc)) * ((indexWidth - offsetSrc)/(double)overlapWidth)
                                + Color.blue(rgbPixel2) * (1 - deltaB2*(indexWidth - srcBitmap2.getWidth()/2)) * (1 - (indexWidth - offsetSrc)/(double)overlapWidth)));
            }
        }
        Log.e("tim_hu", "result@fillImageAdjustShadingTwoOverlayBlend time:" + (System.currentTimeMillis() - time));

        Bitmap bitmap = Bitmap.createBitmap(desData, srcBitmap1.getWidth() + srcBitmap2.getWidth() - overlapWidth, srcBitmap1.getHeight(), Bitmap.Config.ARGB_8888);
        storeImage(bitmap, inputDir+"/go6d/stiching/input/1/resultAdjustShadingTwoOverlayBlend.jpg");
        return true;
    }

    private boolean storeImage(Bitmap imageData, String filename) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filename);
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            imageData.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            Log.w("tim_hu", "close image file to: " + filename);
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
