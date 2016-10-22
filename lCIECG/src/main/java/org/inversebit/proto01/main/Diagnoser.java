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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.inversebit.proto01.Constants;
import org.inversebit.proto01.data.DataReceiver;
import org.inversebit.proto01.data.ElectroData;
import org.inversebit.proto01.parser.ParseDataTask;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridLayout;
import android.widget.Space;
import android.widget.TextView;
import eu.deustotech.clips.Environment;
import eu.deustotech.clips.MultifieldValue;
import eu.deustotech.clips.PrimitiveValue;


public class Diagnoser implements DataReceiver
{
	private ArrayList<Integer> vals;
	private ElectroData ed;
	private DiagnoserFragment fa;
	
	public Diagnoser(DiagnoserFragment diagnoserFragment)
	{
		vals = new ArrayList<Integer>(Constants.MEASUREMENTS_PER_SECOND + Constants.DATA_SECONDS);
		ed = new ElectroData();
		fa = diagnoserFragment;
	}
	
	public ElectroData getElectroData()
	{
		return ed;
	}
	
	public void publishData()
	{
		fa.publishData(ed);
	}
	
	@Override
	public void setNextVal(int pVal)
	{
		vals.add(pVal);
	}

	public void parseCollectedData()
	{
		Log.d(Constants.TAG, "Gonna parse data");
		
		int valsArr[] = new int[vals.size()];
		for(int i = 0; i < vals.size(); i++)
			valsArr[i] = vals.get(i);
		
		(new ParseDataTask(	valsArr, 
							Constants.MEASUREMENTS_PER_SECOND, 
							Constants.VAL_OFFSET,
							Constants.VAL_REL,
							this)
		).execute();
	}
	
