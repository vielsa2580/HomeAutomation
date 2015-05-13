package com.orvito.homevito.helpers;

import java.util.List;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.orvito.homevito.R;
import com.orvito.homevito.models.MODELBlock;
import com.orvito.homevito.models.MODELResultSet;
import com.orvito.homevito.outmsgfactory.OMUserDirectory;
import com.orvito.homevito.utils.UTILConstants;
import com.orvito.homevito.utils.UTILSharedPreference;


public class HLPRUserDirectory {

	public List<MODELBlock> populateResidentDirectory(Activity activity,Handler handler,LinearLayout _UILLResidentDirectory){
		
		String authToken=UTILSharedPreference.getPreference(activity, UTILConstants.AUTHTOKEN);
		MODELResultSet resultSetObject = new OMUserDirectory().tcpEmpDirectory(activity,handler);
		ViewPager viewPager = (ViewPager)_UILLResidentDirectory.findViewById(R.id.awesomepager);
	
		if(resultSetObject.getError()==null){
			List<MODELBlock> blocksList=(List<MODELBlock>)resultSetObject.getDataList();
			
			if(blocksList==null){
				viewPager.setVisibility(View.INVISIBLE);
				Toast.makeText(activity, "No files assigned to you", Toast.LENGTH_LONG).show();
				return null;
			}else if(blocksList.size()<1){
				viewPager.setVisibility(View.INVISIBLE);
				Toast.makeText(activity, "No files assigned to you", Toast.LENGTH_LONG).show();
				return null;
			}else{
				viewPager.setVisibility(View.VISIBLE);
				LinearLayout LLResidentDetail=(LinearLayout) _UILLResidentDirectory.findViewById(R.id.LLresidentDetail);
				viewPager.setAdapter(new ADPTUserDirPager(activity,handler, LLResidentDetail,blocksList));
				return blocksList;
			}

		}else{
			Toast.makeText(activity, resultSetObject.getMessage(), Toast.LENGTH_LONG).show();
			return null;
		}	
		

		
	}

}
