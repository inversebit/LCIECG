/*
Copyright (C) 2014 Inversebit

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>

This code has been modified by Alexander Mariel for th project LCIECG
github.com/Inversebit/LCIECG
*/
package org.inversebit.proto01.connection;

import java.io.IOException;

import org.inversebit.proto01.Constants;

import android.bluetooth.BluetoothSocket;
import android.util.Log;


public class SocketHolder{
	private static SocketHolder mySH = new SocketHolder();
	private BluetoothSocketWrapper btsw;

	private SocketHolder(){
		btsw = new BluetoothSocketWrapper(null);
	}
	
	public static SocketHolder getMySH(){
		return mySH;
	}
	
	public synchronized BluetoothSocketWrapper getBluetoothSocket(){
		//Bad getter
		return btsw;
	}
	
	public synchronized void setBluetoothSocket(BluetoothSocket bts){
		this.btsw = new BluetoothSocketWrapper(bts);
	}
	
	public synchronized void releaseBluetoothSocket(){
		if(btsw != null){
			try
			{
				btsw.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		Log.d(Constants.TAG, "SH: SocketReleased");
	}
	
}
