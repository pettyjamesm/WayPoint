package com.crobot.waypoint.tracking;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.location.Location;
import android.os.IBinder;
import android.util.AttributeSet;
import android.view.View;

public class ArrowView extends View implements WayPointSvc.IListener {
	private static final int DEFAULT_SIZE 	= 100;
	
	// Drawing Components	
	private Paint	mLHPaint = new Paint();
	private Path	mLHalf = new Path();
	
	private Paint	mRHPaint = new Paint();
	private Path	mRHalf = new Path();
	
	private Paint 	mPaint = new Paint();
	private Path	mArrow = new Path();
	private Path	mSplit = new Path();
	
	//	Arrow Direction Pointer
	private float	mRotation 		= 0;
	private float	mLastDrawAngle	= 0;
	
	//	GPS State Helpers
	private float	mGpsState  = WayPointSvc.GPS_OFFLINE;
	private Paint	mTextPaint = new Paint();
	

	public ArrowView(Context context) {
		super(context);
		this.commonConstruction();
	}	
	public ArrowView(Context context, AttributeSet attributes){
		super(context, attributes);
		this.commonConstruction();
	}
	public ArrowView(Context context, AttributeSet attributes, int style){
		super(context, attributes, style);
		this.commonConstruction();
	}
	
	private void commonConstruction(){
		this.setKeepScreenOn(true);
		this.setScrollContainer(false);
		this.setClickable(false);
		this.initDrawingTools();
		this.getContext().bindService(new Intent(this.getContext(), WayPointSvc.class), this.mConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		int chosenWidth = chooseDimension(widthMode, widthSize);
		int chosenHeight = chooseDimension(heightMode, heightSize);
		
		int dimension = Math.min(chosenWidth, chosenHeight);
		this.setMeasuredDimension(dimension, dimension);
	}	
	private int chooseDimension(int mode, int size){
		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY){
			return size;
		} else {
			return DEFAULT_SIZE;
		}
	}
	
	@Override
	protected void onDraw(Canvas c){
		float size 	= this.getWidth();		
		
		if (this.mGpsState == WayPointSvc.GPS_READY){		
			c.save(Canvas.MATRIX_SAVE_FLAG);
			c.scale(size, size);
			c.rotate(this.mRotation, 0.5f, 0.5f);
			
			c.drawPath(this.mArrow, this.mPaint);
			c.drawPath(this.mLHalf, this.mLHPaint);
			c.drawPath(this.mRHalf, this.mRHPaint);
			c.drawPath(this.mSplit, this.mPaint);		
			c.restore();
		} else {
			c.drawText("Awaiting GPS", size / 2, size / 2, this.mTextPaint);
		}
	}
	
	public void setRotationAngle(final float angle){
		final float safeAngle 	= angle % 360;
		final float diff		= this.mLastDrawAngle - safeAngle;
		this.mRotation 			= safeAngle;
		if (Math.abs(diff) > 2){ this.postInvalidate(); this.mLastDrawAngle = safeAngle;}
	}
	
	private void makeWedge(){
		this.mLHalf.moveTo(0.5f, 0.1f);
		this.mRHalf.moveTo(0.5f, 0.1f);
		this.mSplit.moveTo(0.5f, 0.1f);
		this.mLHalf.lineTo(0.3f, 0.85f);
		this.mRHalf.lineTo(0.7f, 0.85f);
		this.mLHalf.lineTo(0.5f, 0.7f);
		this.mRHalf.lineTo(0.5f, 0.7f);
		this.mSplit.lineTo(0.5f, 0.7f);
		this.mLHalf.close();
		this.mRHalf.close();
		this.mArrow.addPath(this.mLHalf);
		this.mArrow.addPath(this.mRHalf);
	}
	
	private void initDrawingTools(){
		this.makeWedge();
		this.mTextPaint.setTextAlign(Align.CENTER);
		this.mTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
		this.mTextPaint.setStyle(Paint.Style.FILL);
		this.mLHPaint.setStyle(Paint.Style.FILL);
		this.mRHPaint.setStyle(Paint.Style.FILL);
		this.mPaint.setStyle(Paint.Style.STROKE);
		this.mPaint.setStrokeWidth(0.015f);
		this.mPaint.setStrokeJoin(Paint.Join.ROUND);
		this.mPaint.setStrokeCap(Paint.Cap.ROUND);
		this.mTextPaint.setColor(Color.WHITE);
		this.mLHPaint.setColor(Color.LTGRAY);
		this.mRHPaint.setColor(Color.RED);
		this.mPaint.setColor(Color.BLACK);
		this.mTextPaint.setAntiAlias(true);
		this.mLHPaint.setAntiAlias(true);
		this.mRHPaint.setAntiAlias(true);
		this.mPaint.setAntiAlias(true);
	}
	
	//	GPS STATE LISTENER METHODS
	@Override
	public void onGPSLocationUpdate(Location location) {}
	@Override
	public void onGPSStateChange(int gpsState) {
		if (this.mGpsState != gpsState){
			this.mGpsState = gpsState;
			this.postInvalidate();
		}
	}	
	@Override
	public void onWindowFocusChanged(boolean visible){
		super.onWindowFocusChanged(visible);
		if (visible){ if (mWPSvc != null){ mWPSvc.addServiceListener(this);}}
		else { if (mWPSvc != null){ mWPSvc.removeServiceListener(this);}}
	}
	
	private WayPointSvc			mWPSvc		= null;
	private ServiceConnection 	mConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName className, IBinder service){
			mWPSvc = ((WayPointSvc.LocalBinder)service).getService();
		}
		@Override
		public void onServiceDisconnected(ComponentName className){ mWPSvc = null; }
	};
}
