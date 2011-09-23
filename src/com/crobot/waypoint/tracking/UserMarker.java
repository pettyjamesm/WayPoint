package com.crobot.waypoint.tracking;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;

public class UserMarker extends Path {
	
	private Paint mLinePaint 	= new Paint();
	private Paint mAlphaPaint	= new Paint();
	private Paint mColorPaint	= new Paint();
	
	private float mOffset	= 0;
	
	public UserMarker(int size){
		super();
		this.mOffset = ((float)size)/2;
		// Path Setup
		this.addCircle(0.5f * size, 0.5f * size, 0.4f * size, Direction.CW);		
		//	Paint Setup
		this.mLinePaint.setStyle(Paint.Style.STROKE);
		this.mLinePaint.setStrokeWidth(0.05f * size);
		this.mAlphaPaint.setStyle(Paint.Style.FILL);
		this.mColorPaint.setStyle(Paint.Style.FILL);
		this.mLinePaint.setColor(Color.BLACK);
		this.mColorPaint.setColor(Color.BLUE);
		this.mLinePaint.setAntiAlias(true);
		this.mAlphaPaint.setAntiAlias(true);
		this.mAlphaPaint.setShader(new LinearGradient(0.40f * size, 0.0f, 0.60f * size, 1.0f * size, 
				   Color.rgb(0xf0, 0xf5, 0xf0),
				   Color.rgb(0x30, 0x31, 0x30),
				   Shader.TileMode.CLAMP));
		this.mAlphaPaint.setAlpha(90);
	}
	
	public void drawToCanvas(Canvas canvas, int centerX, int centerY){		
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		
		canvas.translate(centerX - this.mOffset, centerY - this.mOffset);		
		
		canvas.drawPath(this, this.mColorPaint);
		canvas.drawPath(this, this.mAlphaPaint);
		canvas.drawPath(this, this.mLinePaint);
		
		canvas.restore();
	}

}
