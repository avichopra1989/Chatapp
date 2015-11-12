package com.chatt.demo;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.UnderlineSpan;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.chatt.demo.custom.CustomActivity;
import com.chatt.demo.model.Conversation;
import com.chatt.demo.utils.Const;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

/**
 * The Class Chat is the Activity class that holds main chat screen. It shows
 * all the conversation messages between two users and also allows the user to
 * send and receive messages.
 */
public class Chat extends CustomActivity implements OnItemClickListener {

	/** The Conversation list. */
	private ArrayList<Conversation> convList;

	/** The chat adapter. */
	private ChatAdapter adp;

	/** The Editext to compose the message. */
	private EditText txt;

	/** The user name of buddy. */
	private String buddy;

	/** The date of last message in conversation. */
	private Date lastMsgDate;

	/** Flag to hold if the activity is running or not. */
	private boolean isRunning;

	/** The handler. */
	private static Handler handler;

	/** Open gallery app */
	private static final int PICK_IMAGE = 100;

	/** Open Maps activity */
	private int PLACE_PICKER_REQUEST = 1;

	/** Image which is being attached */
	private static Uri imageUri;

	/** website snaphsot generator */
	private static String thumbnailGenerator = "https://api.thumbnail.ws/api/abed97fa3d7a40766045d4346f14f068e8abef3d55a1/thumbnail/get?url=";
	private static String thumbPath = "&width=480&format=PNG";

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);

		convList = new ArrayList<Conversation>();
		ListView list = (ListView) findViewById(R.id.list);
		adp = new ChatAdapter();
		list.setAdapter(adp);
		list.setOnItemClickListener(this);
		list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		list.setStackFromBottom(true);

		txt = (EditText) findViewById(R.id.txt);
		txt.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_MULTI_LINE);

		setTouchNClick(R.id.btnSend);

		buddy = getIntent().getStringExtra(Const.EXTRA_DATA);
		getActionBar().setTitle(buddy);

		handler = new Handler();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		isRunning = true;
		loadConversationList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		isRunning = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.socialshare.custom.CustomFragment#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (v.getId() == R.id.btnSend) {
			sendMessage();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
	 * .AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Conversation c = convList.get(arg2);
		if(c.getIsMap()){
			String uri = String.format(Locale.ENGLISH, "geo:" + c.getMsg());
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			startActivity(intent);
		}
	}

	/**
	 * Call this method to Send message to opponent. It does nothing if the text
	 * is empty otherwise it creates a Parse object for Chat message and send it
	 * to Parse server.
	 */
	private void sendMessage() {
		if (txt.length() == 0)
			return;

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);

		final String s = txt.getText().toString();
		if (Patterns.WEB_URL.matcher(s).matches()) {
			/**
			 * Fetch the snapshot from url in background
			 */
			new AsyncTask<String, String, Bitmap>() {
				ProgressDialog progress;

				@Override
				protected void onPreExecute() {
					progress = ProgressDialog.show(Chat.this, null,
							getString(R.string.alert_wait));
					super.onPreExecute();
				}

				@Override
				protected Bitmap doInBackground(String... params) {
					Bitmap bitmap = null;
					try {
						String snapUrl = thumbnailGenerator + s + thumbPath;
						bitmap = BitmapFactory
								.decodeStream((InputStream) new URL(snapUrl)
										.getContent());
					} catch (Exception e) {
						e.printStackTrace();
					}

					return bitmap;
				}

				protected void onPostExecute(Bitmap result) {
					progress.cancel();
					if (result != null) {
						sendImageMessage(result);
					} else {
						sendTextMessage(s);
					}
				};
			}.execute("");
		} else {
			sendTextMessage(s);
		}
		txt.setText(null);
	}

	/**
	 * private function written to send text messages
	 * 
	 * @param message
	 */
	private void sendTextMessage(String message) {
		final Conversation c = new Conversation(message, new Date(),
				UserList.user.getUsername());
		c.setStatus(Conversation.STATUS_SENDING);
		convList.add(c);
		adp.notifyDataSetChanged();

		ParseObject po = new ParseObject("Chat");
		po.put("sender", UserList.user.getUsername());
		po.put("receiver", buddy);
		// po.put("createdAt", "");
		po.put("message", message);
		po.saveEventually(new SaveCallback() {

			@Override
			public void done(ParseException e) {
				if (e == null)
					c.setStatus(Conversation.STATUS_SENT);
				else
					c.setStatus(Conversation.STATUS_FAILED);
				adp.notifyDataSetChanged();
			}
		});
	}

	/**
	 * send Map Event
	 */
	private void sendMapMessage(String map) {
		String message = map.replace("lat/lng: (", "");
		message = message.replace(")", "");
		final Conversation c = new Conversation(message, new Date(),
				UserList.user.getUsername());
		c.setStatus(Conversation.STATUS_SENDING);
		c.setIsMap(true);
		convList.add(c);
		adp.notifyDataSetChanged();

		ParseObject po = new ParseObject("Chat");
		po.put("sender", UserList.user.getUsername());
		po.put("receiver", buddy);
		// po.put("createdAt", "");
		po.put("message", message);
		po.put("isMap", true);
		po.saveEventually(new SaveCallback() {

			@Override
			public void done(ParseException e) {
				if (e == null)
					c.setStatus(Conversation.STATUS_SENT);
				else
					c.setStatus(Conversation.STATUS_FAILED);
				adp.notifyDataSetChanged();
			}
		});
	}

	/**
	 * private function written to send image messages
	 * 
	 * @param bitmap
	 */
	private void sendImageMessage(Bitmap bitmap) {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
			byte[] image = stream.toByteArray();
			final ParseFile file = new ParseFile("myImage", image);
			// Upload the image into Parse Cloud
			file.saveInBackground(new SaveCallback() {

				@Override
				public void done(ParseException e) {
					if (e == null) {
						final Conversation c = new Conversation("", new Date(),
								UserList.user.getUsername());
						c.setStatus(Conversation.STATUS_SENDING);
						c.setIsImage(true);
						c.setImage(file);
						convList.add(c);
						adp.notifyDataSetChanged();
						txt.setText(null);

						ParseObject po = new ParseObject("Chat");
						po.put("sender", UserList.user.getUsername());
						po.put("receiver", buddy);
						po.put("Image", file);
						// po.put("createdAt", "");
						po.put("message", "");
						po.saveEventually(new SaveCallback() {

							@Override
							public void done(ParseException e) {
								if (e == null)
									c.setStatus(Conversation.STATUS_SENT);
								else
									c.setStatus(Conversation.STATUS_FAILED);
								adp.notifyDataSetChanged();
							}
						});
					} else {

					}

				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Load the conversation list from Parse server and save the date of last
	 * message that will be used to load only recent new messages
	 */
	private void loadConversationList() {
		ParseQuery<ParseObject> q = ParseQuery.getQuery("Chat");
		if (convList.size() == 0) {
			// load all messages...
			ArrayList<String> al = new ArrayList<String>();
			al.add(buddy);
			al.add(UserList.user.getUsername());
			q.whereContainedIn("sender", al);
			q.whereContainedIn("receiver", al);
		} else {
			// load only newly received message..
			if (lastMsgDate != null)
				q.whereGreaterThan("createdAt", lastMsgDate);
			q.whereEqualTo("sender", buddy);
			q.whereEqualTo("receiver", UserList.user.getUsername());
		}
		q.orderByDescending("createdAt");
		q.setLimit(30);
		q.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> li, ParseException e) {
				if (li != null && li.size() > 0) {
					for (int i = li.size() - 1; i >= 0; i--) {
						ParseObject po = li.get(i);
						Conversation c = new Conversation(po
								.getString("message"), po.getCreatedAt(), po
								.getString("sender"));
						if (po.has("Image")) {
							c.setIsImage(true);
							c.setImage(po.getParseFile("Image"));
						}
						if (po.has("isMap")) {
							c.setIsMap(po.getBoolean("isMap"));
						}
						convList.add(c);
						if (lastMsgDate == null
								|| lastMsgDate.before(c.getDate()))
							lastMsgDate = c.getDate();
						adp.notifyDataSetChanged();
					}
				}
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						if (isRunning)
							loadConversationList();
					}
				}, 1000);
			}
		});

	}

	/**
	 * The Class ChatAdapter is the adapter class for Chat ListView. This
	 * adapter shows the Sent or Receieved Chat message in each list item.
	 */
	private class ChatAdapter extends BaseAdapter {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return convList.size();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Conversation getItem(int arg0) {
			return convList.get(arg0);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		@Override
		public View getView(int pos, View v, ViewGroup arg2) {
			Conversation c = getItem(pos);
			if (c.isSent())
				v = getLayoutInflater().inflate(R.layout.chat_item_sent, null);
			else
				v = getLayoutInflater().inflate(R.layout.chat_item_rcv, null);

			TextView lbl = (TextView) v.findViewById(R.id.lbl1);
			lbl.setText(DateUtils.getRelativeDateTimeString(Chat.this, c
					.getDate().getTime(), DateUtils.SECOND_IN_MILLIS,
					DateUtils.DAY_IN_MILLIS, 0));

			lbl = (TextView) v.findViewById(R.id.lbl2);
			if (c.getIsImage()) {
				ParseImageView imageView = (ParseImageView) v
						.findViewById(R.id.image);
				lbl.setVisibility(View.GONE);
				imageView.setVisibility(View.VISIBLE);
				imageView.setParseFile(c.getImage());
				imageView.loadInBackground();
			} else {
				if (c.getIsMap()) {
					SpannableString spanStr = new SpannableString(c.getMsg().toString());
					spanStr.setSpan(new UnderlineSpan(), 0, spanStr.length(), 0);
					lbl.setText(spanStr);
				} else {
					lbl.setText(c.getMsg());
				}
			}

			lbl = (TextView) v.findViewById(R.id.lbl3);
			if (c.isSent()) {
				if (c.getStatus() == Conversation.STATUS_SENT)
					lbl.setText("Delivered");
				else if (c.getStatus() == Conversation.STATUS_SENDING)
					lbl.setText("Sending...");
				else
					lbl.setText("Failed");
			} else
				lbl.setText("");

			return v;
		}

	}

	/**
	 * Open the Maps Activity to send Location
	 */
	private void openMaps() {
		PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

		try {
			startActivityForResult(builder.build(Chat.this),
					PLACE_PICKER_REQUEST);
		} catch (GooglePlayServicesRepairableException e) {
			e.printStackTrace();
		} catch (GooglePlayServicesNotAvailableException e) {
			e.printStackTrace();
		}
	}

	/** Open gallery app */
	private void openGallery() {
		Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
		getIntent.setType("image/*");

		Intent pickIntent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		pickIntent.setType("image/*");

		Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
				new Intent[] { pickIntent });

		startActivityForResult(chooserIntent, PICK_IMAGE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == PICK_IMAGE) {
				imageUri = data.getData();
				Bitmap bitmap;
				try {
					bitmap = MediaStore.Images.Media.getBitmap(
							this.getContentResolver(), imageUri);
					sendImageMessage(bitmap);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (requestCode == PLACE_PICKER_REQUEST) {
				Place place = PlacePicker.getPlace(data, this);
				sendMapMessage(place.getLatLng().toString());
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.chat_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		if (item.getItemId() == R.id.attachButton) {
			openGallery();
		}
		if (item.getItemId() == R.id.mapsButton) {
			openMaps();
		}
		return super.onOptionsItemSelected(item);
	}
}
