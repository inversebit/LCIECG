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
package org.inversebit.proto01.main;

import java.io.IOException;

import org.inversebit.proto01.Constants;
import org.inversebit.proto01.data.ElectroData;
import org.inversebit.proto01.data.ReceiveAndStoreTask;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


public class DiagnoserFragment extends Fragment
{
	private ReceiveAndStoreTask rast;
	private Diagnoser diag;
	private static ProgressBar pb;
		
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.fragment_diagnoser, container, false);
    }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		final Button but = new Button(getActivity().getBaseContext());
		but.setText("Start");
		but.setOnClickListener(new OnClickListener() 
		{			
			@Override
			public void onClick(View v) {
				but.setVisibility(View.GONE);
				pb = new ProgressBar(getActivity().getBaseContext(), null, android.R.attr.progressBarStyleHorizontal);
				((LinearLayout)getActivity().findViewById(R.id.progressLayout)).addView(pb, 300, 10);
				getDataAndDiagnose();
			}
		});
		((LinearLayout)getActivity().findViewById(R.id.progressLayout)).addView(but);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
	}
	
	protected void getDataAndDiagnose()
	{
		diag = new Diagnoser(this);
		
		try
		{  
		    rast = new ReceiveAndStoreTask(diag);
		    rast.execute(Constants.MEASUREMENTS_PER_SECOND * Constants.DATA_SECONDS);
		    Log.d(Constants.TAG, "DF: created RDT " + rast.toString());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		if(rast != null)
		{
			Log.d(Constants.TAG, "DF: cancelled RDT " + rast.toString());
			rast.cancel(true);
		}
	}
	
	public static void updateProgressBar(int pProgress)
	{
		pb.setProgress((int)(pb.getMax()*((float)pProgress/3)));
		Log.d(Constants.TAG, "Progess updated");
	}

	public void publishData(ElectroData ed) {
		addData("BPM", ed.getBPM());
		addData("Irreg beats", ed.getIrregularBeatNum());
		addData("Mean P amplitude (mV)", ed.getMeanPAmplitude());
		addData("Mean P length (ms)", ed.getMeanPLength());
		addData("Mean corrected QT length (ms)", ed.getMeanQTcLength());
		addData("Mean RR length (ms)", ed.getMeanRRLength());
		addData("Mean PR length (ms)", ed.getPRLength());
		addData("Mean QRS amplitude (mV)", ed.getQRSAmplitude());
		addData("Mean QRS length (ms)", ed.getQRSLength());
	}

	private void addData(String pText, float pVal) {
		LinearLayout ll = new LinearLayout(getActivity().getBaseContext());
		ll.setOrientation(LinearLayout.VERTICAL);
		
		TextView tv1 = new TextView(getActivity().getBaseContext());
		tv1.setText(pText);
		tv1.setTextColor(getResources().getColor(android.R.color.tertiary_text_light));
		tv1.setTextSize(15.0f);
		ll.addView(tv1);
		
		TextView tv2 = new TextView(getActivity().getBaseContext());
		tv2.setText("\t" + pVal);
		tv2.setTextColor(getResources().getColor(android.R.color.black));
		tv2.setTextSize(15.0f);
		ll.addView(tv2);
		
		((LinearLayout)getActivity().findViewById(R.id.dataLayout)).addView(ll);
	}
	
}
