package com.embroidermodder.embroideryviewer.embroideryview;

/**
 * Created by Tat on 12/5/2016.
 */

import java.io.File;

/**
 * This interface must be implemented by activities that contain this
 * fragment to allow an interaction in this fragment to be communicated
 * to the activity and potentially other fragments contained in that
 * activity.
 * <p/>
 * See the Android Training lesson <a href=
 * "http://developer.android.com/training/basics/fragments/communicating.html"
 * >Communicating with Other Fragments</a> for more preamble.
 */
public interface OnListFragmentInteractionListener {
    void onListFragmentInteraction(File item);
}