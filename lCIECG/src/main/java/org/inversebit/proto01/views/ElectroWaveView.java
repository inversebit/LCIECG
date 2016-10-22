/*
Copyright (C) 2014 Alexander Mariel

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/
package org.inversebit.proto01.views;

import org.inversebit.proto01.data.DataReceiver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;


public class ElectroWaveView extends View implements DataReceiver
{
	private Paint redPaint;
	private Paint bluePaint;
	private int position;
	private float[] vals;
	
	public ElectroWaveView(Context context)
	{
		super(context);
		initPaint();
		initPositionMarker(context);
	}

	public ElectroWaveView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initPaint();
		initPositionMarker(context);
	}

	public ElectroWaveView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initPaint();
		initPositionMarker(context);
	}
	
	private void initPositionMarker(Context context)
	{
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		vals = new float[(int)(size.x - (size.x*0.2))*2];
		position = 0;
	}

	private void initPaint()
	{
		redPaint = new Paint();
		redPaint.setColor(Color.RED);
		redPaint.setStrokeWidth(2.0f);
		
		bluePaint = new Paint();
		bluePaint.setColor(Color.BLUE);
		bluePaint.setStrokeWidth(2.0f);
	}
	
	public void setNextVal(int pVal){
		vals[position] = position/2;
		vals[position+1] = this.getHeight() - ((float)pVal/1024)*getHeight();		
		position = position + 2;
		
		if(position >= vals.length-2){
			position = 0;
		}
	}
	
	@Override
	protected void onDraw (Canvas canvas)
	{		
		super.onDraw(canvas);

		canvas.drawLine(position/2, 0, position/2, 800, bluePaint);
		canvas.drawPoints(vals, redPaint);
	}

}
