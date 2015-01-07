package org.openplaces.widgets;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.text.style.ReplacementSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView;


import org.openplaces.MapActivity;
import org.openplaces.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ggiammat on 11/12/14.
 */
public class OPChipsEditText extends EditText {
    private static final String TAG = "OPChipsEditText";


    public static final String CHIPS_SEPARATOR = ",";

    private int lastChipEnd = -1;

    private Map<ReplacementSpan, Object> chipsRelatedObjects = new HashMap<ReplacementSpan, Object>();


    private List<ChipsWatcher> chipsWatchers = new ArrayList<ChipsWatcher>();

    private List<TextWatcher> textChangedListeners = new ArrayList<TextWatcher>();

    public OPChipsEditText(Context context) {
        super(context);
        this.init();
    }

    public OPChipsEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public OPChipsEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init();
    }

    private void init(){
        addTextChangedListener(textChangeListener);
    }


    @Override
    public void addTextChangedListener(TextWatcher watcher) {
        super.addTextChangedListener(watcher);
        this.textChangedListeners.add(watcher);
    }

    @Override
    public void removeTextChangedListener(TextWatcher watcher) {
        super.removeTextChangedListener(watcher);
        this.textChangedListeners.remove(watcher);
    }

    private void disableTextChangedListeners(){
        for(TextWatcher tw: this.textChangedListeners){
            super.removeTextChangedListener(tw);
        }
    }

    private void enableTextChangedListeners(){
        for(TextWatcher tw: this.textChangedListeners){
            super.addTextChangedListener(tw);
        }
    }

    public void appendChip(String text, Object relatedObject){

        //TODO: what if I want multiple chips to have same related object?
        if(this.chipsRelatedObjects.values().contains(relatedObject)){
            Log.d(MapActivity.LOGTAG, "Chips already exists. Do not adding again");
            return;
        }

        this.disableTextChangedListeners();

        String textToReplace = this.getLastUnchipedToken();


        Log.d(MapActivity.LOGTAG, "Adding chip with text " + text + " replacing " + textToReplace);
        Log.d(MapActivity.LOGTAG, "lastChipEnd is  " + this.lastChipEnd);


        int start = this.getLastTokenStart();
        int end = start + text.length();
        Log.d(MapActivity.LOGTAG, "start is "  + start);
        Log.d(MapActivity.LOGTAG, "End is " + end);




//        int start = -1 ;
//        int stop = -1;
//        if(replacedText == null || "".equals(replacedText.trim())){
//            start = this.getText().length();
//            this.getText().append(text);
//            stop = this.getText().length() - 1;
//        }
//        else {
//            start = this.getText().toString().indexOf(textToReplace);
//            stop = start + textToReplace.length() - 1;
//            this.getText().replace(start, stop, text);
//        }

        this.getText().replace(start, this.getText().length(), "");
        this.getText().append(text);
        this.getText().append(CHIPS_SEPARATOR);

        Log.d(MapActivity.LOGTAG, "Setting lastChipEnd to " + (end + 1));

        this.lastChipEnd = end;


        LayoutInflater lf = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        TextView textView = (TextView) lf.inflate(R.layout.chip_layout, null);
        textView.setText(text);
        int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        textView.measure(spec, spec);
        textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());
        //Bitmap b = Bitmap.createBitmap(textView.getWidth(), textView.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.translate(-textView.getScrollX(), -textView.getScrollY());
        textView.draw(canvas);
        textView.setDrawingCacheEnabled(true);
        Bitmap cacheBmp = textView.getDrawingCache();
        Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
        textView.destroyDrawingCache();  // destory drawable
        // create bitmap drawable for imagespan
        BitmapDrawable bmpDrawable = new BitmapDrawable(viewBmp);
        bmpDrawable.setBounds(0, 0,bmpDrawable.getIntrinsicWidth(),bmpDrawable.getIntrinsicHeight());

        ReplacementSpan span = new ImageSpan(bmpDrawable);
        this.getText().setSpan(span, start, end , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        this.chipsRelatedObjects.put(span, relatedObject);

        this.enableTextChangedListeners();
        this.notifyChipAdded(span, relatedObject);
    }

    public String getLastUnchipedToken(){
        String all = this.getText().toString();
        int l = all.lastIndexOf(CHIPS_SEPARATOR);
        if(all.length() <= l+1){
            return "";
        }
        else {
            return all.substring(all.lastIndexOf(CHIPS_SEPARATOR) + 1);
        }
    }

    public int getLastTokenStart(){
        return this.getText().toString().lastIndexOf(CHIPS_SEPARATOR) + CHIPS_SEPARATOR.length();
    }


    public String getUnChipedText(){
//
//        if(this.getText().length() < 1 || this.getText().length() <  this.lastChipEnd+1){
//            return "";
//        }
//
//        Log.d(MapActivity.LOGTAG, "Returning unchiped text with start, end: " + (this.lastChipEnd+1) + "," + (this.getText().length()));
//        Log.d(MapActivity.LOGTAG, this.getText().subSequence(this.lastChipEnd+1, this.getText().length()).toString());
//
//        return this.getText().subSequence(this.lastChipEnd+1, this.getText().length() ).toString();

        Editable text = getText();
        ReplacementSpan[] spans = text.getSpans(0, text.length() - 1, ImageSpan.class);
        String unspannedText = text.toString();
        for(ReplacementSpan sp: spans){
            CharSequence spanText = text.subSequence(text.getSpanStart(sp), text.getSpanEnd(sp));
            unspannedText = unspannedText.replace(spanText, "");
        }

        return unspannedText.replaceAll(CHIPS_SEPARATOR, " ").trim();
    }

