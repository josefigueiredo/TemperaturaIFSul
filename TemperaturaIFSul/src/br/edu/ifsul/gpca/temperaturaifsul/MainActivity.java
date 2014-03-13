package br.edu.ifsul.gpca.temperaturaifsul;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.util.FloatMath;

public class MainActivity extends Activity {
	private static boolean debugar = true;
	private TextView temp;
	private TextView ur;
	private TextView pressao;
	private TextView velVento;
	private TextView sensacaoTermica;
	private double speedWin;
	private double tempMedia;
	private boolean tempLida,windSpeedLida;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		temp = (TextView) findViewById(R.id.txtTemp);
		ur = (TextView) findViewById(R.id.txtUR);
		pressao = (TextView) findViewById(R.id.txtPress);
		velVento = (TextView) findViewById(R.id.txtWindSpeed);
		sensacaoTermica = (TextView) findViewById(R.id.txtTermalSensation);

		
		//inicializa com false as flags de leiturasObitdas
		tempLida = false;
		windSpeedLida = false;

	}

	@Override
	protected void onResume() {
		super.onResume();
		new buscaJson().execute("");
		
	}

	public void refresh(View v) {
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
			dialog.setTitle("Buscando dados na miniEstação arduino!");
			if (debugar == true) {
				Log.d("debug_arduino", "preexecute");
			}
			dialog.show();
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			if (result != null) {
				if (debugar == true) {
					Log.d("debug_arduino", result.toString());
				}
				try {
					tempMedia = (result.getDouble("tempBMP") + result
							.getDouble("tDTH")) / 2;
					Double umidity = result.getDouble("uDTH");
					Double press = result.getDouble("pressBMP");
					tempLida = true;

					temp.setText("Temperatura: " + tempMedia + " ºC");
					ur.setText("Umidade Relativa: " + umidity + "%");
					pressao.setText("Pressão Atmosférica: " + String.format("%.2f", press/100) + "hpa");
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				if (debugar == true) {
					Log.d("debug_arduino", "erro de busca de json");
				}
			}
			dialog.dismiss();
			//agora abre nova assyncTask para pegar velocidade do vento....
			new ReadWeatherJSONFeedTask().execute("");

		}

		@Override
		protected JSONObject doInBackground(String... params) {
			try {
				String param = params[0];
				String addressArduino = "http://10.5.99.5/json/";
				String url = Uri.parse(addressArduino + param).toString();
				if (debugar == true) {Log.d("debug_arduino", "buscando em: " + url);}

				String jsonUrl = HTTPUtils.acessar(url);
				if (debugar == true) {Log.d("debug_arduino", "o que pegou foi: " + jsonUrl);}

				JSONObject resultJson = new JSONObject(jsonUrl);
				if (debugar == true) {Log.d("debug_arduino", "o resultJson =" + resultJson);}

				JSONObject resultados = resultJson.getJSONObject("results");
				if (debugar == true) {Log.d("debug_arduino", "o array =" + resultados);}

				return resultados;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private class ReadWeatherJSONFeedTask extends AsyncTask<String, Void, JSONObject> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(MainActivity.this);
			dialog.setTitle("Buscando velocidade do vento em api.openweathermap.org !");

			if (debugar == true) {
				Log.d("debug_wheater", "preexecute");
			}
			dialog.show();
		}
		@Override
		protected JSONObject doInBackground(String... params) {
			try {
				String param = params[0];
				String weatherLink = "http://api.openweathermap.org/data/2.5/weather?q=Passo%20Fundo,BR";
				String weatherURL = Uri.parse(weatherLink + param).toString();

				if (debugar == true) {Log.d("debug_wheater", "buscando em: " + weatherURL);}

				String jsonUrl = HTTPUtils.acessar(weatherURL);
				if (debugar == true) {Log.d("debug_wheater", "o que pegou foi: " + jsonUrl);}

				JSONObject resultJson = new JSONObject(jsonUrl);
				if (debugar == true) {Log.d("debug_wheater", "o resultJson =" + resultJson);}

				JSONObject resultados = resultJson.getJSONObject("wind");
				if (debugar == true) {Log.d("debug_wheater", "o array =" + resultados);}

				return resultados;

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(JSONObject result) {
			if (result != null) {
				if (debugar == true) {Log.d("debug_wheater", result.toString());}
				try {
					if (debugar == true) {Log.d("debug_wheater", result.getString("speed").toString() );}
					
					// a velocidade obtida está em m/s então preciso converter para km/h
					//1ms = 3,6km/h
					speedWin = result.getDouble("speed")* 3.6 ;
					windSpeedLida = true;

					velVento.setText("Velocidade vento: " + String.format("%.2f", speedWin)+ " km/h");
					
					float p3=0,p2=0,p1=0; 

					//caluclo de sensacao termica baseado na formula do link
					//http://www.sofisica.com.br/conteudos/curiosidades/sensacaotermica.php
							
					if(tempLida ){
						p3 = ((float)tempMedia-33)/22;}
					if(windSpeedLida){
						p1 = 10 * FloatMath.sqrt((float)(speedWin));
						p2 = (float) (10.45 - (float)(speedWin));
					}
					Log.d("debug_sensacao","p1="+p1);
					Log.d("debug_sensacao","p2="+p2);
					Log.d("debug_sensacao","p3="+p3);
					
					float st = 33 + (p1 + p2)* p3;
					//String.format("%.4f", vis
					sensacaoTermica.setText("Sensação Térmica: "+String.format("%.2f", st)+" ºC");
					windSpeedLida = false;
					tempLida = false;

				} catch (Exception e) {
					Log.d("ReadWeatherJSONFeedTask", e.getLocalizedMessage());
				}

			}
			dialog.dismiss();
		}
	}

	public String readJSONFeed(String URL) {
		StringBuilder stringBuilder = new StringBuilder();
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(URL);
		try {
			HttpResponse response = httpClient.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream inputStream = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream));
				String line;
				while ((line = reader.readLine()) != null) {
					stringBuilder.append(line);
				}
				inputStream.close();
			} else {
				Log.d("debug_wheater", "Failed to download file");
			}
		} catch (Exception e) {
			Log.d("readJSONFeed", e.getLocalizedMessage());
		}
		return stringBuilder.toString();
	}

}
