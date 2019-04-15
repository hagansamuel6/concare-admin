package io.icode.concareghadmin.application.activities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

import io.icode.concareghadmin.application.activities.adapters.MessageAdapter;

public class RecyclerItemSingleClickListener implements RecyclerView.OnItemTouchListener{

    public static OnItemClickListener mSingleClickListener;

    GestureDetector mGestureDetector;

    public interface OnItemClickListener {
       void onItemSingleClick(View view, int position);
    }

    public RecyclerItemSingleClickListener(Context context, OnItemClickListener listener){
        mSingleClickListener = listener;
        mGestureDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return super.onDoubleTap(e);
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView view,@NonNull MotionEvent event) {
        View childView = view.findChildViewUnder(event.getX(),event.getY());
        if(childView != null && mSingleClickListener != null && mGestureDetector.onTouchEvent(event)){
            mSingleClickListener.onItemSingleClick(childView, view.getChildLayoutPosition(childView));
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean b) {

    }
}
