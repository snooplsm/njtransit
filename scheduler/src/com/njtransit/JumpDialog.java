package com.njtransit;

import java.util.Collection;
import java.util.Collections;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Simple Dialog intended for alpha/numeric index selection
 * 
 * @author dtangren
 */
public class JumpDialog extends Dialog {

  public static interface OnJumpListener {
    void onJump(Character c);
  }

  private static final Character[] ALPHA = new Character[] { 
	  'A', 'B', 'C', 'D', 'E',
    'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
    'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '#' 
  };
  private static final Character[] NUMER = new Character[] { 
	  '0', '1', '2', '3', '4',
    '5', '6', '7', '8', '9', 'A' 
  };
  
  private int cols = 4;
  private Collection<Character> subset = Collections.<Character> emptyList();
  private final OnJumpListener listener;
  private final ViewGroup grid;

  public JumpDialog(Context ctx, final OnJumpListener listener) {
    super(ctx);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    this.listener = listener;
    LayoutParams lp = new LayoutParams();
    lp.width = LayoutParams.FILL_PARENT;
    lp.flags = LayoutParams.FLAG_FULLSCREEN;

    grid = new TableLayout(ctx);
    setContentView(grid, lp);
    ColorDrawable cd = new ColorDrawable(0);
    cd.setAlpha(230);
    getWindow().setBackgroundDrawable(cd);
  }

  public JumpDialog inRowsOf(int n) {
    cols = n;
    return this;
  }

  public JumpDialog only(Collection<Character> subset) {
    this.subset = subset;
    return this;
  }

  @Override
  public final void onAttachedToWindow() {
    withView(ALPHA);
    grid.requestLayout();
  }

  private JumpDialog flip(boolean fromAlpha) {
    return withView(fromAlpha ? NUMER : ALPHA);
  }

  private JumpDialog withView(Character[] opts) {
	  grid.removeAllViews();
    TableRow row = new TableRow(getContext());
    for (int i = 0; i < opts.length; i++) {

      if (i % cols == 0 || i == opts.length - 1) {
        grid.addView(row);
      }

      if (i % cols == 0) {
        row = new TableRow(getContext());
      }

      final TextView c = new TextView(getContext());
      c.setText(""+opts[i]);
      c.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 52);
      c.setPadding(10, 10, 10, 10);
      c.setClickable(true);
      c.setGravity(Gravity.CENTER);
      c.setTextColor(
    		  subset.isEmpty() || subset.contains(opts[i]) ? Color.WHITE : Color.GRAY
      );
      if (i < opts.length - 1) {
        boolean selectable = subset.isEmpty() || subset.contains(opts[i]);
        c.setTextColor(selectable ? Color.WHITE : Color.GRAY);
        if (selectable) {
          c.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              listener.onJump(c.getText().charAt(0));
              JumpDialog.this.dismiss();
            }
          });
          c.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				c.setTextColor(Color.parseColor("#F4A83D"));
				
				return false;
			}
        });
        }
      } else {
        c.setTextColor(Color.WHITE);
        c.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
        	//
            flip(ALPHA[ALPHA.length - 1].equals(c.getText()));
          }
        });
      }
      row.addView(c);
    }
    return this;
  }
}
