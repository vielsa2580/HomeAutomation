package com.orvito.homevito.helpers;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.orvito.homevito.R;
import com.orvito.homevito.models.MODELBlock;
import com.orvito.homevito.models.MODELUser;
import com.orvito.homevito.utils.UTILConstants;

public class ADPTUserDirPager extends PagerAdapter implements OnItemClickListener{

	List<MODELUser> modelResidentsList,modelResidentsFilteredList;
	ImageView _UIIVSearch;
	Activity activity;
	TextView _UITVName,_UITVAddress,_UITVEmailId,_UITVPhoneNo,_UITVWelcomeTitle,_UITVWelcomeMsg,_UITVSkillsTitle,_UITVSkills,_UITVHobbies;
	ImageView _UIIVResidentImage,seperator1,seperator2;
	ViewFlipper viewFlipper;
	LinearLayout _UILLResidentDetail,_UILLResidentDetailSkillSet,_UILLResidentDetailMessage;
	RelativeLayout _UIRLResidentDetailVisitingCard;
	//Button _UIBTIntercom,_UIBTSms,_UIBTInvite;
	List<MODELBlock> blocksList;
	ADPTUserGrid _adptUserGrid;
	Handler handler;

	public ADPTUserDirPager(final Activity activity, 	Handler handler, final LinearLayout _UILLResidentDetail,List<MODELBlock> blocksList) {

		this.blocksList = blocksList;
		this.handler=handler;
		getHandles(activity,_UILLResidentDetail);
	}


	public int getCount() {
		return 1;
	}


	public Object instantiateItem(View collection, int position) {

		LayoutInflater inflater = (LayoutInflater) collection.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.plain_temp_layout, null);
		view = residentGridPopulate(inflater);
		((ViewPager) collection).addView(view, 0);

