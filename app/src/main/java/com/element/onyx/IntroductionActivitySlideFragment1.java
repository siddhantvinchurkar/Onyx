package com.element.onyx;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;

/**
 * This code is in no manner open for anyone to use. The use of this code
 * in any manner anywhere is governed by Siddhant Vinchurkar (yours truly)
 * for all eternity. Now go die.
 **/

/* This is a Fragment Class and is only required to inflate the fragment of the first slide in the
 * introduction activity. Apart from changing the slide transition color, it does not alter the
 * general behaviour of the app */

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link IntroductionActivitySlideFragment1.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link IntroductionActivitySlideFragment1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IntroductionActivitySlideFragment1 extends Fragment implements ISlideBackgroundColorHolder{

    // Change slide transition color

    @Override
    public int getDefaultBackgroundColor() {
        return Color.parseColor("#000000");
    }

    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        FrameLayout slide1frag = (FrameLayout) getActivity().findViewById(R.id.slide1frag);
        slide1frag.setBackgroundColor(backgroundColor);
    }

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public IntroductionActivitySlideFragment1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Introduction_Slide1.
     */
    // TODO: Rename and change types and number of parameters
    public static IntroductionActivitySlideFragment1 newInstance(String param1, String param2) {
        IntroductionActivitySlideFragment1 fragment = new IntroductionActivitySlideFragment1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Get view to perform UI changes

        View view = inflater.inflate(R.layout.introduction_slide_fragment_1, container, false);

        /* The following block of code will change the font of text views on the first
         * introduction screen to 'ubuntu_r.ttf' */

        TextView title1, desc1;
        title1 = (TextView) view.findViewById(R.id.title1);
        desc1 = (TextView) view.findViewById(R.id.desc1);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "ubuntu_r.ttf");
        title1.setTypeface(typeface);
        desc1.setTypeface(typeface);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.introduction_slide_fragment_1, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