	public void diagnosePatient()
	{
		Log.d(Constants.TAG, "Running CLIPS ver " + Environment.getCLIPSVersion());
		Environment clips = new Environment();
		
		generateCLIPSFileInAppFileDir("tmp_mod.clp");
		clips.load(fa.getActivity().getFilesDir().getAbsolutePath() + "/" + "tmp_mod.clp");
		
		clips.eval("(set-current-module TMP)");
		
		//1. Load templates file
		generateCLIPSFileInAppFileDir("electro_templates.clp");
		clips.load(fa.getActivity().getFilesDir().getAbsolutePath() + "/" + "electro_templates.clp");
		//2. Load facts file
		generateCLIPSFileInAppFileDir("electro_facts.clp");
		clips.load(fa.getActivity().getFilesDir().getAbsolutePath() + "/" + "electro_facts.clp");
		//3. Load rules file
		generateCLIPSFileInAppFileDir("electro_rules.clp");
		clips.load(fa.getActivity().getFilesDir().getAbsolutePath() + "/" + "electro_rules.clp");
		//4. Load rules data file
		generateCLIPSFileInAppFileDir("electro_rules_data.clp");
		clips.load(fa.getActivity().getFilesDir().getAbsolutePath() + "/" + "electro_rules_data.clp");
		//5. Load rules diagnostic file(electro(BPM 140))
		generateCLIPSFileInAppFileDir("electro_rules_diagnostic.clp");
		clips.load(fa.getActivity().getFilesDir().getAbsolutePath() + "/" + "electro_rules_diagnostic.clp");
		//4. Initial assert with ElectroData
		clips.reset();
		String assertData= "(electro" + 
								"(BPM " 					+ ed.getBPM() 			+ ")" + 
								"(QRS-complex-amplitude " 	+ ed.getQRSAmplitude()	+ ")" +
								"(QRS-complex-length "		+ ed.getQRSLength()		+ ")" +
								"(RR-intval-length "		+ ed.getMeanRRLength()	+ ")" +
								"(P-amplitude "				+ ed.getMeanPAmplitude()+ ")" +
								"(P-length "				+ ed.getMeanPLength()	+ ")" +
								"(PR-intval-length "		+ ed.getPRLength()		+ ")" +
								"(QTc-length "				+ ed.getMeanQTcLength() + ")" +
								"(irregular-beats "			+ ed.getIrregularBeatNum() + ")" +
							")";
		
		clips.assertString(assertData);
		//5. Run system
		clips.run();
		//6. Extract conclusion	
		String evalStr = "(find-all-facts (( ?f diagnostico )) TRUE)";
		final PrimitiveValue evaluated = (MultifieldValue) clips.eval( evalStr );
		try{
			PrimitiveValue conclusionFact = evaluated.get(0);
			String enf = conclusionFact.getFactSlot("patologias").toString();
			String raz = conclusionFact.getFactSlot("razonamientos").toString();
			
			String[] enfermedades = enf.split(" ");
			String[] razonamientos = raz.split(" ");
			
			showResults(enfermedades, razonamientos);
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		DiagnoserFragment.updateProgressBar(3);
	}
	
	private void showResults(String[] enfermedades, String[] razonamientos) {
		GridLayout gl = (GridLayout) fa.getActivity().findViewById(R.id.diagnoserGrid);
		
		gl.setColumnCount(3);
		gl.setRowCount(enfermedades.length - 1);
		
		TextView tvEnfTitulo = new TextView(gl.getContext());
		tvEnfTitulo.setText("Disease");
		tvEnfTitulo.setTextAppearance(tvEnfTitulo.getContext(), android.R.style.TextAppearance_DeviceDefault_Large);
		tvEnfTitulo.setTextColor(fa.getResources().getColor(android.R.color.tertiary_text_light));
		gl.addView(tvEnfTitulo);
		
		Space space = new Space(gl.getContext());
		space.setLayoutParams(new LayoutParams(25, 0));
		gl.addView(space);
		
		TextView tvRazTitulo = new TextView(gl.getContext());
		tvRazTitulo.setText("Reasoning");
		tvRazTitulo.setTextAppearance(tvRazTitulo.getContext(), android.R.style.TextAppearance_DeviceDefault_Large);
		tvRazTitulo.setTextColor(fa.getResources().getColor(android.R.color.tertiary_text_light));
		gl.addView(tvRazTitulo);
		
		for(int i = 1; i < enfermedades.length-1; i++)
		{
			TextView tvEnf = new TextView(gl.getContext());
			tvEnf.setText("\t" + enfermedades[i]);
			tvEnf.setTextAppearance(tvEnf.getContext(), android.R.style.TextAppearance_DeviceDefault_Medium);
			gl.addView(tvEnf);
			
			space = new Space(gl.getContext());
			space.setLayoutParams(new LayoutParams(40, 0));
			gl.addView(space);
			
			TextView tvRaz = new TextView(gl.getContext());
			tvRaz.setText("\t" + razonamientos[i]);
			tvRaz.setTextAppearance(tvRaz.getContext(), android.R.style.TextAppearance_DeviceDefault_Medium);
			
			gl.addView(tvRaz);
		}
		
		TextView tvEnf = new TextView(gl.getContext());
		String disease = enfermedades[enfermedades.length-1];
		tvEnf.setText("\t" + disease.substring(0, disease.length()-1));
		tvEnf.setTextAppearance(tvEnf.getContext(), android.R.style.TextAppearance_DeviceDefault_Medium);
		gl.addView(tvEnf);
		
		space = new Space(gl.getContext());
		space.setLayoutParams(new LayoutParams(40, 0));
		gl.addView(space);
		
		TextView tvRaz = new TextView(gl.getContext());
		String reason = razonamientos[enfermedades.length-1];
		tvRaz.setText("\t" + reason.substring(0, reason.length()-1));
		tvRaz.setTextAppearance(tvRaz.getContext(), android.R.style.TextAppearance_DeviceDefault_Medium);
		
		gl.addView(tvRaz);
	}

	private void generateCLIPSFileInAppFileDir(String fileName)
	{
		FileOutputStream destinationFileStream = null;
		InputStream assetsOriginFileStream = null;
		try{
			destinationFileStream = fa.getActivity().openFileOutput(fileName, Context.MODE_PRIVATE);
			assetsOriginFileStream = fa.getActivity().getAssets().open(fileName);
			
			int aByte;
			while((aByte = assetsOriginFileStream.read())!=-1){
				destinationFileStream.write(aByte);
			}			
		}
		catch (IOException e){
			e.printStackTrace();
		}
		finally{
			try
			{
				assetsOriginFileStream.close();
				destinationFileStream.close();
			}
			catch (IOException e){
				e.printStackTrace();
			}			
		}		
	}
}
