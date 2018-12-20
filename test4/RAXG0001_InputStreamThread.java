package dis.RAXG.util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * InputStreamを読み込むスレッド
 */
public class RAXG0001_InputStreamThread extends Thread {

	private BufferedReader br;

	private List<String> list = new ArrayList<String>();

	/** コンストラクター */
	public RAXG0001_InputStreamThread(InputStream is) {
		br = new BufferedReader(new InputStreamReader(is));
	}

	/** コンストラクター */
	public RAXG0001_InputStreamThread(InputStream is, String charset) {
		try {
			br = new BufferedReader(new InputStreamReader(is, charset));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() {
		try {
			for (;;) {
				String line = br.readLine();
				if (line == null) 	break;
				list.add(line);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** 文字列取得 */
	public List<String> getStringList() {
		return list;
	}
}