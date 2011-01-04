package com.njtransit;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.njtransit.rail.R;

import java.util.Collection;
import java.util.Collections;

/**
 * Simple Dialog intended for alpha/numeric index selection
 * 
 * @author dtangren
 */
public abstract class JumpDialog extends Dialog {

	public static interface OnJumpListener {
		void onJump(String c);
	}

	private Collection<Character> subset = Collections.<Character> emptyList();
	private final OnJumpListener listener;

	private boolean alpha;
	
	public JumpDialog(Context ctx, final OnJumpListener listener) {
		super(ctx);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.listener = listener;
		LayoutParams lp = new LayoutParams();
		Display display = getWindow().getWindowManager().getDefaultDisplay();
		lp.width = display.getWidth();
		lp.height = display.getHeight();
		View view = getLayoutInflater().inflate(R.layout.new_jumper, null);
		alpha = true;
		setContentView(view, lp);
		ColorDrawable cd = new ColorDrawable(0);
		cd.setAlpha(230);
		getWindow().setBackgroundDrawable(cd);		
	}

	@Override
	public void onAttachedToWindow() {
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.jumper_root);
		for (int i = 0; i < linearLayout.getChildCount(); i++) {
			LinearLayout row = (LinearLayout) linearLayout.getChildAt(i);
			for (int j = 0; j < row.getChildCount(); j++) {
				final TextView text = (TextView) row.getChildAt(j);
				final String c = text.getText().toString();
				if (subset.contains(text.getText().charAt(0)) || ((alpha && c.equals("#") || (!alpha && c.equals("A"))))) {
					text.setClickable(true);
					text.setTextColor(Color.WHITE);
					text.setOnClickListener(new View.OnClickListener() {
						
						public void onClick(View v) {
							onLetterSelect(c);
							if(alpha && c.equals("#")) {
								LayoutParams lp = new LayoutParams();
								Display display = getWindow().getWindowManager().getDefaultDisplay();
								lp.width = display.getWidth();
								lp.height = display.getHeight();
								JumpDialog.this.dismiss();
								JumpDialog.this.setContentView(getLayoutInflater().inflate(R.layout.new_jumper_number, null),lp);
								alpha = !alpha;
								JumpDialog.this.show();
								return;
							}
							if(!alpha && c.equals("A")) {
								LayoutParams lp = new LayoutParams();
								Display display = getWindow().getWindowManager().getDefaultDisplay();
								lp.width = display.getWidth();
								lp.height = display.getHeight();
								JumpDialog.this.dismiss();
								JumpDialog.this.setContentView(getLayoutInflater().inflate(R.layout.new_jumper,null),lp);
								JumpDialog.this.show();
								alpha = !alpha;
								return;
							} else {
								listener.onJump(c);
								JumpDialog.this.dismiss();
							}
							
							
						}
					});
					text.setOnTouchListener(new OnTouchListener() {
						@Override
						public boolean onTouch(View v, MotionEvent event) {
							text.setTextColor(getContext().getResources().getColor(R.color.jumper_touch));
							return false;
						}
					});
				}
			}

		}
	}

	protected abstract void onLetterSelect(String c);

	public JumpDialog only(Collection<Character> subset) {
		this.subset = subset;
		return this;
	}

}
