/*
 *  UVCCamera
 *  library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *  All files in the folder are under this Apache License, Version 2.0.
 *  Files in the libjpeg-turbo, libusb, libuvc, rapidjson folder
 *  may have a different license, see the respective files.
 */

package com.serenegiant.widget;

import static com.serenegiant.utils.ThreadPool.queueEvent;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import javax.microedition.khronos.opengles.GL10;
//filter
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

//import com.googlecode.javacv.cpp.opencv_core;
import com.serenegiant.usb.UVCCamera;

import org.wysaid.camera.CameraInstance;
import org.wysaid.texUtils.*;

import org.wysaid.myUtils.Common;
import org.wysaid.myUtils.ImageUtil;
import org.wysaid.recorder.MyRecorderWrapper;
import org.wysaid.view.CameraGLSurfaceView;

import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

//import com.googlecode.javacv.cpp.opencv_core;

import org.wysaid.camera.CameraInstance;
import org.wysaid.texUtils.*;

import org.wysaid.myUtils.Common;
import org.wysaid.myUtils.ImageUtil;
import org.wysaid.recorder.MyRecorderWrapper;

import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * change the view size with keeping the specified aspect ratio.
 * if you set this view with in a FrameLayout and set property "android:layout_gravity="center",
 * you can show this view in the center of screen and keep the aspect ratio of content
 * XXX it is better that can set the aspect ratio as xml property
 */
public class AspectRatioTextureView extends TextureView implements IAspectRatioView,GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {	// API >= 14IAspectRatioView {

	private static final boolean DEBUG = true;	// TODO set false on release
	private static final String TAG = "AbstractCameraView";

    private double mRequestedAspect = -1.0;
	private CameraViewInterface.Callback mCallback;

	public AspectRatioTextureView(final Context context) {
		this(context, null, 0);
	}

	public AspectRatioTextureView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AspectRatioTextureView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
    public void setAspectRatio(final double aspectRatio) {
        if (aspectRatio < 0) {
            throw new IllegalArgumentException();
        }
        if (mRequestedAspect != aspectRatio) {
            mRequestedAspect = aspectRatio;
            requestLayout();
        }
    }

	@Override
    public void setAspectRatio(final int width, final int height) {
		setAspectRatio(width / (double)height);
    }

	@Override
	public double getAspectRatio() {
		return mRequestedAspect;
	}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		if (mRequestedAspect > 0) {
			int initialWidth = MeasureSpec.getSize(widthMeasureSpec);
			int initialHeight = MeasureSpec.getSize(heightMeasureSpec);

			final int horizPadding = getPaddingLeft() + getPaddingRight();
			final int vertPadding = getPaddingTop() + getPaddingBottom();
			initialWidth -= horizPadding;
			initialHeight -= vertPadding;

			final double viewAspectRatio = (double)initialWidth / initialHeight;
			final double aspectDiff = mRequestedAspect / viewAspectRatio - 1;

			if (Math.abs(aspectDiff) > 0.01) {
				if (aspectDiff > 0) {
					// width priority decision
					initialHeight = (int) (initialWidth / mRequestedAspect);
				} else {
					// height priority decision
					initialWidth = (int) (initialHeight * mRequestedAspect);
				}
				initialWidth += horizPadding;
				initialHeight += vertPadding;
				widthMeasureSpec = MeasureSpec.makeMeasureSpec(initialWidth, MeasureSpec.EXACTLY);
				heightMeasureSpec = MeasureSpec.makeMeasureSpec(initialHeight, MeasureSpec.EXACTLY);
			}
		}

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    //filter
//	public void onSurfacesetAspectRatioed(GL10 gl10, EGLConfig eglConfig) {
//		mReactShape = new RectShape();
//		int textureId = GLUtil.generateOESTexture();
//		mSurfaceTexture = new SurfaceTexture(textureId);
//		mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
//			@Override
//			public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//				mFrameAvailable = true;
//			}
//		});
//		mSurface = new Surface(mSurfaceTexture);
//		mReactShape.setTextureId(textureId);
//		setupPlayer();
//	}

	private int genSurfaceTextureID() {
		int[] texID = new int[1];
		GLES20.glGenTextures(1, texID, 0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texID[0]);
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
				GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
				GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
				GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
				GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
		return texID[0];
	}


	//filter
	private int mTextureID;
	private SurfaceTexture mSurfaceTexture;
	private long mTimeCount = 0;
	private long mFramesCount = 0;
	private long mLastTimestamp = 0;

