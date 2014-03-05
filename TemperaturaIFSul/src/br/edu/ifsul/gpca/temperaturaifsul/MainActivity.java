package br.edu.ifsul.gpca.temperaturaifsul;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static boolean debugar = true;
	private TextView temp;
	private TextView ur;
	private TextView pressao;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		temp = (TextView) findViewById(R.id.txtTemp);
		ur = (TextView) findViewById(R.id.txtUR);
		pressao = (TextView) findViewById(R.id.txtPress);

	}

	@Override
	protected void onResume() {
		super.onResume();
		new buscaJson().execute("");
	}
	
	public void refresh(View v){
		onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onMenuItemSelected(int featurId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mBlog:
			Uri url = Uri.parse("http://arduinizando.blogspot.com");
			Intent itBlog = new Intent(Intent.ACTION_VIEW, url);
			startActivity(itBlog);
			return true;
		case R.id.mAbout:
			Intent itSobre = new Intent(this, About.class);
			startActivity(itSobre);
			return true;
		}
		return false;

	}

	private class buscaJson extends AsyncTask<String, Void, JSONObject> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(MainActivity.this);
			if (debugar == true) {
				Log.d("debug", "preexecute");
			}
			dialog.show();
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			if (result != null) {
				if (debugar == true) {
					Log.d("debug", result.toString());
				}
				try {
					Double tempMedia = (result.getDouble("tempBMP") + result
							.getDouble("tDTH")) / 2;
					Double umidity = result.getDouble("uDTH");
					Double press = result.getDouble("pressBMP");

					temp.setText("Temperatura: " + tempMedia + " ºC");
					ur.setText("Umidade Relativa: " + umidity + "%");
					pressao.setText("Pressão Atmosférica: " + press + "Pa");
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				if (debugar == true) {
					Log.d("debug", "erro de busca de json");
				}
			}
			dialog.dismiss();

		}

		@Override
		protected JSONObject doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				String param = params[0];
				String addressArduino = "http://192.168.0.5/json/";
				String url = Uri.parse(addressArduino + param).toString();
				if (debugar == true) {
					Log.d("debug", "buscando em: " + url);
				}

				String jsonUrl = HTTPUtils.acessar(url);
				if (debugar == true) {
					Log.d("debug", "o que pegou foi: " + jsonUrl);
				}

				JSONObject resultJson = new JSONObject(jsonUrl);
				if (debugar == true) {
					Log.d("debug", "o resultJson =" + resultJson);
				}

				JSONObject resultados = resultJson.getJSONObject("results");
				if (debugar == true) {
					Log.d("debug", "o array =" + resultados);
				}

				return resultados;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}