//
//
//    public void setChips(){
//        if(getText().toString().contains(",")) // check comma in string
//        {
//
//            SpannableStringBuilder ssb = new SpannableStringBuilder(getText());
//            // split string wich comma
//            String chips[] = getText().toString().trim().split(",");
//            int x =0;
//            // loop will generate ImageSpan for every country name separated by comma
//            for(String c : chips){
//                // inflate chips_edittext layout
//                LayoutInflater lf = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
//                TextView textView = (TextView) lf.inflate(R.layout.chip_layout, null);
//                textView.setText(c); // set text
////                int image = ((ChipsAdapter) getAdapter()).getImage(c);
////                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, image, 0);
////                // capture bitmapt of genreated textview
//                int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
//                textView.measure(spec, spec);
//                textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());
//                //Bitmap b = Bitmap.createBitmap(textView.getWidth(), textView.getHeight(),Bitmap.Config.ARGB_8888);
//                Canvas canvas = new Canvas();
//                canvas.translate(-textView.getScrollX(), -textView.getScrollY());
//                textView.draw(canvas);
//                textView.setDrawingCacheEnabled(true);
//                Bitmap cacheBmp = textView.getDrawingCache();
//                Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
//                textView.destroyDrawingCache();  // destory drawable
//                // create bitmap drawable for imagespan
//                BitmapDrawable bmpDrawable = new BitmapDrawable(viewBmp);
//                bmpDrawable.setBounds(0, 0,bmpDrawable.getIntrinsicWidth(),bmpDrawable.getIntrinsicHeight());
//                // create and set imagespan
//                ssb.setSpan(new ImageSpan(bmpDrawable),x ,x + c.length() , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                x = x+ c.length() +1;
//            }
//            // set chips span
//            setText(ssb);
//            // move cursor to last
//            setSelection(getText().length());
//        }
//
//
//    }

    TextWatcher textChangeListener = new TextWatcher() {
        ReplacementSpan manipulatedSpan;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            ReplacementSpan[] spans = ((Spannable)s).getSpans(start, start+count, ReplacementSpan.class);
            manipulatedSpan = null;
            if (spans.length == 1) {
                manipulatedSpan = spans[0];
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(manipulatedSpan != null){
                int start = s.getSpanStart(manipulatedSpan);
                int end = s.getSpanEnd(manipulatedSpan);
                if (start > -1 && end > -1) {
                    s.delete(start, end);
                    Log.d(MapActivity.LOGTAG, "Setting lastChipEnd to " + (start - CHIPS_SEPARATOR.length()));
                    lastChipEnd = start - CHIPS_SEPARATOR.length();
                    notifyChipRemoved(manipulatedSpan, chipsRelatedObjects.remove(manipulatedSpan));
                }
            }
        }
    };


    public void addChipsWatcherListener(ChipsWatcher listener){
        this.chipsWatchers.add(listener);
    }

    public void removeChipsWatcherListener(ChipsWatcher listener){
        this.chipsWatchers.remove(listener);
    }


    public void notifyChipRemoved(ReplacementSpan chip, Object relatedObject){
        for(ChipsWatcher wt: this.chipsWatchers){
            wt.onChipRemoved(chip, relatedObject);
        }
    }

    public void notifyChipAdded(ReplacementSpan chip, Object relatedObject){
        for(ChipsWatcher wt: this.chipsWatchers){
            wt.onChipAdded(chip, relatedObject);
        }
    }

    public interface ChipsWatcher {
        public void onChipAdded(ReplacementSpan chip, Object relatedObj);
        public void onChipRemoved(ReplacementSpan chip, Object relatedObj);
    }
}