	public void setEGLContextClientVersion(int i) {
	}

	public class ClearColor {
		public float r, g, b, a;
	}
	public CameraGLSurfaceView.ClearColor clearColor;
	private float[] mTransformMatrix = new float[16];
	public TextureRenderer.Viewport drawViewport;
	private int[] mRecordStateLock = new int[0];
	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		if(mLastTimestamp == 0)
			mLastTimestamp = mSurfaceTexture.getTimestamp();

		long currentTimestamp = mSurfaceTexture.getTimestamp();

		++mFramesCount;
		mTimeCount += currentTimestamp - mLastTimestamp;
		mLastTimestamp = currentTimestamp;
		if(mTimeCount >= 1e9)
		{
			Log.i(LOG_TAG, String.format("TimeCount: %d, Fps: %d", mTimeCount, mFramesCount));
			mTimeCount -= 1e9;
			mFramesCount = 0;
	}
		//requestRender();
	}


	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.i(LOG_TAG, "onSurfaceCreated...");

		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glDisable(GLES20.GL_STENCIL_TEST);

		mTextureID = genSurfaceTextureID();
		mSurfaceTexture = new SurfaceTexture(mTextureID);
		mSurfaceTexture.setOnFrameAvailableListener((SurfaceTexture.OnFrameAvailableListener) this);

		TextureRendererWave rendererWave = new TextureRendererWave();
		if(!rendererWave.init(true)) {
			Log.e(LOG_TAG, "init filter failed!\n");
		}

		rendererWave.setAutoMotion(0.4f);

		mMyRenderer = rendererWave;
		mMyRenderer.setRotation(-(float) (Math.PI / 2.0));
		mMyRenderer.setFlipscale(1.0f, 1.0f);

		//requestRender();

	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Log.i(LOG_TAG, String.format("onSurfaceChanged: %d x %d", width, height));

		GLES20.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);



		//calcViewport();
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		if(mMyRenderer == null || mSurfaceTexture == null) {
			Log.e(LOG_TAG, "Invalid Texture Renderer!");
			return;
		}

		mSurfaceTexture.updateTexImage();
		mSurfaceTexture.getTransformMatrix(mTransformMatrix);
		mMyRenderer.setTransform(mTransformMatrix);

		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

		mMyRenderer.renderTexture(mTextureID, drawViewport);


	}

	public enum FilterButtons {
		Filter_Origin,
		Filter_Wave,
		Filter_Blur,
		Filter_Emboss,
		Filter_Edge,
		Filter_BlurLerp,
	}

	private TextureRenderer mMyRenderer;
	public static final String LOG_TAG = Common.LOG_TAG;
	public synchronized void setFrameRenderer(final int filterID) {
		Log.i(LOG_TAG, "setFrameRenderer to " + filterID);
		queueEvent(new Runnable() {
			@Override
			public void run() {
				TextureRenderer renderer = null;
				boolean isExternalOES = true;
				switch (filterID) {
					case 1:
						renderer = TextureRendererDrawOrigin.create(isExternalOES);
						break;
					case 2:
						renderer = TextureRendererWave.create(isExternalOES);
						if (renderer != null)
							((TextureRendererWave) renderer).setAutoMotion(0.4f);
						break;
//					case Filter_Blur:
//						renderer = TextureRendererBlur.create(isExternalOES);
//						if(renderer != null) {
//							((TextureRendererBlur) renderer).setSamplerRadius(50.0f);
//						}
//						break;
					case 4:
						renderer = TextureRendererEdge.create(isExternalOES);
						break;
					case 3:
						renderer = TextureRendererEmboss.create(isExternalOES);
						break;
//					case Filter_BlurLerp:
//						renderer = TextureRendererLerpBlur.create(isExternalOES);
//						if(renderer != null) {
//							((TextureRendererLerpBlur) renderer).setIntensity(16);
//						}
						//break;
					default:
						break;
				}

				if (renderer != null) {
					mMyRenderer.release();
					mMyRenderer = renderer;
					mMyRenderer.setTextureSize(UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_WIDTH);
					mMyRenderer.setRotation(-(float) (Math.PI / 2.0));
					mMyRenderer.setFlipscale(1.0f, 1.0f);
				}

				GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

				Common.checkGLError("setFrameRenderer...");
			}
		});

	}



}
