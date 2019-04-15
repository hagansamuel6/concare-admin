package io.icode.concareghadmin.application.activities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class RecyclerItemDoubleClickListener implements RecyclerView.OnItemTouchListener{

    private OnItemDoubleClickListener doubleClickListener;

    GestureDetector mGestureDetector;

    public interface OnItemDoubleClickListener {
       void onItemDoubleClick(View view, int position);
    }

    public RecyclerItemDoubleClickListener(Context context, OnItemDoubleClickListener  listener){
        doubleClickListener = listener;
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
        if(childView != null && doubleClickListener != null && mGestureDetector.onTouchEvent(event)){
            doubleClickListener.onItemDoubleClick(childView, view.getChildLayoutPosition(childView));
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
