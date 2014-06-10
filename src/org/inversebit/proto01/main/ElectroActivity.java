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


import org.inversebit.proto01.Constants;
import org.inversebit.proto01.connection.SocketHolder;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class ElectroActivity extends FragmentActivity
{
	private FragmentNavigationDrawer dlDrawer;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.d(Constants.TAG, "EA: onCreate");
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_electro); 
		
		initDrawer(savedInstanceState);
	}

	private void initDrawer(Bundle savedInstanceState)
	{
		dlDrawer = (FragmentNavigationDrawer) findViewById(R.id.drawer_layout);
        // Setup drawer view
        dlDrawer.setupDrawerConfiguration((ListView) findViewById(R.id.lvDrawer), 
                     R.layout.drawer_nav_item, R.id.flContent);
        // Add nav items
        dlDrawer.addNavItem("Electro", "Electro", ElectroWavesFragment.class);
        dlDrawer.addNavItem("Diagnostico", "Diagnostico", DiagnoserFragment.class);
        
        // Select default
        if (savedInstanceState == null) {
            dlDrawer.selectDrawerItem(0);   
        }		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.electro, menu);
		return true;
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		SocketHolder.getMySH().releaseBluetoothSocket();
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
	    if (dlDrawer.getDrawerToggle().onOptionsItemSelected(item)) {
	        return true;
	    }
	
	    return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
	    super.onPostCreate(savedInstanceState);
	    // Sync the toggle state after onRestoreInstanceState has occurred.
	    dlDrawer.getDrawerToggle().syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    // Pass any configuration change to the drawer toggles
	    dlDrawer.getDrawerToggle().onConfigurationChanged(newConfig);
	}
	
}
