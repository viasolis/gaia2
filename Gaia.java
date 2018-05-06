package gaia;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
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

import javax.imageio.ImageIO;

public class Gaia {
	private StringBuffer sb;
	private static final String GAIA_SOURCE_URL = "http://cdn.gea.esac.esa.int/Gaia/gdr2/gaia_source/csv/";
	private static final String TEMP_DIR_PATH = "d:\\gaia\\";
	private static final String TEMP_FILE_PATH = TEMP_DIR_PATH + "temp.gz";

	public Gaia() {
		step4();
	}

	public void step1() {
		if (input(GAIA_SOURCE_URL, false)) {
			String[] str = sb.toString().split("<a href=\"", 0);
			int len = str.length;
			int i = 0;
			Pattern p = Pattern.compile(".*.csv.gz\"");
			List<String> list = new ArrayList<String>();
			File file = null;

			for (i = 0; i < len; i++) {
				Matcher m = p.matcher(str[i]);
				while (m.find()) {
					list.add(m.group().replace("\"", ""));
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

	public void step2() {
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File file, String str) {
				if (str.length() > 53) {
					if (str.indexOf("GaiaSource_6") != -1) {
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			}
		};
		File[] list = new File(TEMP_DIR_PATH).listFiles(filter);

		if (list != null) {
			int i, m, n = 0, len = list.length;
			float ra = 0, ra_max = -1, ra_min = 400, ra_max_i = -1, ra_min_i = 400, dec = 0, dec_max = -100, dec_min = 100, dec_max_i = -100, dec_min_i = 100;
			PrintWriter pw = null;

			try {
				pw = new PrintWriter(new BufferedWriter(new FileWriter(new File("d:\\homepage\\astro.starfree.jp\\gaia\\table.txt"))));
			} catch (IOException e) {
				e.printStackTrace();
			}

			java.util.Arrays.sort(list, new java.util.Comparator<File>() {
				public int compare(File file1, File file2) {
					return file1.getName().compareTo(file2.getName());
				}
			});

			for (i = 0; i < len; i++) {
				if (list[i].isFile()) {
					try {
						File file = new File(list[i].toString());
						BufferedReader br = new BufferedReader(new FileReader(file));
						String line = null;
						String name = list[i].toString().replace(TEMP_DIR_PATH, "");
						String[] data = null;
						m = 0;
						ra_max_i = -1;
						ra_min_i = 400;
						dec_max_i = -100;
						dec_min_i = 100;
						System.out.println(name);

						while ((line = br.readLine()) != null) {
							m++;
							n++;
							data = line.split(",", 0);
							ra = Float.parseFloat(data[1] + "." + data[2]);
							dec = Float.parseFloat(data[3] + "." + data[4]);

							if (ra > ra_max) {
								ra_max = ra;
							} else if (ra < ra_min) {
								ra_min = ra;
							}

							if (ra > ra_max_i) {
								ra_max_i = ra;
							} else if (ra < ra_min_i) {
								ra_min_i = ra;
							}

							if (dec > dec_max) {
								dec_max = dec;
							} else if (dec < dec_min) {
								dec_min = dec;
							}

							if (dec > dec_max_i) {
								dec_max_i = dec;
							} else if (dec < dec_min_i) {
								dec_min_i = dec;
							}
						}

						br.close();
						pw.println(name + "," + String.format("%.3f", ra_min_i / 15.0f) + "," + String.format("%.3f", ra_max_i / 15.0f) + ","
								+ String.format("%+.3f", dec_min_i) + "," + String.format("%+.3f", dec_max_i) + "," + m);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			pw.println("total," + String.format("%.3f", ra_min / 15.0f) + "," + String.format("%.3f", ra_max / 15.0f) + "," + String.format("%+.3f", dec_min) + "," + String.format("%+.3f", dec_max) + "," + n);
			pw.close();

		} else {
			System.out.println("NG:File not found.");
		}
	}

	public void step3() {
		File[] list = new File(TEMP_DIR_PATH).listFiles();

		if (list != null) {
			int i, j, ra, dec, len = list.length;
			PrintWriter pw = null;
			int map[][] = new int[182][361];
			String line = null;

			try {
				pw = new PrintWriter(new BufferedWriter(new FileWriter(new File("d:\\homepage\\astro.starfree.jp\\gaia\\table.csv"))));
			} catch (IOException e) {
				e.printStackTrace();
			}

			for (i = 0; i < len; i++) {
				if (list[i].isFile()) {
					try {
						File file = new File(list[i].toString());
						BufferedReader br = new BufferedReader(new FileReader(file));
						String[] data = null;
						System.out.println(i);

						while ((line = br.readLine()) != null) {
							data = line.split(",", 0);
							ra = Integer.parseInt(data[1]);

							if ("-0".equals(data[3])) {
								map[90][ra]++;
							} else {
								dec = Integer.parseInt(data[3]);
								if (dec < 0) {
									dec += 90;
								} else {
									dec += 91;
								}
								map[dec][ra]++;
							}
						}

						br.close();

					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			for (i = 0; i < 182; i++) {
				line = "";
				for (j = 0; j < 361; j++) {
					line += map[i][j] + ",";
				}
				pw.println(line);
			}

			pw.println("---");

			for (i = 181; i >= 0; i--) {
				line = "";
				for (j = 360; j >= 0; j--) {
					line += map[i][j] + ",";
				}
				pw.println(line);
			}

			pw.close();

		} else {
			System.out.println("NG:File not found.");
		}
	}

	public void step4() {
		int i = 0, j;
		int map[][] = new int[181][360];
		String line = null;
		String[] data = null;
		Color[] color = { new Color(0, 0, 0), new Color(32, 32, 32), new Color(64, 64, 64), new Color(96, 96, 96), new Color(128, 128, 128), new Color(160, 160, 160), new Color(192, 192, 192), new Color(224, 224, 224), new Color(255, 255, 255) };

		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("d:\\homepage\\astro.starfree.jp\\gaia\\gdr2_map_asc.csv")));
			br.readLine();

			while ((line = br.readLine()) != null) {
				data = line.split(",", 0);

				for (j = 1; j < 361; j++) {
					map[i][j - 1] = Integer.parseInt(data[j]);
				}

				i++;
			}

			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		BufferedImage bi = new BufferedImage(360, 180, BufferedImage.TYPE_INT_BGR);
		Graphics2D g = bi.createGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 360, 180);

		for (i = 0; i < 180; i++) {
			for (j = 0; j < 360; j++) {
				g.setColor(color[color_id(map[i][j])]);
				g.fillRect(j, i, 1, 1);
			}
		}

		try {
			ImageIO.write(bi, "png", new File("d:\\homepage\\astro.starfree.jp\\gaia\\map_asc.png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int color_id(int n) {
		int ret = 0;

		if (n >= 500000) {
			ret = 8;
		} else if (n >= 100000) {
			ret = 7;
		} else if (n >= 50000) {
			ret = 6;
		} else if (n >= 10000) {
			ret = 5;
		} else if (n >= 5000) {
			ret = 4;
		} else if (n >= 1000) {
			ret = 3;
		} else if (n >= 500) {
			ret = 2;
		} else if (n >= 100) {
			ret = 1;
		} else {
			ret = 0;
		}

		return ret;
	}

	private boolean input(String url_str, boolean save) {
		boolean ret = false;
		HttpURLConnection con = null;

		try {
			Thread.sleep(3000);
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