		return view;
	}

	private View residentGridPopulate(LayoutInflater inflater) {

		View view = inflater.inflate(R.layout.list_users, null);
		final ListView _GVResidentGrid = (ListView)view.findViewById(R.id.LVResident);
		_GVResidentGrid.setOnItemClickListener(this);

		EditText _UIEDSearch = (EditText)view.findViewById(R.id.EDSearch);
		TextView TVResidentDirectory=(TextView)view.findViewById(R.id.TVResidentDirectory);
		TVResidentDirectory.setText(activity.getString(R.string.userdir).toUpperCase()+" DIRECTORY");
		_adptUserGrid=new ADPTUserGrid(activity,handler,getResidentData(blocksList));
		_GVResidentGrid.setAdapter(_adptUserGrid);
		modelResidentsList =  getResidentData(blocksList);
		if(modelResidentsList == null) return null; 
		modelResidentsFilteredList=new ArrayList<MODELUser>(modelResidentsList);
		showFullDetail(modelResidentsFilteredList.get(0),UTILConstants.getUsersColor(0, activity));

		_UIEDSearch.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				_GVResidentGrid.setBackgroundResource(0); 
				if(s.length()<1){
					modelResidentsFilteredList=modelResidentsList;
					_GVResidentGrid.setAdapter(new ADPTUserGrid(activity,handler, modelResidentsFilteredList));
					showFullDetail(modelResidentsFilteredList.get(0),UTILConstants.getUsersColor(0, activity));
					_UILLResidentDetail.setVisibility(View.VISIBLE);
					return;
				}

				modelResidentsFilteredList=null;
				modelResidentsFilteredList=new ArrayList<MODELUser>();		
				List<String> userProvidedStringList;


				int  m=0;
				userProvidedStringList=new ArrayList<String>(); 
				for (int j=0; j < s.length(); j++){

					if(new String(""+s.charAt(j)).equals(" ")){
						userProvidedStringList.add(new String(s.toString()).substring(m,j).toLowerCase());
						m=j+1;
					}						
				}

				String tempString = new String(s.toString()).substring(m,s.length());
				if(tempString.length()!=0){

					// Add "tempstring" to "userProvidedStringList" only when value is added and neglect adding to "tempString" if space is encountered   // 

					userProvidedStringList.add(tempString);

				}
				//userProvidedStringList.add(new String(s.toString()).substring(m,s.length()));

				for (int i = 0; i < userProvidedStringList.size(); i++) {
					for (int j = 0; j < modelResidentsList.size(); j++) {


						if(modelResidentsList.get(j).getFirstName().toLowerCase().startsWith(userProvidedStringList.get(i)) || modelResidentsList.get(j).getLastName().toLowerCase().startsWith(userProvidedStringList.get(i))  ||  modelResidentsList.get(j).getBlockName().toLowerCase().startsWith(userProvidedStringList.get(i)) || modelResidentsList.get(j).getFlatNo().startsWith(userProvidedStringList.get(i)) || modelResidentsList.get(j).getAddress().startsWith(userProvidedStringList.get(i)) || modelResidentsList.get(j).getAddress().toLowerCase().contains(userProvidedStringList.get(i).toLowerCase())) {

							Boolean found=false;
							for (int k = 0; k < modelResidentsFilteredList.size(); k++) {
								if(modelResidentsFilteredList.get(k).equals(modelResidentsList.get(j))){
									found=true;
								}
							}

							if(!found) modelResidentsFilteredList.add(modelResidentsList.get(j));

						}

					}
				}
				if(modelResidentsFilteredList.size()<1){
					_GVResidentGrid.setBackgroundResource(R.drawable.nocontacts);//
					_UILLResidentDetail.setVisibility(View.INVISIBLE);
				}else{
					_UILLResidentDetail.setVisibility(View.VISIBLE);					
					showFullDetail(modelResidentsFilteredList.get(0),UTILConstants.getUsersColor(0, activity));
				}
				_GVResidentGrid.setAdapter(new ADPTUserGrid(activity,handler, modelResidentsFilteredList));


			}


			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}


			public void beforeTextChanged(CharSequence s, int start, int count,int after) {
				// TODO Auto-generated method stub

			}	
		}
				);			


		return view;
	}



	private  List<MODELUser> getResidentData(List<MODELBlock> blockList){

		if(blockList != null){

			List<MODELUser> residentList = new ArrayList<MODELUser>();

			for (int i = 0; i < blockList.size(); i++) {
				List<MODELUser> residentList2 = blockList.get(i).getModelResidentList();	
				for (int j = 0; j < residentList2.size(); j++) {

					residentList.add(residentList2.get(j));
				}

			}
			return residentList; 

		}else {

			Toast.makeText(activity, "No data available..", Toast.LENGTH_LONG).show();
			return null; 
		}



	}


	public void destroyItem(View arg0, int arg1, Object arg2) {
		((ViewPager) arg0).removeView((View) arg2);

	}


	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == ((View) arg1);

	}


	public Parcelable saveState() {
		return null;
	}


	int flag1 = 0,flag2 = 0, flag3 = 0;




	public void onItemClick(AdapterView<?> arg0, View v, int position,long arg3) {
		ImageView residentOriginalImage=(ImageView) v.findViewById(R.id.originalImage);
		MODELUser modelUser=modelResidentsFilteredList.get(position);
		modelUser.setUserImage(residentOriginalImage.getBackground());
		showFullDetailOnItemClick(modelUser,UTILConstants.getUsersColor(position, activity));	
	}

	public  void showFullDetail(final MODELUser resident,int colorcode){
		//colorcode=android.R.color.transparent;
		_UIRLResidentDetailVisitingCard.setBackgroundColor(colorcode);
		_UILLResidentDetailMessage.setBackgroundColor(colorcode);
		_UILLResidentDetailSkillSet.setBackgroundColor(colorcode);
		_UITVName.setText(resident.getFirstName()+" "+resident.getLastName());
		_UITVAddress.setText(resident.getAddress());
		_UITVEmailId.setText(resident.getEmail());_UITVWelcomeTitle.setText("INTRODUCTION");
		_UITVSkillsTitle.setText("SKILLS & TITLE");
		seperator1.setVisibility(View.VISIBLE);
		seperator2.setVisibility(View.VISIBLE);
		_UITVSkills.setText("Skills:             "+resident.getSkills());
		_UITVHobbies.setText("Hobbies:       "+resident.getHobbies());
		_UITVWelcomeMsg.setText("It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using &apos;Content here, content here&apos;, making it look like readable English");
		if(resident.getUserImage()!=null){
			Bitmap bitmap = ((BitmapDrawable)resident.getUserImage()).getBitmap();
			bitmap =Bitmap.createScaledBitmap(bitmap, 129, 128, false);
			_UIIVResidentImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			_UIIVResidentImage.setVisibility(View.VISIBLE);
		}else{
			_UIIVResidentImage.setBackgroundResource(R.drawable.emptycontact);
			_UIIVResidentImage.setVisibility(View.VISIBLE);
		}
	}


	public  void showFullDetailOnItemClick(MODELUser resident,int colorcode){
		//colorcode=android.R.color.transparent;
		_UIRLResidentDetailVisitingCard.setBackgroundColor(colorcode);
		_UILLResidentDetailMessage.setBackgroundColor(colorcode);
		_UILLResidentDetailSkillSet.setBackgroundColor(colorcode);
		_UITVName.setText(resident.getFirstName()+" "+resident.getLastName());
		_UITVAddress.setText(resident.getAddress());
		_UITVEmailId.setText(resident.getEmail());
		_UITVWelcomeTitle.setText("INTRODUCTION");
		_UITVSkillsTitle.setText("SKILLS & TITLE");
		seperator1.setVisibility(View.VISIBLE);
		seperator2.setVisibility(View.VISIBLE);
		_UITVHobbies.setText("Hobbies:       "+resident.getHobbies());
		_UITVWelcomeMsg.setText("It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using &apos;Content here, content here&apos;, making it look like readable English");

		if(resident.getUserImage()!=null){
			Bitmap bitmap = ((BitmapDrawable)resident.getUserImage()).getBitmap();
			bitmap =Bitmap.createScaledBitmap(bitmap, 129, 128, false);
			_UIIVResidentImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			_UIIVResidentImage.setVisibility(View.VISIBLE);
		}else{
			_UIIVResidentImage.setBackgroundResource(R.drawable.emptycontact);
			_UIIVResidentImage.setVisibility(View.VISIBLE);
		}
		//_UITVPhoneNo.setText(resident.getSipId());
		//_UILLResidentDetail.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.right_to_left));

	}





	private void getHandles(Activity activity, LinearLayout _UILLResidentDetail) {
		final Typeface tf = Typeface.createFromAsset(activity.getAssets(),"Ubuntu-Title.ttf");
		final Typeface tf1 = Typeface.createFromAsset(activity.getAssets(),"PinstripeLimo.ttf");


		this.activity = activity;
		this._UIRLResidentDetailVisitingCard=(RelativeLayout) _UILLResidentDetail.findViewById(R.id.userdetailvisitingcard);
		this._UILLResidentDetailMessage=(LinearLayout) _UILLResidentDetail.findViewById(R.id.userdetailmessage);
		this._UILLResidentDetailSkillSet=(LinearLayout) _UILLResidentDetail.findViewById(R.id.userdetailskillset);
		this._UITVName = (TextView) _UILLResidentDetail.findViewById(R.id.residentname);

		this._UITVAddress =(TextView) _UILLResidentDetail.findViewById(R.id.residentaddress);

		this._UITVEmailId = (TextView) _UILLResidentDetail.findViewById(R.id.residentemail);

		this._UITVPhoneNo = (TextView) _UILLResidentDetail.findViewById(R.id.residentmobile);
		_UITVWelcomeTitle = (TextView) _UILLResidentDetail.findViewById(R.id.residentwelcometitle);
		_UITVWelcomeMsg = (TextView) _UILLResidentDetail.findViewById(R.id.residentwelcomemsg);
		_UITVSkillsTitle = (TextView) _UILLResidentDetail.findViewById(R.id.residentskillstitle);
		_UITVSkills = (TextView) _UILLResidentDetail.findViewById(R.id.residentskills);
		_UITVHobbies = (TextView) _UILLResidentDetail.findViewById(R.id.residenthobbies);
		_UIIVResidentImage= (ImageView) _UILLResidentDetail.findViewById(R.id.residentimage);
		seperator1=(ImageView) _UILLResidentDetail.findViewById(R.id.seperator1);
		seperator2=(ImageView) _UILLResidentDetail.findViewById(R.id.seperator2);


		ADPTUserDirPager.this._UITVName.setTypeface(tf1);
		ADPTUserDirPager.this._UITVAddress.setTypeface(tf);
		ADPTUserDirPager.this._UITVEmailId.setTypeface(tf);
		ADPTUserDirPager.this._UITVPhoneNo.setTypeface(tf);



		this._UILLResidentDetail = _UILLResidentDetail;

		/*this._UIBTIntercom = (Button) _UILLResidentDetail.findViewById(R.id.BTIntercom);
		this._UIBTSms = (Button) _UILLResidentDetail.findViewById(R.id.BTSms);
		this._UIBTInvite = (Button) _UILLResidentDetail.findViewById(R.id.BTInvite);
		_UIBTIntercom.setOnClickListener(this);
		_UIBTSms.setOnClickListener(this);
		_UIBTInvite.setOnClickListener(this);*/


		ADPTUserDirPager.this._UITVName.setTypeface(tf1);
		ADPTUserDirPager.this._UITVAddress.setTypeface(tf);
		ADPTUserDirPager.this._UITVEmailId.setTypeface(tf);
		ADPTUserDirPager.this._UITVPhoneNo.setTypeface(tf);

	}



}