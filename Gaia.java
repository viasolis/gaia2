package gaia;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class Gaia {
	private StringBuffer sb;
	private static final String GAIA_SOURCE_URL = "http://cdn.gea.esac.esa.int/Gaia/gdr2/gaia_source/csv/";
	private static final String TEMP_DIR_PATH = "d:\\gaia\\";
	private static final String TEMP_FILE_PATH = TEMP_DIR_PATH + "temp.gz";

	public Gaia() {
		if (input(GAIA_SOURCE_URL, false)) {
			String[] str = sb.toString().split("<a href=\"", 0);
			int len = str.length;
			int i = 0;
			Pattern p = Pattern.compile(".*.csv.gz");
			List<String> list = new ArrayList<String>();
			File file = null;

			for (i = 0; i < len; i++) {
				Matcher m = p.matcher(str[i]);
				while (m.find()) {
					list.add(m.group());
				}
			}

			len = list.size();

			for (i = 0; i < len; i++) {
				file = new File(TEMP_DIR_PATH + list.get(i).replace(".gz", ""));

				if (!file.exists()) {
					if (input(GAIA_SOURCE_URL + list.get(i), true)) {
						output(file);
						System.out.println(i + ":OK:" + list.get(i));
					} else {
						System.out.println(i + ":NG:" + list.get(i));
					}
				} else {
					System.out.println(i + ":Skip:" + list.get(i));
				}
			}
		}
	}

	private boolean input(String url_str, boolean save) {
		boolean ret = false;
		HttpURLConnection con = null;

		try {
			Thread.sleep(5000);
			URL url = new URL(url_str);
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.connect();
			final int status = con.getResponseCode();

			if (status == HttpURLConnection.HTTP_OK) {
				final InputStream is = con.getInputStream();

				if (save) {
					DataInputStream dis = new DataInputStream(is);
					FileOutputStream fos = new FileOutputStream(TEMP_FILE_PATH);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					DataOutputStream dos = new DataOutputStream(bos);
					byte[] b = new byte[4096];
					int n = 0;

					while (-1 != (n = dis.read(b))) {
						dos.write(b, 0, n);
					}

					dos.close();
					bos.close();
					fos.close();
					dis.close();

				} else {
					String encoding = con.getContentEncoding();

					if (encoding == null) {
						encoding = "UTF-8";
					}

					final InputStreamReader isr = new InputStreamReader(is, encoding);
					final BufferedReader br = new BufferedReader(isr);
					String line = null;
					sb = new StringBuffer();

					while ((line = br.readLine()) != null) {
						sb.append(line);
					}

					br.close();
					isr.close();
				}

				is.close();
				ret = true;
			} else {
				System.out.println(status);
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}

		return ret;
	}

	private void output(File fo) {
		File fi = new File(TEMP_FILE_PATH);

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new BufferedInputStream(new FileInputStream(fi)))));
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fo)));
			String line = br.readLine();
			String[] data = null;

			while ((line = br.readLine()) != null) {
				data = line.split(",", 0);
				pw.println(data[3] + "," + data[5].replace(".", ",") + "," + data[7].replace(".", ",") + "," + data[50].replace(".", ","));
			}

			br.close();
			pw.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		fi.delete();
	}
}
