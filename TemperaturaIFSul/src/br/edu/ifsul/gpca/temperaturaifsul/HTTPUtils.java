package br.edu.ifsul.gpca.temperaturaifsul;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class HTTPUtils {
	public static String acessar(String endereco) {
		try {
			URL url = new URL(endereco);
			URLConnection conn = url.openConnection();
			InputStream inStream = conn.getInputStream();
			Scanner scanner = new Scanner(inStream);
			String conteudo = scanner.useDelimiter("\\A").next();
			return conteudo;
		} catch (Exception e) {
			return null;
		}
	}

}
