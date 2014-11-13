package org.openplaces.widgets;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
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

/**
 * Created by ggiammat on 11/12/14.
 */
public class OPChipsEditText extends EditText {
    private static final String TAG = "OPChipsEditText";


    public static final String CHIPS_SEPARATOR = " ";


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


    public void appendChip(String text){
        int start = this.getText().length() + CHIPS_SEPARATOR.length();
        this.getText().append(CHIPS_SEPARATOR + text);
        int end = start + text.length();
        Log.d(MapActivity.LOGTAG, "New text lenght is " + this.getText().length());
        Log.d(MapActivity.LOGTAG, "start is " + start);
        Log.d(MapActivity.LOGTAG, "end is " + end);


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

        this.getText().setSpan(new ImageSpan(bmpDrawable), start, end , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }



    public void setChips(){
        if(getText().toString().contains(",")) // check comma in string
        {

            SpannableStringBuilder ssb = new SpannableStringBuilder(getText());
            // split string wich comma
            String chips[] = getText().toString().trim().split(",");
            int x =0;
            // loop will generate ImageSpan for every country name separated by comma
            for(String c : chips){
                // inflate chips_edittext layout
                LayoutInflater lf = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                TextView textView = (TextView) lf.inflate(R.layout.chip_layout, null);
                textView.setText(c); // set text
//                int image = ((ChipsAdapter) getAdapter()).getImage(c);
//                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, image, 0);
//                // capture bitmapt of genreated textview
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
                // create and set imagespan
                ssb.setSpan(new ImageSpan(bmpDrawable),x ,x + c.length() , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                x = x+ c.length() +1;
            }
            // set chips span
            setText(ssb);
            // move cursor to last
            setSelection(getText().length());
        }


    }

    TextWatcher textChangeListener = new TextWatcher() {
        ReplacementSpan manipulatedSpan;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            ReplacementSpan[] spans = ((Spannable)s).getSpans(start, start+count, ReplacementSpan.class);

            Log.d(TAG, "number of spans is" + spans.length);

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
                }
            }
        }
    };


//
//    public void makeChip(int start, int end) {
//        int maxWidth = getWidth() - getPaddingLeft() - getPaddingRight();
//        String finalText = null;
//        try {
//            getText().insert(start, " ");
//            getText().insert(end + 1, " ");
//            end += 2;
//            finalText = getText().subSequence(start + 1, end - 1).toString();
//        } catch (java.lang.IndexOutOfBoundsException e) {
//            return;
//            // possibly some other entity (Random Shit Keyboard™) is changing
//            // the text here in the meanwhile resulting in a crash
//        }
//        int textSize = (int)(getTextSize() - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getContext().getResources().getDisplayMetrics()));
//        Utils.bubblify(getText(), finalText, start, end, maxWidth, DefaultBubbles.get(DefaultBubbles.GRAY_WHITE_TEXT, getContext(), textSize), this, null);
//    }
}
