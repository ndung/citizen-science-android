package id.go.fimo.step;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.stepstone.stepper.BlockingStep;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import net.alhazmy13.mediapicker.Image.ImagePicker;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import androidx.annotation.RequiresApi;
import id.go.fimo.App;
import id.go.fimo.R;
import id.go.fimo.RecordActivity;
import id.go.fimo.fragment.BaseFragment;
import id.go.fimo.model.Data;
import id.go.fimo.model.Image;
import id.go.fimo.model.SurveyQuestion;
import id.go.fimo.model.User;

import static android.app.Activity.RESULT_OK;

public class ImageStep extends BaseFragment implements BlockingStep {

    public static final String TAG = ImageStep.class.toString();
    //private String mPath;
    //private ImageView ivPreview;
    private int imgPos=999;
    private Data data;
    private SliderLayout slider;
    private List<String> imagePaths;
    private String sectionId;

    public ImageStep(String sectionId){
        this.sectionId = sectionId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.step_image, container, false);
        slider = v.findViewById(R.id.slider);

        slider.setPresetTransformer(SliderLayout.Transformer.Default);
        slider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        slider.setDuration(4000);
        slider.setVisibility(View.VISIBLE);
        //slider.stopAutoCycle();
        slider.addOnPageChangeListener(new ViewPagerEx.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                imgPos = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //ivPreview = v.findViewById(R.id.iv_preview);

        Button capture = v.findViewById(R.id.btn_capture);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImages();
            }
        });

        Button delete = v.findViewById(R.id.btn_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imgPos!=999) {
                    imagePaths.remove(imgPos);
                    slider.removeSliderAt(imgPos);
                    Map<String,List<String>> map = data.getImagePaths();
                    if (map==null){
                        map = new TreeMap<>();
                    }
                    map.put(sectionId,imagePaths);
                    data.setImagePaths(map);
                    App.getInstance().getPreferenceManager().setData(data);
                }
            }
        });
        return v;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> list = data.getStringArrayListExtra(ImagePicker.EXTRA_IMAGE_PATH);
            if (list!=null && list.size()>0){
                if (this.data ==null){
                    this.data = new Data();
                }
                if (this.data.getId()==null){
                    this.data.setId(Settings.Secure.getString(getContext().getContentResolver(),
                            Settings.Secure.ANDROID_ID)+"_"+ UUID.randomUUID());
                }
                if (this.data.getStartDate()==null){
                    this.data.setStartDate(new Date());
                }
                //barrier.setImagePath(imgPath);
                //loadImage();
                if (imagePaths==null){
                    imagePaths = new ArrayList<>();
                }
                for (String imgPath : list) {
                    imagePaths.add(imgPath);
                    DefaultSliderView sliderView = new DefaultSliderView(getActivity());
                    sliderView.image(new File(imgPath)).setScaleType(BaseSliderView.ScaleType.CenterCrop);
                    sliderView.bundle(new Bundle());
                    sliderView.getBundle().putString("extra", imgPath);
                    sliderView.setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                        @Override
                        public void onSliderClick(BaseSliderView slider) {
                            Toast.makeText(getActivity(), slider.getBundle().get("extra") + "", Toast.LENGTH_SHORT).show();
                        }
                    });
                    slider.addSlider(sliderView);
                }
                Map<String,List<String>> map = this.data.getImagePaths();
                if (map==null){
                    map = new TreeMap<>();
                }
                map.put(sectionId,imagePaths);
                this.data.setImagePaths(map);
                App.getInstance().getPreferenceManager().setData(this.data);
            }
        }
    }

    @Override
    public VerificationError verifyStep() {
        if (imagePaths == null || imagePaths.size()==0) {
            return new VerificationError(getResources().getString(R.string.please_select_image_first));
        }
        /**File file = new File(mPath);
        if (!file.exists()){
            return new VerificationError(getResources().getString(R.string.image_file_is_not_exist));
        }*/
        return null;
    }

    @Override
    public void onSelected() {
        data = App.getInstance().getPreferenceManager().getData();
        //update UI when selected
        if (data !=null && data.getImagePaths()!=null && data.getImagePaths().get(sectionId)!=null){
            imagePaths = data.getImagePaths().get(sectionId);
            //mPath = barrier.getImagePath();
            //loadImage();
        }
        if (data !=null && imagePaths!=null){
            slider.removeAllSliders();
            for (String path : imagePaths){
                DefaultSliderView sliderView = new DefaultSliderView(getActivity());
                sliderView.image(new File(path)).setScaleType(BaseSliderView.ScaleType.CenterCrop);
                sliderView.bundle(new Bundle());
                sliderView.getBundle().putString("extra",path);
                sliderView.setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                    @Override
                    public void onSliderClick(BaseSliderView slider) {
                        Toast.makeText(getActivity(),slider.getBundle().get("extra") + "",Toast.LENGTH_SHORT).show();
                    }
                });
                slider.addSlider(sliderView);
            }
        }
    }

    @Override
    public void onError(@NonNull VerificationError error) {
        showSnackbar(error.getErrorMessage());
    }

    private void loadImage() {
        //Bitmap bitmap = BitmapFactory.decodeFile(mPath);
        //ivPreview.setImageBitmap(bitmap);
    }

    private void pickImages(){
        RecordActivity.SECTION = sectionId;
        new ImagePicker.Builder(getActivity())
                .mode(ImagePicker.Mode.CAMERA_AND_GALLERY)
                .compressLevel(ImagePicker.ComperesLevel.NONE)
                .extension(ImagePicker.Extension.JPG)
                .directory(ImagePicker.Directory.DEFAULT)
                .allowMultipleImages(true)
                .enableDebuggingMode(true)
                .build();
        //Intent intent = new Intent();
        //intent.setType("image/*");
        //intent.setAction(Intent.ACTION_GET_CONTENT);
        //startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    @Override
    public void onNextClicked(StepperLayout.OnNextClickedCallback onNextClickedCallback) {
        onNextClickedCallback.goToNextStep();
    }

    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback onCompleteClickedCallback) {
        onCompleteClickedCallback.complete();
    }

    @Override
    public void onBackClicked(StepperLayout.OnBackClickedCallback onBackClickedCallback) {
        onBackClickedCallback.goToPrevStep();
    }
}
