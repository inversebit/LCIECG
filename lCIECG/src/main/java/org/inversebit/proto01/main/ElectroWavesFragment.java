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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import org.inversebit.proto01.Constants;
import org.inversebit.proto01.data.ReceiveAndPlotElectroTask;
import org.inversebit.proto01.views.ElectroWaveView;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class ElectroWavesFragment extends Fragment
{
	private ReceiveAndPlotElectroTask rapet;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

		return inflater.inflate(R.layout.fragment_electro_waves, container, false);
    }
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		ElectroWaveView[] ewv = new ElectroWaveView[3];
		ewv[0] = (ElectroWaveView)getView().findViewById(R.id.electroWaveView1);
		ewv[1] = (ElectroWaveView)getView().findViewById(R.id.electroWaveView2);
		ewv[2] = (ElectroWaveView)getView().findViewById(R.id.electroWaveView3);
		
		try
		{  
		    rapet = new ReceiveAndPlotElectroTask(ewv[0]);
		    rapet.execute();
		    Log.d(Constants.TAG, "EWF: created RDT " + rapet.toString());
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
		Log.d(Constants.TAG, "EWF: cancelled RDT " + rapet.toString());
		rapet.cancel(true);		
	}
}
